package ru.korpse.testapp.websocketclient

import java.net.{InetSocketAddress, URI}
import java.util.concurrent.Executors

import akka.actor.TypedActor
import akka.event.Logging
import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.channel._
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.jboss.netty.handler.codec.http.websocketx._
import org.jboss.netty.handler.codec.http.{HttpRequestEncoder, HttpResponse, HttpResponseDecoder}
import org.jboss.netty.util.CharsetUtil
import ru.korpse.testapp.exception.WebSocketException
import ru.korpse.testapp.trustline.TrustlineServiceComponent
import spray.json._

import scala.collection.JavaConversions.mapAsJavaMap

trait SimpleWebSocketClientComponent {
  this: TrustlineServiceComponent =>

  def client: SimpleWebSocketClient

  class SimpleWebSocketClientImpl(val url: URI) extends SimpleWebSocketClient {
    val log = Logging(TypedActor.context.system, TypedActor.context.self)
    val bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool))
    val normalized = url.normalize()
    val tgt = if (normalized.getPath == null || normalized.getPath.trim().isEmpty) {
      new URI(normalized.getScheme, normalized.getAuthority, "/", normalized.getQuery, normalized.getFragment)
    } else normalized
    val handshaker = new WebSocketClientHandshakerFactory().newHandshaker(tgt, WebSocketVersion.V13, null, false, Map.empty[String, String])
    val client = this;
    var channel: Channel = _

    bootstrap.setPipelineFactory(new ChannelPipelineFactory {
      def getPipeline = {
        val pipeline = Channels.pipeline()
        pipeline.addLast("decoder", new HttpResponseDecoder)
        pipeline.addLast("encoder", new HttpRequestEncoder)
        pipeline.addLast("ws-handler", new SimpleWebSocketClientHandler(client, handshaker))
        pipeline
      }
    })

    def connect = {
      log.debug("Connect")
      if (channel == null || !channel.isConnected) {
        val listener = futureListener { future =>
          if (future.isSuccess) {
            synchronized {
              channel = future.getChannel
            }
            handshaker.handshake(channel)
          } else {
            //clientActors.foreach { actor => actor ! Option(future.getCause)}
          }
        }
        trustlineService.connecting
        val fut = bootstrap.connect(new InetSocketAddress(url.getHost, url.getPort))
        fut.addListener(listener)
        fut.await(5000L)
      }
    }

    def disconnect = {
      log.debug("Disconnect")
      if (channel != null && channel.isConnected) {
        trustlineService.disconnecting
        channel.write(new CloseWebSocketFrame())
      }
    }

    def send(message: String) = {
      log.debug(s"Sending: '$message'")
      channel.write(new TextWebSocketFrame(ChannelBuffers.copiedBuffer(message, CharsetUtil.UTF_8))).addListener(futureListener { fut =>
        if (!fut.isSuccess) {
          //clientActors.foreach { actor => actor ! Option(fut.getCause)}
        }
      })
    }

    def futureListener(handleWith: ChannelFuture => Unit) = new ChannelFutureListener {
      def operationComplete(future: ChannelFuture) {
        handleWith(future)
      }
    }

    def shutdown = {
      bootstrap.shutdown()
      System.exit(0)
    }

    class SimpleWebSocketClientHandler(client: SimpleWebSocketClientImpl, handshaker: WebSocketClientHandshaker) extends SimpleChannelUpstreamHandler {
      override def channelClosed(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
        trustlineService.disconnected
      }

      override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
        e.getMessage match {
          case resp: HttpResponse if handshaker.isHandshakeComplete =>
            throw new WebSocketException("Unexpected HttpResponse (status=" + resp.getStatus + ", content="
              + resp.getContent.toString(CharsetUtil.UTF_8) + ")")
          case resp: HttpResponse =>
            handshaker.finishHandshake(ctx.getChannel, e.getMessage.asInstanceOf[HttpResponse])
            trustlineService.connected
          case f: TextWebSocketFrame => trustlineService.processJson(f.getText.parseJson)
          case _: PongWebSocketFrame =>
          case _: PingWebSocketFrame =>
          case _: CloseWebSocketFrame => {
            ctx.getChannel.close()
          }
        }
      }
    }

  }
}
