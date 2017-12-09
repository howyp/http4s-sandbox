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
    "can be created and then viewed" in new TestCase {
      val originalOffer = json"""
        {
          "merchantId": 1234,
          "price": { "currency": "GBP", "amount": 503762 },
          "productId": "AM039827X"
        }
      """
      val postResponse  = responseTo(Request(POST, uri("/offers")).withBody(originalOffer))
      postResponse should have(status(Status.Created), header(headers.Location))

      responseTo(Request(GET, postResponse.headers.get(headers.Location).value.uri)) should have(status(Status.Ok),
                                                                                                 body(originalOffer))
    }
    "can be listed" in new TestCase {
      responseTo(Request(GET, uri("/offers"))) should have(status(Status.Ok), body(json"[]"))

      val offer1 = json"""
        {
          "merchantId": 1234,
          "price": { "currency": "GBP", "amount": 503762 },
          "productId": "AM039827X"
        }
      """
      val offer2 = json"""
        {
          "merchantId": 5678,
          "price": { "currency": "USD", "amount": 0.00342 },
          "productId": "BNT93876"
        }
      """
      val offer3 = json"""
        {
          "merchantId": 9999,
          "price": { "currency": "GBP", "amount": 12.23 },
          "productId": "27365CV"
        }
      """
      responseTo(Request(POST, uri("/offers")).withBody(offer1)) should have(status(Status.Created))
      responseTo(Request(POST, uri("/offers")).withBody(offer2)) should have(status(Status.Created))
      responseTo(Request(POST, uri("/offers")).withBody(offer3)) should have(status(Status.Created))

      responseTo(Request(GET, uri("/offers"))) should have(status(Status.Ok), body(json"[$offer1,$offer2,$offer3]"))
    }
  }

  private trait TestCase {
    private val service: HttpService = Offers.service

    protected def responseTo(request: Request)       = service.orNotFound(request).unsafeRun()
    protected def responseTo(request: Task[Request]) = request.flatMap(service.orNotFound(_)).unsafeRun()
  }
}
