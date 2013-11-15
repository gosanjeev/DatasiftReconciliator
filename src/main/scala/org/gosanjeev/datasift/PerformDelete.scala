package org.gosanjeev.datasift

import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import scala.concurrent.duration._
import org.json4s._
import JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import java.io.{File, FileWriter, PrintWriter}
import org.gosanjeev.http.AsyncHttpClient
import org.gosanjeev.json.JsonParser
import org.slf4j.LoggerFactory

object PerformDelete {

  def delete(conf: Config) = {

    val log = LoggerFactory.getLogger(getClass)

    // Read the delete file
    // For each json entry
    //    extract the subscription
    //    delete the subscription
    val badWriterFileName = conf.getString("app.datasift.subscriptionsToDelFile")
    val badDataFile = scala.io.Source.fromFile(badWriterFileName)
    val badDataContent = "{ \"data\":[" + badDataFile.getLines.mkString + "]}"
    badDataFile.close

    val dataSiftUser = conf.getString("app.datasift.user")
    val apiKey = conf.getString("app.datasift.apiKey")
    val deleteUrl = conf.getString("app.datasift.deleteUrl")
    val asyncHttpClient = new AsyncHttpClient(conf)
    val jParser = new JsonParser
    val badDataJson = jParser.fromSerializedString(badDataContent)
    val badObjs:List[JValue] = jParser.extractElementsFromJSON("data", badDataJson)
    log.info(s"Count of items for deletion: ${badObjs.size} ")
    badObjs.foreach {
      badObj =>
        val subscriptionId = jParser.extractElementFromJSON("subscription_id", badObj)
        log.info(s"Data to delete: $subscriptionId")
        val deleteUrlResponse = asyncHttpClient.doPost(postUrl = deleteUrl,
          params = Map("id" -> s"$subscriptionId"),
          httpHeaders = Map("Authorization" -> s"$dataSiftUser:$apiKey")
        )
        val responseDel = scala.concurrent.Await.result(deleteUrlResponse, 10 seconds)
        log.info(s"Response from Datasift on delete invocation: $responseDel.body")
    }
  }

  def main(args: Array[String]) {

    val conf = ConfigFactory.load()
    delete(conf)
  }
}
