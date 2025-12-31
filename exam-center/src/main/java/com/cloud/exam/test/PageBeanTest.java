package com.cloud.exam.test;

import com.cloud.exam.utils.PageBean;
import com.cloud.exam.vo.DrawResultVO;
import io.swagger.models.auth.In;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by dyl on 2022/5/17.
 */
public class PageBeanTest {

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("李");
        list.add("王");
        list.add("王");
        list.add("B");
        list.add("A");
        list.add("A");
        list.add("a");
        list.add("李1");
        list.add("王2");
        list.add("王3");
        list.add("B4");
        list.add("A5");
        list.add("A6");
        list.add("a6");
        List<String> collect1 = list.stream().skip((1 - 1) * 10).limit(10).collect(Collectors.toList());
        PageBean pageBean1 = PageBean.getPageBean(1, 10, list.size(), collect1);
        System.out.println(pageBean1);
    }
}
