package httpserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, entity, get, getFromResource, path, post}
import akka.stream.ActorMaterializer
import config.propertiesLoader.loadProperties
import httpserver.GpsDataJsonProtocol.gpsDataFormat
import spray.json._
import akka.http.scaladsl.server.Directives._
import httpserver.KafkaConsumer.getLatestCoordinates

import java.util.Properties
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.language.postfixOps

object server extends App {
  implicit val system: ActorSystem = ActorSystem("gps-data-system")
  implicit val materialized: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val configFilePath: String = "C:\\path\\to\\project\\src\\main\\resources\\application.properties"
  val properties: Properties = loadProperties(configFilePath)

  KafkaConsumer.consume()

  private val route =
    path("send_gps") {
      post {
        entity(as[String]) { gpsDataJson =>
          try {
            val gpsData = gpsDataJson.parseJson.convertTo[GpsData]
            val jsonMessage = gpsData.toJson.toString()  //convert GPS data to json string
            KafkaProducer.sendToKafka(properties.getProperty("kafka.output.topic"), jsonMessage)   //send data to kafka
            complete(StatusCodes.OK -> "GPS data sent to kafka.")
          } catch {
            case ex: Exception =>
              complete(StatusCodes.BadRequest -> s"Failed to parse GPS data: ${ex.getMessage}")
          }
        }
      }
    } ~
    path("latest_coordinates") {
      get {
        getLatestCoordinates match {
          case Some((lat, lon)) =>
            complete(StatusCodes.OK, s"""{"latitude": $lat, "longitude": $lon}""")
          case None =>
            complete(StatusCodes.NotFound, "No GPS data available.")
        }
      }
    } ~
    get {
        getFromResource("public/index.html")
    }

  //Bind the server to localhost and port 8080
  private val ipaddress = properties.getProperty("httpserver.ipaddress")
  private val port = properties.getProperty("httpserver.port").toInt
  private val bindingFuture = Http().bindAndHandle(route, ipaddress, port)

  println(s"Server online at http://$ipaddress:$port/\nPress RETURN to stop...")
  StdIn.readLine()

  // Clean up
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => {
      KafkaProducer.close()
      system.terminate()
    })
}