name := "my-recommendation-spark-engine"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.4"

val sparkVersion = "1.6.1"

val akkaVersion = "2.3.11" // override Akka to be this version to match the one in Spark

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka_2.10" % "0.8.1"
      exclude("javax.jms", "jms")
      exclude("com.sun.jdmk", "jmxtools")
      exclude("com.sun.jmx", "jmxri"),
   //not working play module!! check
   //jdbc,
   //anorm,
   //cache,
   // HTTP client
   "net.databinder.dispatch" %% "dispatch-core" % "0.11.1",
   // HTML parser
   "org.jodd" % "jodd-lagarto" % "3.5.2",
   "com.typesafe" % "config" % "1.2.1",
   "com.typesafe.play" % "play-json_2.10" % "2.4.0-M2",
   "org.scalatest" % "scalatest_2.10" % "2.2.1" % "test",
   "org.twitter4j" % "twitter4j-core" % "4.0.2",
   "org.twitter4j" % "twitter4j-stream" % "4.0.2",
   "org.codehaus.jackson" % "jackson-core-asl" % "1.6.1",
   "org.scala-tools.testing" % "specs_2.8.0" % "1.6.5" % "test",
   "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.6.1" ,
   "org.apache.spark" % "spark-core_2.10" % "1.6.1" ,
   "org.apache.spark" % "spark-streaming_2.10" % "1.6.1",
   "org.apache.spark" % "spark-sql_2.10" % "1.6.1",
   "org.apache.spark" % "spark-mllib_2.10" % "1.6.1",
   "com.google.code.gson" % "gson" % "2.6.2",
   "commons-cli" % "commons-cli" % "1.3.1",
   "com.stratio.datasource" % "spark-mongodb_2.10" % "0.11.1",
   // Akka
   "com.typesafe.akka" %% "akka-actor" % akkaVersion,
   "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
   // MongoDB
   "org.reactivemongo" %% "reactivemongo" % "0.10.0"
)

packAutoSettings

//play.Project.playScalaSettings
