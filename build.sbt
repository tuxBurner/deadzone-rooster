name := """deadzone-rooster"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "com.github.tototoshi" %% "scala-csv" % "1.3.4",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

