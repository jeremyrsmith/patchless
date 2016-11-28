package patchless

import shapeless.labelled.{FieldType, field}
import shapeless._

sealed trait Patchable[T] {
  type Updates <: HList
  def apply(t: T, updates: Updates): T
}

object Patchable {
  type Aux[T, U <: HList] = Patchable[T] { type Updates = U }

  def apply[T](implicit patchable: Patchable[T]): Aux[T, patchable.Updates] = patchable

  implicit val hnil: Aux[HNil, HNil] = new Patchable[HNil] {
    final type Updates = HNil
    def apply(t: HNil, updates: HNil): HNil = HNil
  }

  implicit def hcons[K <: Symbol, H, T <: HList, TU <: HList](implicit
    patchTail: Aux[T, TU]
  ): Aux[FieldType[K, H] :: T, FieldType[K, Option[H]] :: TU] = new Patchable[FieldType[K, H] :: T] {
    final type Updates = FieldType[K, Option[H]] :: TU
    def apply(t: FieldType[K, H] :: T, updates: FieldType[K, Option[H]] :: TU) =
      field[K](updates.head.getOrElse(t.head)) :: patchTail(t.tail, updates.tail)
  }

  implicit def generic[T, L <: HList, U <: HList](implicit
    gen: LabelledGeneric.Aux[T, L],
    patchL: Aux[L, U]
  ): Aux[T, U] = new Patchable[T] {
    final type Updates = U
    def apply(t: T, updates: U) = gen.from(patchL(gen.to(t), updates))
  }
}
