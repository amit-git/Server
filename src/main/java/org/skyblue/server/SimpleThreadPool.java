package org.skyblue.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleThreadPool {

    public static class Worker extends Thread {
        private BlockingQueue<Runnable> workQueue;
        private Boolean running;

        public Worker(BlockingQueue<Runnable> workQueue) {
            this.workQueue = workQueue;
            this.setDaemon(false);
            running = true;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    final Runnable task = workQueue.take();
                    if (task != null) {
                        task.run();
                    }
                } catch (InterruptedException ignored) {
                }

            }
        }

        public void stopWork() {
            running = false;
        }
    }

    public static class ThreadPoolBuilder {
        private int size;

        public ThreadPoolBuilder() {
        }

        public ThreadPoolBuilder withSize(int s) {
            this.size = s;
            return this;
        }

        public SimpleThreadPool build() {
            return new SimpleThreadPool(size);
        }
    }

    private final BlockingQueue<Runnable> workQueue;
    private List<Worker> pool;
    private int poolSize;

    private SimpleThreadPool(int size) {
        workQueue = new LinkedBlockingQueue<>();
        pool = new ArrayList<>(size);
        poolSize = size;
    }

    public void start() {
        for (int i = 0; i < poolSize; i++) {
            final Worker worker = new Worker(workQueue);
            worker.start();
            pool.add(worker);
        }
    }

    public void submitTask(Runnable task) {
        try {
            workQueue.put(task);
        } catch (InterruptedException ignored) {
        }
    }

    public void shutdown() {
        for (Worker worker : pool) {
            worker.stopWork();
        }
    }

    public static void main(String... args) {
        SimpleThreadPool stp = new SimpleThreadPool(5);
        for (int i = 0; i < 10; i++) {
            stp.submitTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                        System.out.println("Done work in thread with id " + Thread.currentThread().getId());
                    } catch (InterruptedException ignored) {
                    }
                }
            });
        }

        stp.start();
    }
}
