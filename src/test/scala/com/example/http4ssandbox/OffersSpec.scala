package com.example.http4ssandbox

import com.example.http4ssandbox.test.Http4sMatchers
import fs2.Task
import io.circe.literal._
import org.http4s.Method.{GET, POST}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.scalatest.OptionValues._
import org.scalatest.Inspectors._
import org.scalatest.{FreeSpec, Matchers}

class OffersSpec extends FreeSpec with Matchers with Http4sMatchers {
  "Offers" - {
    "can be created and then viewed" in new TestCase {
      forAll(offers) { offer =>
        val postResponse = responseTo(Request(POST, uri("/offers")).withBody(offer))
        postResponse should have(status(Status.Created), header(headers.Location))
        responseTo(Request(GET, postResponse.headers.get(headers.Location).value.uri)) should have(status(Status.Ok),
                                                                                                   body(offer))
      }
    }
    "can be listed" in new TestCase {
      responseTo(Request(GET, uri("/offers"))) should have(status(Status.Ok), body(json"[]"))

      forAll(offers)(offer =>
        responseTo(Request(POST, uri("/offers")).withBody(offer)) should have(status(Status.Created)))

      responseTo(Request(GET, uri("/offers"))) should have(status(Status.Ok), body(json"[$offer1,$offer2,$offer3]"))
    }
  }

  private trait TestCase {
    val service: HttpService = Offers.service

    val offers @ List(offer1, offer2, offer3) = List(
      json"""
        {
          "merchantId": 1234,
          "price": { "currency": "GBP", "amount": 503762 },
          "productId": "AM039827X"
        }
      """,
      json"""
        {
          "merchantId": 5678,
          "price": { "currency": "USD", "amount": 0.00342 },
          "productId": "BNT93876"
        }
      """,
      json"""
        {
          "merchantId": 9999,
          "price": { "currency": "GBP", "amount": 12.23 },
          "productId": "27365CV"
        }
      """
    )

    def responseTo(request: Request)       = service.orNotFound(request).unsafeRun()
    def responseTo(request: Task[Request]) = request.flatMap(service.orNotFound(_)).unsafeRun()
  }
}
