package com.cloud.exam.utils;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by dyl on 2022/08/22.
 */
public class ListUtils<T> {

    /**
     * 获取集合中的共同的元素
     * @param list
     * @return
     */
    public static Set<Long> getSameElement(List<Long> list){
        List<Long> collect = list.stream().filter(kpId -> list.indexOf(kpId) != list.lastIndexOf(kpId)).distinct().collect(Collectors.toList());
        Set collect1 = collect.stream().collect(Collectors.toSet());
        return collect1 ;
    }

    /**
     * 获取多个集合中的共同的元素
     * @param list
     * @return
     */
    public static Set<String> getSameElementBylists(List<Set<String>> list){
        Set resultSet = list.get(0);
        for (Set set:list) {
            if(CollectionUtils.isEmpty(set)){
                return new HashSet<>();
            }
            resultSet.retainAll(set);
        }
        return resultSet ;
    }

    public static void main(String[] args) {
//        List<Set<Long>> ss = new ArrayList<>();
//        Set<Long> set0 = new HashSet<>();
//        Set<Long> set1 = new HashSet<>();
//        set1.add(1L);
//        set1.add(2L);
//        Set<Long> set2 = new HashSet<>();
//        set2.add(4L);
//        set2.add(2L);
//        Set<Long> set3 = new HashSet<>();
//        set3.add(1L);
//        set3.add(9L);
//        set3.add(2L);
//        ss.add(set0);
//        ss.add(set1);
//        ss.add(set2);
//        ss.add(set3);
//        Set<Long> sameElementBylists = getSameElementBylists(ss);
//        System.out.println(sameElementBylists);
    }
}
