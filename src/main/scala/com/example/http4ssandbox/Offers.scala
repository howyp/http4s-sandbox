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

import scala.util.Try

trait Offers {
  def clock: Clock
  def repo: Repository

  private def offerUri(id: Repository.ID) = uri("/offers") / id.toString
  private val offerCollectionItem: ((Repository.ID, Offer)) => Json = {
    case (id, offer) => Json.obj("href" -> offerUri(id).asJson, "item" -> offer.asJson)
  }

  private object MerchantId extends OptionalQueryParamDecoderMatcher[Long]("merchantId")
  private object ProductId  extends OptionalQueryParamDecoderMatcher[String]("productId")

  val service = HttpService {
    case GET -> Root / "offers" :? MerchantId(merchant) +& ProductId(product) =>
      Ok(repo.find(merchant, product).map(offerCollectionItem).asJson)
    case GET -> Root / "offers" / UUIDPath(id) =>
      repo.find(id, expiresAfter = clock.instant()) match {
        case Some(offer) => Ok(offer.asJson)
        case None        => Gone()
      }
    case DELETE -> Root / "offers" / UUIDPath(id) =>
      repo.cancel(id)
      NoContent()
    case req @ POST -> Root / "offers" =>
      req.as(jsonOf[Offer]).flatMap { offer =>
        val id = repo.insert(offer)
        Task.now(Response(Created, headers = Headers(Location(offerUri(id)))))
      }
  }
}
object Offers extends Offers {
  val clock = Clock.systemUTC()
  val repo  = new Repository()
}
object UUIDPath {
  def unapply(str: String): Option[UUID] = Try(UUID.fromString(str)).toOption
}
