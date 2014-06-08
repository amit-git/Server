package org.skyblue.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.Runnable;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CachingServer {
    static final int port = 5555;

    static Map<String, String> cache = new HashMap<String, String>();
    
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
                
                boolean badCmd = false;

                
                String cmd;
                do {
                    cmd = br.readLine().replace("\n","");
                    if (cmd.startsWith("put")) {
                        cmd = cmd.replace("put","").trim();
                        String [] keyValuePairParts = cmd.split("=");
                        if (keyValuePairParts.length == 2) {
                            final String key = keyValuePairParts[0].trim();;
                            final String value = keyValuePairParts[1].trim();;
                            synchronized(CachingServer.class) {
                                cache.put(key, value);
                            }
                        } else {
                            badCmd = true;
                        }
                    } else if (cmd.startsWith("get")) {
                        String [] parts = cmd.split(" ");
                        
                        if (parts.length == 2) {
                            final String findKey = parts[1];
                            String value = null;
                            synchronized(CachingServer.class) {
                                value = cache.get(findKey);
                            }
                            if (value == null) {
                                outStream.write("Key not found\n".getBytes());
                            } else {
                                final String resp = "Key found : Value = " + value + "\n";
                                outStream.write(resp.getBytes());
                            }
                        } else {
                            badCmd = true;
                        }
                    }

                    if (badCmd) {
                        outStream.write("Sorry - unknown command\n".getBytes());
                    }

                } while (cmd != null && ! cmd.equals("quit"));
                clientSocket.close();
            } catch(Exception e) {
                System.out.println("Exception caught " + e.getMessage());
            }
        }
    }

    public static void main(String [] args) {
        System.out.println("Starting server on " + port);
        ExecutorService executor = Executors.newFixedThreadPool(20);
        

        try {
            ServerSocket ss = new ServerSocket(port);
            while (true) {
                Socket client = ss.accept();
                executor.execute(new CacheHandler(client));
                //new Thread(new CacheHandler(client)).start();
            }
        } catch(Exception e) {
            System.out.println("Exception caught " + e.getMessage());

        }
    }
}
