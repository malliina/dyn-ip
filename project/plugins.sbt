scalaVersion := "2.12.20"

Seq(
  "com.github.sbt" % "sbt-native-packager" % "1.11.1",
  "com.eed3si9n" % "sbt-buildinfo" % "0.12.0",
  "org.scalameta" % "sbt-scalafmt" % "2.5.6"
) map addSbtPlugin
