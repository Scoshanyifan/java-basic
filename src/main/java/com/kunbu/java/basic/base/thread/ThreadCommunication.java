package com.kunbu.java.basic.base.thread;

/**
 * 线程间通信
 *

 *
 * @author kunbu
 **/
public class ThreadCommunication {

    public static void testJoin() {
        printCurrentThread("");

        Thread t1 = new Thread(new JoinRunnable(1000));
        Thread t2 = new Thread(new JoinRunnable(2000));
        t1.start();
        t2.start();

        try {
            // 内部是调用Object中的wait方法实现线程阻塞
            t1.join();
            printCurrentThread("");
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 等待执行了join方法的线程执行终止后，才会走到这
        printCurrentThread("等待join()结束");
    }

    public static void printCurrentThread(String description) {
        Thread currThread = Thread.currentThread();
        System.out.println(currThread.getName() + "-" + currThread.getState() + " " + description);
    }


    public static void main(String[] args) {

        testJoin();
    }

    static class JoinRunnable implements Runnable {

        private long sleepTime;

        public JoinRunnable(long sleepTime) {
            this.sleepTime = sleepTime;
        }

        @Override
        public void run() {
            ThreadCommunication.printCurrentThread("开始");
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ThreadCommunication.printCurrentThread("任务");
        }
    }
}

