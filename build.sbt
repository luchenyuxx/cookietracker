name := """cookietracker"""

// akka version should be compatible with akka http version
lazy val akkaVersion = "2.4.19"
lazy val akkaHttpVersion = "10.0.8"

lazy val common = (project in file("common")).settings(commonSettings)

lazy val crawler = (project in file("crawler")).dependsOn(common).settings(crawlerSettings)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

inThisBuild(List(
  organization := "com.cookietracker",
  scalaVersion := "2.12.5",
  version := "0.1.0-SNAPSHOT"
))

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.25",
    "org.scalatest" %% "scalatest" % "3.0.3" % Test,
    "net.debasishg" %% "redisclient" % "3.5"
  )
)

lazy val crawlerSettings = Seq(
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    // used for HTML parse
    "org.jsoup" % "jsoup" % "1.10.2",
    // used for URL validate
    "commons-validator" % "commons-validator" % "1.6",
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    // https://mvnrepository.com/artifact/com.google.guava/guava
    "com.google.guava" % "guava" % "22.0"
  )
)
