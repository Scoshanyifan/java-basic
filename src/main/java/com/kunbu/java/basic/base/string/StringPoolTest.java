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
         *  1. 编译期：hello会被放在Class文件的Constant Pool中（其中还包含符号引用literalStr）
         *
         *      0 ldc #2 <hello>
         *      2 astore_0
         *
         *  2. 类加载：字面量会在[加载]阶段被加载到类的运行时常量池中（方法区），符号引用会在解析阶段替换为直接引用
         *
         *
         * 结论：栈上声明的变量literalStr指向的是全局字符串池中的hello的地址
         *
         **/
        String literalStr = "hello";

        /**
         * new形式创建：
         *  1. 编译期：如果new String("hello") 是第一句，说明Class常量池中还没有hello，那么会和字面量形式创建一样。
         *            如果literalStr = "hello" 在前面，说明hello已经在Class常量池中了，两者指向同一个常量池位置（#2）
         *
         *      3 new #3 <java/lang/String>
         *      6 dup
         *      7 ldc #2 <hello>                                # 和上面的字面量创建共同指向同一个hello
         *      9 invokespecial #4 <java/lang/String.<init>>
         *      12 astore_1
         *      13 return
         *
         *  2. 类加载：加载阶段，hello从class常量池到了运行时常量池，然后在全局常量池中驻留（即存有引用）
         *
         *  3. 运行期：invokespecial被执行，调用String的初始化方法，在堆中创建对象，该对象指向全局常量池中的引用
         *
         *
         *  结论：newStr指向堆中的地址s1，而s1指向全局字符串池中hello的地址，而newStr2在堆中是另一个地址s2，s2和s1都指向全局字符串池hello的地址
         *
         *
         *  PS：https://www.hollischuang.com/archives/2517 中提到"类加载时，该字符串常量在常量池中已经有了，那这一步就省略了"
         *      是指在解析阶段
         **/
        String newStr = new String("hello");
        String newStr2 = new String("hello");

        // false / false
        System.out.println("literalStr == newStr：" + (literalStr == newStr));
        System.out.println("newStr == newStr2：" + (newStr == newStr2));

        ObjectAddressUtil.printAddressByJDK("literalStr", literalStr);
        ObjectAddressUtil.printAddressByJDK("newStr", newStr);
        ObjectAddressUtil.printAddressByJDK("newStr2", newStr2);
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

        /**
         *        栈                     堆                    全局字符串池
         *     literalStr                                        ls                     char[]={world}
         *     literalStrIntern                                  ls                     char[]={world}
         **/
        // 在全局字符串池中生成String对象ls=world，指向char[]={world}。literalStr指向ls
        String literalStr = "world";
        // 返回literalStr所指向的全局字符串池中的引用ls
        String literalStrIntern = literalStr.intern();

        /**
         *        栈                     堆                    全局字符串池
         *     newStr                   hs                                            char[]={world}
         *     newStrIntern                                     ls                    char[]={world}
         **/
        // 生成1个对象，heap中的String对象hs，指向char[]={world}，因为world已经在全局字符串池中存在引用，所以不再创建。newStr指向hs
        String newStr = new String("world");
        // 检查全局字符串池，返回newStr指向的hs所对应的字面量world的引用ls
        String newStrIntern = newStr.intern();

        /**
         *        栈                     堆                    全局字符串池
         *     intern                   hs                        ls                    char[]={world}
         *
         *
         *  0 new #3 <java/lang/String>
         *  3 dup
         *  4 ldc #13 <world>
         *  6 invokespecial #4 <java/lang/String.<init>>
         *  9 invokevirtual #14 <java/lang/String.intern>
         * 12 astore_0
         * 13 return
         *
         * 从字节码层面看，先检查全局字符串池中的world（无则创建），然后在堆中创建对象，最后再执行intern，返回的引用指向intern()所在常量池中的world引用ls
         **/
        //
        String intern = new String("world").intern();

        System.out.println((literalStr == literalStrIntern) + " >>> literalStr == literalStrIntern\n");
        System.out.println((newStr == newStrIntern) + " >>> newStr == newStrIntern\n");
        System.out.println((literalStrIntern == newStrIntern) + " >>> literalStrIntern == newStrIntern\n");

        System.out.println((literalStr == intern) + " >>> literalStr == intern\n");
        System.out.println((newStr == intern) + " >>> newStr == intern\n");
        System.out.println((literalStrIntern == intern) + " >>> literalStrIntern == intern\n");
        System.out.println((newStrIntern == intern) + " >>> newStrIntern == intern\n");

        ObjectAddressUtil.printAddressByJDK("literalStr", literalStr);
        ObjectAddressUtil.printAddressByJDK("literalStrIntern", literalStrIntern);
        ObjectAddressUtil.printAddressByJDK("newStr", newStr);
        ObjectAddressUtil.printAddressByJDK("newStrIntern", newStrIntern);
        ObjectAddressUtil.printAddressByJDK("intern", intern);
    }

    public static void testStringLink() {

        // 生成两个对象，全局字符串池中的String对象ls，指向char[]={1}；堆中的String对象hs，指向char[]={11}（此时全局字符串池中没有11）
        String s1 = new String("1") + new String("1");
        // TODO 将s1指向的hs所对应的11放入全局字符串池，因为池中不存在11，需要生成，但是此时堆中已经存在11，所以可以直接拿来用，也就是hs
        String si = s1.intern();
        // 显示声明11，会去检查全局字符串池，发现已经存在11（hs）
        String s2 = "11";
        // s1是堆中的，s2是全局字符串池的，但是指向堆中的引用
        System.out.println(s1 == s2);

        ObjectAddressUtil.printAddressByJDK("s1", s1);
        ObjectAddressUtil.printAddressByJDK("s2", s2);
        ObjectAddressUtil.printAddressByJDK("si", si);


        // 创建2个对象：全局字符串池中的String对象ls=2，指向char[]={2}和heap中的String对象hs=22，指向char[]={22}。s3指向hs，此时全局字符串池中没有22的引用
        String s3 = new String("2") + new String("2");
        // 显示创建22：全局字符串池中String对象ls2=22，指向char[]={22}。s4指向ls2
        String s4 = "22";
        // 将s3（hs）对应的22放入全局字符串池，但是池中已经存在了22（ls2指向），所以此句无效
        String si2 = s3.intern();
        // s3是堆中的，s4是全局字符串池中的
        System.out.println(s3 == s4);

        ObjectAddressUtil.printAddressByJDK("s3", s3);
        ObjectAddressUtil.printAddressByJDK("s4", s4);
        ObjectAddressUtil.printAddressByJDK("si2", si2);
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
//        StringA.getString();
//        StringB.getString();
//
//        testLiteralAndNew();
//
        testIntern();

//        testStringLink();
    }

    /**
     * jdk 1.8
     *
     * 字面量形式，String s = "abc"; 常量char[]={a,b,c}会进入方法区，全局字符串池中的String对象ls会持有这个char[]的地址，然后s指向ls的地址
     *
     * 如果是new String("123"); 实际字面量char[]={1,2,3}也是在堆中的一个地方，类加载期间全局字符串池中有一个String对象指向char[]，运行期执行new，在堆中新生成String对象，
     * 同样指向char[]，所以创建了2个对象
     *
     *
     *
     *
     **/

}
