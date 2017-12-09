package com.example.http4ssandbox

import java.time.Instant

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.server._
import org.http4s.dsl._

object Offers {
  case class Offer(merchantId: Long)
  implicit val offerDecoder: Decoder[Offer] = deriveDecoder[Offer]
  implicit val offerEncoder: Encoder[Offer] = deriveEncoder[Offer]
  var currentOffers: IndexedSeq[Offer]      = IndexedSeq.empty

  val service = HttpService {
    case GET -> Root / "offers" => Ok(currentOffers.asJson)
    case req @ POST -> Root / "offers" =>
      req.as(jsonOf[Offer]).flatMap { offer =>
        currentOffers = currentOffers :+ offer
        Created()
      }
  }
}
