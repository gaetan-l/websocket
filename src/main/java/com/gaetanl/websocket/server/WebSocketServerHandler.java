package com.gaetanl.websocket.server;

import org.slf4j.*;

import com.gaetanl.websocket.message.*;
import com.google.gson.*;

import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.*;

public class WebSocketServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object frame) {
        logger.debug(String.format("Received (%s) frame on channel %s", frame.getClass().getSimpleName(), ctx.channel()));

        // Par défaut on log simplement la classe de la frame reçue
        String logMessageRecu = frame.getClass().getSimpleName();

        // On indique s'il existe un traitement serveur pour la frame reçue
        boolean traitement = false;

        // Accusé de réception à envoyer par le serveur en réponse de la frame
        WebSocketFrame responseFrame = null;

        if (frame instanceof WebSocketFrame) {
            if (frame instanceof TextWebSocketFrame) {
                String receivedText = ((TextWebSocketFrame) frame).text();
                logMessageRecu = receivedText.replaceAll("(?m)^", "    ");

                WsMessage receivedMessage = null;
                try {
                    GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
                    builder.registerTypeAdapter(WsMessage.class, new WsMessageDeserializer());
                    Gson gson = builder.create();
                    receivedMessage = gson.fromJson(receivedText, WsMessage.class);

                    String receivedType = receivedMessage.getType();
                    if (WsMsgText.class.getName().equals(receivedType)) {
                        // Do nothing
                    }
                    else if (WsMsgDeconnexion.class.getName().equals(receivedType)) {
                        ctx.channel().close();
                        // TODO: Code métier
                    }
                    else if (WsAckText.class.getName().equals(receivedType)) {
                        // Do nothing
                    }

                    WsMessage ack = receivedMessage.getAck();

                    if (ack != null) {
                        Gson gsonWriter = new GsonBuilder().setPrettyPrinting().create();
                        responseFrame = new TextWebSocketFrame(gsonWriter.toJson(ack));
                    }
                }
                catch (JsonSyntaxException e) {
                    logger.info(String.format("[NOK] websocket/in:  TextWebSocketFrame/WsMessage ('%s' couldn't be parsed)", receivedText), e.getMessage(), e);
                }
            }

            else if (frame instanceof BinaryWebSocketFrame) {
                // Do nothing
            }

            else if (frame instanceof PingWebSocketFrame) {
                responseFrame = new PongWebSocketFrame();
            }

            else if (frame instanceof PongWebSocketFrame) {
                // Do nothing
            }

            else if (frame instanceof CloseWebSocketFrame) {
                // Do nothing
            }
        }

        logger.info(String.format("[OK] websocket/in:\n\n%s\n", logMessageRecu));

        // On loggue si aucun traitement n'est effectué
        if (!traitement) {
            logger.debug(String.format("No action defined for type %s", frame.getClass().getSimpleName()));
        }

        // On envoie l'accusé de réception le cas échéant
        if (responseFrame != null) {
            ctx.channel().writeAndFlush(responseFrame);
        }
    }
}
