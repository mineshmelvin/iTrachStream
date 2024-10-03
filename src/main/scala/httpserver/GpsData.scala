package httpserver

import spray.json._

case class GpsData(latitude: String, longitude: String, timestamp: String, details: String) {
  def getLatitude: Double = latitude.toDouble
  def getLongitude: Double = longitude.toDouble
}

object GpsDataJsonProtocol extends DefaultJsonProtocol{
  implicit val gpsDataFormat: RootJsonFormat[GpsData] = jsonFormat4(GpsData)
}