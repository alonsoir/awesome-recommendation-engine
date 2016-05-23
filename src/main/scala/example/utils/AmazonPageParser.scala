package example.utils

import jodd.lagarto.dom.{NodeSelector, LagartoDOMBuilder}
import example.model.{AmazonProduct,AmazonProductAndRating,AmazonRating}
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.libs.json._
import example.utils._
import example.producer._

object AmazonPageParser {

  /**
  * productId: String 
  * userId: String
  * rating: Double
  */
  def parse(productId: String,userId:String,rating:Double): Future[AmazonRating] = {

    println("Trying to parse product with id " + productId)
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

          //case class AmazonProductAndRating(product: AmazonProduct, rating: AmazonRating)
          //val amazonProduct = AmazonProduct(productId, title, responseUrl, img, description)
          //println("amazonProduct is " + amazonProduct.toString)
          //I will have to include something from the http call... for now i let it as is
          val amazonRating = AmazonRating(userId, productId, rating)
          println("amazonRating is " + amazonRating.toString)

          amazonRating
        } else {
          println("An error happened! " + httpResponse.getStatusCode)
          throw new RuntimeException(s"Invalid url $url")
        }
    }//map
  }//parse method  
}
