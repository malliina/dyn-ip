package com.malliina.dynip

import cats.effect.{IO, IOApp}
import com.malliina.logback.LogbackUtils

object DynApp extends IOApp.Simple:
  val _ = LogbackUtils.init()

  override def run: IO[Unit] =
    Dyn
      .resource[IO]
      .use: dyn =>
        dyn.program.compile.drain
