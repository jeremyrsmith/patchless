val versions = new {
  val circe = "0.11.2"
  val shapeless = "2.3.3"
  val scalatest = "3.1.0"
  val scalacheck = "1.14.1"
}

inThisBuild(List(
  scalaVersion := "2.11.12",
  crossScalaVersions := Seq("2.11.12","2.12.10"),
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
  }
))

val `patchless-core` = project.settings(
  name := "patchless-core",
  publishTo := sonatypePublishToBundle.value, 
  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % versions.shapeless,
    "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
  )
)

val `patchless-circe` = project.settings(
  name := "patchless-circe",
  publishTo := sonatypePublishToBundle.value, 
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-generic" % versions.circe,
    "io.circe" %% "circe-generic-extras" % versions.circe % "provided,test",
    "io.circe" %% "circe-parser" % versions.circe % "test"
  )
).dependsOn(`patchless-core`)

val `patchless` = (project in file(".")).
  settings(
    name := "patchless",
    publishArtifact := false,
    publish := {},
    publishLocal := {},
    skip in publish := true
  ).
  aggregate(`patchless-core`, `patchless-circe`)

