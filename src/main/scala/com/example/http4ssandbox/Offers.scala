package com.example.http4ssandbox

import java.time.Clock
import java.util.UUID

import fs2.Task
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.headers.Location
import cats.syntax.eq._
import cats.instances.all._

import scala.util.Try

trait Offers {
  def clock: Clock

  private type OfferId = UUID
  private var currentOffers: Map[OfferId, Offer] = Map.empty

  private def offerUri(id: OfferId) = uri("/offers") / id.toString
  private val offerCollectionItem: ((OfferId, Offer)) => Json = {
    case (id, offer) => Json.obj("href" -> offerUri(id).asJson, "item" -> offer.asJson)
  }
  private def orderMatches(maybeMerchantId: Option[Long],
                           maybeProductId: Option[String]): ((OfferId, Offer)) => Boolean = {
    case (_, offer) =>
      maybeMerchantId.forall(_ === offer.merchantId) &&
        maybeProductId.forall(_ === offer.productId)
  }

  object MerchantId extends OptionalQueryParamDecoderMatcher[Long]("merchantId")
  object ProductId  extends OptionalQueryParamDecoderMatcher[String]("productId")

  val service = HttpService {
    case GET -> Root / "offers" :? MerchantId(merchant) +& ProductId(product) =>
      Ok(currentOffers.filter(orderMatches(merchant, product)).map(offerCollectionItem).asJson)
    case GET -> Root / "offers" / UUIDPath(id) =>
      currentOffers
        .get(id)
        .filterNot(_.expires.toInstant isBefore clock.instant())
        .map(o => Ok(o.asJson))
        .getOrElse(Gone())
    case DELETE -> Root / "offers" / UUIDPath(id) =>
      currentOffers = currentOffers - id
      NoContent()
    case req @ POST -> Root / "offers" =>
      req.as(jsonOf[Offer]).flatMap { offer =>
        val id = UUID.randomUUID()
        currentOffers = currentOffers + (id -> offer)
        Task.now(Response(Created, headers = Headers(Location(offerUri(id)))))
      }
  }
}
object Offers extends Offers {
  val clock = Clock.systemUTC()
}
object UUIDPath {
  def unapply(str: String): Option[UUID] = Try(UUID.fromString(str)).toOption
}
