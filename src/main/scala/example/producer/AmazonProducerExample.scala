package example.producer

import play.api.libs.json._
import example.utils._
import scala.concurrent.Future
import example.model.AmazonProduct


/**
	args(0) : productId
	args(1) : userdId
*/
object AmazonProducerExample {
  def main(args: Array[String]): Unit = {
  	/*
  	val productId = args(0)
    val topicName = "amazonRatingsTopic"
    val amazonPageParser = AmazonPageParser

    val producer = Producer[String](topicName)

    val amazonProduct = amazonPageParser.parse(productId)

    implicit val writes = Json.writes[AmazonProduct]

    producer.send(Json.toJson(amazonProduct).toString)
    */
    println("Done...")
  }
}

