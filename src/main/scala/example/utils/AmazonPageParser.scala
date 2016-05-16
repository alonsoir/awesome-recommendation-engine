package example.utils

import jodd.lagarto.dom.{NodeSelector, LagartoDOMBuilder}
import example.model.AmazonProduct
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.libs.json._
import example.utils._
import example.producer._

object AmazonPageParser {

  private val topicName = "amazonRatingsTopic"
    
  private val producer = Producer[String](topicName)


  def parse(productId: String): Future[AmazonProduct] = {
    val url = s"http://www.amazon.com/dp/$productId"
    HttpClient.fetchUrl(url) map {
      httpResponse =>
        if (httpResponse.getStatusCode == 200) {
          val body = httpResponse.getResponseBody
          val domBuilder = new LagartoDOMBuilder()
          val doc = domBuilder.parse(body)

          val responseUrl = httpResponse.getUri.toString
          val nodeSelector = new NodeSelector(doc)
          val title = nodeSelector.select("span#productTitle").head.getTextContent
          val img = nodeSelector.select("div#main-image-container img").head.getAttribute("src")
          val description = nodeSelector.select("div#feature-bullets").headOption.map(_.getHtml).mkString

          val amazonProduct = AmazonProduct(productId, title, responseUrl, img, description)

          println("amazonProduct is " + amazonProduct.toString)
          amazonProduct
        } else {
          println("An error happened!")
          throw new RuntimeException(s"Invalid url $url")
        }
    }//map
  }//parse method

  def main(args: Array[String]): Unit = {
   
    //Scala Puzzlers...
    AmazonPageParser.parse("0981531679").onSuccess { case amazonProduct =>

      implicit val amazonFormat = Json.format[AmazonProduct]
      producer.send(Json.toJson(amazonProduct).toString)
      println("amazon product sent to kafka cluster..." + amazonProduct.toString)
    }
  }
}
