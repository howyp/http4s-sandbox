package com.example.http4ssandbox

import fs2.Task
import io.circe._
import io.circe.generic.extras.semiauto.{deriveEnumerationDecoder, deriveEnumerationEncoder}
import io.circe.generic.semiauto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

object Offers {
  sealed trait CurrencyAmount
  object CurrencyAmount {
    case object GBp extends CurrencyAmount
  }
  case class Price(currency: CurrencyAmount, amount: Long)
  case class Offer(merchantId: Long, productId: String, price: Price)

  implicit val currencyAmountDecoder: Decoder[CurrencyAmount] = deriveEnumerationDecoder[CurrencyAmount]
  implicit val currencyAmountEncoder: Encoder[CurrencyAmount] = deriveEnumerationEncoder[CurrencyAmount]
  implicit val priceDecoder: Decoder[Price]                   = deriveDecoder[Price]
  implicit val priceEncoder: Encoder[Price]                   = deriveEncoder[Price]
  implicit val offerDecoder: Decoder[Offer]                   = deriveDecoder[Offer]
  implicit val offerEncoder: Encoder[Offer]                   = deriveEncoder[Offer]

  var currentOffers: IndexedSeq[Offer] = IndexedSeq.empty

  val service = HttpService {
    case GET -> Root / "offers"      => Ok(currentOffers.asJson)
    case GET -> Root / "offers" / id => Ok(currentOffers.head.asJson)
    case req @ POST -> Root / "offers" =>
      req.as(jsonOf[Offer]).flatMap { offer =>
        currentOffers = currentOffers :+ offer
        Task.now(Response(Created, headers = Headers(headers.Location(uri("/offers/1")))))
      }
  }
}
