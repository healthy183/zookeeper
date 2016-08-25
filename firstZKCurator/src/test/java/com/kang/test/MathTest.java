package com.kang.test;

import java.util.Random;

/**
 * Created by Healthy on 2015/10/6.
 */
public class MathTest {

    public static void main(String[] args) {

        Random  random = new Random();

        int j =  random.nextInt(1 << 3);

        //System.out.println(j);

        System.out.println(1 << 4);

        System.out.println(4 >> 2);

        // << 左移 表示1乘于2的4次方
        // >>右移  表示4除于2的3次方
        int i = 1000 * Math.max(1, random.nextInt(1 << (2 + 1)));
        System.out.println(i);



    }
}
