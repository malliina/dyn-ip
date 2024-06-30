import scala.sys.process.Process
import scala.util.Try
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoPackage}
import sbtbuildinfo.{BuildInfoKey, BuildInfoPlugin}

val libVersion = "3.6.0"

val dynip = project
  .in(file("."))
  .enablePlugins(DebPlugin, BuildInfoPlugin)
  .settings(
    version := "0.0.1",
    scalaVersion := "3.4.0",
    libraryDependencies ++= Seq(
      "com.malliina" %% "okclient-io" % libVersion,
      "com.malliina" %% "config" % libVersion,
      "com.malliina" %% "logstreams-client" % "2.7.0",
      "org.scalameta" %% "munit" % "1.0.0" % Test
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
    .orElse(Try(Process("git rev-parse HEAD").lineStream.head).toOption)
    .getOrElse("unknown")
