package example.producer

import play.api.libs.json._
import example.utils._
import scala.concurrent.Future
import example.model.AmazonProduct
import example.utils.AmazonPageParser
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
	args(0) : productId
	args(1) : userdId
*/
object AmazonProducerExample {
  def main(args: Array[String]): Unit = {
   
   	val productId = args(0).toString
   	val topicName = "amazonRatingsTopic"
    
   	val producer = Producer[String](topicName)

    //0981531679 is Scala Puzzlers...
    AmazonPageParser.parse(productId).onSuccess { case amazonProduct =>

      implicit val amazonFormat = Json.format[AmazonProduct]
      producer.send(Json.toJson(amazonProduct).toString)
      println("amazon product sent to kafka cluster..." + amazonProduct.toString)
      System.exit(0)
    }
  }
}

