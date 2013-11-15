package org.gosanjeev.json

import org.json4s._
import JsonDSL._
import org.json4s.native.JsonMethods._
import org.slf4j.LoggerFactory

object JsonParser {
  def apply() = new JsonParser
}

class JsonParser {

  val log = LoggerFactory.getLogger(getClass)
   // Create implicit formats that is used for JSON de-serialization
  implicit val formats = DefaultFormats

  def fromSerializedString(jsonString: String): JValue = {
    try {
      val json = parse(jsonString)
      Some(json)
    } catch {
      case e: Throwable =>
        log.error(s"Could not parse JSON string for message, JSON = $jsonString")
        None
    }
  }

  def extractElementFromJSON(element: String, json: JValue) = {

    var retVal: String = null
    try {
       retVal = (json \ element).extract[String]
    } catch {
      case e: Throwable =>
        log.error(s"Could not extract JSON element for $element - Message: $e.message")
        None
    }
    retVal
  }

  def extractElementsFromJSON(element: String, json: JValue) = {

    var retVal: List[JValue] = null
    try {
       retVal = (json \ element).extract[List[JValue]]
    } catch {
      case e: Throwable =>
        log.error(s"Could not extract JSON elements for $element - Message: $e.message")
        None
    }
    retVal
  }
}
