import org.scalajs.linker.interface.ModuleSplitStyle

ThisBuild / name := "contracts"
ThisBuild / organization := "demo.rest"
ThisBuild / version := "0.0.7"
ThisBuild / scalaVersion := "3.4.1"
ThisBuild / scalafmtOnCompile := true
ThisBuild / versionScheme := Some("early-semver")


// Common settings
lazy val commonSettings = Seq(
  buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
  buildInfoPackage := "counterparties.buildinfo",
  // this is our model libraries, generated from the service.yaml and created/publised via 'make packageRestCode'
  libraryDependencies += "com.github.aaronp" %%% "contract" % "0.0.3",
  libraryDependencies ++= Seq(
    "dev.zio" %%% "zio" % "2.0.22",
    "org.scalatest" %%% "scalatest" % "3.2.18" % Test,
    "com.lihaoyi" %%% "upickle" % "3.2.0",
    "com.lihaoyi" %%% "sourcecode" % "0.4.1"
  )
)

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-rewrite",//-rewrite -source 3.4-migration
  "-Xlint",
  "-Xsource:3.4"
)

lazy val app = crossProject(JSPlatform, JVMPlatform).in(file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(commonSettings).
  jvmSettings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "cask" % "0.9.2")
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