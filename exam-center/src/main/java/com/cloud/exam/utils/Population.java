package com.cloud.exam.utils;

import cn.hutool.core.util.ObjectUtil;
import com.cloud.exam.model.exam.Paper;
import com.cloud.exam.model.exam.QuestionManage;
import com.cloud.exam.service.QuestionManageService;
import com.cloud.exam.vo.RuleBeanVO;
import lombok.NoArgsConstructor;
import org.bouncycastle.asn1.cmp.POPODecKeyChallContent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

/**
 * 种群，即多套试卷
 */
@Component
@NoArgsConstructor
public class Population {

    @Resource
    private QuestionManageService questionManageService;
    private static Population population;

    @PostConstruct
    public void init() {
        population = this;
        population.questionManageService = this.questionManageService;

    }

    /**
     * 试卷集合
     */
    private Paper[] papers;

    /**
     * 初始种群
     *
     * @param populationSize 种群规模
     * @param initFlag       初始化标志 true-初始化
     * @param rule           规则bean
     */
    public Population(int populationSize, boolean initFlag, RuleBeanVO rule) {
        papers = new Paper[populationSize];
        if (initFlag) {
            Paper paper;
            Random random = new Random();
            for (int i = 0; i < populationSize; i++) {
                paper = new Paper();
                paper.setId(i + 1L);
                /*while (paper.getTotalScore() != rule.getTotalMark()) {*/
                paper.getQuestionList().clear();
                List<String> kpString = rule.getKpIds();
                List<Long> idString = rule.getIntDirectIds();
                String pdStr[] = rule.getPdTypestr().split(",");
                List<String> pdString = new ArrayList<>();
                for (int k = 0; k < pdStr.length; k++) {
                    pdString.add("'" + pdStr[k] + "'");
                }
                rule.setPdTypes(pdString);
                // 单选题
                if (rule.getSingleChoiceNum() > 0) {
                    generateQuestion(1, random, rule.getSingleChoiceNum(), rule.getSingleChoiceScore(), kpString, idString,
                            "单选题数量不够，", paper, rule.getQIds(), rule.getPdTypes(),rule.getDifficulty());
                }
                if (rule.getMultipleChoiceNum() > 0) {
                    generateQuestion(2, random, rule.getMultipleChoiceNum(), rule.getMultipleChoiceScore(), kpString, idString,
                            "多选题数量不够，", paper, rule.getQIds(), rule.getPdTypes(),rule.getDifficulty());
                }
                // 判断题
                if (rule.getJudgeNum() > 0) {
                    generateQuestion(3, random, rule.getJudgeNum(), rule.getJudgeScore(), kpString, idString,
                            "判断题数量不够，", paper, rule.getQIds(), rule.getPdTypes(),rule.getDifficulty());
                }
                // 填空题
                if (rule.getCompleteNum() > 0) {
                    generateQuestion(4, random, rule.getCompleteNum(), rule.getCompleteScore(), kpString, idString,
                            "填空题数量不够，", paper, rule.getQIds(), rule.getPdTypes(),rule.getDifficulty());
                }
                //简答题
                if (rule.getBriefNum() > 0) {
                    generateQuestion(5, random, rule.getBriefNum(), rule.getBriefScore(), kpString, idString,
                            "简答题数量不够，", paper, rule.getQIds(), rule.getPdTypes(),rule.getDifficulty());
                }
                //论述题
                if (rule.getDiscussNum() > 0) {
                    generateQuestion(6, random, rule.getDiscussNum(), rule.getDiscussScore(), kpString, idString,
                            "问答题数量不够，", paper, rule.getQIds(), rule.getPdTypes(),rule.getDifficulty());
                }
                //情析题
                if (rule.getIntelAnalysisNum() > 0) {
                    generateQuestion(7, random, rule.getIntelAnalysisNum(), rule.getIntelAnalysisScore(), kpString, idString,
                            "情析题数量不够，", paper, rule.getQIds(), rule.getPdTypes(),rule.getDifficulty());
                }
                //实操题
                if (rule.getPracticeNum() > 0) {
                    generateQuestion(8, random, rule.getPracticeNum(), rule.getPracticeScore(), kpString, idString,
                            "实操题数量不够，", paper, rule.getQIds(), rule.getPdTypes(),rule.getDifficulty());
                }
                //连线题
                if (ObjectUtil.isNotNull(rule.getConnectNum()) && rule.getConnectNum() > 0) {
                    generateQuestion(9, random, rule.getConnectNum(), rule.getConnectScore(), kpString, idString,
                            "连线题数量不够，", paper, rule.getQIds(), rule.getPdTypes(),rule.getDifficulty());
                }
//                }
                // 计算试卷知识点覆盖率
                paper.setKpCoverage(rule);
                //计算情报方向覆盖率
                if (null != rule.getIntDirectIds() && rule.getIntDirectIds().size() > 0) {
                    paper.setIDCoverage(rule);
                }
                // 计算试卷适应度
                paper.setAdaptationDegree(rule, Global.KP_WEIGHT, Global.ID_WEIGHT, Global.DIFFCULTY_WEIGHt);
                papers[i] = paper;
            }
        }
    }


    private static void generateQuestion(int type, Random random, int questionNum, double score, List<String> kpString, List<Long> idString,
                                         String errorMsg, Paper paper, List<Long> qIds, List<String> pdTypes,Double diff) {
        if (questionNum > 0 && score <= 0) {
            throw new IllegalArgumentException("请设置试题分数。");
        }
        QuestionManage[] singleArray = population.questionManageService.getQuestionArray(type, kpString
                , idString, qIds, pdTypes,diff);
        if (singleArray.length < questionNum) {
            throw new IllegalArgumentException(errorMsg + "仅找到" + singleArray.length + "道试题");
        }
        QuestionManage tmpQuestion;
        for (int j = 0; j < questionNum; j++) {
            int index = random.nextInt(singleArray.length - j);
            // 初始化分数
            singleArray[index].setScore(score);
            paper.addQuestion(singleArray[index]);
            // 保证不会重复添加试题
            tmpQuestion = singleArray[singleArray.length - j - 1];
            singleArray[singleArray.length - j - 1] = singleArray[index];
            singleArray[index] = tmpQuestion;
        }
    }

    /**
     * 获取种群中最优秀个体
     * 优化：返回的个体不能有重复的试题
     *
     * @return
     */
    public Paper getFitness() {
        Paper paper = papers[0];
        for (int i = 1; i < papers.length; i++) {
            List<QuestionManage> questionList = papers[i].getQuestionList();
            Set questionHashSet = new HashSet<>(questionList);
            //保证最优数组中不存在重复元素
            if (paper.getAdaptationDegree() < papers[i].getAdaptationDegree() && questionList.size() == questionHashSet.size()) {
                paper = papers[i];
            }
        }
        return paper;
    }

    public Population(int populationSize) {
        papers = new Paper[populationSize];
    }

    /**
     * 获取种群中某个个体
     *
     * @param index
     * @return
     */
    public Paper getPaper(int index) {
        return papers[index];
    }

    /**
     * 设置种群中某个个体
     *
     * @param index
     * @param paper
     */
    public void setPaper(int index, Paper paper) {
        papers[index] = paper;
    }

    /**
     * 返回种群规模
     *
     * @return
     */
    public int getLength() {
        return papers.length;
    }

}
