name := "my-recommendation-spark-engine"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.4"

val sparkVersion = "1.6.0-cdh5.7.0"

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
   "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.6.0-cdh5.7.0",
   "org.apache.spark" % "spark-core_2.10" % "1.6.0-cdh5.7.0",
   "org.apache.spark" % "spark-streaming_2.10" % "1.6.0-cdh5.7.0",
   "org.apache.spark" % "spark-sql_2.10" % "1.6.0-cdh5.7.0",
   "org.apache.spark" % "spark-mllib_2.10" % "1.6.0-cdh5.7.0",
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

resolvers ++= Seq(
  "JBoss Repository" at "http://repository.jboss.org/nexus/content/repositories/releases/",
  "Spray Repository" at "http://repo.spray.cc/",
  "Cloudera Repository" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
  "Akka Repository" at "http://repo.akka.io/releases/",
  "Twitter4J Repository" at "http://twitter4j.org/maven2/",
  "Apache HBase" at "https://repository.apache.org/content/repositories/releases",
  "Twitter Maven Repo" at "http://maven.twttr.com/",
  "scala-tools" at "https://oss.sonatype.org/content/groups/scala-tools",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Second Typesafe repo" at "http://repo.typesafe.com/typesafe/maven-releases/",
  "Mesosphere Public Repository" at "http://downloads.mesosphere.io/maven",
  Resolver.sonatypeRepo("public")
)