package com.example.http4ssandbox

import java.time.ZonedDateTime

import io.circe.{Decoder, Encoder}
import io.circe.java8.time._
import io.circe.generic.auto._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.generic.extras.semiauto.{deriveEnumerationDecoder, deriveEnumerationEncoder}

sealed trait Currency
object Currency {
  case object GBP extends Currency
  case object USD extends Currency

  implicit val currencyAmountDecoder: Decoder[Currency] = deriveEnumerationDecoder[Currency]
  implicit val currencyAmountEncoder: Encoder[Currency] = deriveEnumerationEncoder[Currency]
}

case class Price(currency: Currency, amount: BigDecimal)
case class Offer(merchantId: Long, productId: String, price: Price, expires: ZonedDateTime)
object Offer {
  implicit val offerDecoder: Decoder[Offer] = deriveDecoder[Offer]
  implicit val offerEncoder: Encoder[Offer] = deriveEncoder[Offer]
}
