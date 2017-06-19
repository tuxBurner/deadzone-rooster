import com.typesafe.sbt.packager.docker.ExecCmd

name := """deadzone-roster"""

version := "1.0-SNAPSHOT"

maintainer := "Sebastian Hardt <s.hardt@micromata.de>"


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
  "it.innove" % "play2-pdf" % "1.6.0",
"org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

resolvers += "tuxburner.github.io" at "http://tuxburner.github.io/repo"

// http://www.scala-sbt.org/sbt-native-packager/formats/docker.html
// docker infos go here
maintainer in Docker := "Sebastian Hardt"
packageName in Docker := "tuxburner/deadzoneroster"
dockerExposedPorts in Docker := Seq(9000,9443)
dockerExposedVolumes in Docker := Seq("/data")

// add the command to use whalerider conf
dockerCommands  ++= Seq(
  ExecCmd("CMD","-Dconfig.file=/opt/docker/conf/deadzone-roster.conf")
)



dockerUpdateLatest in Docker := true
