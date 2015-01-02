package org.skyblue.server;

import java.io.IOException;

public class ServerUtils {
    public static void handlePutCommand(CachingServer.CacheServerContext serverContext) {
        if (!isServerContextValid(serverContext)) {
            return;
        }
        String cmd = serverContext.commandLine.replace("put", "").trim();
        String[] keyValuePairParts = cmd.split("=");
        if (keyValuePairParts.length == 2) {
            final String key = keyValuePairParts[0].trim();
            final String value = keyValuePairParts[1].trim();
            synchronized (CachingServer.class) {
                serverContext.cache.put(key, value);
            }
        } else {
            throw new IllegalArgumentException("PUT command format error");
        }
    }

    public static void handleGetCommand(CachingServer.CacheServerContext serverContext) {
        if (!isServerContextValid(serverContext)) {
            return;
        }

        String[] parts = serverContext.commandLine.split(" ");
        if (parts.length == 2) {
            final String findKey = parts[1];
            String value = null;
            synchronized (CachingServer.class) {
                value = serverContext.cache.get(findKey);
            }
            try {
                if (value == null) {
                    serverContext.outputStream.write("Key not found\n".getBytes());
                } else {
                    final String resp = "Key found : Value = " + value + "\n";
                    serverContext.outputStream.write(resp.getBytes());
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("GET command - output stream invalid");
            }
        } else {
            throw new IllegalArgumentException("GET command format error");
        }
    }

    public static boolean isServerContextValid(CachingServer.CacheServerContext serverContext) {
        return !(serverContext.commandLine == null || serverContext.commandLine.isEmpty() || serverContext.cache == null);
    }
}
