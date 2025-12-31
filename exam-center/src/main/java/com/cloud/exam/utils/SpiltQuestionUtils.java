package com.cloud.exam.utils;

import com.cloud.exam.model.exam.DrawResult;
import com.cloud.exam.vo.QuestionVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by dyl on 2021/06/30.
 * 轮答试卷给每个小组分配试题
 */
public class SpiltQuestionUtils {

    public static HashMap<String, Object> spiltQuestion(List<DrawResult> drawResultList, List<QuestionVO> questionVOList) {
        //试题id-->人员id
        HashMap<String, Object> map = new LinkedHashMap<>();
        List<List<QuestionVO>> ll = new ArrayList<>();
        List<Long> userIds = new ArrayList<>();
        for (DrawResult dr : drawResultList) {
            //List list = new ArrayList();
            //map.put(dr.getUserId(), list);
            userIds.add(dr.getUserId());
        }
        Integer size1 = drawResultList.size();
        Integer size2 = questionVOList.size();
        int middle = size2 / size1;
        if (size2 % size1 > 0) {
            middle++;
        }
        //分割试题集合
        for (int i = 0; i < middle; i++) {
            //开始位置
            int fromIndex = i * size1;
            //结束位置
            int toIndex = (i + 1) * size1 < size2 ? (i + 1) * size1 : size2;
            ll.add(questionVOList.subList(fromIndex,toIndex));
        }
        //给每个学生分题
        for (int i = 0;i<ll.size();i++) {
            List<QuestionVO> questionVOS = ll.get(i);
            for (int j = 0;j<questionVOS.size();j++){
                Long userId = userIds.get(j);
                map.put(questionVOS.get(j).getId().toString(),userId);
            }
        }
        return map;
    }

    public static HashMap<Integer, Integer> spiltQuestiontEST(List<Integer> drawResultList, List<Integer> questionVOList) {
        //试题id-->人员id
        HashMap<Integer, Integer> map = new LinkedHashMap<>();

        List<Integer> userIds = new ArrayList<>();
        for (Integer dr : drawResultList) {
            //List list = new ArrayList();
            //map.put(dr, list);
            userIds.add(dr);
        }

        List<List<Integer>> ll = new ArrayList<>();
        Integer size1 = drawResultList.size();
        Integer size2 = questionVOList.size();
        int middle = size2 / size1;
        if (size2 % size1 > 0) {
            middle++;
        }
        for (int i = 0; i < middle; i++) {
            //开始位置
            int fromIndex = i * size1;
            //结束位置
            int toIndex = (i + 1) * size1 < size2 ? (i + 1) * size1 : size2;
            ll.add(questionVOList.subList(fromIndex,toIndex));
        }
        for (int i = 0;i<ll.size();i++) {
            List<Integer> questionVOS = ll.get(i);
            for (int j = 0;j<questionVOS.size();j++){
                Integer userId = userIds.get(j);
                map.put(questionVOS.get(j),userId);
            }
        }
        System.out.println(map);
        return map;
    }
}