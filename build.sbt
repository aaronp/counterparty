import org.scalajs.linker.interface.ModuleSplitStyle

ThisBuild / name := "contracts"
ThisBuild / organization := "demo.rest"
ThisBuild / version := "0.0.1"
ThisBuild / scalaVersion := "3.4.1"
ThisBuild / scalafmtOnCompile := true

// Common settings
lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "dev.zio" %%% "zio" % "2.0.22",
    "org.scalatest" %%% "scalatest" % "3.2.18" % Test,
    "com.lihaoyi" %%% "upickle" % "3.2.0"
  )
)

lazy val app = crossProject(JSPlatform, JVMPlatform).in(file(".")).
  settings(commonSettings).
  jvmSettings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "cask" % "0.9.2",
      "counterparty.service" %%% "counterparty-service" % "0.0.1-SNAPSHOT")
  ).
  jsSettings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.5.0",
      "com.lihaoyi" %%% "scalatags" % "0.12.0",
      "org.scala-js" %%% "scalajs-dom" % "2.4.0"
    ),
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(
          ModuleSplitStyle.SmallModulesFor(List("livechart")))
    },
  )

lazy val root = project.in(file(".")).
  aggregate(app.js, app.jvm).
  settings(
    publish := {},
    publishLocal := {},
  )


ThisBuild / publishMavenStyle := true

val githubUser = "aaronp"
val githubRepo = "counterparty"
ThisBuild / publishTo := Some("GitHub Package Registry" at s"https://maven.pkg.github.com/$githubUser/$githubRepo")

sys.env.get("GITHUB_TOKEN") match {
  case Some(token) if token.nonEmpty =>
    ThisBuild / credentials += Credentials(
      "GitHub Package Registry",
      "maven.pkg.github.com",
      githubUser,
      token
    )
  case _ =>
    println("\n\t\tGITHUB_TOKEN not set - assuming a local build\n\n")
    credentials ++= Nil
}