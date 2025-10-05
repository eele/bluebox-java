package art.Byte.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class AQSTest {

    private static final Sync sync = new Sync();
    private static final SyncFair syncFair = new SyncFair();
    private static final SyncShared syncShared = new SyncShared();
    private static volatile int currentThread = -1;
    private static volatile int lastThread = -1;

    /**
     * customize an synchronizer
     */
    private static class SyncShared extends AbstractQueuedSynchronizer {
        /**
         * initialize the synchronization state to 0
         */
        SyncShared() {
            setState(0);
        }

        protected int tryAcquireShared(int acquires) {
            return getState() == 1 ? 1 : -1;
        }

        protected boolean tryReleaseShared(int releases) {
            return compareAndSetState(0, 1);
        }
    }

    /**
     * customize an synchronizer
     */
    private static class Sync extends AbstractQueuedSynchronizer {
        /**
         * initialize the synchronization state to 0
         */
        Sync() {
            setState(0);
        }

        protected boolean tryAcquire(int acquires) {
            return compareAndSetState(0, 1);
        }

        protected boolean tryRelease(int releases) {
            return compareAndSetState(1, 0);
        }
    }

    /**
     * customize an synchronizer
     */
    private static class SyncFair extends AbstractQueuedSynchronizer {
        /**
         * initialize the synchronization state to 0
         */
        SyncFair() {
            setState(0);
        }

        protected boolean tryAcquire(int acquires) {
            if (hasQueuedPredecessors()) { // Check whether other threads are in queue
                return false;
            }
            return compareAndSetState(0, 1);
        }

        protected boolean tryRelease(int releases) {
            return compareAndSetState(1, 0);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        // Shared Synchronizer
        for (int i = 0; i < 5; i++) {
            executorService.execute(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " is waiting for the lock of shared synchronizer");
                    syncShared.acquireSharedInterruptibly(1);
                    System.out.println(Thread.currentThread().getName() + " got the lock of shared synchronizer");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        Thread.sleep(2000);
        syncShared.releaseShared(1);

        // Exclusive Synchronizer
        Thread.sleep(2000);
        System.out.println("===========================================================================");

        for (int i = 0; i < 5; i++) {
            executorService.execute(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " is waiting for the lock of exclusive synchronizer");
                    sync.acquireInterruptibly(1);
                    System.out.println(Thread.currentThread().getName() + " got the lock of exclusive synchronizer");
                    Thread.sleep(1000);
                    System.out.println(Thread.currentThread().getName() + " is finished");
                    sync.release(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        // Unfair Synchronizer
        Thread.sleep(6000);
        System.out.println("===========================================================================");

        currentThread = -1;
        lastThread = -1;
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            executorService.execute(() -> {
                for (int j = 0; j < 2; j++) {
                    try {
                        sync.acquireInterruptibly(1);
                        // 记录当前获取锁的线程
                        currentThread = threadId;
                        // 检查是否连续被同一个线程获取
                        if (currentThread == lastThread) {
                            System.out.println("Thread-" + threadId + " got the lock again");
                        }
                        lastThread = currentThread;
                        System.out.println("Thread-" + threadId + " got the lock");
                        Thread.sleep(100);
                        sync.release(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        Thread.sleep(6000);
        System.out.println("===========================================================================");

        currentThread = -1;
        lastThread = -1;
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            executorService.execute(() -> {
                for (int j = 0; j < 2; j++) {
                    try {
                        syncFair.acquireInterruptibly(1);
                        // 记录当前获取锁的线程
                        currentThread = threadId;
                        // 检查是否连续被同一个线程获取
                        if (currentThread == lastThread) {
                            System.out.println("Thread-" + threadId + " got the lock again");
                        }
                        lastThread = currentThread;
                        System.out.println("Thread-" + threadId + " got the lock");
                        Thread.sleep(100);
                        syncFair.release(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        Thread.sleep(2000);

    }

}
