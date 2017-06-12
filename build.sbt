name := """deadzone-rooster"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala,PlayEbean)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.mariadb.jdbc" % "mariadb-java-client" % "1.5.8",
  "com.github.tototoshi" %% "scala-csv" % "1.3.4",
  "org.webjars" %% "webjars-play" % "2.5.0",
  "org.webjars" % "bootstrap" % "3.1.1-2",
  "com.github.tuxBurner" %% "play-jsannotations" % "2.5.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

resolvers += "tuxburner.github.io" at "http://tuxburner.github.io/repo"

