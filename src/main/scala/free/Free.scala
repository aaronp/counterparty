package free


trait Monad[F[_]]:
  def pure[A](a: A): F[A]
  def flatMap[A, B](fa: F[A], f: A => F[B]): F[B]

object Monad:
  def apply[F[_]](using monad: Monad[F]): Monad[F] = monad

extension[F[_] : Monad, A] (fa: F[A])
  def flatMap[B](f: A => F[B]): F[B] = summon[Monad[F]].flatMap(fa, f)

trait ~>[F[_], G[_]]:
  def apply[A](fa: F[A]): G[A]
  
enum Free[F[_], A]:
  case Pure(value: A) extends Free[F, A]
  case Suspend(fa: F[A]) extends Free[F, A]
  case FlatMap[G[_], In, Out](self: Free[G, In], f: In => Free[G, Out]) extends Free[G, Out]

  final def flatMap[B](f: A => Free[F, B]): Free[F, B] = FlatMap[F, A, B](this, f)

  final def map[B](f : A => B) : Free[F, B] = FlatMap[F, A, B](this, a => Free.pure(f(a)))

  final def foldMap[G[_] : Monad](using nt: F ~> G): G[A] = this match {
    case Pure(value) => Monad[G].pure(value)
    case Suspend(fa) => nt(fa)
    case FlatMap(inner, f) =>
      val ge = inner.foldMap(Monad[G], nt)
      Monad[G].flatMap(ge, in => f(in).foldMap(Monad[G], nt))
  }

object Free:
  def pure[F[_], A](a: A): Free[F, A] = Free.Pure(a)
  def liftM[F[_], A](fa: F[A]): Free[F, A] = Free.Suspend(fa)

extension [F[_], A](fa: F[A])
  def freeM: Free[F, A] = Free.liftM(fa)

extension [A](value : A)
  def free[F[_]]: Free[F, A] = Free.pure(value)