package com.github.howyp.merchant

import java.time.Clock
import java.util.UUID

import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.headers.Location

import scala.util.Try

trait Service {
  def clock: Clock
  def repo: Repository

  import Codecs._

  private val Offers                      = "offers"
  private def offerUri(id: Repository#ID) = uri("/offers") / id.toString

  private object MerchantId extends OptionalQueryParamDecoderMatcher[Long]("merchantId")
  private object ProductId  extends OptionalQueryParamDecoderMatcher[String]("productId")
  private object UUIDPath { def unapply(str: String): Option[UUID] = Try(UUID.fromString(str)).toOption }

  val service = HttpService {
    case GET -> Root / Offers :? MerchantId(merchant) +& ProductId(product) =>
      Ok(repo.find(merchant, product).asJson(collectionWithHref(offerUri)))

    case req @ POST -> Root / Offers =>
      req.as(jsonOf[Offer.Valid]).map { offer =>
        val id = repo.insert(offer)
        Response(Created, headers = Headers(Location(offerUri(id))))
      }

    case GET -> Root / Offers / UUIDPath(id) =>
      repo.find(id, expiresAfter = clock.instant()) match {
        case Some(offer: Offer.Valid)              => Ok(offer.asJson)
        case Some(Offer.Expired | Offer.Cancelled) => Gone()
        case None                                  => NotFound()
      }

    case DELETE -> Root / Offers / UUIDPath(id) =>
      repo.cancel(id)
      NoContent()
  }
}
