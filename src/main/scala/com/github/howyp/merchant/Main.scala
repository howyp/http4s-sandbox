package com.github.howyp.merchant

import java.time.Clock

import fs2.Task
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp

object Main extends Service with StreamApp {
  val clock = Clock.systemUTC()
  val repo  = new Repository()

  def stream(args: List[String]): fs2.Stream[Task, Nothing] =
    BlazeBuilder
      .bindHttp(8080)
      .mountService(service, "/")
      .serve
}
