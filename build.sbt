name := "backend-play-activator-java"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalacOptions := Seq("-unchecked", "-deprecation")

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "mysql" % "mysql-connector-java" % "5.1.18",
  "com.googlecode.json-simple" % "json-simple" % "1.1",
  "com.google.gcm" % "gcm-server" % "1.0.0",
  "org.apache.opennlp" % "opennlp-tools" % "1.6.0",
  "org.apache.lucene" % "lucene-snowball" % "3.0.3",
  "org.apache.lucene" % "lucene-analyzers" % "3.4.0",
  "javax.mail" % "mail" % "1.4.7"
)


fork in run := true