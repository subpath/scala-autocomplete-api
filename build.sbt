name := "scala-autocomplete-api"
version := "0.1.0"
scalaVersion := "2.13.6"

lazy val root = (project in file(".")).dependsOn(scalaProgressBar)

lazy val scalaProgressBar =
  ProjectRef(uri("git://github.com/a8m/pb-scala.git#master"), "pb-scala")
libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor-typed_2.13" % "2.6.14",
  "com.typesafe.akka" % "akka-stream-typed_2.13" % "2.6.14",
  "com.typesafe.akka" % "akka-http_2.13" % "10.2.4",
  "com.typesafe.akka" % "akka-http-spray-json_2.13" % "10.2.4",
  "ch.megard" % "akka-http-cors_2.13" % "1.1.1",
  "org.apache.lucene" % "lucene-suggest" % "8.6.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.scala-lang.modules" %% "scala-collection-contrib" % "0.2.2",
  "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime
)
