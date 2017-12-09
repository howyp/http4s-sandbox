package com.example.http4ssandbox

import fs2.Task
import io.circe._
import io.circe.literal._
import org.http4s.{Method, _}
import Method.{GET, POST}
import org.http4s.circe._
import org.http4s.dsl._
import org.scalatest.OptionValues._
import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import org.scalatest.{FreeSpec, Matchers}

class OffersSpec extends FreeSpec with Matchers {
  "Offers" - {
    "can be created by a merchant" in {
      responseTo(Request(GET, uri("/offers"))) should have(status(Status.Ok), body(json"""[]"""))

      val postResponse = responseTo(Request(POST, uri("/offers")).withBody(json"""{ "merchantId": 1234 }"""))
      postResponse should have(status(Status.Created), header(headers.Location))

      responseTo(Request(GET, postResponse.headers.get(headers.Location).value.uri)) should have(
        status(Status.Ok),
        body(json"""{ "merchantId": 1234 }"""))

      responseTo(Request(GET, uri("/offers"))) should have(status(Status.Ok), body(json"""[{ "merchantId": 1234 }]"""))
    }
  }
  def responseTo(request: Request)       = Offers.service.orNotFound(request).unsafeRun()
  def responseTo(request: Task[Request]) = request.flatMap(Offers.service.orNotFound(_)).unsafeRun()

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
    val actual = response.as[Json].unsafeRun()
    HavePropertyMatchResult(actual == expected, "body", expected, actual)
  }
}
