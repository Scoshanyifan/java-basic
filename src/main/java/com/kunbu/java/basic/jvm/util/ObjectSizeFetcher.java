package com.kunbu.java.basic.jvm.util;

import java.lang.instrument.Instrumentation;

/**
 * https://www.cnblogs.com/niurougan/p/4196048.html
 * https://www.cnblogs.com/Kidezyq/p/8030098.html
 * https://blog.csdn.net/iter_zc/article/details/41822719
 *
 * @project: java-basic
 * @author: kunbu
 * @create: 2020-02-18 15:22
 **/
public class ObjectSizeFetcher {

    private static Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
    }

    public static long getObjectSize(Object o) {
        return instrumentation.getObjectSize(o);
    }
}
