scalaVersion := "2.12.12"

Seq(
  "com.malliina" %% "sbt-utils-maven" % "1.2.3",
  "ch.epfl.scala" % "sbt-bloop" % "1.4.8",
  "org.scalameta" % "sbt-scalafmt" % "2.4.2"
) map addSbtPlugin
