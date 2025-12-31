package com.cloud.exam.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by dyl on 2022/5/18.
 */
public class StreamTest {

    public static void main(String[] args) {
        Set<Long> list=new HashSet<>();
        list.add(100L);
        list.add(200L);
        String name = list.stream().map(String::valueOf).collect(Collectors.joining(","));
        System.out.println(name);//输出 张三,李四
    }
}
