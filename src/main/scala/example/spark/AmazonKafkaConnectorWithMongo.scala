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

Usage: nohup ./amazon-kafka-connector 192.168.1.35:9092 amazonRatingsTopic > nohup.out

where $1 is kafka broker and $2 is kafka-topicval ratingFile="hdfs://127.0.0.1:8020/user/cloudera/ratings.csv"

case class AmazonRating(userId: String, productId: String, rating: Double)

val NumRecommendations = 10
val MinRecommendationsPerUser = 10
val MaxRecommendationsPerUser = 20
val MyUsername = "myself"
val NumPartitions = 20

  
println("Using this ratingFile: " + ratingFile)
  // first create an RDD out of the rating file
val rawTrainingRatings = sc.textFile(ratingFile).map {
    line =>
      val Array(userId, productId, scoreStr) = line.split(",")
      AmazonRating(userId, productId, scoreStr.toDouble)
}

  // only keep users that have rated between MinRecommendationsPerUser and MaxRecommendationsPerUser products
val trainingRatings = rawTrainingRatings.groupBy(_.userId).filter(r => MinRecommendationsPerUser <= r._2.size  && r._2.size < MaxRecommendationsPerUser).flatMap(_._2).repartition(NumPartitions).cache()

println(s"Parsed $ratingFile. Kept ${trainingRatings.count()} ratings out of ${rawTrainingRatings.count()}")

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
                                   //.setMaster("spark://quickstart.cloudera:7077")
                                   .set("spark.driver.allowMultipleContexts", "true")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    //sc.addJar("target/scala-2.10/blog-spark-recommendation_2.10-1.0-SNAPSHOT.jar")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    //this checkpointdir should be in a conf file, for now it is hardcoded!
    val streamingCheckpointDir = "/Users/aironman/my-recommendation-spark-engine/checkpoint"
    ssc.checkpoint(streamingCheckpointDir)
    
    // Create direct kafka stream with brokers and topics
    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)
    println("Initialized Streaming Spark Context and kafka connector...")

    //create recomendation module
    println("Creating rating recommender module...")
    //val ratingFile= "/Users/aironman/my-recommendation-spark-engine/ratings.csv"
    val ratingFile= "hdfs://quickstart.cloudera:8020/user/cloudera/ratings.csv"
    val recommender = new Recommender(sc,ratingFile)
    println("Initialized rating recommender module...")

    try{
    messages.foreachRDD(rdd => {
     val count = rdd.count()
     if (count > 0){
       val json= rdd.map(_._2)
       val dataFrame = sqlContext.read.json(json) //converts json to DF
       val myRow = dataFrame.select(dataFrame("userId"),dataFrame("productId"),dataFrame("rating")).take(count.toInt)
       println("myRow is: " + myRow)
       //case class AmazonRating(userId: String, productId: String, rating: Double)
       val myAmazonRating = AmazonRating(myRow(0).getString(0), myRow(0).getString(1), myRow(0).getDouble(2))
       println("myAmazonRating is: " + myAmazonRating.toString)
       val arrayAmazonRating = Array(myAmazonRating)
       //this method needs Seq[AmazonRating]
       recommender.predict(arrayAmazonRating.toSeq)
       }//if
    })      
    }catch{
      case e: IllegalArgumentException => {println("illegal arg. exception")};
      case e: IllegalStateException    => {println("illegal state exception")};
      case e: ClassCastException       => {println("ClassCastException")};
      case e: Exception                => {println(" Generic Exception")};
    }finally{

      println("Finished taking data from kafka topic...")
    }
    
    ssc.start()
    ssc.awaitTermination()

    println("Finished!")
  }
}
