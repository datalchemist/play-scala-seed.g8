package shared
import endpoints.algebra.{Decoder,Encoder,JsonEntities, Endpoints}

import play.api.libs.json.{JsValue,Format, Json, Reads, Writes}

/** Implementation of play-json based endpoints */
trait PlayJsonEntities extends JsonEntities {

  type JsonResponse[A] = Format[A]
  type JsonRequest[A] = Format[A]

  /** Provides a Json [[Decoder]] based on an existing circe decoder */
  implicit def jsonDecoder[A](implicit fmt: Format[A]): Decoder[JsValue, A] =
    new Decoder[JsValue, A] {
      def decode(from: JsValue): Either[Throwable, A] = 
        fmt.reads(from)
        .fold(failure => Left(new Exception(failure.toString)), Right(_))
    }
  /** Provides a Json [[Encoder]] based on an existing circe encoder */
  implicit def jsonEncoder[A](implicit fmt: Format[A]): Encoder[A, JsValue] =
    new Encoder[A, JsValue] {
      def encode(from: A): JsValue = fmt.writes(from)
    }

}
