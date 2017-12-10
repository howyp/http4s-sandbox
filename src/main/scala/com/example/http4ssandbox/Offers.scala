package com.example.http4ssandbox

import fs2.Task
import io.circe._
import io.circe.generic.extras.semiauto.{deriveEnumerationDecoder, deriveEnumerationEncoder}
import io.circe.generic.semiauto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.headers.Location

class Offers {
  var currentOffers: IndexedSeq[Option[Offer]] = IndexedSeq.empty

  val service = HttpService {
    case GET -> Root / "offers"              => Ok(currentOffers.zipWithIndex.map(offerCollectionItem).asJson)
    case GET -> Root / "offers" / IntVar(id) => currentOffers(id).map(o => Ok(o.asJson)).getOrElse(Gone())
    case DELETE -> Root / "offers" / IntVar(id) =>
      currentOffers = currentOffers.patch(id - 1, Seq(None), 1)
      NoContent()
    case req @ POST -> Root / "offers" =>
      req.as(jsonOf[Offer]).flatMap { offer =>
        currentOffers = currentOffers :+ Some(offer)
        val id = currentOffers.size - 1
        Task.now(Response(Created, headers = Headers(Location(offerUri(id)))))
      }
  }

  private def offerUri(id: Int) = uri("/offers") / id.toString
  private val offerCollectionItem: ((Option[Offer], Int)) => Option[Json] = {
    case (Some(offer), id) => Some(Json.obj("href" -> offerUri(id).asJson, "item" -> offer.asJson))
    case (None, _)         => None
  }
}
object Offers {
  def service: HttpService = new Offers().service
}
