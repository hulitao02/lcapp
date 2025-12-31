package com.cloud.exam.vo;

import com.cloud.exam.model.exam.Question;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;
import java.util.List;

/**
 * 组卷规则Bean
 *
 * @author: md
 *
 */
@Data
public class RuleBeanVO implements Serializable {

    private static final long serialVersionUID = -5504552551190052308L;
    /**
     * 规则id
     */
    private Long id;

    /**
     * 试卷名称
     */
    private String paperName;

    /**
     * 试卷描述
     */
    private String paperDescribe;


    /**
     * 答题时间
     */
    private Integer paperTime;


    /**
     * 组卷类型
     * 1：理论  2：情报方向  3：实操
     */
    private Integer paperType;
    // 1：自测试卷 2：考核试卷
    private Integer paperFlg;


    /**
     * 对应的活动id
     */
    private Long examId;

    /**
     * 对应的部门id
     */
    private Long departId;
    /**
     * 试卷总分
     */
    private Double totalMark;
    /**
     * 试卷期望难度系数
     */
    private Double difficulty;
    /**
     * 单选题数量
     */
    private Integer singleChoiceNum;
    /**
     * 选择题单个分值
     */
    private Double singleChoiceScore;

    //竞答试卷中分难度随机选题
    private Integer sigleSimpleNum;
    private Double  singleSimpleScore;
    /**
     * 多选题数量
     */
    private Integer multipleChoiceNum;

    /**
     * 多选题单题分值
     */
    private Double multipleChoiceScore;

    /**
     * 填空题数量
     */
    private Integer completeNum;
    /**
     * 填空题单个分值
     */
    private Double completeScore;
    /**
     * 判断题数量
     */
    private Integer judgeNum;
    /**
     * 判断题单个分值
     */
    private Double judgeScore;


    /**
     * 简答题
     */
    private Integer briefNum;

    private Double briefScore;

    /**
     * 论述题
     */
    private Integer discussNum;

    private Double discussScore;

    /**
     * 情报分析题
     */
    private Integer intelAnalysisNum;

    private Double intelAnalysisScore;

    /**
     * 实操题
     */
    private Integer practiceNum;

    private Double practiceScore;

    /**
     * 连线题
     */
    private Integer connectNum ;
    private Double connectScore ;


    /**
     * 试卷包含的知识点id
     */
    private List<String> kpIds;
    // 试卷包含的判读类型
    private List<String>pdTypes;
    private String pdTypestr;
    /**
     * 规则创建时间
     */
    private Date createTime;

    /**
     * 试卷包含的情报方向
     */
    private List<Long> intDirectIds;


    /**
     * 组卷方式（0 随机选题 1 分难度选题）
     */

    private Integer generateType;


    /**
     * 所选试题封装
     */
    private List<QuestionTypeVO> qlist;

    /**
     * 是否加时试卷(0 否 ，1 是)
     */

    private Integer isOvertime;


    /**
     * 手动创建试卷选择参数
     */
    //Long->试题id,  String->试题难度&试题类型
    private List<Question> questions;
    private Double simpleScore;
    private Double nomalScore;
    private Double middleScore;
    private Double lessHardScore;
    private Double moreHardScore;

    //已经被使用的试题id
    private List<Long> qIds;
    //单个试题试卷难度
    private Double questionDifficulty;


    /**
     *
     */
    /*public void setKpIds(String pointIds) {
        // 是否是表单传过来的数据
        if (pointIds.endsWith("#")){
            pointIds = pointIds.substring(0, pointIds.lastIndexOf("#"));
        }
        // 使用HashSet去重
        this.kpIds = new ArrayList<>(new HashSet<>(Arrays.asList(pointIds.split("#"))));
    }


    public void  setIntDirectIds(String intDirectIds) {
        // 是否是表单传过来的数据
        if (intDirectIds.endsWith("#")){
            intDirectIds = intDirectIds.substring(0, intDirectIds.lastIndexOf("#"));
        }
        // 使用HashSet去重
        this.kpIds = new ArrayList<>(new HashSet<>(Arrays.asList(intDirectIds.split("#"))));
    }*/
}
