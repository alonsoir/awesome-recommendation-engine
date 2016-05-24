package example.spark

import java.io.File
import java.util.Date

import play.api.libs.json._
import com.google.gson.{Gson,GsonBuilder, JsonParser}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._

import com.mongodb.casbah.Imports._
import com.mongodb.QueryBuilder
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}

import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONDocument

import org.apache.spark.streaming.kafka._
import kafka.serializer.StringDecoder
import example.model._

import example.utils.Recommender
//import com.github.nscala_time.time.Imports._

/**
 * Collect at least the specified number of json amazon products in order to feed recomedation system and feed mongo instance with results.

Usage: ./amazon-kafka-connector 127.0.0.1:9092 amazonRatingsTopic

on mongo shell:

 use alonsodb;
 db.amazonRatings.find();
 */
object AmazonKafkaConnector {

  private var numAmazonProductCollected = 0L
  private var partNum = 0
  private val numAmazonProductToCollect = 10000000
  
  //this settings must be in reference.conf
  private val Database = "alonsodb"
  private val ratingCollection = "amazonRatings"
  private val MongoHost = "127.0.0.1"
  private val MongoPort = 27017
  private val MongoProvider = "com.stratio.datasource.mongodb"

  private val jsonParser = new JsonParser()
  private val gson = new GsonBuilder().setPrettyPrinting().create()

  private def prepareMongoEnvironment(): MongoClient = {
      val mongoClient = MongoClient(MongoHost, MongoPort)
      mongoClient
  }

  private def closeMongoEnviroment(mongoClient : MongoClient) = {
      mongoClient.close()
      println("mongoclient closed!")
  }

  private def cleanMongoEnvironment(mongoClient: MongoClient) = {
      cleanMongoData(mongoClient)
      mongoClient.close()
  }

  private def cleanMongoData(client: MongoClient): Unit = {
      val collection = client(Database)(ratingCollection)
      collection.dropCollection()
  }

  def main(args: Array[String]) {
    // Process program arguments and set properties

    if (args.length < 2) {
      System.err.println("Usage: " + this.getClass.getSimpleName + " <brokers> <topics>")
      System.exit(1)
    }

    val Array(brokers, topics) = args

    println("Initializing Streaming Spark Context and kafka connector...")
    // Create context with 2 second batch interval
    val sparkConf = new SparkConf().setAppName("AmazonKafkaConnector")
                                   .setMaster("local[4]")
                                    .set("spark.driver.allowMultipleContexts", "true")

    val sc = new SparkContext(sparkConf)
    sc.addJar("target/scala-2.10/blog-spark-recommendation_2.10-1.0-SNAPSHOT.jar")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    //val streamingCheckpointDir = "someDir"
    //ssc.checkpoint(streamingCheckpointDir)
    
    // Create direct kafka stream with brokers and topics
    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)
    println("Initialized Streaming Spark Context and kafka connector...")

    //create recomendation module
    println("Creating rating recommender module...")
    val ratingFile= "ratings.csv"
    val recommender = new Recommender(sc,ratingFile)
    println("Initialized rating recommender module...")
      

    //i have to convert messages which is a InputDStream into a Seq...
    //val amazonRatings = recommender.predict(messages.take(MaxRecommendations)).toSeq
    try{
    messages.foreachRDD( rdd =>{
      val count = rdd.count()
      if (count > 0){
        //someMessages should be AmazonRating...
        val someMessages = rdd.take(count.toInt)
        println("<------>")
        println("someMessages is " + someMessages)
        someMessages.foreach(println)
        println("<------>")
        println("<---POSSIBLE SOLUTION--->")
        messages
        .map { case (_, jsonRating) => 
          val jsValue = Json.parse(jsonRating)
          AmazonRating.amazonRatingFormat.reads(jsValue) match {
            case JsSuccess(rating, _) => rating
            case JsError(_) => AmazonRating.empty
          }
             }
        .filter(_ != AmazonRating.empty)
        //this line provokes an compile error...
        .foreachRDD(_.foreachPartition(it => recommender.predictWithALS(it.toSeq)))
          
        println("<---POSSIBLE SOLUTION--->")
        
      }
      }
    )
    }catch{
      case e: IllegalArgumentException => {println("illegal arg. exception")};
      case e: IllegalStateException    => {println("illegal state exception")};
      case e: ClassCastException       => {println("ClassCastException")};
      case e: Exception                => {println(" Generic Exception")};
    }finally{

      println("Finished taking data from kafka topic...")
    }
    
    //println("jsonParsed is " + jsonParsed)
    //The idea is to save results from Recommender.predict within mongodb, so i will have to deal with this issue 
    //after resolving the issue of .foreachRDD(_.foreachPartition(recommender.predict(_.toSeq)))
    /*
    println("Initializing mongodb connector...")

    val mongoClient = prepareMongoEnvironment()
    val collection = mongoClient(Database)(ratingCollection)
    
    println("Initialized mongodb connector...")

    try {
        val sqlContext = new SQLContext(sc)
        println("Creating temporary table in mongo instance...")
        sqlContext.sql(
            s"""|CREATE TEMPORARY TABLE $ratingCollection
              |(id STRING, amazonProduct STRING)
              |USING $MongoProvider
              |OPTIONS (
              |host '$MongoHost:$MongoPort',
              |database '$Database',
              |collection '$ratingCollection'
              |)
            """.stripMargin.replaceAll("\n", " "))

        messages.foreachRDD(rdd => {
          val count = rdd.count()
          if (count>0) {
            val topList = rdd.take(count.toInt)
            println("\nReading data from kafka broker... (%s total):".format(rdd.count()))
            topList.foreach(println)
            //println
            
            for (amazonProduct <- topList) {
               collection.insert {MongoDBObject("id" -> new Date(),"amazonProduct" -> amazonProduct)}
            }//for (tweet <- topList)
            
            numAmazonProductCollected += count
            if (numAmazonProductCollected > numAmazonProductToCollect) {
              println
              println("amazonProduct > amazonProduct condition is reached. Stopping..." + numAmazonProductCollected + " " + count)
              //cleanMongoEnvironment(mongoClient)
              closeMongoEnviroment(mongoClient)
              println("shutdown mongodb connector...")
              System.exit(0)
            }
          }//if(count>0)
        })//messages.foreachRDD(rdd =>
        
        //studentsDF.where(studentsDF("age") > 15).groupBy(studentsDF("enrolled")).agg(avg("age"), max("age")).show(5)
        val amazonProductsCollectedDF = sqlContext.read.format("com.stratio.datasource.mongodb").table(s"$ratingCollection")
        amazonProductsCollectedDF.show(5)
        println("tested a mongodb connection with stratio library...")
    } finally {
        //sc.stop()
        println("finished withSQLContext...")
    }
  */
    ssc.start()
    ssc.awaitTermination()

    println("Finished!")
  }
}
