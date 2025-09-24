package threadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class MyThreadPool {

    private final int corePoolSize;
    private final int maxPoolSize;
    private final long timeOut;
    private final TimeUnit timeUnit;
    private final RejectHandler rejectHandler;
    private final BlockingQueue<Runnable> workQueue;
    private final AtomicInteger workerCount = new AtomicInteger(0);
    private final ReentrantLock lock = new ReentrantLock();
    private final List<Worker> workers = new ArrayList<>();
    private volatile boolean isShutdown = false;

    public MyThreadPool(int corePoolSize, int maxPoolSize, long timeOut,
                        TimeUnit timeUnit, BlockingQueue<Runnable> workQueue,
                        RejectHandler rejectHandler) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.timeOut = timeOut;
        this.timeUnit = timeUnit;
        this.workQueue = workQueue;
        this.rejectHandler = rejectHandler;
    }

    public void execute(Runnable task) {
        if (task == null) {
            throw new NullPointerException();
        }
        if (isShutdown) {
            return;
        }
        // 1. 如果当前工作线程数小于核心线程数，创建核心线程
        if (workerCount.get() < corePoolSize) {
            if (addWorker(task, true)) {
                return;
            }
        }
        // 2. 尝试将任务加入阻塞队列
        if (workQueue.offer(task)) {
            return;
        }
        // 3. 阻塞队列已满，尝试创建非核心线程
        if (!addWorker(task, false)) {
            // 4. 无法创建线程，执行拒绝策略
            rejectHandler.reject(task, this);
        }
    }

    private boolean addWorker(Runnable task, boolean core) {
        // 循环CAS增加workerCount
        while (true) {
            int wc = workerCount.get();
            int max = core ? corePoolSize : maxPoolSize;
            // 检查线程数是否超过限制
            if (wc >= max) {
                return false;
            }
            // CAS增加workerCount
            if (!workerCount.compareAndSet(wc, wc + 1)) {
                continue;
            }
            break;
        }
        boolean workerStart = false;
        Worker worker = null;
        try {
            worker = new Worker(task);
            final Thread t = worker.thread;
            if (t != null) {
                lock.lock();
                try {
                    workers.add(worker);
                } finally {
                    lock.unlock();
                }
                t.start();
                workerStart = true;
            }
        } finally {
            if (!workerStart) {
                addWorkerFailed(worker);
            }
        }
        return workerStart;
    }

    private void addWorkerFailed(Worker worker) {
        lock.lock();
        try {
            if (worker != null) {
                workers.remove(worker);
            }
            workerCount.decrementAndGet();
        } finally {
            lock.unlock();
        }
    }

    public void shutdown() {
        lock.lock();
        try {
            isShutdown = true;
            for (Worker worker : workers) {
                Thread t = worker.thread;
                if (!t.isInterrupted() && t.isAlive()) {
                    try {
                        t.interrupt();
                    } catch (SecurityException ignore) {
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private class Worker extends ReentrantLock implements Runnable {
        final Thread thread;
        Runnable task;

        Worker(Runnable task) {
            this.task = task;
            this.thread = new Thread(this);
        }

        @Override
        public void run() {
            runWorker(this);
        }
    }

    private void runWorker(Worker worker) {
        try {
            Runnable task = worker.task;
            worker.task = null;
            while (task != null || (task = getTask()) != null) {
                worker.lock();
                try {
                    task.run();
                } finally {
                    task = null;
                    worker.unlock();
                }
            }
        } finally {
            processWorkerExit(worker);
        }
    }

    private Runnable getTask() {
        while (true) {
            if (isShutdown && workQueue.isEmpty()) {
                return null;
            }
            boolean timed = workerCount.get() > corePoolSize;
            try {
                Runnable r = timed ?
                        workQueue.poll(timeOut, timeUnit) :
                        workQueue.take();

                if (r != null) {
                    return r;
                }
            } catch (InterruptedException e) {
                // 被中断，重新检查关闭状态
                if (isShutdown && workQueue.isEmpty()) {
                    return null;
                }
            }
        }
    }

    private void processWorkerExit(Worker worker) {
        lock.lock();
        try {
            workers.remove(worker);
            workerCount.decrementAndGet();
        } finally {
            lock.unlock();
        }
    }

    @FunctionalInterface
    public interface RejectHandler {
        void reject(Runnable command, MyThreadPool pool);
    }

    // 测试主函数
    public static void main(String[] args) {
        int corePoolSize = 2;
        int maxPoolSize = 4;
        long timeOut = 10;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(2);

        MyThreadPool pool = new MyThreadPool(
                corePoolSize, maxPoolSize, timeOut, unit, workQueue,
                (command, threadPool) -> {
                    long start = System.currentTimeMillis();
                    System.out.println("[" + start + "]" + "Task rejected: " + command.toString());
                }
        );

        // 提交10个任务
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            pool.execute(() -> {
                long start = System.currentTimeMillis();
                System.out.println(
                        "[" + start + "] " + Thread.currentThread().getName() + " START task " + taskId
                );
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println(
                        "[" + System.currentTimeMillis() + "] " + Thread.currentThread().getName() + " END task " + taskId
                );
            });
        }

        // 等待5s后关闭线程池
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        pool.shutdown();
        System.out.println("Thread pool shutdown");
    }
}