package com.kunbu.java.basic.base.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Date;
import java.util.concurrent.*;

/**
 * @project: java-basic
 *
 * https://crossoverjie.top/2018/07/29/java-senior/ThreadPool/
 * http://ifeve.com/java-threadpool/
 *
 * @author: kunbu
 **/
public class ThreadPool {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 20;
    private static final Long KEEP_ALIVE_TIME = 1L;


    public static void myThreadPool() {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy());

        for (int i = 0; i < 30; i++) {
            executor.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " Start. Time = " + new Date());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + " End. Time = " + new Date());
            });
        }
        //终止线程池
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
    }


    //	private static final int MISSION_SIZE = Integer.MAX_VALUE;
    private static final int MISSION_SIZE = 20;//50

    /**
     * 模拟使用Executors下API出现OOM
     * @throws InterruptedException
     */
    public static void testExecutors() throws InterruptedException {
        ExecutorService fixed = Executors.newFixedThreadPool(10);
//		ExecutorService cache = Executors.newCachedThreadPool();
        for (int i = 0; i < MISSION_SIZE; i++) {
            //java.lang.OutOfMemoryError: GC overhead limit exceeded
            fixed.execute(() -> {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        closePool(fixed);
    }

    /**
     * 正确配置线程池
     *
     * 1.CPU密集型任务配置尽可能少的线程数量，如配置Ncpu+1个线程
     * 2.IO密集型任务则由于需要等待IO操作，可以配置2*Ncpu个
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static ExecutorService getThreadPool() {
        int cpuSize = Runtime.getRuntime().availableProcessors();
        System.out.println("本机CPU核心数：" + cpuSize);
        ExecutorService pool = new ThreadPoolExecutor(
                cpuSize,
                cpuSize * 2,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>(1024),
                new ThreadFactoryBuilder().setNameFormat("go-pool-%d").build(),
                new ThreadPoolExecutor.AbortPolicy()) {

            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                System.out.println("抛弃策略（执行前）：" + t + " >>> " + r);
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
//						System.out.println("抛弃策略（执行后）：" + r);
            }
        };
        return pool;
    }

    /**
     * 优雅的关闭线程池
     */
    public static void closePool(ExecutorService pool) throws InterruptedException {
        if (pool != null) {
            //已经提交的任务会执行完
            System.out.println(Thread.currentThread().getName() + " >>> 开始关闭线程池");
            pool.shutdown();
            /**
             * 判断线程池是否终止，每隔1秒检查并提醒
             *
             * 其中awaitTermination的timeout表示超时时间
             * 		1.timeout内，如果线程池终止了，返回true，否则一直阻塞（无限循环的检查）
             * 		2.超过timeout，线程池池终止了返回true，否则返回false
             */
            while (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
                System.out.println("尚有程序还在执行...线程池是否停止运行：" + pool.isTerminated());
            }
            //跳出循环时表示线程池完全终止了
            System.out.println("线程池是否停止运行：" + pool.isTerminated());

            //关闭后，如果再提交任务会抛出RejectedExecutionException
//			pool.execute(() -> {});

            //强制关闭线程池，运行中的任务全部被中断取消，抛弃并返回队列中等待的任务
//			List<Runnable> lefts = pool.shutdownNow();
//			System.out.println("等待中的任务数：" + lefts.size()); //MISSION_SIZE=50时打印出42
        }
    }

    public static void main(String[] args) {
        ExecutorService pool = null;
        try {
//			testExecutors();

            pool = getThreadPool();
            for (int i = 0; i < MISSION_SIZE; i++) {
                // MISSION_SIZE = Integer.MAX_VALUE 时报错 java.util.concurrent.RejectedExecutionException
                pool.execute(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            System.out.println(">>> 任务已全部提交");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                closePool(pool);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(">>> 主程序已结束");
    }

}
