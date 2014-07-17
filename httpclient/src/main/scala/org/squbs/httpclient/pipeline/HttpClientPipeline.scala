package org.squbs.httpclient.pipeline

import spray.httpx.{UnsuccessfulResponseException, PipelineException}
import spray.client.pipelining._
import akka.actor.{ActorRef, ActorSystem}
import spray.httpx.unmarshalling._
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import org.squbs.httpclient._
import spray.http.{Uri, HttpRequest, HttpResponse}
import org.squbs.httpclient.HttpResponseEntityWrapper
import scala.util.Try
import akka.pattern._
import akka.util.Timeout
import spray.can.Http
import akka.io.IO

/**
 * Created by hakuang on 5/1/2014.
 */
trait Pipeline {
  def requestPipelines: Seq[RequestTransformer]

  def responsePipelines: Seq[ResponseTransformer]
}

trait PipelineHandler {
  def processRequest: RequestTransformer

  def processResponse: ResponseTransformer
}

trait RequestPipelineHandler extends PipelineHandler {
  override def processResponse: ResponseTransformer = {httpResponse => httpResponse}
}

trait ResponsePipelineHandler extends PipelineHandler {
  override def processRequest: RequestTransformer = {httpRequest => httpRequest}
}

object EmptyPipeline extends Pipeline {
  override def responsePipelines: Seq[ResponseTransformer] = Seq.empty[ResponseTransformer]

  override def requestPipelines: Seq[RequestTransformer] = Seq.empty[RequestTransformer]
}

trait PipelineManager extends ConfigurationSupport{

  private def pipelining(client: Client)(implicit actorSystem: ActorSystem) = {
    implicit val connectionTimeout: Timeout = hostSettings(client).connectionSettings.connectingTimeout.toMillis
    import ExecutionContext.Implicits.global
    for (
      Http.HostConnectorInfo(connector, _) <-
      IO(Http) ? hostConnectorSetup(client)
    ) yield sendReceive(connector)
  }

  def hostConnectorSetup(client: Client)(implicit actorSystem: ActorSystem) = {
    val uri = Uri(client.endpoint.get.uri)
    val host = uri.authority.host.toString
    val port = if (uri.effectivePort == 0) 80 else uri.effectivePort
    val isSecure = uri.scheme.toLowerCase.equals("https")
    val defaultHostConnectorSetup = Http.HostConnectorSetup(host, port, isSecure)
    defaultHostConnectorSetup.copy(settings = Some(hostSettings(client)), connectionType = config(client).connectionType)
  }

  def invokeToHttpResponse(client: Client)(implicit actorSystem: ActorSystem): Try[(HttpRequest => Future[HttpResponseWrapper])] = {
    val pipelines = client.pipeline.getOrElse(EmptyPipeline)
    val reqPipelines = pipelines.requestPipelines
    val resPipelines = pipelines.responsePipelines
    val connTimeout = hostSettings(client).connectionSettings.connectingTimeout
    Try{
      val futurePipeline = pipelining(client)
      val pipeline = Await.result(futurePipeline, connTimeout)
      (reqPipelines, resPipelines, client.status) match {
        case (_, _, Status.DOWN) =>
          throw new HttpClientMarkDownException(client.name, client.env)
        case (Seq(), Seq(), _) =>
          pipeline ~> withWrapper
        case (Seq(), _: Seq[ResponseTransformer], _) =>
          pipeline ~> resPipelines.reduceLeft[ResponseTransformer](_ ~> _) ~> withWrapper
        case (_: Seq[RequestTransformer], Seq(), _) =>
          reqPipelines.reduceLeft[RequestTransformer](_ ~> _) ~> pipeline ~> withWrapper
        case (_: Seq[RequestTransformer], _: Seq[ResponseTransformer], _) =>
          reqPipelines.reduceLeft[RequestTransformer](_ ~> _) ~> pipeline ~> resPipelines.reduceLeft[ResponseTransformer](_ ~> _) ~> withWrapper
      }
    }
  }

  def invokeToHttpResponseWithoutSetup(client: Client, actorRef: ActorRef)(implicit actorSystem: ActorSystem): Try[(HttpRequest => Future[HttpResponseWrapper])] = {
    val pipelines = client.pipeline.getOrElse(EmptyPipeline)
    val reqPipelines = pipelines.requestPipelines
    val resPipelines = pipelines.responsePipelines
    implicit val connTimeout: Timeout = hostSettings(client).connectionSettings.connectingTimeout.toMillis
    val pipeline = spray.client.pipelining.sendReceive(actorRef)
    Try{
      (reqPipelines, resPipelines, client.status) match {
        case (_, _, Status.DOWN) =>
          throw new HttpClientMarkDownException(client.name, client.env)
        case (Seq(), Seq(), _) =>
          pipeline ~> withWrapper
        case (Seq(), _: Seq[ResponseTransformer], _) =>
          pipeline ~> resPipelines.reduceLeft[ResponseTransformer](_ ~> _) ~> withWrapper
        case (_: Seq[RequestTransformer], Seq(), _) =>
          reqPipelines.reduceLeft[RequestTransformer](_ ~> _) ~> pipeline ~> withWrapper
        case (_: Seq[RequestTransformer], _: Seq[ResponseTransformer], _) =>
          reqPipelines.reduceLeft[RequestTransformer](_ ~> _) ~> pipeline ~> resPipelines.reduceLeft[ResponseTransformer](_ ~> _) ~> withWrapper
      }
    }
  }

  def invokeToEntity[T: FromResponseUnmarshaller](client: Client)(implicit actorSystem: ActorSystem): Try[(HttpRequest => Future[HttpResponseEntityWrapper[T]])] = {
    val pipelines = client.pipeline.getOrElse(EmptyPipeline)
    val reqPipelines = pipelines.requestPipelines
    val resPipelines = pipelines.responsePipelines
    val connTimeout = hostSettings(client).connectionSettings.connectingTimeout
    Try{
      val futurePipeline = pipelining(client)
      val pipeline = Await.result(futurePipeline, connTimeout)
      (reqPipelines, resPipelines, client.status) match {
        case (_, _, Status.DOWN) =>
          throw new HttpClientMarkDownException(client.name, client.env)
        case (Seq(), Seq(), _) =>
          pipeline ~> unmarshalWithWrapper[T]
        case (Seq(), _: Seq[ResponseTransformer], _) =>
          pipeline ~> resPipelines.reduceLeft[ResponseTransformer](_ ~> _) ~> unmarshalWithWrapper[T]
        case (_: Seq[RequestTransformer], Seq(), _) =>
          reqPipelines.reduceLeft[RequestTransformer](_ ~> _) ~> pipeline ~> unmarshalWithWrapper[T]
        case (_: Seq[RequestTransformer], _: Seq[ResponseTransformer], _) =>
          reqPipelines.reduceLeft[RequestTransformer](_ ~> _) ~> pipeline ~> resPipelines.reduceLeft[ResponseTransformer](_ ~> _) ~> unmarshalWithWrapper[T]
      }
    }
  }

  def invokeToEntityWithoutSetup[T: FromResponseUnmarshaller](client: Client, actorRef: ActorRef)(implicit actorSystem: ActorSystem): Try[(HttpRequest => Future[HttpResponseEntityWrapper[T]])] = {
    val pipelines = client.pipeline.getOrElse(EmptyPipeline)
    val reqPipelines = pipelines.requestPipelines
    val resPipelines = pipelines.responsePipelines
    implicit val connTimeout: Timeout = hostSettings(client).connectionSettings.connectingTimeout.toMillis
    val pipeline = spray.client.pipelining.sendReceive(actorRef)
    Try{
      (reqPipelines, resPipelines, client.status) match {
        case (_, _, Status.DOWN) =>
          throw new HttpClientMarkDownException(client.name, client.env)
        case (Seq(), Seq(), _) =>
          pipeline ~> unmarshalWithWrapper[T]
        case (Seq(), _: Seq[ResponseTransformer], _) =>
          pipeline ~> resPipelines.reduceLeft[ResponseTransformer](_ ~> _) ~> unmarshalWithWrapper[T]
        case (_: Seq[RequestTransformer], Seq(), _) =>
          reqPipelines.reduceLeft[RequestTransformer](_ ~> _) ~> pipeline ~> unmarshalWithWrapper[T]
        case (_: Seq[RequestTransformer], _: Seq[ResponseTransformer], _) =>
          reqPipelines.reduceLeft[RequestTransformer](_ ~> _) ~> pipeline ~> resPipelines.reduceLeft[ResponseTransformer](_ ~> _) ~> unmarshalWithWrapper[T]
      }
    }
  }

  private def withWrapper: HttpResponse => HttpResponseWrapper = {
    response => HttpResponseWrapper(response.status, Right(response))
  }

  private def unmarshalWithWrapper[T: FromResponseUnmarshaller]: HttpResponse ⇒ HttpResponseEntityWrapper[T] = {
    response =>
      if (response.status.isSuccess)
        response.as[T] match {
          case Right(value) ⇒ HttpResponseEntityWrapper[T](response.status, Right(value), Some(response))
          case Left(error) ⇒ HttpResponseEntityWrapper[T](response.status, Left(throw new PipelineException(error.toString)), Some(response))
        }
      else HttpResponseEntityWrapper[T](response.status, Left(new UnsuccessfulResponseException(response)), Some(response))
  }
}