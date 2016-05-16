package example.topic

import java.util.Properties

import kafka.admin.AdminUtils
import org.I0Itec.zkclient.ZkClient

case class TopicAdmin(zkClient: ZkClient) {

  def createTopic(name: String, partitionNum: Int = 1, replicationFactor: Int = 1, config: Properties = new Properties()): Unit = {
    AdminUtils.createTopic(zkClient, name, partitionNum, replicationFactor, config)
  }

  def deleteTopic(topicName: String) {
    AdminUtils.deleteTopic(zkClient, topicName)
  }
}
