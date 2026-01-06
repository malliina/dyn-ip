package com.malliina.dynip

import cats.effect.std.Dispatcher
import cats.effect.{Async, Resource}
import cats.implicits.toShow
import cats.syntax.all.{catsSyntaxApplicativeError, catsSyntaxFlatMapOps, toFlatMapOps, toFunctorOps}
import com.malliina.dynip.Dyn.log
import com.malliina.http.{FullUrl, HttpClient, HttpHeaders, SimpleHttpClient}
import com.malliina.logstreams.client.LogstreamsUtils
import com.malliina.util.AppLogger
import fs2.Stream

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.duration.DurationInt

object Dyn:
  private val log = AppLogger(getClass)

  def resource[F[_]: Async] =
    for
      http <- HttpClient.resource[F]()
      d <- Dispatcher.parallel[F]
      conf <- Resource.eval(Async[F].fromEither(LocalConf.parse()))
      _ <- LogstreamsUtils.resource[F](conf.logs, d, http)
    yield Dyn(conf.zone, conf.domain, conf.token, http)

class Dyn[F[_]: Async](zone: ZoneId, domain: String, token: APIToken, http: SimpleHttpClient[F]):
  private val recordType = RecordType.A
  log.info(s"Managing $recordType record of domain '$domain' in zone '$zone'.")
  private val F = Async[F]

  private val zones = FullUrl.https("api.cloudflare.com", "/client/v4/zones")

  private def recordsUrl = zones / zone.show / "dns_records"
  private def recordUrl(record: RecordId) = recordsUrl / record.show

  val program = Stream
    .repeatEval:
      F.sleep(100.millis) >> checkAndUpdateIpRecovered >> F.sleep(5.minutes)

  private def checkAndUpdateIpRecovered = checkAndUpdateIp.handleError: error =>
    log.error(s"Failed to check and update IP of $domain.", error)

  private def checkAndUpdateIp: F[Unit] =
    for
      ip <- http.getAs[IPResponse](FullUrl.host("api.ipify.org").withQuery("format" -> "json"))
      records <- http.getAs[Records](
        recordsUrl.withQuery("name" -> domain, "type" -> RecordType.A.name),
        authHeaders
      )
      record <- F.fromEither(
        records.result
          .find(r => r.name == domain && r.`type` == recordType)
          .toRight(RecordNotFound(recordType, domain))
      )
      _ <- compareAndUpdate(ip.ip, record)
    yield ()

  private def compareAndUpdate(ip: String, record: DNSRecord): F[Unit] =
    if ip != record.content then updateRecord(ip, record.id)
    else
      F.delay(
        log.info(
          s"IP is $ip, ${record.`type`} record for '$domain' is '${record.content}'. No changes."
        )
      )

  private def updateRecord(ip: String, record: RecordId): F[Unit] =
    val now = DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())
    val dnsRecord =
      DNSRecord(record, ip, domain, recordType, Option(s"Updated via API at $now."), ttl = None)
    http
      .putAs[DNSRecord, RecordResult](recordUrl(record), dnsRecord, authHeaders)
      .map: res =>
        val updated = res.result
        log.info(
          s"Updated the ${updated.`type`} record of '${updated.name}' to '${updated.content}'."
        )

  private def authHeaders = Map(HttpHeaders.Authorization -> s"Bearer $token")
