package com.cloud.exam.vo;

import com.cloud.exam.model.exam.DrawResult;
import lombok.Data;

import java.util.List;

/**
 * Created by dyl on 2021/04/27.
 */
@Data
public class DrawResultVO extends  DrawResult{
    private String userName;
    private String examName;
    private String departName;
    private String paperName;
    private String placeName;
    private String positionName;
    //考试类型
    private Integer examType;
    //考试开始时间
    private String examDate;
    //考核时长
    private String examTime;
    //活动状态
    private Integer examStatus;
    //参加考试人数
    private Integer userCount;
    //考试座位号中文
    private String groupName;
    //竞答活动考生提交的答案
    private Object stuAnswers;
    //竞答活动每道题的答题时长
    private Integer questionTime;


    // 录屏地址
    private List<String> recordPathList;

    private Long collectionId;
    // 试题id
    private Long questionId;

}
