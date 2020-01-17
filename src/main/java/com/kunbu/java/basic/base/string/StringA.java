package com.kunbu.java.basic.base.string;

import com.kunbu.java.basic.base.ObjectAddressUtil;

/**
 * @project: java-basic
 * @author: kunbu
 * @create: 2020-01-16 13:29
 **/
public class StringA {

    public static void String() {

        String s1 = "literal";
        ObjectAddressUtil.printAddressByJDK("StringA -> \"literal\"", s1);

        String s2 = new String("abc");
        ObjectAddressUtil.printAddressByJDK("StringA -> new String(\"abc\")", s2);

        String s3 = String.valueOf(123);
        ObjectAddressUtil.printAddressByJDK("StringA -> String.valueOf(123)", s3);

    }

}
