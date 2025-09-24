package thread_print;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionABCPattern {
    private static final Lock lock = new ReentrantLock();
    private static final Condition conditionA = lock.newCondition();
    private static final Condition conditionB = lock.newCondition();
    private static final Condition conditionC = lock.newCondition();
    private static int state = 1; // 状态标识：1-A → 2-B → 3-C → 1-A...

    public static void main(String[] args) {
        new Thread(() -> {
            while (true) {
                lock.lock();
                try {
                    while (state != 1) {
                        conditionA.await();
                    }
                    System.out.println("A");
                    state = 2;
                    conditionB.signal(); // 唤醒B线程
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
        }).start();
        
        new Thread(() -> {
            while (true) {
                lock.lock();
                try {
                    while (state != 2) {
                        conditionB.await();
                    }
                    System.out.println("B");
                    state = 3;
                    conditionC.signal(); // 唤醒C线程
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
        }).start();

        new Thread(() -> {
            while (true) {
                lock.lock();
                try {
                    while (state != 3) {
                        conditionC.await();
                    }
                    System.out.println("C");
                    state = 1;
                    conditionA.signal(); // 唤醒A线程
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
        }).start();

    }
}