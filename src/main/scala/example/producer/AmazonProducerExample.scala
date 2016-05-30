package example.producer

import play.api.libs.json._
import example.utils._
import scala.concurrent.Future
import example.model.{AmazonProductAndRating,AmazonProduct,AmazonRating}
import example.utils.AmazonPageParser
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
	args(0) : productId
	args(1) : userdId
  args(2) : rating
  Proof of concept about how to push data within a kafka topic
	Usage: ./amazon-producer-example 0981531679 A2OAG3QXW7A3RA 3.0
*/
object AmazonProducerExample {
  def main(args: Array[String]): Unit = {
   
   	val productId = args(0).toString
    val userId = args(1).toString
    val rating = args(2).toDouble
   	val topicName = "amazonRatingsTopic"
    
    println("Invoking amazon-producer-example with next parameters: ")
    println("productId: " + productId)
    println("userId: " + userId)
    println("rating: " + rating)
   	println("topicName: " + topicName)

    val producer = Producer[String](topicName)

    //0981531679 is Scala Puzzlers...
    //AmazonProductAndRating
    //def parse(productId: String,userId:String,rating:Double): Future[AmazonRating]
    AmazonPageParser.parse(productId,userId,rating).onSuccess { case amazonRating =>
      //Is this the correct way? the best performance? possibly not, what about using avro or parquet?
      producer.send(Json.toJson(amazonRating).toString)
      //producer.send(amazonRating)
      println("amazon product with rating sent to kafka cluster..." + amazonRating.toString)
      System.exit(0)
    }

  }
}

