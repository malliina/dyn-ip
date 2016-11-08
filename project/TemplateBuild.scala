import com.malliina.sbtutils.SbtProjects
import sbt._
import sbt.Keys._

/**
  * A scala build file template.
  */
object TemplateBuild {

  lazy val template = SbtProjects.testableProject("template")
    .settings(projectSettings: _*)

  lazy val projectSettings = Seq(
    version := "0.0.1",
    scalaVersion := "2.12.0"
  )
}
