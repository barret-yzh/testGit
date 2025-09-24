package thread_print;

public class AlternatePrint {
    private static int num = 0;
    private static final Object lock = new Object();

    public static void main(String[] args) {
        new Thread(() -> {
            while (num <= 100) {
                synchronized (lock) {
                    if ((num & 1) == 0) {
                        System.out.println(Thread.currentThread().getName() + ": " + num++);
                        lock.notify();
                    } else {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }, "偶数线程").start();

        new Thread(() -> {
            while (num <= 100) {
                synchronized (lock) {
                    if ((num & 1) == 1) {
                        System.out.println(Thread.currentThread().getName() + ": " + num++);
                        lock.notify();
                    } else {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }, "奇数线程").start();
    }
}