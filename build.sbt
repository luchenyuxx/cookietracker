name := """cookietracker"""

lazy val common = project in file("common")

lazy val server = (project in file("server")).dependsOn(common)

lazy val crawler = (project in file("crawler")).dependsOn(common)

inThisBuild(List(
  organization := "com.cookietracker",
  scalaVersion := "2.11.11",
  version := "0.1.0-SNAPSHOT"
))