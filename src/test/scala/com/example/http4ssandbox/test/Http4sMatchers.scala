package com.example.http4ssandbox.test

import fs2.Task
import cats.instances.all._
import cats.syntax.eq._
import io.circe.Json
import org.http4s._
import org.http4s.circe._
import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}

trait Http4sMatchers {
  def status(expected: Status) = HavePropertyMatcher { (response: Response) =>
    HavePropertyMatchResult(response.status == expected, "status", expected, response.status)
  }
  def header(expected: HeaderKey.Extractable) = HavePropertyMatcher { (response: Response) =>
    HavePropertyMatchResult(response.headers.get(expected).isDefined,
                            "header keys",
                            "containing " + expected.name,
                            response.headers.map(_.name))
  }
  def body(expected: Json) = HavePropertyMatcher { (response: Response) =>
    response.as(jsonOf[Json]).unsafeAttemptRun() match {
      case Left(throwable) =>
        HavePropertyMatchResult(false,
                                "body",
                                expected.toString(),
                                s"parsing body as JSON threw exception '${throwable.getMessage}'")
      case Right(actual) => HavePropertyMatchResult(actual === expected, "body", expected.toString(), actual.toString())
    }
  }
  val noBody = HavePropertyMatcher { (response: Response) =>
    val actual = response.as[String].unsafeRun()
    HavePropertyMatchResult(actual.isEmpty, "body", "", actual)
  }
}
