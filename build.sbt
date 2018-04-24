import com.typesafe.sbt.packager.docker.ExecCmd
import sbt.Path

name := """deadzone-roster"""

version := "1.4"

maintainer := "Sebastian Hardt"


lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  ehcache,
  ws,
  guice,
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  "org.webjars" %% "webjars-play" % "2.6.3",

  "org.webjars" % "bootstrap" % "4.0.0-2",
  "org.webjars" % "font-awesome" % "5.0.10",


"com.github.tuxBurner" %% "play-jsannotations" % "2.6.0",

  "it.innove" % "play2-pdf" % "1.8.0",
  "io.methvin" %% "directory-watcher-better-files" % "0.4.0"

)

// encapsulates the launching of the app in an empty jar. This is needed because the classpath var will be to long under windows.
enablePlugins(LauncherJarPlugin)

// compile all less files
includeFilter in (Assets, LessKeys.less) := "*.less"



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
