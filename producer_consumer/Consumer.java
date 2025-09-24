package producer_consumer;

import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable {
    public BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer>queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                queue.take();
                System.out.println("顾客消费了1个餐品");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}