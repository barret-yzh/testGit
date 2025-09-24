package thread_print;

import java.util.concurrent.Semaphore;

public class ThreadExample {
    public static Semaphore semaphore1 = new Semaphore(1);
    public static Semaphore semaphore2 = new Semaphore(0);
    public static Semaphore semaphore3 = new Semaphore(0);

    public static void main(String[] args) {
       new Thread(() -> {
            while (true) {
                try {
                    semaphore1.acquire();
                    System.out.println("A");
                    semaphore2.release();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

       new Thread(() -> {
            while (true) {
                try {
                    semaphore2.acquire();
                    System.out.println("B");
                    semaphore3.release();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

       new Thread(() -> {
            while (true) {
                try {
                    semaphore3.acquire();
                    System.out.println("C");
                    semaphore1.release();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}