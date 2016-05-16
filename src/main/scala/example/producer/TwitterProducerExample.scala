package example.producer

import twitter4j.Status
import twitter4j.json.DataObjectFactory
import play.api.libs.json.Json
import example.utils.TwitterClient
import org.codehaus.jackson._
import org.codehaus.jackson.JsonToken._

//update this case class to save every field you want to parse...
case class SimpleParsed(id: Long, text: String)

object TwitterProducer {
  def main(args: Array[String]): Unit = {
    val topicName =
      if(args.length == 0) "mali"
      else args(0)
    println("topicName is " + topicName)
    val strProducer = Producer[String](topicName)
    val twitterClient = new TwitterClient
    val parserFactory = new JsonFactory()

  def parse(str: String) = {
    val parser = parserFactory.createJsonParser(str)
    var nested = 0
    if (parser.nextToken() == START_OBJECT) {
      
      var token = parser.nextToken()
      var textOpt:Option[String] = None
      var screen_name:Option[String] = None
      var idOpt:Option[Long] = None
      var place:Option[String] = None
      var lang:Option[String] = None
      var name:Option[String] = None
      var time_zone:Option[String] = None
      var url:Option[String]=None
      var retweet_count:Option[String]=None
      var retweeted:Option[String]=None
      
      while(token != null) {
        if (token == FIELD_NAME && nested >= 0) {
          parser.getCurrentName() match {
            case "text" => {
              parser.nextToken()
              textOpt = Some(parser.getText())
              println("text is " +textOpt.getOrElse("nothing to show"))
            }
            case "id" => {
              parser.nextToken()
              idOpt = Some(parser.getLongValue())
              println("id is " +idOpt.getOrElse("nothing to show"))
            }
            case "place" => {
              parser.nextToken()
              place = Some(parser.getText())
              println("place is " +place.getOrElse("nothing to show"))
            }
            case "lang" => {
              parser.nextToken()
              lang = Some(parser.getText())
              println("lang is " +lang.getOrElse("nothing to show"))
            }
            case "name" => {
              parser.nextToken()
              name = Some(parser.getText())
              println("name is " +name.getOrElse("nothing to show"))
            }
            case "screen_name" => {
              parser.nextToken()
              screen_name = Some(parser.getText())
              println("screen_name is " +screen_name.getOrElse("nothing to show"))
            }
            case "time_zone" => {
              parser.nextToken()
              time_zone = Some(parser.getText())
              println("time_zone is " +time_zone.getOrElse("nothing to show"))
            }
            case "url" => {
              parser.nextToken()
              url = Some(parser.getText())
              println("url is " +url.getOrElse("nothing to show"))
            }
            case "retweet_count" => {
              parser.nextToken()
              retweet_count = Some(parser.getText())
              println("retweet_count is " +retweet_count.getOrElse("nothing to show"))
            }
            case "retweeted" => {
              parser.nextToken()
              retweeted = Some(parser.getText())
              println("retweeted is " +retweeted.getOrElse("nothing to show"))
            }
            
            case _ => // noop
          }
        } else if (token == START_OBJECT) {
          nested += 1
        } else if (token == END_OBJECT) {
          nested -= 1
        }
        token = parser.nextToken()
      }
      //creating the object, i can send it to the broker...
      if (textOpt.isDefined && idOpt.isDefined) {
        Some(SimpleParsed(idOpt.get, textOpt.get))
      } else {
        None
      }
    } else {
      None
    }
  }

  def handler(status: Status) = {
      val data = DataObjectFactory.getRawJSON(status)
      
      parse(data)
      //println
      //sending data to the broker...
      strProducer.send(data)
    }

    twitterClient.addListener(handler)
    twitterClient.run
 }
}
