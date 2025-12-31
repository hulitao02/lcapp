package com.cloud.exam.test;

import com.cloud.exam.model.exam.StudentAnswer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by dyl on 2022/5/17.
 */
public class StreamListTest {
    public static void main(String[] args) {
        List<StudentAnswer> l1 = new ArrayList<>();
        StudentAnswer s1 = new StudentAnswer();
        s1.setStudentId(5L);
        s1.setQuestionId(566L);
        s1.setId(1L);
        StudentAnswer s2 = new StudentAnswer();
        s2.setStudentId(5L);
        s2.setQuestionId(566L);
        s2.setId(2L);
        StudentAnswer s3 = new StudentAnswer();
        s3.setStudentId(6L);
        s3.setQuestionId(566L);
        s3.setId(3L);
        l1.add(s1);
        l1.add(s2);
        l1.add(s3);
        List<StudentAnswer> collect = l1.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(
                o -> o.getStudentId() + ";" + o.getQuestionId()))), ArrayList::new)
        );

        System.out.println(collect);
    }
}
