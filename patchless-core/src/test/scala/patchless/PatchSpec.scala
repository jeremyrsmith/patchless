package patchless

import org.scalatest.{FreeSpec, Matchers}
import shapeless._
import shapeless.labelled.FieldType
import shapeless.record.Record

class PatchSpec extends FreeSpec with Matchers {

  "Case classes" - {

    case class Foo(a: String, b: Int, c: Boolean)

    val patchable = Patchable[Foo]

    "apply syntax" in {
      val patch = Patch[Foo](a = "patched")
      patch.patchUpdates shouldEqual Record(a = Some("patched"), b = None, c = None)

      //val fail = Patch[Foo](nonExisting = "foo") // does not compile (correctly)
    }

    "diff" in {
      val diff = Patch.diff(
        Foo("test", 22, true),
        Foo("test1", 22, true)
      )
      diff.patchUpdates shouldEqual Record(a = Some("test1"):Option[String], b = None:Option[Int], c = None:Option[Boolean])
      diff(Foo("a", 33, false)) shouldEqual Foo("test1", 33, false)
    }

    "concrete patch type should be known to compiler" - {

      "can map with a poly using witness" in {
        object testMap extends Poly1 {
          implicit def cases[K <: Symbol, V](implicit name: Witness.Aux[K]) = at[FieldType[K, Option[V]]] {
            v => name.value.name -> (v: Option[V])
          }
        }

        def fooUpdates(patch: Patch[Foo]) = {
          patch.patchUpdates.map(testMap).toList
        }

        val diff = Patch.diff(
          Foo("test", 22, true),
          Foo("test1", 22, true)
        )

        fooUpdates(diff) shouldEqual List("a" -> Some("test1"), "b" -> None, "c" -> None)
      }

      "can summon typeclasses" in {

        trait SomeTypeclass[T] {
          def wibble(t: T): String
        }

        object SomeTypeclass {
          implicit val string: SomeTypeclass[String] = new SomeTypeclass[String] {
            def wibble(s: String) = s"String $s"
          }

          implicit val int: SomeTypeclass[Int] = new SomeTypeclass[Int] {
            def wibble(i: Int) = s"Int $i"
          }

          implicit val boolean: SomeTypeclass[Boolean] = new SomeTypeclass[Boolean] {
            def wibble(b: Boolean) = s"Boolean $b"
          }

          implicit def opt[T](implicit tc: SomeTypeclass[T]): SomeTypeclass[Option[T]] = new SomeTypeclass[Option[T]] {
            def wibble(o: Option[T]) = o.map(tc.wibble).getOrElse("")
          }

          implicit def labelled[K <: Symbol, V](implicit tc: SomeTypeclass[V]): SomeTypeclass[FieldType[K, V]] = new SomeTypeclass[FieldType[K, V]] {
            def wibble(v: FieldType[K, V]) = tc.wibble(v)
          }

          implicit val hnil: SomeTypeclass[HNil] = new SomeTypeclass[HNil] {
            def wibble(hnil: HNil) = ""
          }

          implicit def hcons[H, T <: HList](implicit tcHead: SomeTypeclass[H], tcTail: SomeTypeclass[T]): SomeTypeclass[H :: T] = new SomeTypeclass[H :: T] {
            def wibble(l: H :: T) = List(Option(tcHead.wibble(l.head)).filter(_.nonEmpty), Option(tcTail.wibble(l.tail)).filter(_.nonEmpty)).flatten.mkString(" :: ")
          }
        }

        implicit class SomeTypeclassOps[T: SomeTypeclass](val self: T) {
          def wibble = implicitly[SomeTypeclass[T]].wibble(self)
        }

        def fooWibble(patch: Patch[Foo]) = {
          patch.patchUpdates.wibble
        }

        val diff = Patch.diff(
          Foo("test", 22, true),
          Foo("test1", 22, true)
        )

        fooWibble(diff) shouldEqual "String test1"

        diff.patchUpdates.wibble shouldEqual "String test1"
      }
    }

  }

  "comparison" in {
    case class Foo(a: Int, b: String)
    Patch[Foo](a = 10) shouldEqual Patch[Foo](a = 10)
    Patch[Foo](a = 10) shouldNot equal (Patch[Foo](a = 11))
  }


}
