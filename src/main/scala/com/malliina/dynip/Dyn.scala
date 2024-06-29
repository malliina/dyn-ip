package com.malliina.dynip

import cats.effect.{Async, Resource}
import cats.implicits.toShow
import cats.syntax.all.*
import com.malliina.dynip.Dyn.log
import com.malliina.http.FullUrl
import com.malliina.http.io.{HttpClientF2, HttpClientIO}
import com.malliina.util.AppLogger
import fs2.Stream

import scala.concurrent.duration.DurationInt

object Dyn:
  private val log = AppLogger(getClass)

  def resource[F[_]: Async] =
    for
      conf <- Resource.eval(Async[F].fromEither(LocalConf.parse()))
      http <- HttpClientIO.resource[F]
    yield Dyn(conf.zone, conf.domain, conf.token, http)

class Dyn[F[_]: Async](zone: ZoneId, domain: String, token: APIToken, http: HttpClientF2[F]):
  log.info(s"Managing zone '$zone'.")
  private val F = Async[F]

  private val zones = FullUrl.https("api.cloudflare.com", "/client/v4/zones")

  private def recordsUrl = zones / zone.show / "dns_records"
  def recordUrl(record: RecordId) = recordsUrl / record.show

  val program = Stream
    .repeatEval:
      F.sleep(100.millis) >> checkAndUpdateIp >> F.sleep(2.seconds)
    .take(3)

  private def checkAndUpdateIp: F[Unit] =
    for
      ip <- http.getAs[IPResponse](FullUrl.https("api.ipify.org", "").withQuery("format" -> "json"))
      records <- http.getAs[Records](
        recordsUrl.withQuery("name" -> domain, "type" -> "A"),
        Map("Authorization" -> s"Bearer $token")
      )
      record <- F.fromEither(
        records.result
          .find(r => r.name == domain && r.`type` == "A")
          .toRight(Exception(s"A record for '$domain' not found."))
      )
    yield log.info(s"IP is ${ip.ip}, A record for $domain is ${record.content}.")
