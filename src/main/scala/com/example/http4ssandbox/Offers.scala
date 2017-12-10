package com.example.http4ssandbox

import fs2.Task
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.headers.Location

class Offers {
  var nextId                         = 0
  var currentOffers: Map[Int, Offer] = Map.empty

  val service = HttpService {
    case GET -> Root / "offers"              => Ok(currentOffers.map(offerCollectionItem).asJson)
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

  private def offerUri(id: Int) = uri("/offers") / id.toString
  private val offerCollectionItem: ((Int, Offer)) => Json = {
    case (id, offer) => Json.obj("href" -> offerUri(id).asJson, "item" -> offer.asJson)
  }
}
object Offers {
  def service: HttpService = new Offers().service
}
