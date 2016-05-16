package example.topic

import scala.util.Random
import example.topic.ZookeeperUtils._

object CreateTopicExample {
  def main(args: Array[String]): Unit = {
    val topicName = if(args.length == 0) {
      Random.alphanumeric.take(5).mkString
    } else {
      args(0).toString
    }

    println(s"Creating topic with name: $topicName")
    val zookeeperClient = createClient()
    TopicAdmin(zookeeperClient).createTopic(topicName)
    println(s"Created topic $topicName")
  }
}