package com.example.http4ssandbox

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto.{deriveEnumerationDecoder, deriveEnumerationEncoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

sealed trait Currency
object Currency {
  case object GBP extends Currency
  case object USD extends Currency

  implicit val currencyAmountDecoder: Decoder[Currency] = deriveEnumerationDecoder[Currency]
  implicit val currencyAmountEncoder: Encoder[Currency] = deriveEnumerationEncoder[Currency]
}

case class Price(currency: Currency, amount: BigDecimal)
object Price {
  implicit val priceDecoder: Decoder[Price] = deriveDecoder[Price]
  implicit val priceEncoder: Encoder[Price] = deriveEncoder[Price]
}

case class Offer(merchantId: Long, productId: String, price: Price)
object Offer {
  implicit val offerDecoder: Decoder[Offer] = deriveDecoder[Offer]
  implicit val offerEncoder: Encoder[Offer] = deriveEncoder[Offer]
}
