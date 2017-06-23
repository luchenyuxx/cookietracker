name := """cookietracker"""

lazy val common = project in file("common")

lazy val server = (project in file("server")).dependsOn(common).enablePlugins(PlayScala).settings(serverSettings)

lazy val crawler = (project in file("crawler")).dependsOn(common).settings(crawlerSettings)

inThisBuild(List(
  organization := "com.cookietracker",
  scalaVersion := "2.11.11",
  version := "0.1.0-SNAPSHOT"
))

lazy val crawlerSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.1",
    "com.typesafe.akka" %% "akka-actor" % "2.5.2",
    "com.typesafe.akka" %% "akka-testkit" % "2.5.2" % Test,
    // used for HTML parse
    "org.jsoup" % "jsoup" % "1.10.2",
    // used for URL validate
    "commons-validator" % "commons-validator" % "1.6",
    "com.typesafe.akka" %% "akka-http" % "10.0.8",
    "com.typesafe.akka" %% "akka-http-testkit" % "10.0.8" % Test
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
