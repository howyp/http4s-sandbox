package com.example.http4ssandbox

import fs2.Task
import io.circe._
import io.circe.literal._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.scalatest.OptionValues._
import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import org.scalatest.{FreeSpec, Matchers}

class OffersSpec extends FreeSpec with Matchers {
  "Offers" - {
    "can be created by a merchant" in {
      responseTo(Request(Method.GET, uri("/offers"))) should have(status(Status.Ok), body(json"""[]"""))

      val offerToCreate = json"""{ "merchantId": 1234 }"""
      responseTo(Request(Method.POST, uri("/offers")).withBody(offerToCreate)) should have(status(Status.Created))

      responseTo(Request(Method.GET, uri("/offers"))) should have(
        status(Status.Ok),
        body(json"""[{ "merchantId": 1234 }]""")
      )
    }
  }
  def responseTo(request: Request)       = Offers.service(request).unsafeRun().toOption.value
  def responseTo(request: Task[Request]) = request.flatMap(Offers.service(_)).unsafeRun().toOption.value
  def status(expected: Status) = HavePropertyMatcher { (response: Response) =>
    HavePropertyMatchResult(response.status == expected, "status", expected, response.status)
  }
  def body(expected: Json) = HavePropertyMatcher { (response: Response) =>
    val actual = response.as[Json].unsafeRun()
    HavePropertyMatchResult(actual == expected, "body", expected, actual)
  }
}
