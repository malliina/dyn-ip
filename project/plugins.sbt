scalaVersion := "2.12.19"

Seq(
  "com.github.sbt" % "sbt-native-packager" % "1.10.0",
  "org.scalameta" % "sbt-scalafmt" % "2.5.2"
) map addSbtPlugin
