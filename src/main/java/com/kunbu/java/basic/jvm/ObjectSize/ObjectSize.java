package com.kunbu.java.basic.jvm.ObjectSize;

/**
 * byte和boolean是1字节
 * short和char是2字节
 * int和float是4字节
 * long和double是8字节
 * reference在32位系统占4字节，64位系统中占8字节
 **/
public class ObjectSize {

    /** 测试一*/
//    long l;

    /** 测试二 */
    byte b;
    byte b2;
    boolean bl;
    short s;
    double d;

    /** 测试三 */
//    boolean bl;
//    byte b;
//    byte b2;
//    short s;
//    char c;
//    float f;
//    int i;
//    long l;
//    double d;

    /** 测试不同数据下ref的位置 */
    Object ref;

    /**
     * 1. 对象大小 = 对象头 + 实际数据 + 补齐
     * 2. 补齐是每个对象结算一次，主要体现在复合对象中
     * 3. Unsafe算出的offset体现了，JVM会实时进行8字节对齐，即偏向于先满足8字节
     *    具体步骤：
     *      对象头12字节，所以优先找出 <= 4字节的数据进行分配，以减少补齐。
     *      如果能达到16字节，接下去就按照类型所占内存的大小降序分配，如果遇到相同大小的数据，按代码中过的顺序分配；
     *      如果不足16字节，剩下的数据肯定只有8字节的long或double，从offset=16开始分配
     *
     * 4. 引用出现在前面的情况只有一种：数据中只有8字节long/double和ref，ref刚好补齐对象头。否则都是在最后
     **/
    public static void main(String[] args) {
        /**
         * 测试一
         *  这里CPU一次直接操作数据的单位是8字节，而long也是8字节，对象头的偏移是12，如果直接跟在后面就
         *  需要两次读取该数据，所以JVM选择从16偏移开始
         *
         * l---offSet:16
         *
         * 测试二
         *  因为对象头12字节，所以优先找出 <= 4字节的数据进行分配，以减少补齐。
         *  如果能达到16字节，接下去就按照类型所占内存的大小降序分配，如果遇到相同大小的数据，按代码中过的顺序分配；
         *  如果不足16字节，剩下的数据肯定只有8字节的long或double，从offset=16开始分配
         *
         * b---offSet:14
         * b2---offSet:15
         * bl---offSet:24
         * s---offSet:12
         * d---offSet:16
         *
         * 测试三
         *  对象头12字节，优先找4字节的进行补齐，float和int，因为float在代码中靠前，优先选中，补齐16字节；
         *  然后再选8字节的数据，如果有的话；long和double选完后，只能选第二大的int，这时还有4字节空缺，继续找匹配的；
         *  发现有2字节的short和char，这轮8字节结束；开启下一轮，只剩boolean和byte共2位，此时大小时42字节，还需6字节补齐整个对象
         *
         * bl---offSet:40
         * b---offSet:41
         * s---offSet:36
         * c---offSet:38
         * f---offSet:12
         * i---offSet:32
         * l---offSet:16
         * d---offSet:24
         *
         *  如果加上引用，为何不是从42而是44偏移位置 todo
         * ref---offSet:44
         **/
        UnsafeUtil.getFiledOffset(ObjectSize.class);
    }

}
