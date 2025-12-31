package com.cloud.exam.vo;

import com.cloud.exam.model.exam.Question;
import lombok.Data;

import java.util.List;

/**
 * Created by dyl on 2021/06/21.
 */
@Data
public class QuestionVO extends Question {

    private Integer questionSort;
    private Integer questionNum;
    private Double difficultyType;
    private Double  questionScore;
    //每道题的答题时长
    private Integer questionTime;
    //选答试题判断是否已被使用,默认未占用
    private Boolean check = false;
    //当前试题所属试卷paperId
    private Long paperId;
    //试题所属活动examId
    private Long examId;
    //当前试题的下标
    private Integer questionIndex;
    //当前试题所属试卷得试卷类型 4 轮答  5 抢答 6 选答
    private Integer paperType;
    //当前题是否被判过 默认false
    private Boolean judge = false;
    //竞答活动  选答选中试题后当前消息标识
    private String only;
    //竞答活动时当前答题组的所有用户id
    private List<Long> userIds;
    //当前答题小组名称
    private String groupName;
    //小组分数
    private Double totalScore;
    //当前答题人的id;
    private Long userId;
}
