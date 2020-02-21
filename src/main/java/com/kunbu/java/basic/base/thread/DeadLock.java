package com.kunbu.java.basic.base.thread;

/**
 * 模拟死锁
 * 
 * 通过jps + jstack可以查找
 *
 * @author Scosyf
 * @date 2018年11月18日 下午8:38:52
 * @since 1.0.0
 */
public class DeadLock {

    public static void main(String[] args) throws InterruptedException {
        DeadLock deadLock = new DeadLock();
        
        new Thread(() -> {
            try {
                deadLock.resource();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "resource").start();
        
        new Thread(() -> {
            try {
                deadLock.data();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "data").start();
        
    }
    
    private void resource() throws InterruptedException {
        synchronized ("resource") {
            System.out.println(Thread.currentThread().toString() + "获取数据data");
            Thread.sleep(1000);
            data();
            System.out.println("获取结束...");
        }
    }
    
    private void data() throws InterruptedException {
        synchronized ("data") {
            System.out.println(Thread.currentThread().toString() + "获取资源resource");
            Thread.sleep(1000);
            resource();
            System.out.println("获取结束...");
        }
    }
}
