package com.kunbu.java.basic.base.thread;


import java.util.concurrent.*;

/**
 * 展示可返回结果的线程运行方式：Future（FutureTask）和 Callable
 * https://www.cnblogs.com/dolphin0520/p/3949310.html
 * <p>
 * Callable通常和ExecutorService线程池一起使用：<T> Future<T> submit(Callable<T> task)
 *
 * @author scosyf
 */
public class FutureCallable {

    private static final ExecutorService pool = ThreadPool.getThreadPool();

    /**
     * Future作用：
     * 1.判断任务是否完成
     * 2.能够尝试中断（取消）任务
     * 3.能够获取任务执行结果
     *
     * public interface Future<V> {
     *      //用于取消任务：参数表示是否允许取消正在执行却没完成的任务。若任务已完成，永远返回false；若还没开始执行，永远返回true
     *      boolean cancel(boolean mayInterruptIfRunning);
     *      //任务是否被取消成功
     *      boolean isCancelled();
     *      //任务是否已完成
     *      boolean isDone();
     *      //获取执行结果，一直阻塞，直到任务执行完毕或出现异常
     *      V get() throws InterruptedException, ExecutionException;
     *      //如果指定时间内没获取到结果，则返回null
     *      V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;
     * }
     */
    public static void testFuture() {
        //Callable本身没任何意义，需要依赖RunnableFuture
        Callable<Double> task = () -> {
            System.out.println(">>> task is start");
            Thread.sleep(2000);
            System.out.println(">>> task is ended");
            return 2000.0;
        };

        try {
            /**
             * public <T> Future<T> submit(Callable<T> task) {
             *		if (task == null) throw new NullPointerException();
             *		//当传参为Callable时，方法内将Callable赋给Runnable
             *		RunnableFuture<T> ftask = newTaskFor(task);
             *		//调用执行方法
             *		execute(ftask);
             *      //线程池中返回的实现类是@see FutureTask
             *		return ftask;
             * }
             */
            Future<Double> result = pool.submit(task);

            //模拟取消任务
            Thread.sleep(500);
            //和任务执行时间一致，表示任务已完成
            ///Thread.sleep(2000);

            /**
             * 1.cancel(true) 会取消所有已经提交的任务，包括【正在等待】和【开始执行】的任务。其中，开始执行包括【阻塞等待】和【正在运行】
             *		PS：中断线程可参考相关demo
             * 		1.1【阻塞等待】指可以响应线程中断，并抛出InterruptedException，包括sleep，wait，join
             * 		1.2【正在运行】指业务代码中如for循环，需要通过Thread.currentThread().isInterrupted()来判断，是否有中断"请求"，如果有就提前跳出循环
             * 2.cancel(false)只能取消已经提交但还没有开始运行的任务（暂时不知道如何模拟）
             *
             * public boolean cancel(boolean mayInterruptIfRunning) {
             * 		if (!(state == NEW && UNSAFE.compareAndSwapInt(this, stateOffset, NEW, mayInterruptIfRunning ? INTERRUPTING : CANCELLED)))
             *			return false;
             *		try {    // in case call to interrupt throws exception
             *			if (mayInterruptIfRunning) {
             *   			try {
             *       			Thread t = runner;
             *       			if (t != null)
             *           			t.interrupt();		// cancel(true)会走到这里，尝试中断线程（不保证中断，只有响应中断的线程才能被中断）
             *            } finally { // final state
             *       			UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);
             *            }
             *            }
             *        } finally {
             *			finishCompletion();
             *        }
             *		return true;
             * }
             */
            System.out.println("cancel task: " + result.cancel(true));

            //因为不能取消已经开始执行的任务，所以">>> task is ended"会被打印，但是依然会报错
            //System.out.println("cancel task: " + result.cancel(false));

            System.out.println("cancel result: " + result.isCancelled());
            System.out.println("done result: " + result.isDone());
            System.out.println("result: " + result.get());

        } catch (InterruptedException e) {
            System.err.println("任务被中断");
        } catch (ExecutionException e) {
            System.err.println("任务执行出错");
        } catch (CancellationException e) {
            System.err.println("任务被取消");
        } finally {
            pool.shutdown();
        }
    }

    /**
     * FutureTask是Future和Runnable的一种实现类，作用类似，只是将两者结合在一起
     */
    public static void testFutureTask() {
        FutureTask<Double> task = new FutureTask<>(
                () -> 0.0
        );
        try {
            pool.submit(task);
            Thread.sleep(1000);
            System.out.println("FutureTask返回结果：" + task.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }

    /**
     * 自定义一个可以获取返回值的模型
     */
    public static void testMyCallable() {
        ReturnRunnable<Double> re = new ReturnRunnable<Double>(
                () -> 2.33
        );

        pool.execute(re);
        pool.shutdown();
        System.out.println("结果：" + re.get());
    }


    public static void main(String[] args) {
//		testFuture();
//		testFutureTask();
        testMyCallable();

        //也可以直接用Thread执行，只是拿不到返回值
        new Thread(new FutureTask<>(
                () -> {
                    System.out.println("这是在Thread中");
                    return 0.0;
                }
        )).start();
    }

    static class ReturnRunnable<V> implements Runnable {

        private V result;
        private volatile boolean flag;
        private ReturnCallable<V> rc;

        public ReturnRunnable(ReturnCallable<V> task) {
            rc = task;
        }

        @Override
        public void run() {
            result = rc.calling();
            flag = true;
        }

        public V get() {
            for (; ; ) {
                if (flag) {
                    return result;
                }
            }
        }

    }

    interface ReturnCallable<V> {
        V calling();
    }

}
