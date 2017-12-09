package com.example.http4ssandbox

import com.example.http4ssandbox.test.Http4sMatchers
import fs2.Task
import io.circe.literal._
import org.http4s.Method.{GET, POST}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.scalatest.OptionValues._
import org.scalatest.{FreeSpec, Matchers}

class OffersSpec extends FreeSpec with Matchers with Http4sMatchers {
  "Offers" - {
    "can be created by a merchant" in {
      responseTo(Request(GET, uri("/offers"))) should have(status(Status.Ok), body(json"[]"))

      val originalOffer = json"""
        {
          "merchantId": 1234,
          "price": { "currency": "GBp", "amount": 503762 },
          "productId": "AM039827X"
        }
      """
      val postResponse  = responseTo(Request(POST, uri("/offers")).withBody(originalOffer))
      postResponse should have(status(Status.Created), header(headers.Location))

      responseTo(Request(GET, postResponse.headers.get(headers.Location).value.uri)) should have(status(Status.Ok),
                                                                                                 body(originalOffer))

      responseTo(Request(GET, uri("/offers"))) should have(status(Status.Ok), body(json"[$originalOffer]"))
    }
  }

  def responseTo(request: Request)       = Offers.service.orNotFound(request).unsafeRun()
  def responseTo(request: Task[Request]) = request.flatMap(Offers.service.orNotFound(_)).unsafeRun()
}
