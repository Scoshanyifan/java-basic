package com.kunbu.java.basic.base.string;

import com.kunbu.java.basic.base.ObjectAddressUtil;

/**
 * @project: java-basic
 * @author: kunbu
 * @create: 2020-01-14 15:46
 **/
public class StringB {

    public static void getString() {
        String str = "ABC";
        System.out.println("StringB: ");
        ObjectAddressUtil.printAddressByJDK(str);
    }

}
