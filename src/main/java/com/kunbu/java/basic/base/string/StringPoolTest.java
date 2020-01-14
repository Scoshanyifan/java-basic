package com.kunbu.java.basic.base.string;

import com.kunbu.java.basic.base.ObjectAddressUtil;

/**
 * 字符串（池）被大家经常误解和搞不清楚的原因：
 * JVM为了提高性能和减少内存开销，在实例化字符串常量的时候进行了一些优化，即减少在JVM中创建的字符串的数量，字符串类维护了一个字符串常量池。
 *
 * 分析String相关的创建按编译期到运行期的步骤进行:（因为涉及到运行时数据区域和类加载，所以需参考那两块知识）
 *  1. 编译期：java文件编译成class文件
 *  2/3. 运行期（类加载）：加载 -> 连接（验证/准备/解析）-> 初始化
 *
 *
 * 各种池子：
 *  1. Class文件常量池（Class Constant Pool）
 *      编译阶段：存放编译器生成的各种字面量(Literal)和符号引用(Symbolic References)
 *          字面量：文本字符串/final常量值/基本数据类型值
 *          符号引用：类和接口的全限定名/字段的名称和描述符/方法的名称和描述符
 *
 *  2. 全局驻留字符串池（string Pool / string Literal Pool）
 *      全局字符串池里的内容是在类加载完成，经过验证，准备阶段之后在堆中生成字符串对象实例，然后将该字符串对象实例的引用值存到string pool中（intern注释中提到的pool），
 *      string pool中存的是引用值而不是具体的实例对象，[具体的实例对象是在堆中开辟的一块空间存放的]。
 *      在HotSpot VM里实现的string pool功能的是一个StringTable类，它是一个哈希表，里面存的是驻留字符串(也就是我们常说的用双引号括起来的)的引用（而不是驻留字符串实例本身），
 *      也就是说在堆中的某些字符串实例被这个StringTable引用之后就等同被赋予了”驻留字符串”的身份。这个StringTable在每个HotSpot VM的实例只有一份，被所有的类共享。
 *
 *      java.lang.String@7fbe847cd object externals:
 *           ADDRESS       SIZE TYPE             PATH               VALUE
 *          d61a0610         24 java.lang.String                    (object)            # 全局字符串池中的驻留字符串地址
 *          d61a0628         32 [C               .value             [H, e, l, l, o]     # String中char[]的地址
 *
 *  3. 运行时常量池（Runtime Constant Pool）
 *      类加载-加载阶段：
 *          用于存放程序中的一切常量，包含代码中所定义的各种基本类型（如int,long等等）和对象型（如String及数组）的常量值（final）。
 *      JVM会将class常量池中的内容存放到运行时常量池中，由此可知，运行时常量池也是每个类都有一个。class常量池中存的是字面量和符号引用，
 *      也就是说他们存的并不是对象的实例，而是对象的符号引用值。而经过解析（resolve）之后，也就是把符号引用替换为直接引用，解析的过程会去查询全局字符串池，
 *      也就是我们上面所说的StringTable，以保证运行时常量池所引用的字符串与全局字符串池中所引用的是一致的。
 *
 *      动态性：即运行时常量池在运行期间也可能有新的常量放入池中（如String类的intern（）方法）
 *
 *
 * String的不可变性：
 * 1. final String是保证String不可被继承
 * 2. 体现在内部char[]是final的，即字符串内容不可变更，初始化时就确定了，并且不提供修改方法，如果调用replace等方法直接返新的String
 * 	  PS：可以利用反射特性破坏String的不可变性      https://segmentfault.com/a/1190000019865846
 *
 *
 * 参考：
 *  http://tangxman.github.io/2015/07/27/the-difference-of-java-string-pool/
 *  https://blog.csdn.net/qq_26222859/article/details/73135660
 *  https://www.cnblogs.com/xuxinstyle/p/9526210.html
 *  https://www.hollischuang.com/archives/1551
 *  https://www.zhihu.com/question/55994121
 *  https://www.hollischuang.com/archives/2517
 *  https://blog.csdn.net/weixin_40999907/article/details/87907083
 *
 *
 * 问题：字面量实例存放在哪，从StringA/StringB的打印地址看，char[]用的是同一个，而运行时常量池是每个类都有，如此的话岂不矛盾？？？
 *
 * @author kunbu
 **/
public class StringPoolTest {

    public static void testLiteralAndNew() {

        /**
         * 字面量形式创建：
         *  1. 编译期：Hello会被放在Class文件的Constant Pool中（其中还包含符号引用literalStr）
         *
         *      0 ldc #2 <Hello>
         *      2 astore_0
         *
         *  2. 类加载：字面量会在[加载]阶段被加载到类的运行时常量池中（方法区），符号引用会在解析阶段替换为直接引用
         *
         *
         * 结论：栈上声明的变量literalStr指向的是全局字符串池中的Hello的地址
         *
         **/
        String literalStr = "Hello";
        // Current address: 3592029712     # 0xd61a0610
        ObjectAddressUtil.printAddressByJDK(literalStr);

        /**
         * new形式创建：
         *  1. 编译期：如果new String("Hello") 是第一句，说明Class常量池中还没有Hello，那么会和字面量形式创建一样。
         *            如果literalStr = "Hello" 在前面，说明Hello已经在Class常量池中了，两者指向同一个常量池位置（#2）
         *
         *      3 new #3 <java/lang/String>
         *      6 dup
         *      7 ldc #2 <Hello>                                # 和上面的字面量创建共同指向同一个Hello
         *      9 invokespecial #4 <java/lang/String.<init>>
         *      12 astore_1
         *      13 return
         *
         *  2. 类加载：加载阶段，Hello从class常量池到了运行时常量池，然后在全局常量池中驻留（即存有引用）
         *
         *  3. 运行期：invokespecial被执行，调用String的初始化方法，在堆中创建对象，该对象指向全局常量池中的引用
         *
         *
         *  结论：newStr指向堆中的地址s1，而s1指向全局字符串池中Hello的地址，而newStr2在堆中是另一个地址s2，s2和s1都指向全局字符串池Hello的地址
         *
         *
         *  PS：https://www.hollischuang.com/archives/2517 中提到"类加载时，该字符串常量在常量池中已经有了，那这一步就省略了"
         *      是指在解析阶段
         **/
        String newStr = new String("Hello");
        // Current address: 3602792208     # 0xd6be3f10
        ObjectAddressUtil.printAddressByJDK(newStr);

        String newStr2 = new String("Hello");
        ObjectAddressUtil.printAddressByJDK(newStr2);

        // false / false
        System.out.println("字面量和new：" + (literalStr == newStr));
        System.out.println("不同new：" + (newStr == newStr2));
    }

    /**
     * 不同版本JDK下，intern的表现不同
     *
     * 作用：返回[全局驻留字符串池]中引用，如果池中已存在，则直接返回池中引用，否则在池中"创建"并返回其引用
     * return a string that has the same contents as this string, but is guaranteed to be from a pool of unique strings.
     *
     * Returns a canonical representation for the string object.
     * A pool of strings, initially empty, is maintained privately by the class String.
     * When the intern method is invoked, if the pool already contains a string equal to this String object as determined by the equals(Object) method,
     * then the string from the pool is returned. Otherwise, this String object is added to the pool and a reference to this String object is returned.
     * It follows that for any two strings s and t, s.intern() == t.intern() is true if and only if s.equals(t) is true.
     * All literal strings and string-valued constant expressions are interned.
     *
     **/
    public static void testIntern() {
        // Call to 'intern()' on compile-time constant is unnecessary
        String internStr = "World".intern();
        // Current address: 3602843912
        ObjectAddressUtil.printAddressByJDK(internStr);

        String literalStr = "World";
        // Current address: 3602843912
        ObjectAddressUtil.printAddressByJDK(literalStr);

        String newStr = new String("World");
        // Current address: 3602884480
        ObjectAddressUtil.printAddressByJDK(newStr);

        String newStrIntern = newStr.intern();
        // Current address: 3602843912
        ObjectAddressUtil.printAddressByJDK(newStrIntern);

        /**
         *  0 new #3 <java/lang/String>
         *  3 dup
         *  4 ldc #13 <World>
         *  6 invokespecial #4 <java/lang/String.<init>>
         *  9 invokevirtual #14 <java/lang/String.intern>
         * 12 astore_0
         * 13 return
         *
         * 从字节码层面看，先在堆中创建对象，然后再执行intern，最后返回引用，也就是说internStr2指向intern返回的常量池中的World地址
         **/
        String internStr2 = new String("World").intern();
        // Current address: 3602843912
        ObjectAddressUtil.printAddressByJDK(internStr2);
    }

    public static void testStringLink() {

    }


    public static void main(String[] args) {

        /**
         * StringA:
         * Current address: 3592040088
         * java.lang.String@7fbe847cd object externals:
         *           ADDRESS       SIZE TYPE             PATH                           VALUE
         *          d61a2e98         24 java.lang.String                                (object)
         *          d61a2eb0         24 [C               .value                         [A, B, C]
         *
         * StringB:
         * Current address: 3592040088
         * java.lang.String@7fbe847cd object externals:
         *           ADDRESS       SIZE TYPE             PATH                           VALUE
         *          d61a2e98         24 java.lang.String                                (object)
         *          d61a2eb0         24 [C               .value                         [A, B, C]
         *
         *
         * 从上面分析，字符串实例，即char[]用的同一份，
         **/
        StringA.getString();
        StringB.getString();

        testLiteralAndNew();

        testIntern();

//        // 1.创建abc实例，放在堆中，然后在全局字符串中存放abc的引用值
//        String literalStr       = "abc";
//        // 2.先在全局字符串中查找abc，若已存在则返回该引用值，否则像之前一样创建
//        String literalStr2      = "abc";
//        // 3.如果abc已存在（即全局字符串中有）则不会再创建abc实例；new会创建一个新的abc实例
//        String newStr           = new String("abc");
//        // 4.
//        String internStr        = newStr.intern();
//        String internStr2       = literalStr.intern();
//
//        System.out.println(literalStr == literalStr2);
//        System.out.println(literalStr == newStr);
//        System.out.println(literalStr == internStr);
//        System.out.println(literalStr == internStr2);
//
//        System.out.println(newStr == internStr);
//        System.out.println(newStr == internStr2);
//
//        System.out.println(internStr == internStr2);
    }



}
