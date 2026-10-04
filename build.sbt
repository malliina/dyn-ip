import scala.sys.process.Process
import scala.util.Try
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoPackage}
import sbtbuildinfo.{BuildInfoKey, BuildInfoPlugin}

val dynip = project
  .in(file("."))
  .enablePlugins(DebPlugin, BuildInfoPlugin)
  .settings(
    version := versions.app,
    scalaVersion := versions.scala,
    libraryDependencies ++= Seq(
      "com.malliina" %% "okclient-io" % versions.malliina,
      "com.malliina" %% "config" % versions.malliina,
      "com.malliina" %% "logstreams-client" % versions.malliina,
      "org.scalameta" %% "munit" % versions.munit % Test
    ),
    buildInfoPackage := "com.malliina.dynip",
    buildInfoKeys ++= Seq[BuildInfoKey](
      "gitHash" -> gitHash
    )
  )

Global / onChangedBuildSource := ReloadOnSourceChanges

def gitHash: String =
  sys.env
    .get("GITHUB_SHA")
    .orElse(Try(Process("git rev-parse HEAD").lazyLines.head).toOption)
    .getOrElse("unknown")
