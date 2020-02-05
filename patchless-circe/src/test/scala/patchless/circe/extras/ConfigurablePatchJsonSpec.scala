package patchless.circe.extras

import cats.syntax.either._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.auto._
import io.circe.parser.parse
import org.scalatest.{FreeSpec, Matchers}
import patchless.Patch
import shapeless.record.Record

class ConfigurablePatchJsonSpec extends FreeSpec with Matchers {


  "Configurable decoder" - {
    "Normal" in {
      import io.circe.generic.extras.defaults._
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

    "Snake case" in {
      implicit val configuration = Configuration.default.withSnakeCaseMemberNames
      case class Foo(aString: String, bInt: Int, cBoolean: Boolean)

      def parsePatch(str: String) =
        parse(str).valueOr(throw _).as[Patch[Foo]].valueOr(throw _)

      val aPatched = parsePatch("""{"a_string": "patched"}""")
      val bPatched = parsePatch("""{"b_int": 22}""")
      val cPatched = parsePatch("""{"c_boolean": false}""")

      aPatched.updates shouldEqual Record(aString = Some("patched"), bInt = None, cBoolean = None)
      bPatched.updates shouldEqual Record(aString = None, bInt = Some(22), cBoolean = None)
      cPatched.updates shouldEqual Record(aString = None, bInt = None, cBoolean = Some(false))
    }

    "Options" in {
      import io.circe.generic.extras.defaults._
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
