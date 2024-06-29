val dynip = project
  .in(file("."))
  .settings(
    version := "0.0.1",
    scalaVersion := "3.4.0",
    libraryDependencies ++= Seq(
      "com.malliina" %% "okclient-io" % "3.6.0",
      "org.scalameta" %% "munit" % "1.0.0" % Test
    )
  )

Global / onChangedBuildSource := ReloadOnSourceChanges
