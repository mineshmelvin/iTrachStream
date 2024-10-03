package httpserver

import config.propertiesLoader.loadProperties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import java.util.Properties

object KafkaProducer {

  private val kafkaProducer: KafkaProducer[String, String] = {
    val configFilePath: String = "C:\\path\\to\\project\\src\\main\\resources\\application.properties"
    val properties: Properties = loadProperties(configFilePath)
    val props = new Properties()
    props.put("bootstrap.servers", properties.getProperty("kafka.bootstrap.servers"))
    props.put("key.serializer", classOf[StringSerializer].getName)
    props.put("value.serializer", classOf[StringSerializer].getName)

    new KafkaProducer[String, String](props)
  }

  def sendToKafka(topic: String, message: String): Unit = {
    val record = new ProducerRecord[String, String](topic, message)
    kafkaProducer.send(record)
  }

  def close(): Unit = kafkaProducer.close()

}
