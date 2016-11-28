package patchless

import scala.reflect.macros.whitebox

import shapeless.labelled.{FieldType, field}
import shapeless.ops.hlist.{Mapper, Zip}
import shapeless._

import scala.language.experimental.macros
import scala.language.dynamics

abstract class Patch[T] extends (T => T) {
  final type Updates = patchable.Updates
  val patchable: Patchable.Aux[T, U] forSome { type U <: HList }
  def updates: Updates
}

object Patch extends Dynamic {

  def ofUpdates[T, U <: HList](up: U)(implicit p: Patchable.Aux[T, U]): Patch[T] = new Patch[T] {
    val patchable = p
    val updates = up
    def apply(t: T): T = patchable(t, up)
  }

  def diff[T, L <: HList, Z <: HList, U <: HList](a: T, b: T)(implicit
    gen: LabelledGeneric.Aux[T, L],
    zip: Zip.Aux[L :: L :: HNil, Z],
    patchable: Patchable.Aux[T, U],
    mapper: Mapper.Aux[differences.type, Z, U]
  ): Patch[T] = ofUpdates[T, U](mapper(zip(gen.to(a) :: gen.to(b) :: HNil)))


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
