package com.malliina.dynip

import cats.effect.{Async, IO}
import com.malliina.dynip.Dyn.IPResponse
import com.malliina.http.FullUrl
import com.malliina.http.io.{HttpClientF2, HttpClientIO}
import fs2.Stream
import io.circe.Codec
import cats.syntax.all.*
import com.malliina.util.AppLogger

import scala.concurrent.duration.DurationInt

object Dyn:
  private val log = AppLogger(getClass)

  case class IPResponse(ip: String) derives Codec.AsObject

  def resource[F[_]: Async] =
    for http <- HttpClientIO.resource[F]
    yield Dyn(http)

class Dyn[F[_]: Async](http: HttpClientF2[F]):
  private val F = Async[F]
  val program = Stream
    .repeatEval:
      F.sleep(100.millis) >> checkAndUpdateIp >> F.sleep(2.seconds)
    .take(3)

  private def checkAndUpdateIp: F[Unit] =
    http
      .getAs[IPResponse](FullUrl.https("api.ipify.org", "").withQuery("format" -> "json"))
      .map: res =>
        println(s"IP is ${res.ip}")
