package com.example.http4ssandbox

import java.time.Instant
import java.util.UUID
import cats.syntax.eq._
import cats.instances.all._

class Repository {
  type ID = UUID

  private var currentOffers: Map[ID, Offer] = Map.empty

  def find(merchant: Option[Long], product: Option[String]): Map[ID, Offer] =
    currentOffers.filter(orderMatches(merchant, product))

  def find(id: ID, expiresAfter: Instant): Option[Offer] =
    currentOffers.get(id).filterNot(_.expires.toInstant isBefore expiresAfter)

  def cancel(id: ID): Unit =
    currentOffers = currentOffers - id

  def insert(offer: Offer): ID = {
    val id = UUID.randomUUID()
    currentOffers = currentOffers + (id -> offer)
    id
  }

  protected def orderMatches(maybeMerchantId: Option[Long], maybeProductId: Option[String]): ((ID, Offer)) => Boolean = {
    case (_, offer) =>
      maybeMerchantId.forall(_ === offer.merchantId) &&
        maybeProductId.forall(_ === offer.productId)
  }
}
