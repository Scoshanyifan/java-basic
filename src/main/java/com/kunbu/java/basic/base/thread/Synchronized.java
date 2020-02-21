package com.kunbu.java.basic.base.thread;

/**
 * @project: java-basic
 * @author: kunbu
 * @create: 2020-02-19 13:33
 **/
public class Synchronized {

    private int count;

    public synchronized void addCount() {
        this.count++;
    }

    public int getCount() {
        synchronized (this) {
            return count;
        }
    }

    public static void main(String[] args) {
        Synchronized sync = new Synchronized();
    }
}
