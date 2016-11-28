package patchless

import shapeless.labelled.{FieldType, field}
import shapeless.ops.hlist.{Mapper, Zip}
import shapeless._


abstract class Patch[T] extends (T => T) {
  type Updates <: HList
  def updates: Updates
}

object Patch {

  type Aux[T, U <: HList] = Patch[T] { type Updates = U }

  def ofUpdates[T, U <: HList](up: U)(implicit
    patchable: Patchable.Aux[T, U]
  ): Aux[T, U] = new Patch[T] {
    type Updates = U
    val updates: U = up
    def apply(t: T): T = patchable(t, up)
  }

  def diff[T, L <: HList, Z <: HList, U <: HList](a: T, b: T)(implicit
    gen: LabelledGeneric.Aux[T, L],
    zip: Zip.Aux[L :: L :: HNil, Z],
    patchable: Patchable.Aux[T, U],
    mapper: Mapper.Aux[differences.type, Z, U]
  ): Aux[T, U] = ofUpdates[T, U](mapper(zip(gen.to(a) :: gen.to(b) :: HNil)))


  object differences extends Poly1 {
    implicit def cases[K <: Symbol, V] = at[(FieldType[K, V], FieldType[K, V])] {
      case (a, b) => if(a == b)
          field[K](None: Option[V])
        else
          field[K](Some(b): Option[V])
    }
  }

  abstract class Updates[T, U <: HList] {
    type Updates = U
  }

  implicit class ConcretePatchOps[T, U <: HList](val patch: Patch[T])(implicit
    patchable: Patchable.Aux[T, U]
  ) {
    def patchUpdates: U = patch.updates.asInstanceOf[U]
  }
}
