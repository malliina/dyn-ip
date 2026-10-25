package com.malliina.dynip

import cats.effect.metrics.CpuStarvationWarningMetrics
import cats.effect.{IO, IOApp}

import scala.concurrent.duration.Duration

object DynApp extends IOApp.Simple:
  override def runtimeConfig =
    super.runtimeConfig.copy(cpuStarvationCheckInitialDelay = Duration.Inf)

  override protected def onCpuStarvationWarn(metrics: CpuStarvationWarningMetrics): IO[Unit] =
    IO.unit

  override def run: IO[Unit] =
    Dyn
      .resource[IO]
      .use: dyn =>
        dyn.program.compile.drain
