package patchless

import cats.syntax.either._
import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor, Json, JsonObject}
import io.circe.generic.decoding.{DerivedDecoder, ReprDecoder}
import io.circe.generic.encoding.{DerivedObjectEncoder, ReprObjectEncoder}
import shapeless.{HList, LabelledGeneric}

package object circe {

  /**
    * This is necessary to allow patching optional fields: https://github.com/circe/circe/issues/304
    */
  implicit def decodeOptionOption[T](
    implicit decodeOpt: Decoder[Option[T]]
  ) : Decoder[Option[Option[T]]] = {
    Decoder.instance {
      cursor => if(cursor.focus == Json.Null) {
        Right(Some(None))
      } else decodeOpt.apply(cursor).map(Some(_))
    }
  }

  implicit def decodePatch[T, U <: HList](implicit
    patchable: Patchable.Aux[T, U],
    decodeU: ReprDecoder[U]
  ): DerivedDecoder[Patch[T]] = new DerivedDecoder[Patch[T]] {
    def apply(c: HCursor): Result[Patch[T]] = decodeU(c).map {
      updates =>
        Patch.ofUpdates[T, U](updates)
    }
  }

  implicit def encodePatch[T, U <: HList](implicit
    patchable: Patchable.Aux[T, U],
    encodeU: ReprObjectEncoder[U]
  ): DerivedObjectEncoder[Patch[T]] = new DerivedObjectEncoder[Patch[T]] {
    def encodeObject(a: Patch[T]): JsonObject = encodeU.encodeObject(a.patchUpdates)
  }

}
