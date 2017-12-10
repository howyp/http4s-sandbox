package com.example.http4ssandbox

import com.example.http4ssandbox.test.Http4sMatchers
import fs2.Task
import io.circe.literal._
import org.http4s.Method.{GET, POST, DELETE}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.headers.Location
import org.scalatest.OptionValues._
import org.scalatest.Inspectors._
import org.scalatest.{FreeSpec, Matchers}

class OffersSpec extends FreeSpec with Matchers with Http4sMatchers {
  "Offers" - {
    "can be created and then viewed" in new TestCase {
      forAll(offers) { offer =>
        val resp = responseTo(Request(POST, uri("/offers")).withBody(offer))
        resp should have(status(Status.Created), header(Location), noBody)
        responseTo(Request(GET, resp.headers.get(Location).value.uri)) should have(status(Status.Ok), body(offer))
      }
    }
    "can be listed" in new TestCase {
      responseTo(Request(GET, uri("/offers"))) should have(status(Status.Ok), body(json"[]"))

      val List(offer1Uri, offer2Uri, offer3Uri) =
        offers.map(offer => responseTo(Request(POST, uri("/offers")).withBody(offer)).headers.get(Location).value.uri)

      responseTo(Request(GET, uri("/offers"))) should have(
        status(Status.Ok),
        body(json"""
          [
            { "href": $offer1Uri, "item": $offer1 },
            { "href": $offer2Uri, "item": $offer2 },
            { "href": $offer3Uri, "item": $offer3 }
          ]""")
      )
    }
    "can be cancelled" in new TestCase {
      {
        val offer    = offer1
        val offerUri = responseTo(Request(POST, uri("/offers")).withBody(offer)).headers.get(Location).value.uri
        responseTo(Request(DELETE, offerUri)) should have(status(Status.NoContent), noBody)
        responseTo(Request(GET, offerUri)) should have(status(Status.Gone), noBody)
      }
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
