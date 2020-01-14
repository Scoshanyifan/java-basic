package com.kunbu.java.basic.base;

import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @project: java-basic
 * @author: kunbu
 * @create: 2020-01-14 10:25
 **/
public class ObjectAddressUtil {

    /**
     * auto detect if possible.
     **/
    static final boolean is64bit = true;

    public static void printAddressByJDK(Object obj) {
        //通过jol工具包打印对象的地址
        System.out.println("Current address: " + VM.current().addressOf(obj));
        System.out.println(GraphLayout.parseInstance(obj).toPrintable());
    }

    public static void printAddress(Object... objects) {
        Unsafe unsafe = getUnsafe();
        System.out.print("address:         0x");
        long last = 0;
        int offset = unsafe.arrayBaseOffset(objects.getClass());
        int scale = unsafe.arrayIndexScale(objects.getClass());
        switch (scale) {
            case 4:
                long factor = is64bit ? 8 : 1;
                final long i1 = (unsafe.getInt(objects, offset) & 0xFFFFFFFFL) * factor;
                last = i1;
                for (int i = 1; i < objects.length; i++) {
                    final long i2 = (unsafe.getInt(objects, offset + i * 4) & 0xFFFFFFFFL) * factor;
                    if (i2 > last) {
                        System.out.print(", +" + Long.toHexString(i2 - last));
                    } else {
                        System.out.print(", -" + Long.toHexString(last - i2));
                    }
                    last = i2;
                }
                break;
            case 8:
                throw new AssertionError("Not supported");
//            default:
//                break;
        }
        System.out.println();
    }

    private static Unsafe getUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

}
