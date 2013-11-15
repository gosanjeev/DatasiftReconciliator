package org.gosanjeev.datasift

import scala.util.{Success, Failure}
import dispatch._
import dispatch.Defaults._
import scala.concurrent.{Future, future, Await}
import scala.concurrent.duration._
import org.json4s._
import JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import java.io._
import org.gosanjeev.http.AsyncHttpClient
import org.gosanjeev.json.JsonParser
import org.slf4j.LoggerFactory

object ListSubsFromDatasift {
  val log = LoggerFactory.getLogger(getClass)

  def process(conf: Config) = {

    val PER_PAGE = 20
    val dataSiftUser = conf.getString("app.datasift.user")
    val apiKey = conf.getString("app.datasift.apiKey")
    val getUrl = conf.getString("app.datasift.getUrl")
    val searchSubscriptionId = conf.getString("app.search.subscriptionId")
    val searchStreamId = conf.getString("app.search.streamId")
    val searchProtocol = conf.getString("app.search.protocol")
    val searchStatus = conf.getString("app.search.status")
    val searchUrl = conf.getString("app.search.url")
    val searchCreatedAt = conf.getString("app.search.createdAtAfter")
    val searchupdatedAt = conf.getString("app.search.updatedAtAfter")
    val outputFileName = conf.getString("app.datasift.subscriptionsFile")
    val asyncHttpClient = new AsyncHttpClient(conf)
    val writer = new PrintWriter(new File(outputFileName))
    implicit val formats = DefaultFormats

    def processResult(page: Int): Int = {

      val getUrlResponse = asyncHttpClient.doPost(postUrl = getUrl,
        params = Map("per_page" -> s"$PER_PAGE", "page" -> s"$page", "order_by" -> "created_at", "order_dir" -> "asc"),
        httpHeaders = Map("Authorization" -> s"$dataSiftUser:$apiKey")
      )
      val responseGet = scala.concurrent.Await.result(getUrlResponse, 10 seconds)
      val jParser = new JsonParser

      val jsonObj = jParser.fromSerializedString(responseGet.body)
      val count: String = jParser.extractElementFromJSON("count", jsonObj)
      val subscriptions: List[JValue] = jParser.extractElementsFromJSON("subscriptions", jsonObj)
      // for each subscription
      subscriptions.foreach {
        subscription =>
          val subId: String = jParser.extractElementFromJSON("id", subscription)
          val outputType: String = jParser.extractElementFromJSON("output_type", subscription)
          val hash: String = jParser.extractElementFromJSON("hash", subscription)
          val status: String = jParser.extractElementFromJSON("status", subscription)
          val lastSuccess: String = jParser.extractElementFromJSON("last_success", subscription)
          val createdAt: String = jParser.extractElementFromJSON("created_at", subscription)
          val registeredUrl: String = if (outputType == "ftp") (subscription \ "output_params" \ "host").extract[String] else (subscription \ "output_params" \ "url").extract[String]

          def checkForSearchParams(): Boolean = {
            if (compare(Option(searchSubscriptionId), subId, "String") &&
              compare(Option(searchStreamId), hash, "String") &&
              compare(Option(searchProtocol), outputType, "String") &&
              compare(Option(searchStatus), status, "String") &&
              compare(Option(searchUrl), registeredUrl, "String") &&
              compare(Option(searchCreatedAt), createdAt, "Int") &&
              compare(Option(searchupdatedAt), lastSuccess, "Int")
            )
              true
            else
              false
          }
          if (checkForSearchParams) {
            val json =
              (
                ("subscription_id" -> subId) ~
                  ("stream_id" -> hash) ~
                  ("url" -> registeredUrl) ~
                  ("protocol" -> outputType) ~
                  ("status" -> status) ~
                  ("created_at" -> createdAt) ~
                  ("updated_at" -> lastSuccess)
                )
            writer.write(compact(render(json)))
            writer.write("\n")
          } else {
            log.debug(s"Skipping writing the record...")
          }

      }
      count.toInt
    }

    val countInt = processResult(1)
    val iter = countInt / PER_PAGE
    if (iter > 0) {
      // Multiple calls are needed
      for (i <- 2 to (iter + 1)) {
        processResult(i)
      }
    }
    writer.close()
  }

  /**
   * If param1 is empty or null or is equal to param2 then return true else false
   * @param param1
   * @param param2
   * @return
   */
  def compare(param1: Option[String], param2: String, pType: String): Boolean = {
    log.debug(s"Param1: $param1, Param2: $param2, Type: $pType")
    param1 match {
      case Some(v) =>
        if (v == "") {
          true
        } else {
          pType match {
            case "Int" =>
              if (v != "null") {
                if (param2.toInt > v.toInt) {
                  true
                } else {
                  false
                }
              } else {
                if (param2 == null || param2.isEmpty) {
                  true
                }
                else {
                  false
                }
              }

            case _ => if (v.toString == param2) {
              true
            } else {
              false
            }
          }
        }
      case None =>
        true
    }
  }

  def main(args: Array[String]) {

    val conf = ConfigFactory.load()
    process(conf)
  }
}
