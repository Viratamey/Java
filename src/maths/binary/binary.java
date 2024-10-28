package maths.binary;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class binary {
    public static void main(String[] args) {

        int k=198;
        k = k << 2;
        System.out.println(k);

        int l=192;
        l = l >> 2;
        System.out.println(l);

        k=198;
//        k = k <<< 2;  not valid <<< for left shift only for right shift
        System.out.println(k);

        l=-36;
        l = l >>> 2;
        System.out.println(l);

        l=-36;
        l = l >> 2;
        System.out.println(l);
        System.out.println(Arrays.toString(ByteBuffer.allocate(4).putInt(l).array()));

    }
}
