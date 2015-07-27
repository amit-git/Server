package org.skyblue.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncServer {
    public void start() {
        try {
            ExecutorService service = Executors.newFixedThreadPool(10);
            AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withThreadPool(service);

            final InetSocketAddress localAddr = new InetSocketAddress(7777);
            final AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open(channelGroup).bind(localAddr);

            assc.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                @Override
                public void completed(final AsynchronousSocketChannel socketChannel, Object attachment) {
                    AsyncServerUtil.handleNewConnection(socketChannel);
                    assc.accept(null, this); // accept next connection
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    System.err.println("Exception in accepting connection");
                }
            });

            System.in.read();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        AsyncServer asyncServer = new AsyncServer();
        asyncServer.start();
    }
}

