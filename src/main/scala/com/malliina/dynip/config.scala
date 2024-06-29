package com.malliina.dynip

import com.malliina.config.{ConfigError, ConfigNode}

import java.nio.file.{Path, Paths}

case class Conf(zone: ZoneId, domain: String, token: APIToken)

object LocalConf:
  private val appDir: Path = Paths.get(sys.props("user.home")).resolve(".dynip")
  private val localConfFile: Path = appDir.resolve("dynip.conf")
  private val localConfig: ConfigNode = ConfigNode.default(localConfFile)

  def parse(root: ConfigNode = localConfig): Either[ConfigError, Conf] =
    for
      cf <- root.parse[ConfigNode]("cloudflare")
      zone <- cf.parse[ZoneId]("zone")
      domain <- cf.parse[String]("domain")
      token <- cf.parse[APIToken]("token")
    yield Conf(zone, domain, token)
