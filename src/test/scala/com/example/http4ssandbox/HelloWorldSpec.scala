package com.example.http4ssandbox

import org.http4s.dsl._
import org.http4s.{Method, Request, Response, Status}
import org.scalatest.{FreeSpec, Matchers}

class HelloWorldSpec extends FreeSpec with Matchers {
  "HelloWorld" - {
    "return 200" in {
      retHelloWorld.status should be(Status.Ok)
    }
    "return hello world" in {
      retHelloWorld.as[String].unsafeRun() should be("{\"message\":\"Hello, world\"}")
    }
  }

  private[this] val retHelloWorld: Response = {
    val getHW = Request(Method.GET, uri("/hello/world"))
    val task  = HelloWorld.service.orNotFound(getHW)
    task.unsafeRun()
  }
}
