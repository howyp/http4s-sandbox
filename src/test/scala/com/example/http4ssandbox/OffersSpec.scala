package com.example.http4ssandbox

import java.time.{Clock, Instant, ZoneOffset, ZonedDateTime}

import com.example.http4ssandbox.test.Http4sMatchers
import fs2.Task
import io.circe.literal._
import org.http4s.Method.{DELETE, GET, POST}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.headers.Location
import org.scalatest.OptionValues._
import org.scalatest.Inspectors._
import org.scalatest.{FreeSpec, Matchers}

class OffersSpec extends FreeSpec with Matchers with Http4sMatchers {
  "Offers can be" - {
    "created and then viewed" in new TestCase {
      forAll(offers) { offer =>
        val resp = responseTo(Request(POST, uri("/offers")).withBody(offer))
        resp should have(status(Status.Created), header(Location), noBody)
        responseTo(Request(GET, resp.headers.get(Location).value.uri)) should have(status(Status.Ok), body(offer))
      }
    }
    "listed" in new TestCase {
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
    "cancelled" in new TestCase {
      forAll(offers) { offer =>
        val offerUri = responseTo(Request(POST, uri("/offers")).withBody(offer)).headers.get(Location).value.uri
        responseTo(Request(DELETE, offerUri)) should have(status(Status.NoContent), noBody)
        responseTo(Request(GET, offerUri)) should have(status(Status.Gone), noBody)
      }
      responseTo(Request(GET, uri("/offers"))) should have(status(Status.Ok), body(json"[]"))
    }
    "expired after a given time" in new TestCase {
      setCurrentSystemTime("2017-12-01T00:00:00Z")
      val offerWithExpiryTime = json"""
        {
          "merchantId": 1234,
          "price": { "currency": "GBP", "amount": 503762 },
          "productId": "AM039827X",
          "expires": "2017-12-24T23:59:59Z"
        }
      """
      val offerUri =
        responseTo(Request(POST, uri("/offers")).withBody(offerWithExpiryTime)).headers.get(Location).value.uri

      setCurrentSystemTime("2017-12-24T23:59:59Z")
      responseTo(Request(GET, offerUri)) should have(status(Status.Ok), body(offerWithExpiryTime))
      setCurrentSystemTime("2017-12-25T00:00:00Z")
      responseTo(Request(GET, offerUri)) should have(status(Status.Gone), noBody)
    }
    "queried by" - {
      "merchant ID" in new TestCase with PreCreatedOffers {
        responseTo(Request(GET, uri("/offers") +? ("merchantId", 1234))) should have(
          status(Status.Ok),
          body(json""" [ { "href": $offer1Uri, "item": $offer1 }, { "href": $offer3Uri, "item": $offer3 } ]""")
        )
        responseTo(Request(GET, uri("/offers") +? ("merchantId", 99999))) should have(status(Status.Ok),
                                                                                      body(json"""[]"""))
      }
      "product ID" in new TestCase with PreCreatedOffers {
        responseTo(Request(GET, uri("/offers") +? ("productId", "BNT93876"))) should have(
          status(Status.Ok),
          body(json""" [ { "href": $offer2Uri, "item": $offer2 }, { "href": $offer3Uri, "item": $offer3 } ]""")
        )
        responseTo(Request(GET, uri("/offers") +? ("productId", 99999))) should have(status(Status.Ok),
                                                                                     body(json"""[]"""))
      }
      "merchant ID and product ID" in new TestCase with PreCreatedOffers {
        responseTo(Request(GET, uri("/offers") +? ("merchantId", 1234) +? ("productId", "BNT93876"))) should have(
          status(Status.Ok),
          body(json""" [ { "href": $offer3Uri, "item": $offer3 } ]""")
        )
        responseTo(Request(GET, uri("/offers") +? ("productId", 99999))) should have(status(Status.Ok),
                                                                                     body(json"""[]"""))
      }
    }
  }

  private trait TestCase {
    val offers @ List(offer1, offer2, offer3) = List(
      json"""
        {
          "merchantId": 1234,
          "price": { "currency": "GBP", "amount": 503762 },
          "productId": "AM039827X",
          "expires": "2099-01-01T00:00:00Z"
        }
      """,
      json"""
        {
          "merchantId": 5678,
          "price": { "currency": "USD", "amount": 0.00342 },
          "productId": "BNT93876",
          "expires": "2099-01-01T00:00:00Z"
        }
      """,
      json"""
        {
          "merchantId": 1234,
          "price": { "currency": "GBP", "amount": 12.23 },
          "productId": "BNT93876",
          "expires": "2099-01-01T00:00:00Z"
        }
      """
    )

    var latestClock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)
    def setCurrentSystemTime(datetime: String) =
      latestClock = Clock.fixed(ZonedDateTime.parse(datetime).toInstant, ZoneOffset.UTC)
    private val offersService: Offers = new Offers { def clock = latestClock }

    def responseTo(request: Request): Response       = offersService.service.orNotFound(request).unsafeRun()
    def responseTo(request: Task[Request]): Response = request.flatMap(offersService.service.orNotFound(_)).unsafeRun()
  }
  private trait PreCreatedOffers { this: TestCase =>
    val List(offer1Uri, offer2Uri, offer3Uri) =
      offers.map(offer => responseTo(Request(POST, uri("/offers")).withBody(offer)).headers.get(Location).value.uri)
  }
}
