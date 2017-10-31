package patchless.circe

import cats.syntax.either._
import io.circe.{Decoder, HCursor, Json, JsonObject}
import io.circe.generic.decoding.DerivedDecoder
import io.circe.generic.extras.decoding.ReprDecoder
import io.circe.generic.encoding.DerivedObjectEncoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.encoding.ReprObjectEncoder
import io.circe.generic.extras.util.RecordToMap
import patchless.{Patch, Patchable}
import shapeless.{Default, HList, Lazy}

package object extras {

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

  implicit def decodePatch[T, U <: HList, D <: HList](implicit
    patchable: Patchable.Aux[T, U],
    decodeU: Lazy[ReprDecoder[U]],
    defaults: Default.AsRecord.Aux[T, D],
    defaultMapper: RecordToMap[D],
    config: Configuration
  ): DerivedDecoder[Patch[T]] = new DerivedDecoder[Patch[T]] {
    def apply(c: HCursor): Decoder.Result[Patch[T]] = decodeU.value.configuredDecode(c)(
      config.transformMemberNames,
      config.transformConstructorNames,
      if(config.useDefaults) defaultMapper(defaults()) else Map.empty,
      config.discriminator
    ).map {
      updates =>
        Patch.ofUpdates[T, U](updates)
    }
  }

  implicit def encodePatch[T, U <: HList](implicit
    patchable: Patchable.Aux[T, U],
    encodeU: Lazy[ReprObjectEncoder[U]],
    config: Configuration
  ): DerivedObjectEncoder[Patch[T]] = new DerivedObjectEncoder[Patch[T]] {
    def encodeObject(a: Patch[T]): JsonObject = encodeU.value.configuredEncodeObject(a.patchUpdates)(
      config.transformMemberNames,
      config.transformConstructorNames,
      config.discriminator
    )
  }

}
