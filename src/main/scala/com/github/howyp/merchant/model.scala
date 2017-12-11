package com.github.howyp.merchant

import java.time.ZonedDateTime

sealed trait Currency
object Currency {
  case object GBP extends Currency
  case object USD extends Currency
}

case class Price(currency: Currency, amount: BigDecimal)

sealed trait Offer
object Offer {
  case class Valid(merchantId: Long, productId: String, price: Price, expires: ZonedDateTime) extends Offer
  case object Cancelled                                                                       extends Offer
  case object Expired                                                                         extends Offer
}
