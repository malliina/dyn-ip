package com.malliina.dynip

import cats.effect.std.Dispatcher
import cats.effect.{Async, Resource}
import cats.implicits.toShow
import cats.syntax.all.*
import com.malliina.dynip.Dyn.log
import com.malliina.http.io.{HttpClientF2, HttpClientIO}
import com.malliina.http.{FullUrl, HttpClient, OkClient}
import com.malliina.logstreams.client.LogstreamsUtils
import com.malliina.util.AppLogger
import fs2.Stream
import io.circe.syntax.EncoderOps
import okhttp3.RequestBody

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.duration.DurationInt

object Dyn:
  private val log = AppLogger(getClass)

  def resource[F[_]: Async] =
    for
      http <- HttpClientIO.resource[F]
      d <- Dispatcher.parallel[F]
      conf <- Resource.eval(Async[F].fromEither(LocalConf.parse()))
      _ <- LogstreamsUtils.resource[F](conf.logs, d, http)
    yield Dyn(conf.zone, conf.domain, conf.token, http)

class Dyn[F[_]: Async](zone: ZoneId, domain: String, token: APIToken, http: HttpClientF2[F]):
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
      ip <- http.getAs[IPResponse](FullUrl.https("api.ipify.org", "").withQuery("format" -> "json"))
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

  private def compareAndUpdate(ip: String, record: DNSRecord) =
    if ip != record.content then updateRecord(ip, record.id)
    else
      F.delay(
        log.info(
          s"IP is $ip, ${record.`type`} record for '$domain' is '${record.content}'. No changes."
        )
      )

  private def updateRecord(ip: String, record: RecordId) =
    val now = DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())
    val dnsRecord =
      DNSRecord(record, ip, domain, recordType, Option(s"Updated via API at $now."), ttl = None)
    val body = RequestBody.create(dnsRecord.asJson.toString, OkClient.jsonMediaType)
    val url = recordUrl(record)
    val req = HttpClient
      .requestFor(url, authHeaders)
      .put(body)
      .build()
    http
      .execute(req)
      .flatMap(res => http.parse[RecordResult](res, url))
      .map: res =>
        val updated = res.result
        log.info(
          s"Updated the ${updated.`type`} record of '${updated.name}' to '${updated.content}'."
        )

  private def authHeaders = Map("Authorization" -> s"Bearer $token")
