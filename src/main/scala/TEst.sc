/**
 * Created by sanjeev on 11/18/13.
 */

/**
 * Created by sanjeev on 11/18/13.
 */

import java.lang.String
import org.json4s.JsonDSL
import org.json4s._
import JsonDSL._
import org.json4s.native.JsonMethods._
import scala.Predef.String

// Can I change this:
// json here is a JValue object from json4s.
//jsonX.transformField {
//  case ("ability", x) => ("abilityId", x)
//  case ("time", x) => ("timeRaw", x)
//}

// To a method, so I can re-use it in multiple classes:
def jsonTransform(json: JValue, mappings: Map[String, String]): JValue = {
  for ((key, value) <- mappings) {
    // Do transformation

    println(s"$key + ${value.toString}")
  }
  json
}





val mappings = Map(
  "ability" -> "abilityId",
  "time" -> "timeRaw"
)

newJson = jsonTransform(jsonX, mappings)








