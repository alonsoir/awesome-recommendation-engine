package example.topic

import java.util.Properties

import example.utils.KafkaConfig

import scala.util.Random

import kafka.utils.ZKStringSerializer
import kafka.producer.{ Producer => KafkaProducer }
import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.serialize.ZkSerializer

object ZookeeperUtils {

  def createClient(
    config: Properties = KafkaConfig(),
    sessTimeout: Int = 10000,
    connTimeout: Int = 10000,
    serializer: ZkSerializer = ZKStringSerializer): ZkClient = {
    val host = config.getProperty("zookeeper.connect")
    new ZkClient(host, sessTimeout, connTimeout, serializer)
  }
}
