# The Free Monad with Scala 3

This is an example following along from Daniel Spiewak's [Free as in Monads](https://www.youtube.com/watch?v=cxMo1RMsD0M) talk, but implemented in scala 3 with some tests/eample

# What is a Free Monad - what's its purpose?
Disclaimer: this topic is by definition a bit heavy on jargon. If you're unfamiliar with what a Monad is, it might help to consult many other great resources to learn about that.

### Monad - you can skip this if you know
Otherwise if you're impatient, it might help to just think of a Monad as being able to provide a sequence (ordering) to some kind of parameterised type (e.g. F[_] is a parameterised type, like List[Boolean], Set[Int], Future[String], etc), and by being able to `flatMap` on that parameterised type implies a sequence of operations, just as imperative programs are written in order - in a sequence. 


# Motivation/Purpose

The "Free" monad offers a way to write programs which, after some initial "eh?" moments, looks and feels similar to how you'd write 
programs normally, but has the effect of separating the control flow (do this, if that, loop here...) from the execution.

## What we're going for - an example
This code repo shows that you can just write a free monad from scratch with very little code (no heavy libraries) in order to treat your programs as values.

We provide an example use-case you can run in the tests where we take a "gam-stop" gaming program flow and print out what our program would do for every possible input (e.g. a "what-if" scenario)

A snippet of that "what-if" output will look like this, showing what actions our program will take when given different input:
```
------------------------------------------------------------------------------------------------------------------------
Given load test is 'off', a user is not self-excluded in OpenBet, and the customer is not self-excluded according to gam-stop: 
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
```

This is one of many possibilities available when your programs are represented as a data structure (tree), rather than imperative instructions.

## How It Works - Imperative vs Functional (e.g. Free Monad) style

So, let's consider this simple program in both an imperative and data-structure (e.g. Free Monad) form:

### An Imperative Example
Here's a program made up of some flow controls (if/else) statements and actions (feedTheCat, launchTheMissiles, println):
```
val catIsHappy = feedTheCat(catNip, amountInGrams = 100)
if (!catIsHappy)
  launchTheMissiles(DateTime.now())
else
  println("phew! The cat's all good")
```

Everyone will be familiar with that, and all we can do is run it, which will apparently feed a cat and maybe launch some missiles. Yikes.
In the non-missile-launching case, I guess we'll just print something out. How are we going to test this? Hmmm...

### The Functional Example - that same program but as data (a tree structure)

We want to represent that flow as a tree data structure, where the nodes of the tree are instances of the "free" monad, which contain the operations we care about (feedTheCat, launchTheMissiles, println).

Those operations will have return value associated with them (e.g. feedTheCat return a boolean of whether the cat is happy), so we'll also need to capture the return value types with a type parameter, so let's do that:
```
// All of our commands will be of the parameterised type 'MyProgramCommand[ResultType]', where the `ResultType` type parameter
// is the return type given if that command were to be executed 
sealed trait MyProgramCommand[ResultType]

// when we feed the cat, it returns a boolean signalling if the cat is happy or not
case class FeedTheCat(food : String, amountInGrams : Int) extends MyProgramCommand[Boolean]
// launching the missiles doesn't have a return type - it's just a side-effect operation
case class LaunchTheMissiles(when : DateTime) extends MyProgramCommand[Unit]
// logging a message also doesn't have a return value, so we just use 'Unit' again as our void return type
case class Log(message : String) extends MyProgramCommand[Unit]
```

Ok, great. That kind-of looks like it'll capture the operations we care about. How do we turn that into our control-flow - our program?

We can't just stick them all in a `List[MyProgramCommand[ResultType]]`, right? We need to represent the if-else branches of code.

Enter the free monad, where instead of `List[MyProgramCommand[ResultType]]` we'll have `Free[MyProgramCommand, ResultType]`, which might be easier to think of as `TreeNode[A]` if it were named better (and lost a type parameter).

Anyway, this is what the imperative control flow looks like, but as a for-comprehension:
```
// now we can use this for our control flow above. The "Free.liftM" is what puts our MyProgramCommand[A] type inside a node within our tree (the Free Monad)
// we can use a for-compehension because, even though our MyProgramCommand[ResultType] doesn't have a flatMap, the Free monad does, and it's the free monad which is wrapping our MyProgramCommand values:
  val ourProgramAsAValue = for {
    catIsHappy <- Free.liftM(FeedTheCat(catNip, amountInGrams = 100))
    _ <- if (!catIsHappy) Free.liftM(LaunchTheMissiles(DateTime.now())) else Free.liftM(Log("phew! The cat's all good")
  } yield ()
```

### Actually Running it - how to execute a tree structure of commands?
That program is now just data - which also means nothing's actually happened. We just have a tree-structure of commands we've called `ourProgramAsAValue` which we can pass around/do things with.

In order to actually __run__ the thing, we'll need some kind of interpreter which can take actions for our made-up `MyProgramCommand[Result]` values.

To cut to the chase, that'll just look like patter-matching our operations to map them onto another (typically executable) type.
In this case, we're assuming our good friend, the imperative functions 'feedTheCat', 'launchTheMissiles', etc are in scope, and the target type we're going to map onto is [IO from cats effect](https://typelevel.org/cats-effect/):
```
...
operation match {
  case FeedTheCat(catNip, amountInGrams) => IO(feedTheCat(catNip, amountInGrams)
  case LaunchTheMissiles(when)           => IO(launchTheMissiles(when))
  case Log(message)                      => IO(println(message))
}
...
```

So that'd be our "interpreter". It tells us how to run individual commands, and the Free Monad gives us the sequencing/order. 

It's worth noting that That target type could be any parameterised type `F[_]`, so long as that type's a Monad (e.g. has a flatMap - Try[_], Future[_], etc)

We actually run that mapping using an operation on the "Free" monad called `foldMap`. So in this case, we'd convert our Free monad into IO, which is also a lazy data structure, and so we have to then call `unsafeRunSync` for us to actually execute that"
```
  val ourProgramAsAValue : Free[MyProgramCommand, Unit] = ...
  val programAsIO : IO[Unit] = ourProgramAsAValue.foldMap[IO] // <-- assumes an interpreter is in scope - e.g. our natrual transformation to IO
  // run it!
  programAsIO.unsafeRunSync() // <-- actually feed the cat - and maybe launch the missiles!
```

#### Quick Aside - Natural Transformations
To do the actual mapping, we use something called a *"natural transformation"*, which is just a way to map one __type__ to another __type__.
So, instead of a normal __function__ which can turn some value `A` into another value `B`:
```A => B```

A natural transformation turns the parameterised type `F[_]` into another type `G[_]`, and sometimes is called "NaturalTransformation[F[_], G[_]]", but often has the more cryptic, symbolic name "~>[F[_], G[_]]":
```
trait ~>[F[_], G[_]]:
  def apply[A](fa: F[A]): G[A]
```

# Why did we go to all that trouble? Some Benefits...

That looks like a lot of overhead/extra steps, so why would anyone do this?

Well, this can be really useful when you have tricky-to-reproduce race-conditions, elaborate/complex control flows, or just economy of scale (e.g. several methods/scenarios which all can make use of a small set of commands/operations).

For example, we might want to interlace different calls to check a race condition, which we can now easily do simply by manipulating or creating the right a tree.  
### Testing
We can inspect the operations given different inputs, stub-out operations (maybe we don't actually want to launch missiles in our tests), or even have regression tests on our control flows themselves.

It can also be really useful for negative tests - as opposed to waiting for something NOT to happen, we just assert an instruction doesn't exist, or has a particular value.

### General code manipulations
Because your program is now data, you can inspect that tree (just pattern-match) and modify it:
 * squash or batch calls - basically in similar ways to database query planning/optimisers
 * apply access controls, retry behaviours, parallelization
 * wrap things with logging/telemetry, retries, metrics, etc

These are the sorts of things which people otherwise have to turn to compiler-plugins or macros to do, like [AOP](https://en.wikipedia.org/wiki/Aspect-oriented_programming).

Instead of having to get into the weeds of your languages internal [AST](https://en.wikipedia.org/wiki/Abstract_syntax_tree) representation of its instruction set, we've now lifted programs into the user-space.

### What-If or dry-run scenarios
All sorts of user immediate-feedback things, like "If I put these values into this form, you tell me what you were going to run/execute".

### Code generation
In this repo, when you run the test we show human-readable output:
```
------------------------------------------------------------------------------------------------------------------------
Given load test is 'off', a user is not self-excluded in OpenBet, and the customer is not self-excluded according to gam-stop: 
                GetFeatureFlags returned FeatureFlags(false,true)
                CheckGamStop(foo) returned Some(UserData(foo,false))
                GetCustomerById(foo) returned UserData(foo,false)
                WriteSelfExclusion(foo,false) returned ()
```

But there's nothing to say that can't be structured. We could generate code in another target language, for example. 
Or our own domain-specific-language.

### Passing around your programs
Serialising or sharing state, comparing against previous status or other user's states ... the sky is the limit really.

As programmers, we work much better with values, and can do a lot more with them.
