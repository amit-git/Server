package org.skyblue.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CachingServer {
    static final int port = 5555;
    static Map<String, String> cache = new HashMap<String, String>();

    static class CacheServerContext {
        public final Map<String, String> cache;
        public final String commandLine;
        public OutputStream outputStream;

        public CacheServerContext(Map<String, String> cache, String commandLine, OutputStream outputStream) {
            this.cache = cache;
            this.commandLine = commandLine;
            this.outputStream = outputStream;
        }
    }

    static class CacheHandler implements Runnable {
        final Socket clientSocket;

        CacheHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                final BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                final OutputStream outStream = clientSocket.getOutputStream();
                outStream.write("Ready..\n".getBytes());
                String cmd;
                do {
                    cmd = br.readLine().replace("\n", "");

                    final CacheServerContext cacheServerContext = new CacheServerContext(cache, cmd, outStream);

                    try {
                        if (cmd.startsWith("put")) {
                            ServerUtils.handlePutCommand(cacheServerContext);
                        } else if (cmd.startsWith("get")) {
                            ServerUtils.handleGetCommand(cacheServerContext);
                        }
                    } catch (IllegalArgumentException e) {
                        outStream.write("Sorry - unknown command\n".getBytes());
                    }
                } while (!cmd.equals("quit"));
                clientSocket.close();
            } catch (Exception e) {
                System.out.println("Exception caught " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting server on " + port);
        ExecutorService executor = Executors.newFixedThreadPool(20);
        try {
            ServerSocket ss = new ServerSocket(port);
            while (true) {
                Socket client = ss.accept();
                executor.execute(new CacheHandler(client));
            }
        } catch (Exception e) {
            System.out.println("Exception caught " + e.getMessage());

        }
    }
}
