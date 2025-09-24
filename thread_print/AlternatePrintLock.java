package thread_print;

import java.util.concurrent.locks.*;

public class AlternatePrintLock {
    private static int num = 0;
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Condition evenCondition = lock.newCondition();
    private static final Condition oddCondition = lock.newCondition();

    public static void main(String[] args) {
        new Thread(() -> {
            lock.lock();
            try {
                while (num <= 100) {
                    if ((num & 1) == 0) {
                        System.out.println(Thread.currentThread().getName() + ": " + num++);
                        oddCondition.signal();
                    }
                    evenCondition.await();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }, "偶数线程").start();

        new Thread(() -> {
            lock.lock();
            try {
                while (num <= 100) {
                    if ((num & 1) == 1) {
                        System.out.println(Thread.currentThread().getName() + ": " + num++);
                        evenCondition.signal();
                    }
                    oddCondition.await();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }, "奇数线程").start();
    }
}