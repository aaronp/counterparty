name := "contracts"
version := "0.0.1"
scalaVersion := "3.3.0"
//> using lib "dev.zio::zio:2.0.21"
libraryDependencies += "counterparty.service" %% "counterparty-service" % "0.0.1-SNAPSHOT"
libraryDependencies += "dev.zio" %% "zio" % "2.0.21"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % "test"


