package com.example.http4ssandbox

import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.server._
import org.http4s.dsl._

object Offers {
  val service = HttpService {
    case GET -> Root / "offers"  => Ok(List.empty.asJson)
    case POST -> Root / "offers" => Created()
  }
}
