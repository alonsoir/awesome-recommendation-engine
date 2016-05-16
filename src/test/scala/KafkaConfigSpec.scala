import example.utils.KafkaConfig
import org.scalatest.FunSuite

class KafkaConfigSpec extends FunSuite {
  val config = new KafkaConfig {}

  test("Consumer config should be read") {
    assert(config.getProperty("group.id") == "1234")
    assert(config.getProperty("zookeeper.connect") == "localhost:2821")
  }

  test("example.producer.Producer config should be read") {
    assert(config.getProperty("metadata.broker.list") == "broker1:9092,broker2:9092")
    assert(config.getProperty("serializer.class") == "kafka.serializer.StringEncoder")
    assert(config.getProperty("partitioner.class") == "example.producer.SimplePartitioner")
    assert(config.getProperty("request.required.acks") == "1")
  }

  test("Missing keys should be null") {
    assert(config.getProperty("some.other.key") == null)
  }
}
