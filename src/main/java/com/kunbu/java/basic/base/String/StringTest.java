package com.kunbu.java.basic.base.String;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * String 常用方法，注意点等
 *
 * @author kunbu
 **/
public class StringTest {

    public static final String SYSTEM_FILE_ENCODING = System.getProperty("file.encoding");

    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String CHARSET_GBK = "GBK";
    public static final String CHARSET_IOS = "ISO-8859-1";
    public static final String CHARSET_UNICODE = "unicode";

	/**
	 * getBytes()方法得到系统编码格式下的字节数组，windows7一般是GBK
	 *
	 **/
	public static void testGetBytes() {
        try {
            String chinese = "昆布";

            byte[] byte2default = chinese.getBytes();
            byte[] byte2gbk = chinese.getBytes(CHARSET_GBK);
            byte[] byte2utf8 = chinese.getBytes(CHARSET_UTF8);
            byte[] byte2iso = chinese.getBytes(CHARSET_IOS);
            byte[] byte2unicode = chinese.getBytes(CHARSET_UNICODE);

            System.out.println(Arrays.toString(byte2default));
            System.out.println(Arrays.toString(byte2gbk));
            System.out.println(Arrays.toString(byte2utf8));
            System.out.println(Arrays.toString(byte2iso));
            System.out.println(Arrays.toString(byte2unicode));

            System.out.println(new String(byte2default));
            System.out.println(new String(byte2gbk, CHARSET_GBK));
            System.out.println(new String(byte2utf8, CHARSET_UTF8));
            // 显示??，因为iso编码表中没有汉字
            System.out.println(new String(byte2iso, CHARSET_IOS));
            System.out.println(new String(byte2unicode, CHARSET_UNICODE));

            // 实际业务中，会遇到苹果浏览器显示文件名乱码（导出excel），因其编码识别iso，所以需要两次转换
            String utf82iso = new String(chinese.getBytes(CHARSET_UTF8), CHARSET_IOS);
            System.out.println(utf82iso);
            String iso2utf8 = new String(utf82iso.getBytes(CHARSET_IOS), CHARSET_UTF8);
            System.out.println(iso2utf8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
	}


	public static void testIndexOf() {
		String origin = "org.apache.catalina.session.StandardSessionFacade@7147820e;admin;aGFkbGlua3MuY29t;";
		String old = "编辑角色（必传参数:'id','roleName','roleMenuFunctionList'）";
		String str = "null',roleName='领航订单客服'}menuId=";
		String splitterStr = "A0BC92D94BD82755F807CBC1B78A0AFF;865533033393442;null;0865533033110341;";
		
		int ftIdx = origin.indexOf(";");
		int secIdx = origin.indexOf(";", ftIdx + 1);
		System.out.println(origin.substring(ftIdx + 1, secIdx));
		
		if (old.indexOf("（") > 0) {
			System.out.println(old.substring(0, old.indexOf("（")));
		}
		
		String rn = "roleName";
		int rnIndex = str.indexOf(rn);
		int lastIndex = str.indexOf("'", rnIndex + rn.length() + 2);
		String roleName = str.substring(rnIndex + rn.length() + 2, lastIndex);
		System.out.println(roleName);
		
		String params[] = splitterStr.split(";");
		System.out.println(Arrays.toString(params));
		System.out.println(params.length);
	}


	private static String subStringContent(String target, String origin) {
		int beginIndex = origin.indexOf(target);
		if (beginIndex > 0) {
			int lastIndex = origin.indexOf("'", beginIndex + target.length() + 2);
			String result = origin.substring(beginIndex + target.length() + 2, lastIndex);
			return result;
		}
		return "";
	}
	

	public static void main(String[] args) {

	    System.out.println(SYSTEM_FILE_ENCODING);

        testGetBytes();
        System.out.println();

		testIndexOf();
		System.out.println(subStringContent("roleName", "null',roleName='领航订单客服'}menuId="));

		String lowerStr = "1z&u2";
		System.out.println("upper: " + lowerStr.toUpperCase());
	}
	
}
