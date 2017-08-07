name := """cookietracker"""

// akka version should be compatible with akka http version
lazy val akkaVersion = "2.4.19"
lazy val akkaHttpVersion = "10.0.8"

lazy val common = (project in file("common")).settings(commonSettings)

lazy val server = (project in file("server")).dependsOn(common).enablePlugins(PlayScala).settings(serverSettings)

lazy val crawler = (project in file("crawler")).dependsOn(common).settings(crawlerSettings)

inThisBuild(List(
  organization := "com.cookietracker",
  scalaVersion := "2.11.11",
  version := "0.1.0-SNAPSHOT"
))

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    // Used for mongodb, but now we don't need
    //"org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0",
    "org.postgresql" % "postgresql" % "42.1.1",
    // Slick for FRM
    "com.typesafe.slick" %% "slick" % "3.1.0",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.slick" %% "slick-hikaricp" % "3.1.0",
    // test
    "org.scalatest" %% "scalatest" % "3.0.3" % Test,
    "com.h2database" % "h2" % "1.4.196" % Test
  )
)

lazy val crawlerSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.1",
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

lazy val serverSettings = Seq(
  libraryDependencies ++= Seq(
    filters, jdbc,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test,
    // Used for HTML parse
    "org.jsoup" % "jsoup" % "1.10.2",
    // Used for URL validate
    "commons-validator" % "commons-validator" % "1.6",
    // Adds additional packages into Twirl
    //TwirlKeys.templateImports += "com.cookietracker.controllers._"
    // Adds additional packages into conf/routes
    // play.sbt.routes.RoutesKeys.routesImport += "com.cookietracker.binders._"
    // play jdbc pool support
    // slick support
    "com.typesafe.play" %% "play-slick" % "2.0.0",
    // https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
    "org.xerial" % "sqlite-jdbc" % "3.16.1",
    // https://mvnrepository.com/artifact/org.postgresql/postgresql
    "org.postgresql" % "postgresql" % "42.1.1"
  )
)
