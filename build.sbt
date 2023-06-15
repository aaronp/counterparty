enablePlugins(ScalaJSPlugin)

import sbt.Keys.testOptions

lazy val root = project
  .in(file("."))
  .settings(
    name := "free",
    version := "0.0.1",
    scalaVersion := "3.3.0",
    libraryDependencies ++= List(
      "dev.zio" %%% "zio" % "2.0.15",
      "org.scalatest" %%% "scalatest" % "3.2.16" % "test"),
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.5.0",
    libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.12.0"
  )

ThisBuild / scalacOptions ++= List(
  "-encoding", "UTF-8",
//  "-explain",
  "-language:implicitConversions"
)


