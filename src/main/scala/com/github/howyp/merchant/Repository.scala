package com.github.howyp.merchant

import java.time.Instant
import java.util.UUID

import cats.instances.all._
import cats.syntax.eq._

class Repository {
  type ID = UUID

  private var currentOffers: Map[ID, Option[Offer.Valid]] = Map.empty

  def find(merchant: Option[Long], product: Option[String]): Map[ID, Offer.Valid] =
    currentOffers.collect {
      case (id, Some(offer))
          if merchant.forall(_ === offer.merchantId) &&
            product.forall(_ === offer.productId) =>
        id -> offer
    }

  def find(id: ID, expiresAfter: Instant): Option[Offer] =
    currentOffers.get(id).map {
      case None                                                         => Offer.Cancelled
      case Some(offer) if offer.expires.toInstant isBefore expiresAfter => Offer.Expired
      case Some(offer)                                                  => offer
    }

  def cancel(id: ID): Unit =
    currentOffers = currentOffers + (id -> None)

  def insert(offer: Offer.Valid): ID = {
    val id = UUID.randomUUID()
    currentOffers = currentOffers + (id -> Some(offer))
    id
  }
}
