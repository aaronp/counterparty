package contract

import zio.{Runtime, Task, Unsafe}

package object server {

  extension [A](job: Task[A])
    def execOrThrow(): A = Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.run(job).getOrThrowFiberFailure()
    }
}
