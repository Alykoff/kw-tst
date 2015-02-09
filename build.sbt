name := """kiwi-tst"""

version := "1.0-SNAPSHOT"

resolvers += "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  cache
)

lazy val root = project.in(file(".")).enablePlugins(PlayScala)
