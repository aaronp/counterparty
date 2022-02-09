# Free Monad w/ Scala 3

This is an example following along from Daniel Spiewak's [Free as in Monads](https://www.youtube.com/watch?v=cxMo1RMsD0M) talk, but implemented in scala 3 with some tests/eample

# What is a Free Monad - what's its purpose?
Disclaimer: this topic is by definition a bit heavy on jargon.

## Short Version
 * don't write imperative code, but rather represent your interactions (database calls, user prompts) with data structures (e.g. PromptUser[Int]("pick a number") or WriteData[Unit](bytes : Array[Byte]))
 * use the "Free" monad to then write your control flow using for-comprehensions (e.g. instead of `val x = promptUser("pick a number")` you have `for ( x <- PromptUser("pick a number").freeM) yield x` (where the `.freeM` is some extension method which "lifts" your data structure into the Free monad, allow you to have a flatMap on it)
 * once you have a "Free" monad (`Free[F[_],A]`) version of your for-comprehension program, your program is now actually a value (a tree structure) which you can do whatever you want to it (pass it around, optimise it, run it, whatever).
 * in order to "run" that tree structure, you have to provide an interpretter whch can turn the operations you made up (e.g. `PromptUser[Int]("pick a number")`) into some kind of Monad. 

In practice, you would have a few "interpreters" - ones which can run "what if" scenarios and just log things, or perhaps ones for testing which just have no-ops for external actions.

To do that, you need a natural transformation (read: instead of your normal `A -> B`, it's a `F[_] -> G[_]`) from your type into a Monad, so something like:

```
    // a cats IO way to actually get input from std-in
    given~>[PromptUser, cats.IO] with
      def apply[A](prompt: PromptUser[A]) = IO(println(prompt.text)) *> IO(scala.io.StdIn.readInt())
```

## Long, drawn-out, wordy version
The purpose of the "Free" Monad is to be able to be able to treat any unconstrained parameterised type as-if it were a Monad.

If you're not familiar with Monad, scroll down and I clumsily try and explain it (much better learning resources on Monads are available)

But basically "Free" has the encoding:
```
  // scala 2
  sealed trait Free[F[_], A]

  // scala 3
  enum Free[F[_], A]
```
Which has the purpose of taking any unconstrained parameterised type `F[_]` and being able to treat it as a Monad.

## What? Why would you do that?
Essentially it enables an alternative style of programming which, instead of using imperative statements, you use data structures to represent the operations your program needs to interact with the outside world (user interactions, network calls, database operations, whatever).

This lets you write your program as "values" rather than imperative statements.

Recently I retweeted Thomas Mikula's tweet:
```
"Programs as Values" has much more potential than just execute later.

Examples:
- statically analyze
- simulate
- optimize
- send over network (e.g. deploy to cloud, skipping docker etc.)
- visualize
- provide structural editor for
- generate code from
- synthesize hardware from
```

An example is worth a thousand words, so let's consider this"

### The example

Recently I tried to recreate from memory some program a friend asked me to review.

Essentially it had a lot of different branches/paths through the code:
 * checking some feature toggles
 * conditionally calling an 'OpenBet' API
 * making a database call based on the result of that network call
 * writing/caching the results of the database call

The actual code was more involved, but the permutations through it made my brain hurt - in particular because the real example had several nested "x && !y || z" kinds of statements.

So basically, instead of this (somewhat simplified imperative version, assuming some implementations for 'checkGamStop' and 'setSelfExclusionStatus', etc)
```
def myProgram(featureFlags : FeatureFlags, customerId : String) : Unit = {
    if (featureFlags.isLoadTest) {
        println("load test - ignoring")
    } else {
        val customerStatus = checkGamStop(customerId)
        val isSelfExcluded = (customerStatus != null) && customerStatus.isSelfExcluded
        if (isSelfExcluded) {
            println("customer is self excluded")
        } else {
            val customer = getCustomerById(customerId)
            setSelfExclusionStatus(customerId, customer.isSelfExcluded)
        }
    }
}
```

If we were to use the "Free" monad, we would represent those external calls by using a restricted (sealed) parameterised data structure:

```
sealed trait Operation[A]
```

That is, the interactions with the outside world (retrieving feature flags, calling 3rd party systems, writing to the database) are all `Operation[A]`s which can return some type `A`.

In our case, this is what I came up with:

```
sealed trait Operation[A]
/** Check our feature toggles */
case object GetFeatureFlags extends Operation[FeatureFlags]
/** Log a message. In practice this is also a useful no-op */
case class Log(message: String) extends Operation[Unit]
/** does what it says on the tin */
case class GetCustomerById(customerId: CustId) extends Operation[UserData]
/** check a third-party system (GamStop) */
case class CheckGamStop(customerId: CustId) extends Operation[Option[UserData]]
/** save a result */
case class WriteSelfExclusion(customerId: CustId, selfExcluded: Boolean) extends Operation[Unit]
```

So far so good (hopefully). Notice nothing actually does anything -- it's just a data representation of the inputs/outputs we're using.

Now we want to write our control-flow using those data types. This is where the "Free" monad comes in. 

At the moment `Operation[A]` is just something we made up. It doesn't have a `flatMap`, so it's not a `Monad`. And if it's not a Monad, then we can't sequence the operations (e.g. do this and then do that).

That's what "Free" gives us - it allows us to treat `Operation[A]` as if it were a Monad

Because our `Free[F[_], A]` type DOES have a flatMap, we can "lift" our `Operation[A]` type into `Free[Operation, A]`.

In my example code I put some fancy post-fix extension methods to allow me to ultimately write this:
```
  def updateUser(customerId: CustId): Free[Operation, Unit] = {
    def checkGamStop: Free[Operation, Boolean] = CheckGamStop(customerId).freeM.flatMap {
      case Some(found) => Free.pure(found.isSelfExcluded)
      case None => Free.pure(false)
    }

    def checkOpenBetAndClobberResult: Free[Operation, Unit] = for {
      openBetUser <- GetCustomerById(customerId).freeM
      _ <- WriteSelfExclusion(customerId, openBetUser.isSelfExcluded).freeM
    } yield ()

    def checkUser: Free[Operation, Unit] = for {
      isSelfExcluded <- checkGamStop
      _ <- if isSelfExcluded then Log(s"User $customerId is already self-excluded").freeM else checkOpenBetAndClobberResult
    } yield ()

    for {
      flags <- GetFeatureFlags.freeM
      _ <- if flags.isLoadTest then Log("Ignoring - load test").freeM else checkUser
    } yield ()
  }
```

That's roughly equivalent to the imperative style, though it does use some inner helper methods for clarity.

Instead of just getting a `Unit` type back though, we now have a `: Free[Operation, Unit]`

All that remains is to the "interpret" our `Free[Operation, Unit]`. The "Free" monad has an operation typically called `foldMap`.

Remember, we currently just have a data representation of our program ("Free" is basically just a directed-graph of instructions - a tree structure which has been built up by using calls of pure, map and flatMap).

`foldMap` allows us to turn that tree structure into "any" other monad `G[_]`, provided there is a "natural transformation" from our made-up parameterised type F[_] to G[_].

That still sounds confusing, so let me try and say it another way:

We've written a control flow using data structures to create a tree of instructions ("pure" this, then "flatMap" that...).
We're now free to "interpret" (e.g. "run" or "execute") that data structure, just so long as we can turn each of our `Operation[A]`s into some Monad `G[_]` (e.g. Future[_], or Try[_], or more typically IO[_]).

If you download/run this repo (e.g. `sbt test`), you'll see that in the OpenBestTest, I wanted to just print out the "what if" control flow given every possible permutation of inputs.

This was to showcase one example of how we might want to just eye-ball check the correctness of the program.

The sample output includes:
```

------------------------------------------------------------------------------------------------------------------------
Given load test is 'off', a user isnot self-excluded in OpenBet, and the customer is not self-excluded according to gam-stop: 
                GetFeatureFlags returned FeatureFlags(false,true)
                CheckGamStop(foo) returned Some(UserData(foo,false))
                GetCustomerById(foo) returned UserData(foo,false)
                WriteSelfExclusion(foo,false) returned ()

------------------------------------------------------------------------------------------------------------------------
Given load test is 'off', a user is self-excluded in OpenBet, and the customer is not self-excluded according to gam-stop: 
                GetFeatureFlags returned FeatureFlags(false,true)
                CheckGamStop(foo) returned Some(UserData(foo,false))
                GetCustomerById(foo) returned UserData(foo,true)
                WriteSelfExclusion(foo,true) returned ()

------------------------------------------------------------------------------------------------------------------------
Given load test is 'off', a user is self-excluded in OpenBet, and the customer is self-excluded according to gam-stop: 
                GetFeatureFlags returned FeatureFlags(false,true)
                CheckGamStop(foo) returned Some(UserData(foo,true))
                Log(User foo is already self-excluded) returned ()

```

# The 'Free' encoding
I'm not sure why I'm putting this all in the readme --- just look at the chuffing code. Anyway, in scala3, it looks a bit like this:
```
enum Free[F[_], A]:
  case Pure(value: A) extends Free[F, A]
  case Suspend(fa: F[A]) extends Free[F, A]
  case FlatMap[G[_], In, Out](self: Free[G, In], f: In => Free[G, Out]) extends Free[G, Out]

  final def flatMap[B](f: A => Free[F, B]): Free[F, B] = FlatMap[F, A, B](this, f)

  final def map[B](f: A => B): Free[F, B] = FlatMap[F, A, B](this, a => Free.pure(f(a)))

  final def foldMap[G[_] : Monad](using nt: F ~> G): G[A] = this match {
    case Pure(value) => Monad[G].pure(value)
    case Suspend(fa) => nt(fa)
    case FlatMap(inner, f) =>
      val ge = inner.foldMap(Monad[G], nt)
      Monad[G].flatMap(ge, in => f(in).foldMap(Monad[G], nt))
  }
```

# More Background: What is a Monad

A "Monad" is just a parameterised type (like List[A], Option[A], Future[A], Set[A], ...) which implements three methods:
 * 'pure': take any type 'A' and put it inside an F[A]. For example, take an 'Int' and make it a List[Int], or Set[Int])
 * 'map': given a function from A -> B, we can then transform F[A] to F[B] (e.g. a List[Int] into a List[String])
 * 'flatMap': given a function from A -> F[B], we can transform an F[A] to and F[B] (as opposed to and F[F[B]]). That is, "flatten" the nested 'F[_]' type 

The important bit is the "flatMap", which basically has the up-shot of sequencing.

Consider this example:

Let's say I have three different programs (or methods):
```
def getPerson(id : Int) : Result[Person] = ....
def getManager(person : Person) : Result[Manager] = .... 
def getName(mgr : Manager) : Result[String] = .... 
```

If, in my program, I have the value:
```
  val somePersonId : Result[Int]
```

then a Monad (e.g. flatMap) will allow me to construct a value just as-if I were writing this imperative program:
```
// imperative program
val personId : Int  ...
val person = getPerson(personId)
val manager = getManager(person)
val name = getName(manager)

// monad (flatMap):
def program(personId : Int) : Result[Name] = {
  getPerson(personId).flatMap { person =>
    getManager(person).flatMap { mgr =>
      getName(mgr)
    }
  } 
}

// or, using scala's for-comprehension syntactic sugar:
def program(personId : Int) : Result[Name] = for {
  person <- getPerson(personId)
  manager <- getManager(person)
  name <- getName(manager) 
} yield name
```

