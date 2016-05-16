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
   
  	val topicName = "amazonRatingsTopic"
    
   	val producer = Producer[String](topicName)

    //Scala Puzzlers...
    AmazonPageParser.parse("0981531679").onSuccess { case amazonProduct =>

      implicit val amazonFormat = Json.format[AmazonProduct]
      producer.send(Json.toJson(amazonProduct).toString)
      println("amazon product sent to kafka cluster..." + amazonProduct.toString)
    }
  }
}

