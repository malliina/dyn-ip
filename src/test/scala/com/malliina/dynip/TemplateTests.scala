package com.malliina.dynip

import cats.{Eq, Show}
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import scala.concurrent.duration.Duration
import concurrent.duration.DurationInt

class TemplateTests extends munit.FunSuite:
  test("can run test"):
    assertEquals(1, 1)
