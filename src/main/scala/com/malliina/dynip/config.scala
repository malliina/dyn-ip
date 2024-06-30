package com.malliina.dynip

import com.malliina.config.{ConfigError, ConfigNode}
import com.malliina.logstreams.client.LogstreamsConf
import com.malliina.values.Password

import java.nio.file.{Path, Paths}

case class Conf(zone: ZoneId, domain: String, token: APIToken, logs: LogstreamsConf)

object LocalConf:
  private val userAgent = s"dynip/${BuildInfo.version} (${BuildInfo.gitHash.take(7)})"

  private val appDir: Path = Paths.get(sys.props("user.home")).resolve(".dynip")
  private val localConfFile: Path = appDir.resolve("dynip.conf")
  private val localConfig: ConfigNode = ConfigNode.default(localConfFile)

  def parse(root: ConfigNode = localConfig): Either[ConfigError, Conf] =
    for
      cf <- root.parse[ConfigNode]("cloudflare")
      zone <- cf.parse[ZoneId]("zone")
      domain <- cf.parse[String]("domain")
      token <- cf.parse[APIToken]("token")
      logs <- root.parse[ConfigNode]("logstreams")
      enabled <- logs.parse[Boolean]("enabled")
      pass <- logs.parse[Password]("pass")
    yield
      val logsConf = LogstreamsConf(enabled, "dynip", pass.pass, userAgent)
      Conf(zone, domain, token, logsConf)
