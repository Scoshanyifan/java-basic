package com.kunbu.java.basic.jvm.pool;

/**
 * @project: java-basic
 * @author: kunbu
 * @create: 2020-01-16 13:29
 **/
public class StringB {

    public static void String() {

        String s1 = "literal";
        ObjectAddressUtil.printAddressByJDK("StringB -> \"literal\"", s1);

        String s2 = new String("abc");
        ObjectAddressUtil.printAddressByJDK("StringB -> new String(\"abc\")", s2);

        String s3 = String.valueOf(123);
        ObjectAddressUtil.printAddressByJDK("StringB -> String.valueOf(123)", s3);

    }

}
