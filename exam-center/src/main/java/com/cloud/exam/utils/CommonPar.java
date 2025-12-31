package com.cloud.exam.utils;

import java.util.HashMap;
import java.util.Map;

public class CommonPar {

    // 试题类型
    public static Map<String, Object> question_type = new HashMap<>();

    static {
        //      题型（1、单选；2：多选；3：判断；4：填空；5：简答；6：问答；7：情报分析（考试题）；8：影像处理；9：连线题；10：标注题；11：报告整编题(实操训练)；12：地图制图整饰题）
        question_type.put("1","单选题");
        question_type.put("2","多选题");
        question_type.put("3","判断题");
        question_type.put("4","填空题");
        question_type.put("5","简答题");
        question_type.put("6","问答题");
        question_type.put("7","情报分析题");
        question_type.put("8","影像处理题");
        question_type.put("9","连线题");
        question_type.put("10","标注题");
        question_type.put("11","报告整编题");
        question_type.put("12","地图制图整饰题");
    }
};



