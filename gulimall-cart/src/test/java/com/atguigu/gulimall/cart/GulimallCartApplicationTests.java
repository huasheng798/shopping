package com.atguigu.gulimall.cart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
class GulimallCartApplicationTests {

    @Test
    void contextLoads() {
        int num[] = {88, 66, 99};
        //7, 6
        for (int i = 0; i < num.length - 1; i++) {
            for (int j = 0; j < num.length - i - 1; j++) {
                if (num[j] > num[j + 1]) {
                    int temp = num[j + 1];
                    num[j + 1] = num[j];
                    num[j] = temp;
                }
            }
        }
        Arrays.stream(num).forEach(System.out::println);

    }


    @Test
    public void commonBinarySearch() {
        int[] arr = {11, 22, 33, 44};
        int key = 33;
        int hear = 0;
        int end = arr.length - 1;
        int middle = 0;

        if (key < arr[hear] || key > arr[end] || hear < end) {

        }
        while (hear <= end) {
            middle = (hear + end) / 2;//首先把头加尾除于2拿出中间数
            if (arr[middle] > key) {
                //说明关键字在右区域
                end = middle - 1;
            } else if (arr[middle] < key) {
                hear = middle + 1;
            } else {
                //找到了
                System.out.println(arr[middle]);
                break;
            }
        }


    }


}
