package example.utils

import com.typesafe.config.ConfigFactory

import twitter4j._
import twitter4j.conf.ConfigurationBuilder
import twitter4j.json.DataObjectFactory

import TwitterClient._

case class TwitterClient {


  private val twitterStream = new TwitterStreamFactory(cb.build()).getInstance()

  def addListener(statusHandler: (Status => Unit)) = {
    val listener = new StatusListener() {
      def onStatus(status: Status) = statusHandler(status)
      def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) = {}
      def onTrackLimitationNotice(numberOfLimitedStatuses: Int) = {}
      def onScrubGeo(userId: Long, upToStatusId: Long) = {}
      def onException(ex: Exception) = {}
      def onStallWarning(warning: StallWarning) = {}
    }

    twitterStream.addListener(listener)
    listener
  }

  def run = twitterStream.sample();
}
object TwitterClient {
  val config = ConfigFactory.load()

  val consumerKey = config.getString("twitter.consumerKey")
  val consumerSecret = config.getString("twitter.consumerSecret")
  val accessToken = config.getString("twitter.accessToken")
  val accessTokenSecret = config.getString("twitter.accessTokenSecret")

  val cb = new ConfigurationBuilder()
  cb.setOAuthConsumerKey(consumerKey)
  cb.setOAuthConsumerSecret(consumerSecret)
  cb.setOAuthAccessToken(accessToken)
  cb.setOAuthAccessTokenSecret(accessTokenSecret)
  cb.setJSONStoreEnabled(true)
  cb.setIncludeEntitiesEnabled(true)

}