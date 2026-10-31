import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
import com.typesafe.sbt.packager.archetypes.systemloader.SystemdPlugin
import com.typesafe.sbt.packager.Keys.linuxPackageMappings
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.Keys.{executableScriptName, maintainer, packageDescription, packageSummary}
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.Universal
import com.typesafe.sbt.packager.debian.DebianPlugin.autoImport.Debian
import com.typesafe.sbt.packager.linux.LinuxPackageMapping
import sbt.*
import sbt.Keys.*
import sbtcompat.PluginCompat
import scala.scalanative.sbtplugin.ScalaNativePlugin
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport.NativeLinkResult
import xsbti.HashedVirtualFileRef

object NativeDebPlugin extends AutoPlugin:
  override def requires = JavaServerAppPackaging && SystemdPlugin && DebianPlugin

  object autoImport:
    val binaryReplacement = taskKey[Option[HashedVirtualFileRef]]("Binary replacement")

    val NativeDebian = config("NativeDebian")

  import autoImport.{binaryReplacement, NativeDebian}

  override def projectSettings: Seq[Setting[?]] = Seq(
    packageSummary := s"${(Debian / name).value} service",
    packageDescription := s"${(Debian / name).value} package.",
    maintainer := "Michael Skogberg <malliina123@gmail.com>",
    Compile / packageDoc / mappings := Nil,
    Compile / packageDoc / publishArtifact := false,
    binaryReplacement := Def.uncached(None),
    Universal / mappings := Def.uncached {
      val name = (Universal / executableScriptName).value
      val ms: Seq[(HashedVirtualFileRef, String)] = (Universal / mappings).value
      ms.map: (src, dest) =>
        val isExecutable = dest == s"bin/$name"
        binaryReplacement.value
          .filter(_ => isExecutable)
          .map: ref =>
            ref -> dest
          .getOrElse:
            src -> dest
    },
    NativeDebian / linuxPackageMappings := {
      val ms: Seq[LinuxPackageMapping] = (Debian / linuxPackageMappings).value
      ms
        .map: mapping =>
          val filtered = mapping.mappings.filterNot((file, _) =>
            file.getName.endsWith(".jar"))
          mapping.copy(mappings = filtered)
        .filter: mapping =>
          mapping.mappings.nonEmpty
    },
    NativeDebian / packageBin := Def.uncached:
      val conv = fileConverter.value
      given FileConverter = conv
      val artifact = (Debian / packageBin).value
      val destName = (Debian / name).value
      val ver = (Debian / version).value
      val dest = target.value / s"$destName-v$ver-linux-aarch64.deb"
      sbt.IO.copyFile(PluginCompat.toFile(artifact), dest)
      streams.value.log.info(s"Copied '$artifact' to '$dest'.")
      conv.toVirtualFile(dest.toPath)
    ,
  )
