package shared
import endpoints.algebra.{Decoder,Encoder,JsonEntities, Endpoints}

import play.api.libs.json.{JsValue,Format, Json, Reads, Writes}

trait PublicEndpoints extends PlayJsonEntities {
  val count=        endpoint(get(path / "api-endpoint" / "count"), jsonResponse[Int])
  val hello=        endpoint(get(path / "api-endpoint" / "hello" / segment[String]), jsonResponse[String])
}
  
object SharedMessages {
  def itWorks = "It works!"
}
