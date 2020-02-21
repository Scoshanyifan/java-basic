package com.kunbu.java.basic.base.thread;

/**
 * 线程中断
 *
 * 涉及中断的方法：
 * 		1.thread.interrupt()：会设置中断标记，表明当前线程可以响应中断
 * 		2.thread.isInterrupted()：返回中断标记，不清除
 * 		3.Thread.interrupted()：类方法，返回中断标记，并清除（置为false）
 *      PS：后两者都调同一方法，区别在于是否清除中断标记 private native boolean isInterrupted(boolean ClearInterrupted);
 *
 * 线程中断的场景：
 *      1.中断【正在运行】的线程，依赖中断标记，如业务代码中的for循环，通过thread.isInterrupted()来判断是否有中断的通知，如果有则提前跳出循环
 * 		2.中断【条件等待】的线程，需要能够响应线程中断，并抛出InterruptedException，包括sleep，wait，join
 * 		PS：线程的等待分两种，锁等待（锁外）和条件等待（锁内），这里指的是条件等待
 *
 * 	参考：
 * 	    https://www.cnblogs.com/skywang12345/p/3479949.html
 *
 * @author kunbu
 **/
public class ThreadInterrupt {

    public static void main(String[] args) throws InterruptedException {
        //中断【正在运行】的线程
//        interruptRunning();

        //中断【条件等待】的线程
        interruptWaiting();
    }

    /**
     * 1.演示如何中断【运行状态】下的线程，两种异曲同工，都是以通知方式告诉目标线程
     */
    public static void interruptRunning() throws InterruptedException {
        /** [1] 通过interrupt()将中断标记设为true */
//        Thread t1 = new RunningThread();
//        printThreadInfo(t1);
//        t1.start();
//        printThreadInfo(t1);
//        // 让t先执行一会
//        Thread.sleep(1000);
//        //main线程调用目标线程的中断方法，通知对方可以中断，此时中断标记被置为true，具体处理由目标线程自己处理
//        t1.interrupt();
//        printThreadInfo(t1, "main发起中断后 ");

//        // 标记在t结束后才清除
//        Thread.sleep(1000);
//        printThreadInfo(t1);

        /** [2] 通过自定义标记来控制 */
        RunningThread2 t2 = new RunningThread2();
        printThreadInfo(t2);
        t2.start();
        printThreadInfo(t2);
        Thread.sleep(1000);
        t2.stopThread();
        printThreadInfo(t2, "main调用stopThread后 ");

        Thread.sleep(1000);
        printThreadInfo(t2);
    }

    static class RunningThread extends Thread {
        @Override
        public void run() {
            printThreadInfo(Thread.currentThread(), "进入循环前 ");
            while (!isInterrupted()) {
            }
            printThreadInfo(Thread.currentThread(), "线程中断，循环结束 ");
        }
    }

    static class RunningThread2 extends Thread {
        private volatile boolean flag = true;
        public void stopThread() {
            flag = false;
        }
        @Override
        public void run() {
            printThreadInfo(Thread.currentThread(), "进入循环前 ");
        	while (flag) {
            }
            printThreadInfo(Thread.currentThread(), "线程中断，循环结束 ");
        }
    }


    /**
     * 2.演示如何中断"阻塞状态"下的线程，线程自己响应中断的异常，清除标记并抛出异常
     *
     * 当目标线程调用了sleep(), wait(), join()等方法进入阻塞状态后，此时外界调用目标线程的interrupt()会将中断标记设为true，
     * 由于处于阻塞状态，目标线程会响应中断，同时产生一个InterruptedException异常，并清除标记。
     *
     */
    public static void interruptWaiting() throws InterruptedException {
        Thread t = new WaitingThread();
        printThreadInfo(t);
        t.start();
        printThreadInfo(t);
        //让出CPU给目标线程执行，循环3次后的第4次进入sleep，此时的状态是TIME_WAITING
        Thread.sleep(3500);
        t.interrupt();
        //此处中断标记的打印，如果先于子线程捕获抛异常会显示true，即还没清除前（大多数情况打印false）
        printThreadInfo(t, "main发起中断后 ");

        //让出CPU给子线程去执行中断
        Thread.sleep(1000);
        printThreadInfo(t);
    }

    static class WaitingThread extends Thread {
        @Override
        public void run() {
            try {
                int i = 0;
                printThreadInfo(Thread.currentThread(), "进入while前 ");
                while (!isInterrupted()) {
                    //第三次sleep的时候中断，在这里抛出异常
                    Thread.sleep(1000);
                    printThreadInfo(Thread.currentThread(), "loop-" + i + " ");
                    i++;
                }
                //这句话不会被打印，因为上面就抛出异常了
                printThreadInfo(Thread.currentThread(), "循环while结束 ");
            } catch (InterruptedException e) {
                //阻塞状态下的中断会抛出异常，并且会将interrupt标记清除，重置为false
                printThreadInfo(Thread.currentThread(), "抛出异常后捕获 ");
            }
            printThreadInfo(Thread.currentThread(), "run()结束 ");
        }
    }



    private static void printThreadInfo(Thread thread) {

        printThreadInfo(thread, "");

    }

    private static void printThreadInfo(Thread thread, String prefix) {
        System.out.println(prefix + thread.getName() + ": " + thread.getState() + ", interrupt: " + thread.isInterrupted());
    }
}
