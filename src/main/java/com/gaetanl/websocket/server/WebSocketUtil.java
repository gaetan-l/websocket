package com.gaetanl.websocket.server;

import java.io.*;
import java.net.InetSocketAddress;

import io.netty.channel.*;

public class WebSocketUtil {
    public static InetSocketAddress getRemoteAddress(ChannelHandlerContext ctx) {
        return getRemoteAddress(ctx.channel());
    }

    public static InetSocketAddress getRemoteAddress(Channel channel) {
        return ((InetSocketAddress) channel.remoteAddress());
    }

    public static String asString(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        // Create a StringBuilder to store the content read from the input
        // stream
        StringBuilder stringBuilder = new StringBuilder();

        // Create a BufferedReader to read the content from the input stream
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        finally {
            // Close the BufferedReader
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                }
                catch (IOException e) {
                    // Handle or log the exception
                }
            }
        }

        // Convert the StringBuilder to a String and return it
        return stringBuilder.toString();
    }
}
