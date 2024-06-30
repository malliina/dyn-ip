package com.malliina.dynip

import cats.Show
import com.malliina.config.ConfigReadable
import io.circe.{Codec, Decoder, Encoder}

import scala.concurrent.duration.{DurationDouble, FiniteDuration}

opaque type ZoneId = String
object ZoneId:
  given show: Show[ZoneId] = z => z
  given ConfigReadable[ZoneId] = ConfigReadable.string.map(z => z)

opaque type RecordId = String
object RecordId:
  given show: Show[RecordId] = r => r
  given Codec[RecordId] = Codec.from(
    Decoder.decodeString.map(s => s),
    Encoder.encodeString.contramap(r => r)
  )

opaque type APIToken = String
object APIToken:
  given show: Show[APIToken] = t => t
  given ConfigReadable[APIToken] = ConfigReadable.string.map(t => t)

/** Response from ipify.org.
  */
case class IPResponse(ip: String) derives Codec.AsObject

given durationCodec: Codec[FiniteDuration] = Codec.from(
  Decoder.decodeDouble.map(_.seconds),
  Encoder.encodeDouble.contramap(_.toSeconds.toDouble)
)

case class DNSRecord(
  id: RecordId,
  content: String,
  name: String,
  `type`: String,
  comment: Option[String],
  ttl: Option[FiniteDuration]
) derives Codec.AsObject

case class Records(result: List[DNSRecord]) derives Codec.AsObject

case class RecordResult(result: DNSRecord) derives Codec.AsObject