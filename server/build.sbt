name := """server"""

enablePlugins(PlayScala)

libraryDependencies += filters
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
// Used for HTML parse
libraryDependencies += "org.jsoup" % "jsoup" % "1.10.2"
// Used for URL validate
libraryDependencies += "commons-validator" % "commons-validator" % "1.6"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.cookietracker.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.cookietracker.binders._"

// play jdbc pool support
libraryDependencies += jdbc
// slick support
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "2.0.0"
)
// https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.16.1"
// https://mvnrepository.com/artifact/org.postgresql/postgresql
libraryDependencies += "org.postgresql" % "postgresql" % "42.1.1"
