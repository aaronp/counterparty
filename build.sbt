import sbt.Keys.testOptions

lazy val root = project
  .in(file("."))
  .settings(
    name := "free",
    version := "0.0.1",
    scalaVersion := "3.0.2",
    libraryDependencies ++= List(
//      "dev.zio" %% "zio" % "1.0.11",
      "dev.zio" %% "zio" % "2.0.0-M2",
      "org.scalatest" %% "scalatest" % "3.2.10" % "test")
  )

ThisBuild / scalacOptions ++= List(
  "-encoding", "UTF-8",
  "-language:implicitConversions"
)


