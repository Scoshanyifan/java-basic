package com.kunbu.java.basic.jvm.ObjectSize;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @project: java-basic
 * @author: kunbu
 * @create: 2020-02-18 15:38
 **/
public class UnsafeUtil {

    private static Unsafe UNSAFE;

    // 通过反射获取Unsafe对象的实例
    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取对象在内存中对偏移起始位置
     *
     **/
    public static void getFiledOffset(Class cls) {
        Field[] fields = cls.getDeclaredFields();
        for(Field field :fields) {
             System.out.println(field.getName() + "---offSet:" + UNSAFE.objectFieldOffset(field));
        }
    }
}
