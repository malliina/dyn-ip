package com.malliina.dynip

import com.malliina.util.Log

class ScribeLog extends Log:
  override def debug(msg: String): Unit = scribe.debug(msg)
  override def debug(msg: String, t: Throwable): Unit = scribe.debug(msg)

  override def info(msg: String): Unit = scribe.info(msg)
  override def info(msg: String, t: Throwable): Unit = scribe.info(msg)

  override def warn(msg: String): Unit = scribe.warn(msg)
  override def warn(msg: String, t: Throwable): Unit = scribe.warn(msg)

  override def error(msg: String): Unit = scribe.error(msg)
  override def error(msg: String, t: Throwable): Unit = scribe.error(msg)
