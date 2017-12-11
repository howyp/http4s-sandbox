package com.example.http4ssandbox

import java.time.ZonedDateTime

sealed trait Currency
object Currency {
  case object GBP extends Currency
  case object USD extends Currency
}
case class Price(currency: Currency, amount: BigDecimal)
case class Offer(merchantId: Long, productId: String, price: Price, expires: ZonedDateTime)
