package com.gaetanl.websocket.server;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.*;

import javax.net.ssl.*;

import org.slf4j.*;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.*;
import io.netty.handler.ssl.*;

public class WebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    private enum WebSocketProtocol {WS, WSS};
    private static final List<Channel> openChannels = new ArrayList<Channel>();

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        final String protocol;
        final int port;

        logger.info("Initiating server configuration");
        try {
            protocol = WebSocketProtocol.valueOf(args[0]).toString().toLowerCase();
            port = Integer.valueOf(args[1]);
        }
        catch (Exception e) {
            logger.error("Exception parsing main arguments", e);

            StringBuilder argsString = new StringBuilder().append("[");
            for (int i = 0 ; i < args.length ; i++) {
                argsString.append(args[i]);
                if (i < args.length - 1) {
                    argsString.append(" ");
                }
            }
            argsString.append("]");

            throw new IllegalArgumentException("Excepted args: WS|WSS port [HTTPServerHandler class name], got: " + argsString.toString());
        }

        // Configure the server
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup(1);
        try {
            ServerBootstrap b = new ServerBootstrap()
                    .group(bossGroup, worker)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();

                            if ("wss".equalsIgnoreCase(protocol)) {
                                SslContext sslCtx = createSSLContext();
                                pipeline.addLast(sslCtx.newHandler(channel.alloc()));
                            }
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(64000));
                            pipeline.addLast(new WebSocketServerProtocolHandler("/") {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    super.channelRead(ctx, msg);
                                    logger.info(String.format("Received frame on channel %s", ctx.channel()));
                                }
                            });
                            pipeline.addLast(new WebSocketServerHandler());
                            pipeline.addLast(new OutboundLoggingHandler());
                        }
                    });

            Channel ch = b.bind(port).sync().channel();
            logger.info(String.format("[OK] Server started at %s://%s:%d", protocol, "localhost", port));
            ch.closeFuture().sync();
        }
        catch (InterruptedException e) {
            logger.error("Exception starting server", e);
        }
        finally {
            bossGroup.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void addOpenChannel(Channel channel) {
        openChannels.add(channel);
    }

    private static SslContext createSSLContext() throws Exception {
        String path = "/TestKeystore.jks";
        logger.debug(String.format("keystore content=\"%s\"", WebSocketUtil.asString(WebSocketServer.class.getResourceAsStream(path))));

        KeyStore keystore = KeyStore.getInstance("JKS");
        InputStream is = WebSocketServer.class.getResourceAsStream(path);
        keystore.load(is, "testtest".toCharArray());
        logger.debug("keystore=" + is);

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keystore, "testtest".toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

        return SslContextBuilder.forServer(keyManagerFactory).build();
    }
}
