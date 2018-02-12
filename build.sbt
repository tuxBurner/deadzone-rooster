import com.typesafe.sbt.packager.docker.ExecCmd
import sbt.Path

name := """deadzone-roster"""

version := "1.0-SNAPSHOT"

maintainer := "Sebastian Hardt"


lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  ehcache,
  ws,
  guice,
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  "org.webjars" %% "webjars-play" % "2.6.1",
  "org.webjars" % "bootstrap" % "3.1.1-2",
  "com.github.tuxBurner" %% "play-jsannotations" % "2.6.0",
  "it.innove" % "play2-pdf" % "1.7.0",
  "io.methvin" %% "directory-watcher-better-files" % "0.4.0"

)

resolvers += "tuxburner.github.io" at "http://tuxburner.github.io/repo"
resolvers += Resolver.mavenLocal

// http://www.scala-sbt.org/sbt-native-packager/formats/docker.html
// docker infos go here
maintainer in Docker := "Sebastian Hardt"
packageName in Docker := "tuxburner/deadzoneroster"
dockerExposedPorts in Docker := Seq(9000, 9443)
dockerExposedVolumes in Docker := Seq("/data")

// add the command to use deadzone roster conf
dockerCommands ++= Seq(
  ExecCmd("CMD", "-Dconfig.file=/opt/docker/conf/deadzone-roster.conf")
)



dockerUpdateLatest in Docker := true
