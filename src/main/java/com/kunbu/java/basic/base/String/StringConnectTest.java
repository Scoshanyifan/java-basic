package com.kunbu.java.basic.base.String;

import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * 字符串连接器：能够按指定分割符分隔，并且有前后缀
 *
 * 1. StringBuilder
 * 2. StringJoiner
 * 3. Collectors.joining
 *
 * @author kunbu
 * @time 2019/2/18 14:00
 **/
public class StringConnectTest {

	private static final List<String> AREA_LIST = Lists.newArrayList("广州", "上海", "杭州", "宁波");

	private static final String SPLITTER	= ",";
	private static final String PREFIX      = "{";
	private static final String SUFFIX	  	= "}";

	/**
	 * 默认创建16位char数组value用于保存数据
	 * 底层调用：System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin);
	 *
	 **/
	public static String testStringBuilder(List<String> items, String delimiter, String prefix, String suffix) {
		StringBuilder builder = new StringBuilder(prefix);
		if (!CollectionUtils.isEmpty(items)) {
			items.stream().forEach(x -> builder.append(x).append(delimiter));
			//去除最后一个逗号
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.append(suffix).toString();
	}

	public static String testStringJoiner(List<String> items, String delimiter, String prefix, String suffix) {
		StringJoiner joiner = new StringJoiner(delimiter, prefix, suffix);
		if (!CollectionUtils.isEmpty(items)) {
			items.stream().forEach(x -> joiner.add(x));
		}
		return joiner.toString();
	}

	/**
	 * 内部通过SpringJoiner实现
	 *
	 **/
	public static String testCollectorsJoining(List<String> items, String delimiter, String prefix, String suffix) {
		if (!CollectionUtils.isEmpty(items)) {
			return items.stream()
					.collect(Collectors.joining(delimiter, prefix, suffix));
		} else {
			return prefix + suffix;
		}
	}


	public static void main(String[] args) {

		System.out.println(testStringBuilder(AREA_LIST, SPLITTER, PREFIX, SUFFIX));
		System.out.println(testStringJoiner(AREA_LIST, SPLITTER, PREFIX, SUFFIX));
		System.out.println(testCollectorsJoining(null, SPLITTER, PREFIX, SUFFIX));

	}

}
