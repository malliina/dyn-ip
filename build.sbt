import scala.sys.process.Process
import scala.util.Try
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoPackage}
import sbtbuildinfo.{BuildInfoKey, BuildInfoPlugin}

val libVersion = "3.7.1"

val versions = new {
  val app = "0.0.2"
  val malliina = "6.10.3"
  val munit = "1.2.1"
  val scala = "3.7.4"
}

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
    .orElse(Try(Process("git rev-parse HEAD").lineStream.head).toOption)
    .getOrElse("unknown")
