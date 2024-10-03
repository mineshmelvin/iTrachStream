package httpserver

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import config.propertiesLoader.loadProperties
import httpserver.GpsDataJsonProtocol.gpsDataFormat
import org.apache.kafka.clients.consumer.KafkaConsumer
import spray.json._

import java.util.Properties
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.jdk.CollectionConverters.iterableAsScalaIterableConverter

object KafkaConsumer {
  implicit val system: ActorSystem = ActorSystem("GpsConsumerSystem")
  implicit val materialized: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val configFilePath: String = "C:\\path\\to\\project\\src\\main\\resources\\application.properties"
  private val appProperties: Properties = loadProperties(configFilePath)

  // Configure Kafka Consumer
  private val kafkaProps = new java.util.Properties()
  kafkaProps.put("bootstrap.servers", appProperties.getProperty("kafka.bootstrap.servers"))
  kafkaProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  kafkaProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  kafkaProps.put("group.id", "gps-consumer-group")

  private val kafkaConsumer = new KafkaConsumer[String, String](kafkaProps)

  // This variable will hold the latest coordinates
  @volatile private var latestCoordinates: Option[(Double, Double)] = None


  kafkaConsumer.subscribe(java.util.Collections.singletonList(appProperties.getProperty("kafka.output.topic")))

  def getLatestCoordinates: Option[(Double, Double)] = latestCoordinates

  def consume(): Future[Unit] = Future {
    while (true) {
      val records = kafkaConsumer.poll(java.time.Duration.ofMillis(100))
      for (record <- records.asScala) {
        val gpsDataJson = record.value()
        // Assuming gpsDataJson has fields 'latitude' and 'longitude'
        val gpsData = gpsDataJson.parseJson.convertTo[GpsData]
        latestCoordinates = Some((gpsData.latitude.toDouble, gpsData.longitude.toDouble))
        println(s"Updated coordinates: $latestCoordinates")
      }
    }
  }
}
