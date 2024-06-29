package com.malliina.dynip

import cats.effect.{IO, IOApp}

object DynApp extends IOApp.Simple:
  override def run: IO[Unit] =
    Dyn
      .resource[IO]
      .use: dyn =>
        dyn.program.compile.drain
