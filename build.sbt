name := """kiwi-tst"""

version := "1.0-SNAPSHOT"

lazy val root = project.in(file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
    cache,
    ws,
    "org.reactivecouchbase" %% "reactivecouchbase-play" % "0.4-SNAPSHOT"
)

resolvers += "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "ReactiveCouchbase" at "https://raw.github.com/ReactiveCouchbase/repository/master/snapshots"