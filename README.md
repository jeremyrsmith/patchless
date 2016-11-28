# patchless

**patchless** is a tiny Scala library which provides:

* A data type `Patch[T]`, which extends `T => T` and encapsulates a set of updates to be performed to values of type `T`.
* A typeclass `Patchable[T]`, which supports the data type above.

It uses [shapeless](https://github.com/milessabin/shapeless) to derive `Patchable[T]` for any case class.

## Dependency

Patchless is published to Maven Central – put this in your build.sbt:

```scala
libraryDependencies += "io.github.jeremyrsmith" %% "patchless" % "1.0.1"
```

## Usage
The core of patchless provides only one simple way to create a `Patch[T]` for any given `T`: the `Patch.diff[T]` static
method:

```scala
case class Foo(a: String, b: Int, c: Boolean)
val a = Foo("test", 22, true)
val b = Foo("patched", 22, true)
val patch = Patch.diff(a, b)
patch(a) // Foo("patched", 22, true)
patch(Foo("wibble", 44, false)) // Foo("patched", 44, false)
```

This alone is not all that useful, but the `patchless-circe` module provides decoders directly from JSON to `Patch[T]`.
See [below](#patchless-circe) for details.

### Using the patch fields
The primary advantage of `Patch[T]` over simply `T => T` is that the updated fields can be accessed as a shapeless
`Record` of `Option`s. Each field retains the name from the original case class `T`, but its value type is lifted to
an `Option` of the original type within the case class.

The `Record` is accessible in two ways. The first is simply by the `updates` member of the `Patch` value:

```scala
println(patch) // Some("patched") :: None :: None :: HNil
```

This alone doesn't turn out to be all that useful from a typelevel standpoint - Scala doesn't inherently know the type
of the `updates` field, so your options there are limited.

So patchless does some additional type voodoo to allow you to recover a statically known `Record` for a `Patch[T]` of a
concrete, statically known type `T`. This is done with the implicit enrichment method `patchUpdates`, which allows you
to do typelevel things like mapping over the updates `HList` or summoning typeclasses for it:

```scala
object mapUpdates extends Poly1 {
  implicit def cases[K <: Symbol, T](implicit
    name: Witness.Aux[K]
  ) = at[FieldType[K, T]] {
    field => name.value.name -> field.toString
  }
}
patch.patchUpdates.map(mapUpdates).toList
// List(("a", "Some(patched)"), ("b", "None"), ("c", "None")) 
```

Please note that this only works for a concrete `T`. If `T` is abstract (such as in a polymorphic method over `Patch`
types) then you'll still have to parameterize over various `HList` types and require various implicit shapeless `Aux`
typeclasses over them as usual – starting with `Patchable.Aux[T, U]` where `U` will be inferred to the type of the
`Updates` record for `T`.

```scala
def doPatchyStuff[T, U <: HList, A <: HList](patch: Patch[T])(implicit
  patchable: Patchable.Aux[T, U],
  liftAll: LiftAll.Aux[MyTC, U, A],
  toList: ToList[A, Any]
) = ???
```

Also, be aware that `patchUpdates` involves a typecast; it's assumed that the `Updates` of the `Patch[T]` value has the
same type as the `Patchable[T]` that is in implicit scope. This is usually a safe assumption, but it's not *guaranteed*
to be safe. In an effort to make it as close as possible to a guarantee, `Patchable` is defined as `sealed`, which means
that only the blessed derivations can ever be used to create it; these ought to be deterministic for a particular `T`,
but Scala provides no way to express this and thus a typecast is still necessary.

## patchless-circe

Derived decoders and encoders are provided in the `patchless-circe` module.

In build.sbt:

```scala
libraryDependencies += "io.github.jeremyrsmith" %% "patchless-circe" % "1.0.1"
```

There are two different imports, depending on how you're using circe. You need to have at least `circe-generic`, and
you can also optionall use `circe-generic-extras` (which is marked as a provided dependency in case you don't use it).

You also need to be using automatic derivation for this to be of any use; it's not possible to derive a `Patch[T]` decoder
for a semiauto or manual decoder of `T`.

For vanilla automatic derivation:

```scala
import io.circe.generic.auto._
import patchless.circe._
import cats.syntax.either._ // for working with results

case class Foo(aString: String, bInt: Int)
val parsed = io.circe.parser.parse("""{"aString": "patched"}""")
parsed.valueOr(throw _).as[Patch[Foo]].valueOr(throw _)
parsed.updates          // Some("patched") :: None :: HNil
parsed(Foo("blah", 22)) // Foo("patched", 22)
```

Configurable derivation is the same, but import `patchless.circe.extras._` instead; your implicit `Configuration` will
be used to derive the decoders for `Patch` types.

Encoders work the same way, but be aware that the JSON output depends on the printer used – particularlly, you'll
typically want to `dropNullKeys` if you're outputting `Patch` values to JSON.

## License

Licensed under the **[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)** (the "License");
you may not use this software except in compliance with the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Code of Conduct
The patchless project supports the [Typelevel Code of Conduct](http://typelevel.org/conduct.html) and wants all its channels
to be welcoming environments for everyone.
