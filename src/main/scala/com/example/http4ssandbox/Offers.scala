package com.example.http4ssandbox

import fs2.Task
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.headers.Location
import cats.syntax.eq._
import cats.instances.all._

class Offers {
  private var nextId                         = 0
  private var currentOffers: Map[Int, Offer] = Map.empty

  private def offerUri(id: Int) = uri("/offers") / id.toString
  private val offerCollectionItem: ((Int, Offer)) => Json = {
    case (id, offer) => Json.obj("href" -> offerUri(id).asJson, "item" -> offer.asJson)
  }
  private def orderMatches(maybeMerchantId: Option[Long], maybeProductId: Option[String]): ((Int, Offer)) => Boolean = {
    case (_, offer) =>
      maybeMerchantId.forall(_ === offer.merchantId) &&
        maybeProductId.forall(_ === offer.productId)
  }

  object MerchantId extends OptionalQueryParamDecoderMatcher[Long]("merchantId")
  object ProductId  extends OptionalQueryParamDecoderMatcher[String]("productId")

  val service = HttpService {
    case GET -> Root / "offers" :? MerchantId(merchant) +& ProductId(product) =>
      Ok(currentOffers.filter(orderMatches(merchant, product)).map(offerCollectionItem).asJson)
    case GET -> Root / "offers" / IntVar(id) => currentOffers.get(id).map(o => Ok(o.asJson)).getOrElse(Gone())
    case DELETE -> Root / "offers" / IntVar(id) =>
      currentOffers = currentOffers - id
      NoContent()
    case req @ POST -> Root / "offers" =>
      req.as(jsonOf[Offer]).flatMap { offer =>
        nextId += 1
        val id = nextId
        currentOffers = currentOffers + (id -> offer)
        Task.now(Response(Created, headers = Headers(Location(offerUri(id)))))
      }
  }
}
object Offers {
  def service: HttpService = new Offers().service
}
