package com.example.http4ssandbox

import java.time.Instant
import java.util.UUID
import cats.syntax.eq._
import cats.instances.all._

class Repository {
  protected var currentOffers: Map[Repository.ID, Offer] = Map.empty
  protected def orderMatches(maybeMerchantId: Option[Long],
                             maybeProductId: Option[String]): ((Repository.ID, Offer)) => Boolean = {
    case (_, offer) =>
      maybeMerchantId.forall(_ === offer.merchantId) &&
        maybeProductId.forall(_ === offer.productId)
  }

  def find(merchant: Option[Long], product: Option[String]): Map[Repository.ID, Offer] =
    currentOffers.filter(orderMatches(merchant, product))

  def find(id: Repository.ID, expiresAfter: Instant): Option[Offer] =
    currentOffers
      .get(id)
      .filterNot(_.expires.toInstant isBefore expiresAfter)

  def cancel(id: Repository.ID): Unit =
    currentOffers = currentOffers - id

  def insert(offer: Offer): Repository.ID = {
    val id = UUID.randomUUID()
    currentOffers = currentOffers + (id -> offer)
    id
  }
}

object Repository {
  type ID = UUID
}
