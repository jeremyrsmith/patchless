package patchless.circe

import cats.syntax.either._
import org.scalatest.{FreeSpec, Matchers}
import io.circe.parser.parse
import patchless.Patch
import shapeless.record.Record
import io.circe.generic.auto._

class PatchJsonSpec extends FreeSpec with Matchers {

  "Decoder" - {

    "Auto" in {
      case class Foo(aString: String, bInt: Int, cBoolean: Boolean)

      def parsePatch(str: String) =
        parse(str).valueOr(throw _).as[Patch[Foo]].valueOr(throw _)

      val aPatched = parsePatch("""{"aString": "patched"}""")
      val bPatched = parsePatch("""{"bInt": 22}""")
      val cPatched = parsePatch("""{"cBoolean": false}""")

      aPatched.updates shouldEqual Record(aString = Some("patched"), bInt = None, cBoolean = None)
      bPatched.updates shouldEqual Record(aString = None, bInt = Some(22), cBoolean = None)
      cPatched.updates shouldEqual Record(aString = None, bInt = None, cBoolean = Some(false))
    }

    "Options" in {
      case class Foo(aString: Option[String])
      def parsePatch(str: String) =
        parse(str).valueOr(throw _).as[Patch[Foo]].valueOr(throw _)

      val aPatchedSome = parsePatch("""{"aString": "patched"}""")
      val aPatchedNone = parsePatch("""{"aString": null}""")

      aPatchedSome.updates shouldEqual Record(aString = Some(Some("patched")))
      aPatchedNone.updates shouldEqual Record(aString = Some(None))
    }

  }

}
