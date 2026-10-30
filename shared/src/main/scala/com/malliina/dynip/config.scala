package com.malliina.dynip

import cats.data.NonEmptyList
import com.malliina.config.{ConfigError, ConfigNode, InvalidValue, MissingValue}
import com.malliina.values.{Password, Readable}

import java.nio.file.{Path, Paths}

case class Conf(zone: ZoneId, domain: String, token: APIToken)

object LocalConf:
  private val userAgent = s"dynip/${BuildInfo.version} (${BuildInfo.gitHash.take(7)})"

  private val appDir: Path = Paths.get(sys.props("user.home")).resolve(".dynip")
  private val localConfFile: Path = appDir.resolve("dynip.conf")
  private val localConfig: ConfigNode = ConfigNode.parseFile(localConfFile)

  def parse() = fromEnv(sys.env.get).orElse(fromConf())

  def fromConf(root: ConfigNode = localConfig): Either[ConfigError, Conf] =
    for
      cf <- root.parse[ConfigNode]("cloudflare")
      zone <- cf.parse[ZoneId]("zone")
      domain <- cf.parse[String]("domain")
      token <- cf.parse[APIToken]("token")
      logs <- root.parse[ConfigNode]("logstreams")
      enabled <- logs.parse[Boolean]("enabled")
      pass <- logs.parse[Password]("pass")
    yield Conf(zone, domain, token)

  def fromEnv(read: String => Option[String]): Either[ConfigError, Conf] =
    def parse[T: Readable](s: String): Either[ConfigError, T] =
      read(s)
        .toRight(MissingValue(NonEmptyList.of(s)))
        .flatMap(s =>
          Readable[T].read(s).left.map(err => InvalidValue(err, NonEmptyList.of(s), None))
        )
    for
      zone <- parse[ZoneId]("CLOUDFLARE_ZONE")
      domain <- parse[String]("CLOUDFLARE_DOMAIN")
      token <- parse[APIToken]("CLOUDFLARE_TOKEN")
    yield Conf(zone, domain, token)
