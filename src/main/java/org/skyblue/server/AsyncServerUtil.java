package org.skyblue.server;


import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AsyncServerUtil {

    public static final int READ_BUF_SIZE = 1024;
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[READ_BUF_SIZE];

    public static void handleNewConnection(final AsynchronousSocketChannel socketChannel) {
        //System.out.println("Accepting async socket channel in thread " + Thread.currentThread().getId());

        ByteBuffer byteBuff = ByteBuffer.allocate(READ_BUF_SIZE);
        socketChannel.read(byteBuff, byteBuff, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer channelByteBuffer) {
                processClientRequest(socketChannel, result, channelByteBuffer);
                readNext(channelByteBuffer);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.err.println("Exception in reading from socket channel " + exc);
            }

            private void readNext(ByteBuffer channelByteBuffer) {
                // wire next read before clearing buffer
                channelByteBuffer.clear();
                channelByteBuffer.put(EMPTY_BYTE_ARRAY);
                channelByteBuffer.flip();
                socketChannel.read(channelByteBuffer, channelByteBuffer, this);
            }

        });
    }

    public static void processClientRequest(AsynchronousSocketChannel socketChannel, Integer result, ByteBuffer channelByteBuffer) {
        //System.out.println("Reading thread " + Thread.currentThread().getId());
        if (result > 0) {
            // its a heap byte buffer, so it will have a backing array
            final String strRead = new String(channelByteBuffer.array());
            //System.out.println("Read " + strRead);

            final String strWrite = strRead.toUpperCase();
            ByteBuffer outByteBuffer = ByteBuffer.wrap(strWrite.getBytes());
            writeAsync(socketChannel, outByteBuffer);
        }
    }

    private static void writeAsync(AsynchronousSocketChannel socketChannel, ByteBuffer outByteBuffer) {
        socketChannel.write(outByteBuffer, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                //System.out.println("Writing thread " + Thread.currentThread().getId());
                if (result <= 0) {
                    System.out.println("No bytes sent out - please investigate");
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.err.println("Exception in writing to socket channel " + exc);
            }
        });
    }
}

