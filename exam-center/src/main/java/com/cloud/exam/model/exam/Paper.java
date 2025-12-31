package com.cloud.exam.model.exam;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.cloud.exam.vo.RuleBeanVO;
import com.cloud.utils.Validator;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 遗传算法中的个体，即一套可能的试卷。对试卷进行编码，而不是对整个题库编码
 *
 * @author md
 */
@Data
public class Paper implements Serializable {

    private static final long serialVersionUID = 3748424532509166353L;
    /**
     * 个体id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Long  id;

    /**
     * 试卷名称
     */
    @NotBlank(message = "试卷名不能为空")
    @Length(max = 100,message = "名字长度最大100")
    private String paperName;

    /**
     * 适应度
     */
    private transient double adaptationDegree = 0.00;
    /**
     * 知识点覆盖率
     */
    private transient double kPCoverage = 0.00;

    private transient double iDCoverage = 0.00;
    /**
     * 试卷总分
     */
    private double totalScore;
    /**
     * 试卷难度系数
     */
    private double difficulty;
    // 1:训练试卷 2：考核试卷
    private int paperFlg;

    /**
     * 考试时间
     */
    private Integer totalTime;

    /**
     * 试卷说明【非必填项】
     */
    private String describe;

    /**
     * 试卷类型【理论、情析、实操】
     */
    private Integer type;

    /**
     * 试卷竞答类型 （0必答1选答2抢答）
     *//*
    private Integer vieanswerType;*/


    /**
     * 创建人
     */
    private Long creator;

    private Date createTime;

    private Date updateTime;

    private Integer status;

    private  transient Integer current;

    private transient Integer size;

    /**
     * 试题列表
     */
    private transient List<QuestionManage> questionList = new ArrayList<>();

    public Paper(int size) {
        for (int i = 0; i < size; i++) {
            questionList.add(null);
        }
    }

    public Paper() {
        super();
    }

    /**
     * 计算试卷总分
     *
     * @return
     */
    public double getTotalScore() {
        if (totalScore == 0) {
            double total = 0;
            for (QuestionManage question : questionList) {
                if (!Validator.isEmpty(question)) {
                    if (!Validator.isEmpty(question.getScore())) {
                        total += question.getScore();
                    }
                }
            }
            totalScore = total;
        }
        return totalScore;
    }

    /**
     * 计算试卷个体难度系数 计算公式： 每题难度*分数求和除总分
     *
     * @return
     */
    public double getDifficulty() {
        if (difficulty == 0 ) {
            double _difficulty = 0;
            int count = 0;
            for (QuestionManage question : questionList) {
                if (question != null) {
                    if (null == question.getScore()) {
                        question.setScore(0d);
                    }
                    _difficulty += question.getScore() * question.getDifficulty();
                }
            }
            difficulty = _difficulty / getTotalScore();
        }
        return difficulty;
    }

    /**
     * 获取试题数量
     *
     * @return
     */
    public int getQuestionSize() {
        return questionList.size();
    }

    /**
     * 计算知识点覆盖率 公式为：个体包含的知识点/期望包含的知识点
     *
     * @param rule
     */
    public void setKpCoverage(RuleBeanVO rule) {
        if (kPCoverage == 0) {
            Set<String> result = new HashSet<>();
            result.addAll(rule.getKpIds());
            Set<String> another = new HashSet<>();
            for (QuestionManage question : questionList) {
                if (question != null) {
                    another.add(question.getKpId());
                }
            }
            // 交集操作
            result.retainAll(another);
            kPCoverage = result.size() / rule.getKpIds().size();
        }
    }

    /**
     * 计算情报方向覆盖率 公式为：个体包含的情报方向点/期望包含的情报方向点
     *
     * @param rule
     */
    public void setIDCoverage(RuleBeanVO rule) {
        if (kPCoverage == 0) {
            Set<Long> result = new HashSet<>();
            result.addAll(rule.getIntDirectIds());
            Set<String> another = questionList.stream().map(question -> String.valueOf(question.getDirectId())).collect(Collectors.toSet());
            // 交集操作
            result.retainAll(another);
            iDCoverage = result.size() / rule.getIntDirectIds().size();
        }
    }

    /**
     * 计算个体适应度 公式为：f=1-(1-M/N)*f1-(1-A/B)*f2-|EP-P|*f3
     * 其中M/N为知识点覆盖率，A/B为情报方向覆盖率,EP为期望难度系数，P为种群个体难度系数，f1为知识点分布的权重
     * ，f2为难度系数所占权重。当f1=0时退化为只限制试题难度系数，当f2=0时退化为只限制知识点分布
     *
     * @param rule 组卷规则
     * @param f1   知识点分布的权重
     * @param f2   难度系数的权重
     */
    public void setAdaptationDegree(RuleBeanVO rule, double f1, double f2,double f3) {
        if (adaptationDegree == 0) {
            adaptationDegree = 1 - (1 - getkPCoverage()) * f1 - (1 - getIDCoverage()) * f2-Math.abs(rule.getDifficulty() - getDifficulty()) * f3;
        }
    }

    public boolean containsQuestion(QuestionManage question) {
        if (question == null) {
            for (int i = 0; i < questionList.size(); i++) {
                if (questionList.get(i) == null) {
                    return true;
                }
            }
        } else {
            for (QuestionManage aQuestionList : questionList) {
                if (aQuestionList != null) {
                    if (aQuestionList.equals(question)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 增加问题
     *
     * @param question
     */
    public void saveQuestion(int index, QuestionManage question) {
        this.questionList.set(index, question);
        this.totalScore = 0;
        this.adaptationDegree = 0;
        this.difficulty = 0;
        this.kPCoverage = 0;
    }

    public void addQuestion(QuestionManage question) {
        this.questionList.add(question);
        this.totalScore = 0;
        this.adaptationDegree = 0;
        this.difficulty = 0;
        this.kPCoverage = 0;
    }

    public QuestionManage getQuestion(int index) {
        return questionList.get(index);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getkPCoverage() {
        return kPCoverage;
    }

    public double getAdaptationDegree() {
        return adaptationDegree;
    }

    public List<QuestionManage> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<QuestionManage> questionList) {
        this.questionList = questionList;
    }

}
