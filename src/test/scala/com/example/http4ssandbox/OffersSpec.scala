package com.example.http4ssandbox

import io.circe.Json
import org.http4s._
import org.http4s.dsl._
import org.scalatest.OptionValues._
import org.scalatest.{FreeSpec, Matchers}
import org.http4s.circe._
import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}

class OffersSpec extends FreeSpec with Matchers {
  "Offers" - {
    "can be created by a merchant" in {
      responseTo(Request(Method.GET, uri("/offers"))) should have(status(Status.Ok), jsonBody(Json.arr()))
      responseTo(Request(Method.POST, uri("/offers")).withBody(Json.obj()).unsafeRun()) should have(
        status(Status.Created))
    }
  }
  def responseTo(request: Request) = Offers.service(request).unsafeRun().toOption.value
  def status(expected: Status) = HavePropertyMatcher { (response: Response) =>
    HavePropertyMatchResult(response.status == expected, "status", expected, response.status)
  }
  def jsonBody(expected: Json) = HavePropertyMatcher { (response: Response) =>
    val actual = response.as[Json].unsafeRun()
    HavePropertyMatchResult(actual == expected, "body", expected, actual)
  }
}
