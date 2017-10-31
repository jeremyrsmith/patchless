import ReleaseTransformations._

val versions = new {
  val circe = "0.9.0-M1"
  val shapeless = "2.3.2"
  val scalatest = "3.0.3"
  val scalacheck = "1.13.5"
}

inThisBuild(List(
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.11.11","2.12.3"),
  organization := "io.github.jeremyrsmith",
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % versions.scalatest % "test",
    "org.scalacheck" %% "scalacheck" % versions.scalacheck % "test"
  ),
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url("https://github.com/jeremyrsmith/patchless")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/jeremyrsmith/patchless"),
      "scm:git:git@github.com:jeremyrsmith/patchless.git"
    )
  ),
  pomExtra := {
    <developers>
    <developer>
    <id>jeremyrsmith</id>
    <name>Jeremy Smith</name>
    <url>https://github.com/jeremyrsmith</url>
        </developer>
    </developers>
  },
  releaseCrossBuild := true
))

val `patchless-core` = project.settings(
  name := "patchless-core",
  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % versions.shapeless,
    "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
  )
)

val `patchless-circe` = project.
  settings(
    name := "patchless-circe",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-generic" % versions.circe,
      "io.circe" %% "circe-generic-extras" % versions.circe % "provided,test",
      "io.circe" %% "circe-parser" % versions.circe % "test"
    )
  ).
  dependsOn(`patchless-core`)

val `patchless` = (project in file(".")).
  settings(
    name := "patchless",
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  ).
  aggregate(`patchless-core`, `patchless-circe`)

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true),
  pushChanges
)
