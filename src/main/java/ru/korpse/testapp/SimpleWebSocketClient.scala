package ru.korpse.testapp

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
import scala.collection.JavaConversions.mapAsJavaMap
import ru.korpse.testapp.Messages._
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

class SimpleWebSocketClient(val url: URI)(private[this] val handler: PartialFunction[Messages.WebSocketClientMessage, Unit]) {
  var channel: Channel = _
  val bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool))
  val normalized = url.normalize()
  val tgt = if (normalized.getPath == null || normalized.getPath.trim().isEmpty) {
    new URI(normalized.getScheme, normalized.getAuthority, "/", normalized.getQuery, normalized.getFragment)
  } else normalized
  val handshaker = new WebSocketClientHandshakerFactory().newHandshaker(tgt, WebSocketVersion.V13, null, false, Map.empty[String, String])

  var client = this;
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
          handler(ConnectionFailed(this, Option(future.getCause)))
        }
      }
      handler(Connecting)
      val fut = bootstrap.connect(new InetSocketAddress(url.getHost, url.getPort))
      fut.addListener(listener)
      fut.await(5000L)
    }
  }
  
  def disconnect = {
    if (channel != null && channel.isConnected) {
      handler(Disconnecting)
      channel.write(new CloseWebSocketFrame())
    }
  }

  def send(message: String) = {
    channel.write(new TextWebSocketFrame(ChannelBuffers.copiedBuffer(message, CharsetUtil.UTF_8))).addListener(futureListener { fut =>
      if (!fut.isSuccess) {
        handler(WriteFailed(this, message, Option(fut.getCause)))
      }
    })
  }

  class SimpleWebSocketClientHandler(client: SimpleWebSocketClient, handshaker: WebSocketClientHandshaker) extends SimpleChannelUpstreamHandler {

    import Messages._
    override def channelClosed(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
      handler(Disconnected(client))
    }

    override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
      e.getMessage match {
        case resp: HttpResponse if handshaker.isHandshakeComplete =>
          throw new WebSocketException("Unexpected HttpResponse (status=" + resp.getStatus + ", content="
            + resp.getContent.toString(CharsetUtil.UTF_8) + ")")
        case resp: HttpResponse =>
          handshaker.finishHandshake(ctx.getChannel, e.getMessage.asInstanceOf[HttpResponse])
          handler(Messages.Connected(client))

        case f: TextWebSocketFrame  => handler(TextMessage(client, f.getText))
        case _: PongWebSocketFrame  =>
        case _: CloseWebSocketFrame => ctx.getChannel.close()
      }
    }
  }
}