package producer_consumer;

import java.util.concurrent.BlockingQueue;

public class Producer implements Runnable {
    public BlockingQueue<Integer> queue;

    public Producer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(3000);
                queue.put(1);
                System.out.println("厨师放入了一个餐品");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}