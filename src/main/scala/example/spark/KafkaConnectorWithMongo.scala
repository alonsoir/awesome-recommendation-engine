package example.spark

import java.io.File
import java.util.Date

import com.google.gson.{Gson,GsonBuilder, JsonParser}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._

import com.mongodb.casbah.Imports._
import com.mongodb.QueryBuilder
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}


import org.apache.spark.streaming.kafka._
import kafka.serializer.StringDecoder

//import com.github.nscala_time.time.Imports._

/**
 * Collect at least the specified number of json tweets into cassandra, mongo...

on mongo shell:

 use alonsodb;
 db.tweets.find();
 */
object KafkaConnector {

  private var numTweetsCollected = 0L
  private var partNum = 0
  private val numTweetsToCollect = 10000000
  
  //this settings must be in reference.conf
  private val Database = "alonsodb"
  private val Collection = "tweets"
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
      val collection = client(Database)(Collection)
      collection.dropCollection()
  }

  def main(args: Array[String]) {
    // Process program arguments and set properties

    if (args.length < 2) {
      System.err.println("Usage: " + this.getClass.getSimpleName +
        "<brokers> <topics>")
      System.exit(1)
    }

    val Array(brokers, topics) = args

    println("Initializing Streaming Spark Context and kafka connector...")
    // Create context with 2 second batch interval
    val sparkConf = new SparkConf().setAppName("KafkaConnector").setMaster("local[4]").set("spark.driver.allowMultipleContexts", "true")
    val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sparkConf, Seconds(2))

    // Create direct kafka stream with brokers and topics
    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)
    
    println("Initialized Streaming Spark Context and kafka connector...")  

    println("Initializing mongodb connector...")

    val mongoClient = prepareMongoEnvironment()
    val collection = mongoClient(Database)(Collection)
    
    println("Initialized mongodb connector...")

    try {
        val sqlContext = new SQLContext(sc)
        println("Creating temporary table in mongo instance...")
        sqlContext.sql(
            s"""|CREATE TEMPORARY TABLE $Collection
              |(id STRING, tweets STRING)
              |USING $MongoProvider
              |OPTIONS (
              |host '$MongoHost:$MongoPort',
              |database '$Database',
              |collection '$Collection'
              |)
            """.stripMargin.replaceAll("\n", " "))

        messages.foreachRDD(rdd => {
          val count = rdd.count()
          if (count>0) {
            val topList = rdd.take(count.toInt)
            println("\nReading data from kafka broker... (%s total):".format(rdd.count()))
            topList.foreach(println)
            //println
            
            for (tweet <- topList) {
               collection.insert {MongoDBObject("id" -> new Date(),"tweets" -> tweet)}
            }//for (tweet <- topList)
            
            numTweetsCollected += count
            if (numTweetsCollected > numTweetsToCollect) {
              println
              println("numTweetsCollected > numTweetsToCollect condition is reached. Stopping..." + numTweetsCollected + " " + count)
              //cleanMongoEnvironment(mongoClient)
              closeMongoEnviroment(mongoClient)
              println("shutdown mongodb connector...")
              System.exit(0)
            }
          }//if(count>0)
        })//messages.foreachRDD(rdd =>
        
        //studentsDF.where(studentsDF("age") > 15).groupBy(studentsDF("enrolled")).agg(avg("age"), max("age")).show(5)
        val tweetsDF = sqlContext.read.format("com.stratio.datasource.mongodb").table(s"$Collection")
        //tweetsDF.show(numTweetsCollected.toInt)
        tweetsDF.show(5)
        println("tested a mongodb connection with stratio library...")
    } finally {
        //sc.stop()
        println("finished withSQLContext...")
    }

    ssc.start()
    ssc.awaitTermination()

    println("Finished!")
  }
}
