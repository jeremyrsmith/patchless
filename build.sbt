val versions = new {
  val circe211 = "0.11.2"
  val circe = "0.12.2"
  val shapeless = "2.3.3"
  val scalatest = "3.1.0"
  val scalacheck = "1.14.1"
}

val circeVersion = settingKey[String]("Circe version for the target Scala binary version")

inThisBuild(List(
  scalaVersion := "2.11.12",
  crossScalaVersions := Seq("2.11.12","2.12.10","2.13.1"),
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
  circeVersion := (if (scalaBinaryVersion.value == "2.11") versions.circe211 else versions.circe),
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-generic" % circeVersion.value,
    "io.circe" %% "circe-generic-extras" % circeVersion.value % "provided,test",
    "io.circe" %% "circe-parser" % circeVersion.value % "test"
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

