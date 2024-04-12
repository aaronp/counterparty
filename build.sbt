name := "contracts"
version := "0.0.1"
scalaVersion := "3.3.0"
libraryDependencies += "dev.zio" %% "zio" % "2.0.15"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.16" % "test"
scalacOptions ++= List(
  "-encoding", "UTF-8",
  "-language:implicitConversions"
)


