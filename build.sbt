import scala.sys.process.Process
import scala.util.Try
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoPackage}
import sbtbuildinfo.{BuildInfoKey, BuildInfoPlugin}

inThisBuild(
  Seq(
    version := versions.app,
    scalaVersion := versions.scala
  )
)

def linkingOptions =
  if sys.env.contains("CI") then
    Seq(s"-L/home/runner/work/dyn-ip/dyn-ip/s2n-tls/s2n-tls-install/lib")
  else
    Nil

val dynip = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .enablePlugins(DebPlugin, BuildInfoPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.malliina" %% "http-client-ember" % versions.malliina,
      "com.malliina" %% "config" % versions.malliina,
      "com.outr" %% "scribe" % versions.scribe,
//      "com.malliina" %% "logstreams-client" % versions.malliina,
      "org.scalameta" %% "munit" % versions.munit % Test
    ),
    buildInfoPackage := "com.malliina.dynip",
    buildInfoKeys ++= Seq[BuildInfoKey](
      "gitHash" -> gitHash
    ),
    nativeConfig ~= { c =>
      c.withLinkingOptions(c.linkingOptions ++ linkingOptions)
    }
  )
  .nativeSettings(
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %% "scala-java-time" % versions.scalajavatime
    )
  )

val dynipJvm = dynip.jvm
val dynipNative = dynip.native

Global / onChangedBuildSource := ReloadOnSourceChanges

def gitHash: String =
  sys.env
    .get("GITHUB_SHA")
    .orElse(Try(Process("git rev-parse HEAD").lazyLines.head).toOption)
    .getOrElse("unknown")
