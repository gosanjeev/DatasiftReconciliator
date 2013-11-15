package org.gosanjeev.http

import scala.util.Left
import scala.util.Right
import com.typesafe.config.Config
import scala.concurrent.Future
import dispatch._
import dispatch.Defaults._
import org.slf4j.LoggerFactory

/**
 * Created by sanjeev on 11/8/13.
 */

case class HttpResponse(statusCode: Int, body: String) {
  def isSuccess(): Boolean = statusCode == 200
}

object AsyncHttpClient {
  def apply(conf: Config):AsyncHttpClient = new AsyncHttpClient(conf)
}

class AsyncHttpClient(conf: Config) {
  val log = LoggerFactory.getLogger(getClass)
  // Create a dispatch http client with right configuration
  val client = dispatch.Http.configure(builder => builder
    .setCompressionEnabled(conf.getBoolean("app.asyncHttpClient.enableCompression"))
    .setAllowPoolingConnection(conf.getBoolean("app.asyncHttpClient.allowPoolingConnection"))
    .setMaximumConnectionsPerHost(conf.getInt("app.asyncHttpClient.maxConnectionsPerHost"))
    .setMaximumConnectionsTotal(conf.getInt("app.asyncHttpClient.maxConnectionsTotal"))
    .setMaxRequestRetry(conf.getInt("app.asyncHttpClient.maxRequestRetry"))
    .setConnectionTimeoutInMs(conf.getInt("app.asyncHttpClient.connectionTimeoutInMs"))
    .setRequestTimeoutInMs(conf.getInt("app.asyncHttpClient.requestTimeoutInMs")))

  def doPost(
              postUrl: String,
              params: Map[String, String],
              httpHeaders: Map[String, String] = Map.empty[String, String]): Future[HttpResponse] = {

    // Build request object
    val request = url(postUrl).POST

    // Add POST params
    params foreach { case (k,v) => request.addParameter(k, v) }

    // Add custom headers
    httpHeaders foreach { case (k, v) => request.addHeader(k, v)}

    // Get result of HTTP POST wrapped in Either so we can get the HTTP status code in case of failure
    val result = client(request OK as.String).either

    // Create appropriate Future[HttpResponse] in case of both success(Right) and failure(Left)
    val fHttpResponse = result map {
      case Right(content) => HttpResponse(200, content)
      case Left(StatusCode(code)) => HttpResponse(code, s"Failed to do Http Post to url = $postUrl, params = $params")
      case Left(e: Throwable) =>
        log.error( e.getMessage)
        HttpResponse(0, s"Failed to do Http Post to url = $postUrl, params = $params")
    }

    fHttpResponse
  }
}
