package ru.korpse.testapp.actors

import java.net.URI
import java.net.InetSocketAddress
import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelFuture
import org.jboss.netty.channel.ChannelFutureListener
import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import java.util.concurrent.Executors
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion
import ru.korpse.testapp.messages.Messages._
import ru.korpse.testapp.messages.ReplyMessages._
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channels
import org.jboss.netty.handler.codec.http.HttpResponseDecoder
import org.jboss.netty.handler.codec.http.HttpRequestEncoder
import org.jboss.netty.channel.ChannelStateEvent
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.channel.SimpleChannelUpstreamHandler
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker
import org.jboss.netty.util.CharsetUtil
import org.jboss.netty.buffer.ChannelBuffers
import spray.json._
import spray.json.DefaultJsonProtocol._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import ru.korpse.testapp.messages.Messages
import ru.korpse.testapp.exception.WebSocketException
import collection.JavaConversions.mapAsJavaMap
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.actor.ActorLogging
import ru.korpse.testapp.util.ReceiveLogger
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame

class SimpleWebSocketClientActor(val url: URI, clientActors: Array[ActorRef]) extends Actor with ActorLogging with ReceiveLogger {
  var channel: Channel = _
  val bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool))
  val normalized = url.normalize()
  val tgt = if (normalized.getPath == null || normalized.getPath.trim().isEmpty) {
    new URI(normalized.getScheme, normalized.getAuthority, "/", normalized.getQuery, normalized.getFragment)
  } else normalized
  val handshaker = new WebSocketClientHandshakerFactory().newHandshaker(tgt, WebSocketVersion.V13, null, false, Map.empty[String, String])

  val client = this;

  bootstrap.setPipelineFactory(new ChannelPipelineFactory {
    def getPipeline = {
      val pipeline = Channels.pipeline()
      pipeline.addLast("decoder", new HttpResponseDecoder)
      pipeline.addLast("encoder", new HttpRequestEncoder)
      pipeline.addLast("ws-handler", new SimpleWebSocketClientHandler(client, handshaker))
      pipeline
    }
  })

  def futureListener(handleWith: ChannelFuture => Unit) = new ChannelFutureListener {
    def operationComplete(future: ChannelFuture) { handleWith(future) }
  }

  def connect = {
    if (channel == null || !channel.isConnected) {
      val listener = futureListener { future =>
        if (future.isSuccess) {
          synchronized { channel = future.getChannel }
          handshaker.handshake(channel)
        } else {
          clientActors.foreach { actor => actor ! Option(future.getCause) }
        }
      }
      clientActors.foreach { actor => actor ! Connecting }
      val fut = bootstrap.connect(new InetSocketAddress(url.getHost, url.getPort))
      fut.addListener(listener)
      fut.await(5000L)
    }
  }

  def disconnect = {
    if (channel != null && channel.isConnected) {
      clientActors.foreach { actor => actor ! Disconnecting }
      channel.write(new CloseWebSocketFrame())
    }
  }

  def send(message: String) = {
    channel.write(new TextWebSocketFrame(ChannelBuffers.copiedBuffer(message, CharsetUtil.UTF_8))).addListener(futureListener { fut =>
      if (!fut.isSuccess) {
          clientActors.foreach { actor => actor ! Option(fut.getCause) }
      }
    })
  }

  def shutdown = {
    bootstrap.shutdown()
    System.exit(0)
  }
  
  def receive: Receive = logMessage orElse {
    case DoConnect => connect
    case DoDisconnect => disconnect
    case DoSendMessage(msg) => send(msg)
    case DoShutdown => shutdown
  }

  class SimpleWebSocketClientHandler(client: SimpleWebSocketClientActor, handshaker: WebSocketClientHandshaker) extends SimpleChannelUpstreamHandler {

    import Messages._
    override def channelClosed(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
      clientActors.foreach { actor => actor ! Disconnected }
    }

    override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
      e.getMessage match {
        case resp: HttpResponse if handshaker.isHandshakeComplete =>
          throw new WebSocketException("Unexpected HttpResponse (status=" + resp.getStatus + ", content="
            + resp.getContent.toString(CharsetUtil.UTF_8) + ")")
        case resp: HttpResponse =>
          handshaker.finishHandshake(ctx.getChannel, e.getMessage.asInstanceOf[HttpResponse])
      clientActors.foreach { actor => actor ! Connected }

        case f: TextWebSocketFrame => clientActors.foreach { actor => actor ! JsonMessage(f.getText.parseJson) }
        case _: PongWebSocketFrame =>
        case _: PingWebSocketFrame =>
        case _: CloseWebSocketFrame => {
          ctx.getChannel.close()
        }
      }
    }
  }
}