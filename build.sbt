name := "patchless"

val versions = new {
  val circe = "0.6.1"
  val shapeless = "2.3.2"
  val scalatest = "3.0.0"
  val scalacheck = "1.13.4"
}

val commonSettings = Seq(
  version := "1.0.1",
  scalaVersion := "2.11.8",
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
  publish <<= publish.dependsOn(test in Test)
)

val `patchless-core` = project.settings(
  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % versions.shapeless
  ),
  commonSettings
)

val `patchless-circe` = project.settings(
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-generic" % versions.circe,
    "io.circe" %% "circe-generic-extras" % versions.circe % "provided,test",
    "io.circe" %% "circe-parser" % versions.circe % "test"
  ),
  commonSettings
) dependsOn `patchless-core`

val `patchless` = (project in file(".")).settings(commonSettings)
  .dependsOn(`patchless-core`)
  .aggregate(`patchless-core`, `patchless-circe`)
