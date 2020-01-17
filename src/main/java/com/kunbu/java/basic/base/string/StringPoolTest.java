package com.kunbu.java.basic.base.string;

import com.kunbu.java.basic.base.ObjectAddressUtil;

/**
 * 字符串（池）被大家经常误解和搞不清楚的原因：
 * JVM为了提高性能和减少内存开销，在实例化字符串常量的时候进行了一些优化，即减少在JVM中创建的字符串的数量，专门维护了一个全局字符串常量池。
 *
 * 说白了，编译期就能确定的在类加载时统统去了全局字符串池，即全局字符串池存的是已确定的字面量的引用；而编译期不能确定的，会在运行期执行的时候按照每个实例对象进行分配
 *
 * PS：全局字符串池JDK6时在Pem区，JDK1.7移到了堆中，从测试结果看，本机的JDK1.8也是在堆中
 *
 *
 *
 * 三种池子：
 *  1. Class文件常量池（Class Constant Pool）
 *      在编译阶段，存放编译器生成的各种字面量(Literal)和符号引用(Symbolic References)
 *          字面量：文本字符串/final常量值/基本数据类型值
 *          符号引用：类和接口的全限定名/字段的名称和描述符/方法的名称和描述符
 *
 *
 *  2. 全局驻留字符串常量池（string Pool / string Literal Pool） TODO 池的大小及实现 老版本是1009 老版本fastJson就是因为过度使用intern而造成性能问题
 *      类加载期间，经过加载，验证/准备阶段之后，在堆中（JDK1.7）的一块区域生成字符串对象实例，然后将该实例的引用值存到全局字符串池中（intern注释中提到的pool）。
 *
 *      在HotSpot VM里实现的string pool功能的是一个StringTable类，它是一个哈希表，里面存的是驻留字符串（字面量）的引用，
 *      也就是说在堆中的某些字符串实例被这个StringTable引用之后就等同被赋予了”驻留字符串”的身份。这个StringTable在每个HotSpot VM的实例只有一份，被所有的类共享。
 *
 *    关键点1：pool中存的是引用而不是具体的实例对象，具体的实例对象是在堆中开辟的一块空间（来源不定）。
 *    关键点2：堆中的字符串对象被赋予”驻留字符串“身份，这在理解testStringLink()第二段代码结果很重要，即驻留字符串的引用是普通实例对象的引用
 *
 *
 *  3. 运行时常量池（Runtime Constant Pool）TODO 具体哪个阶段
 *      类加载到内存后，JVM会将class常量池中的内容存放到运行时常量池（方法区），由此可知，运行时常量池也是每个类都有一个。
 *
 *      运行时常量池存的并不是对象的实例，而是对象的符号引用值。经过解析（resolve）之后，会把符号引用替换为直接引用，TODO https://www.zhihu.com/question/55994121
 *      解析的过程会去查询全局字符串池，以保证运行时常量池所引用的字符串与全局字符串池中所引用的是一致的。
 *
 *      动态性：不只是包括在编译的时候产生的常量，也可以在运行的时候扩展，如动态代理
 *
 *    关键点：动态生成的String对象ns调用intern()方法，查找全局字符串池否有相同Unicode的字符串常量，如果有，则返回其的引用，如果没有，则在池中增加该对象的引用值。
 *           该引用值怎么增加：动态生成的String对象hs位于堆中，然后在全局字符串池中存进这个引用hs，也就是说hs被赋予了驻留字符串身份
 *
 *
 *  小结：全局字符串池只存有引用，具体实例可以来自解析时的字面量，动态生成的String所在的运行时常量
 *
 *
 *
 * String的不可变性：
 *  1. final String是保证String不可被继承
 *  2. 体现在内部char[]是final的，即字符串内容不可变更，初始化时就确定了，并且不提供修改方法，如果调用replace等方法直接返新的String
 * 	PS：可以利用反射特性破坏String的不可变性      https://segmentfault.com/a/1190000019865846
 *
 *
 * 分析String的创建过程按编译期到运行期的步骤进行:（因为涉及到运行时数据区域和类加载，需参考那两块知识）
 *  1. 编译期：java文件编译成class文件
 *  2/3. 运行期（类加载）：加载 -> 连接（验证/准备/解析）-> 初始化
 *
 *
 *
 * 参考：
 *  R大：https://www.iteye.com/blog/rednaxelafx-774673
 *  英文文献：https://ibytecode.com/blog/string-literal-pool/
 *  http://tangxman.github.io/2015/07/23/java-memory-allocation/
 *  http://tangxman.github.io/2015/07/27/the-difference-of-java-string-pool/
 *
 *  https://blog.csdn.net/qq_26222859/article/details/73135660
 *  https://www.cnblogs.com/xuxinstyle/p/9526210.html
 *  https://www.hollischuang.com/archives/1551
 *  https://www.zhihu.com/question/55994121
 *  https://www.hollischuang.com/archives/2517
 *  https://blog.csdn.net/weixin_40999907/article/details/87907083
 *
 * @author kunbu
 **/
public class StringPoolTest {

    /**
     * 使用诸如String str = “abc”并不能保证一定会创建对象，有可能只是指向一个已经创建好的对象，而通过new()方法是能保证每次会创建一个新对象。
     *
     **/
    public static void testLiteralAndNew() {
        /**
         * 字面量形式创建：
         *  1. 编译期：hello会被放在Class文件的Constant Pool中（其中还包含符号引用literalStr）
         *
         *      0 ldc #2 <hello>
         *      2 astore_0
         *
         *  2. 类加载：直接在解析阶段生成一个实例对象放到堆中，然后全局字符串池中存放该实例的引用。
         *
         *
         * 结论：栈上声明的变量literalStr指向的是全局字符串池中的hello的引用ls
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
         *  2. 类加载：在解析阶段，生成一个String实例对象放到堆中，然后字符串池(String pool)中存放该实例的引用ls
         *
         *  3. 运行期：invokespecial被执行，调用String的初始化方法，在堆中创建对象hs，该对象指向全局字符串池中的引用ls
         *
         *
         *  结论：newStr指向堆中的地址hs，而hs指向全局字符串池中的ls，而newStr2在堆中是另一个地址hs2，hs和hs2都指向全局字符串池中的ls
         *
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
     * 1. 全局字符串池中的String引用所指向的char[]在单独区域中
     * 2. 运行时创建的String对象所指向的char[]在每个类所在的堆内存中
     *
     * 从打印的地址看出：
     *      [1] 创建字面量literal，在类加载-连接期间直接进到全局字符串池，创建驻留String，返回池中引用，char[]也只有一份
     *      [2] new String("abc“)，字面量abc和[1]一样；new阶段，在堆中创建String对象，指向全局字符串池中的abc，但返回堆中引用
     *      [3] 编译期无法确定字面量123，只能到运行期的时候在堆中创建String对象，实例数据char[]也不同：0xd6bdf238 / 0xd6bf24e0
     *      [4] 同一个类下，通过运行期创建字符串，即使123相同，也是分配在不同区域
     **/
    public static void testStringAddress() {

        /**
         * StringA -> "literal" Current address: 3592047080
         * java.lang.String@7fbe847cd object externals:
         *           ADDRESS       SIZE TYPE             PATH                           VALUE
         *          d61a49e8         24 java.lang.String                                (object)
         *          d61a4a00         32 [C               .value                         [l, i, t, e, r, a, l]
         *
         *
         * StringA -> new String("abc") Current address: 3602746608
         * java.lang.String@36aa7bc2d object externals:
         *           ADDRESS       SIZE TYPE             PATH                           VALUE
         *          d6bd8cf0         24 java.lang.String                                (object)
         *          d6bd8d08         24 (something else) (somewhere else)               (something else)
         *          d6bd8d20         24 [C               .value                         [a, b, c]
         *
         *
         * StringA -> String.valueOf(123) Current address: 3602772560
         * java.lang.String@182decdbd object externals:
         *           ADDRESS       SIZE TYPE             PATH                           VALUE
         *          d6bdf238         24 [C               .value                         [1, 2, 3]
         *          d6bdf250         24 java.lang.String                                (object)
         **/
        StringA.String();

        /**
         * StringB -> "literal" Current address: 3592047080
         * java.lang.String@7fbe847cd object externals:
         *           ADDRESS       SIZE TYPE             PATH                           VALUE
         *          d61a49e8         24 java.lang.String                                (object)
         *          d61a4a00         32 [C               .value                         [l, i, t, e, r, a, l]
         *
         *
         * StringB -> new String("abc") Current address: 3602825256
         * java.lang.String@4361bd48d object externals:
         *           ADDRESS       SIZE TYPE             PATH                           VALUE
         *          d6bd8d20         24 [C               .value                         [a, b, c]
         *          d6bd8d38      78576 (something else) (somewhere else)               (something else)
         *          d6bec028         24 java.lang.String                                (object)
         *
         *
         * StringB -> String.valueOf(123) Current address: 3602851064
         * java.lang.String@53bd815bd object externals:
         *           ADDRESS       SIZE TYPE             PATH                           VALUE
         *          d6bf24e0         24 [C               .value                         [1, 2, 3]
         *          d6bf24f8         24 java.lang.String                                (object)
         **/
        StringB.String();

        /**
         * C Current address: 3602871600
         * java.lang.String@7637f22d object externals:
         *           ADDRESS       SIZE TYPE             PATH                           VALUE
         *          d6bf7518         24 [C               .value                         [1, 2, 3]
         *          d6bf7530         24 java.lang.String                                (object)
         *
         *
         * D Current address: 3602871648
         * java.lang.String@762efe5dd object externals:
         *           ADDRESS       SIZE TYPE             PATH                           VALUE
         *          d6bf7548         24 [C               .value                         [1, 2, 3]
         *          d6bf7560         24 java.lang.String                                (object)
         **/
        String strC = String.valueOf(123);
        String strD = String.valueOf(123);
        ObjectAddressUtil.printAddressByJDK("C", strC);
        ObjectAddressUtil.printAddressByJDK("D", strD);
    }

    /**
     * 不同版本JDK下，intern的表现不同
     *
     * 作用：返回[全局驻留字符串池]中引用，如果池中已存在，则直接返回池中引用，否则在池中"创建"并返回其引用（为了减少String对象的创建）
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
         *                                                      ls                    char[]={world}
         *     newStr                    hs                     ls                    char[]={world}
         *     newStrIntern                                     ls                    char[]={world}
         **/
        // 生成1个对象，heap中的String对象hs，指向char[]={world}，因为world已经在全局字符串池中存在引用，所以不再创建。newStr指向hs
        String newStr = new String("world");
        // 检查全局字符串池，返回newStr指向的hs所对应的字面量world的引用ls
        String newStrIntern = newStr.intern();

        /**
         *        栈                     堆                    全局字符串池
         *     intern         hs（创建了但是没被引用所以会被GC）      ls                    char[]={world}
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
        String intern = new String("world").intern();

        ObjectAddressUtil.printAddressByJDK("literalStr", literalStr);
        ObjectAddressUtil.printAddressByJDK("literalStrIntern", literalStrIntern);
        ObjectAddressUtil.printAddressByJDK("newStr", newStr);
        ObjectAddressUtil.printAddressByJDK("newStrIntern", newStrIntern);
        ObjectAddressUtil.printAddressByJDK("intern", intern);

    }

    /**
     * 字符串的拼接只有编译期确定的，或者是类解析能确定的，才会进入全局字符串池，下面的1，3可以，等效于String ab = "ab"
     *      1. 直接字面量拼接："a" + "b"
     *      2. 包含引用的拼接："a" + new String("b") / "a" + String b = "b"
     *      3. final修饰的拼接："a" + final b = "b"
     *      4. 方法调用拼接："a" + getB() = "b"
     *
     **/
    public static void testStringLink() {

        // 生成两个对象，全局字符串池中的String对象ls，指向char[]={1}；堆中的String对象hs，指向char[]={11}，此时全局字符串池中没有11的引用
        // 题外话，2个匿名类因为没有引用，所以会被GC
        String s1 = new String("1") + new String("1");
        // TODO 将s1指向的hs所对应的11放入全局字符串池，因为池中不存在，本来是需要生成的，但是堆中已经存在11了，也就是hs，可以直接拿来用（即hs被赋予了驻留字符串的身份，nb了）
        String si = s1.intern();
        // 显示声明11，会去检查全局字符串池，发现已经存在11（hs）
        String s2 = "11";
        // s1指向hs，s2指向ls，ls指向hs
        System.out.println(s1 == s2);

        ObjectAddressUtil.printAddressByJDK("s1", s1);
        ObjectAddressUtil.printAddressByJDK("s2", s2);
        ObjectAddressUtil.printAddressByJDK("si", si);


        // 生成两个对象，全局字符串池中的String对象ls，指向char[]={2}；heap中的String对象hs，指向char[]={22}，此时全局字符串池中没有22的引用
        String s3 = new String("2") + new String("2");
        // 显示创建22：全局字符串池中String对象ls2，指向char[]={22}，s4指向ls2
        String s4 = "22";
        // 将s3指向的hs对应的22放入全局字符串池，但是池中已经存在了22引用（驻留字符串ls2），所以此句无效
        String si2 = s3.intern();
        // s3指向hs，s4指向ls
        System.out.println(s3 == s4);

        ObjectAddressUtil.printAddressByJDK("s3", s3);
        ObjectAddressUtil.printAddressByJDK("s4", s4);
        ObjectAddressUtil.printAddressByJDK("si2", si2);


        // 全局字符串池要保存的是已确定的字面量值
        String s5 = "33";
        String s6 = "44";
        String s7 = "3344";
        // 纯字面量和字面量的拼接，会把拼接结果作为常量保存到全局字符串池
        String s8 = "33" + "44";
        // 非字面量拼接，整个拼接操作会被编译成StringBuilder.append，这种情况编译器是无法确定其值的，只有在运行期才可以。
        String s9 = s5 + s6;
        final String s10 = "33";
        // final修饰的变量，在编译期被解析为常量（即s10被替换为33）
        String s11 = s10 + "44";
        ObjectAddressUtil.printAddressByJDK("s5", s5);
        ObjectAddressUtil.printAddressByJDK("s6", s6);
        ObjectAddressUtil.printAddressByJDK("s7", s7);
        ObjectAddressUtil.printAddressByJDK("s8", s8);
        ObjectAddressUtil.printAddressByJDK("s9", s9);
        ObjectAddressUtil.printAddressByJDK("s10", s10);
        ObjectAddressUtil.printAddressByJDK("s11", s11);

    }

    /**
     * 测试第3点常量池的小结：全局字符串池只存有引用，具体实例可以来自解析时的字面量，动态生成的String所在的堆中的实例，
     *
     **/
    public static void testStringPool() {
        // 123实例在堆中的一个地方，引用在全局字符串池中持有，new String实例持有全局字符串池中引用的地址
        String s2 = new String("123");
        // 123 在类的堆区域中
        String s3 = String.valueOf(123);
        ObjectAddressUtil.printAddressByJDK("s2", s2);
        ObjectAddressUtil.printAddressByJDK("s3", s3);

        // 456实例在类的堆区域中
        String s4 = String.valueOf(456);
        // 456被加入到运行时常量池中，顺带在全局字符串持有引用 TODO 原来在堆中的实例，现在被转移到方法区中了？还是说方法区也有一份？
        String s5 = s4.intern();
        // 从打印结果看，s4和s5地址一样，说明intern()返回的是堆中的地址
        ObjectAddressUtil.printAddressByJDK("s4", s4);
        ObjectAddressUtil.printAddressByJDK("s5", s5);


    }


    public static void main(String[] args) {

//        testLiteralAndNew();
//
//        testIntern();

//        testStringLink();

//        testStringAddress();

//        testStringPool();


        String s1 = new String("11");
        String si = s1.intern();
        String s2 = "11";
        System.out.println(s1 == s2);

        ObjectAddressUtil.printAddressByJDK("s1", s1);
        ObjectAddressUtil.printAddressByJDK("s2", s2);
        ObjectAddressUtil.printAddressByJDK("si", si);
    }


}
