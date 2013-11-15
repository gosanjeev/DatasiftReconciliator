package org.gosanjeev.datasift

import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import org.json4s._
import JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import java.io.{File, FileWriter, PrintWriter}
import org.gosanjeev.json.JsonParser
import org.slf4j.LoggerFactory

/**
 * Created by sanjeev on 11/8/13.
 */
object PerformDiff {

  val log = LoggerFactory.getLogger(getClass)

  // Read content from local i/p file into jsonLocal
  // perform any post processing if needed
  // Read content from datasift i/p file jsonRemote
  // for each jsonRemote:
  //   Put in hashmap key (subId), value (List(json))
  // for each jsonLocal:
  //   Put in hashmap key (subId), value (List(json))
  // For each key in hashmap:
  //   if List has two elements then write to goodData
  //   else write to badData
  def diffCompute(conf: Config) = {
    val localData = scala.io.Source.fromFile(conf.getString("app.search.subscriptionsLocalFile") )
    val remoteData = scala.io.Source.fromFile(conf.getString("app.datasift.subscriptionsFile") )

    val localDataContent = "{ \"data\":[" + localData.getLines.mkString + "]}"
    val remoteDataContent = "{ \"data\":[" + remoteData.getLines.mkString + "]}"

    val jParser = new JsonParser
    val localJson = jParser.fromSerializedString(localDataContent)
    val remoteJson = jParser.fromSerializedString(remoteDataContent)

    localData.close()
    remoteData.close()

    val localObjs:List[JValue] = jParser.extractElementsFromJSON("data", localJson)
    val remoteObjs:List[JValue] = jParser.extractElementsFromJSON("data", remoteJson)
    val cache = collection.mutable.Map[String, List[JValue]]()

    remoteObjs.foreach {
      remoteObj =>
        val subscriptionId = jParser.extractElementFromJSON("subscription_id", remoteObj)
        log.debug(s"SubId(R): $subscriptionId")
        cache put (subscriptionId, List(remoteObj))
    }
    localObjs.foreach {
      localObj =>
        val subscriptionId = jParser.extractElementFromJSON("subscription_id", localObj)
        log.debug(s"SubId(L): $subscriptionId")
        val cachedData = cache.get(subscriptionId)
        log.debug(s"CachedData: $cachedData")
        cachedData match {
          case Some(data) => cache put (subscriptionId, List(data.head, localObj))
          case None => log.debug(s"SubId(L): $subscriptionId Not found in datasift file!!!")
        }
    }
    val goodWriterFileName = conf.getString("app.result.subscriptionsGoodFile")
    val badWriterFileName = conf.getString("app.result.subscriptionsBadFile")
    val goodWriter = new PrintWriter(new FileWriter(new File(goodWriterFileName), false))
    val badWriter = new PrintWriter(new FileWriter(new File(badWriterFileName), false))

    val keys = cache.keys
    keys.foreach {
      key =>
        log.debug(s"Key: $key")
        val value = cache get key
        value match {
          case Some(v) =>
            if (v.size > 1) { // Good data
              goodWriter.write(compact(render(v(0))))
              goodWriter.write("\n")
            } else { // Bad data
              badWriter.write(compact(render(v(0))))
              badWriter.write("\n")
            }
          case None =>
            println(s"ERROR for key $key")
        }
    }
    goodWriter.close()
    badWriter.close()
  }

  def main(args: Array[String]) {

    val conf = ConfigFactory.load()
    diffCompute(conf)
  }
}
