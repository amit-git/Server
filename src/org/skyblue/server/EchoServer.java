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

public class EchoServer {
    static final int port = 4444;
    static final List<String> USERS = new ArrayList<String>();
    static {
        USERS.add("manali");
        USERS.add("amit");
        USERS.add("vyom");
    }

    static Map<String, OutputStream> userStreams = new HashMap<String, OutputStream>();
    
    static class EchoService implements Runnable {
        final Socket clientSocket;
        EchoService(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                final BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                final OutputStream outStream = clientSocket.getOutputStream();
                outStream.write("Login: ".getBytes());
                final String userName = br.readLine();
                if (USERS.contains(userName.toLowerCase())) {
                    synchronized(EchoServer.class) {
                        userStreams.put(userName.toLowerCase(), outStream);
                    }

                    final String welcomeStr = "Welcome " + userName + "\n";
                    outStream.write(welcomeStr.getBytes());
                    String line = null;
                    do {
                        line = br.readLine();
                        line = line.replace("\n","");
                        if (line.contains("@")) {
                            String [] parts = line.split(" ");
                            String posting = userName + ": ";
                            String talkingToUser = null;
                            for (String part : parts) {
                                if (part.contains("@")) {
                                    talkingToUser = part.replace("@","");
                                } else {
                                    posting += part + " ";
                                }
                            }
                            posting += "\n";

                            if (talkingToUser != null) {
                                OutputStream otherUserStream = null;
                                synchronized(EchoServer.class) {
                                    otherUserStream = userStreams.get(talkingToUser);
                                }
                                if (otherUserStream != null) {
                                    otherUserStream.write(posting.getBytes());
                                } else {
                                    final String notice = talkingToUser + " is not online\n";
                                    outStream.write(notice.getBytes());
                                }
                            }
                        }
                        System.out.println("Client Said " + line);
                    } while (line != null && ! line.equals("quit"));

                    System.out.println("Connection closing with client");
                    outStream.write("Good buy".getBytes());
                    synchronized(EchoServer.class) {
                        userStreams.remove(userName.toLowerCase());
                    }

                } else {
                    outStream.write("Invalid User\n".getBytes());
                }
                clientSocket.close();
                
            } catch(Exception e) {
                System.out.println("Exception caught " + e.getMessage());
            }
        }
    }

    public static void main(String [] args) {
        System.out.println("Starting server on " + port);

        try {
            ServerSocket ss = new ServerSocket(port);
            while (true) {
                Socket client = ss.accept();
                new Thread(new EchoService(client)).start();
            }
        } catch(Exception e) {
            System.out.println("Exception caught " + e.getMessage());

        }
    }
}
