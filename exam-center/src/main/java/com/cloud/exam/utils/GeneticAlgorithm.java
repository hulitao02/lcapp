package com.cloud.exam.utils;

import cn.hutool.core.util.ObjectUtil;
import com.cloud.exam.model.exam.Paper;
import com.cloud.exam.model.exam.QuestionManage;
import com.cloud.exam.service.QuestionManageService;
import com.cloud.exam.vo.RuleBeanVO;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * 算法组卷实现类
 *
 * @author meidan
 */
@Component
@NoArgsConstructor
public class GeneticAlgorithm {

    @Resource
    private QuestionManageService questionManageService;
    private static GeneticAlgorithm geneticAlgorithm;

    @PostConstruct
    public void init() {
        geneticAlgorithm = this;
        geneticAlgorithm.questionManageService = this.questionManageService;

    }

    /**
     * 变异概率
     */
    private static final double mutationRate = 0.085;
    /**
     * 精英主义
     */
    private static final boolean elitism = true;
    /**
     * 淘汰数组大小
     */
    private static final int tournamentSize = 5;

    // 进化种群
    public static Population evolvePopulation(Population pop, RuleBeanVO rule) {
        Population newPopulation = new Population(pop.getLength());
        int elitismOffset;
        // 精英主义
        if (elitism) {
            elitismOffset = 1;
            // 保留上一代最优秀个体
            Paper fitness = pop.getFitness();
            fitness.setId(0L);
            newPopulation.setPaper(0, fitness);
        }
        // 种群交叉操作，从当前的种群pop来创建下一代种群newPopulation
        int a = newPopulation.getLength();
        for (int i = elitismOffset; i < newPopulation.getLength(); i++) {
            //较优选择parent
            Paper parent1 = select(pop);
            Paper parent2 = select(pop);
            while (parent2.getId().longValue() == parent1.getId().longValue()) {
                parent2 = select(pop);
            }
            // 交叉
            Paper child = crossover(parent1, parent2, rule);
            child.setId((long) i);
            newPopulation.setPaper(i, child);
        }
        // 种群变异操作
        Paper tmpPaper;
        for (int i = elitismOffset; i < newPopulation.getLength(); i++) {
            tmpPaper = newPopulation.getPaper(i);
            if (tmpPaper != null){
                mutate(tmpPaper);
            }
            // 计算知识点覆盖率与适应度
            tmpPaper.setKpCoverage(rule);
            if (rule.getIntDirectIds().size() >0){
                tmpPaper.setIDCoverage(rule);
            }
            if (tmpPaper.getQuestionList().size() >0){
                tmpPaper.setAdaptationDegree(rule, Global.KP_WEIGHT,Global.ID_WEIGHT, Global.DIFFCULTY_WEIGHt);
            }
        }
        return newPopulation;
    }

    /**
     * 交叉算子
     *
     * @param parent1
     * @param parent2
     * @return
     */
    public static Paper crossover(Paper parent1, Paper parent2, RuleBeanVO rule) {
        Paper child = new Paper(parent1.getQuestionSize());
        int s1 = (int) (Math.random() * parent1.getQuestionSize());
        int s2 = (int) (Math.random() * parent1.getQuestionSize());
        List<Long> qIds = rule.getQIds();
        // parent1的startPos endPos之间的序列，会被遗传到下一代
        int startPos = s1 < s2 ? s1 : s2;
        int endPos = s1 > s2 ? s1 : s2;
        for (int i = startPos; i < endPos; i++) {
            child.saveQuestion(i, parent1.getQuestion(i));
        }
        // 继承parent2中未被child继承的question
        // 防止出现重复的元素
        List<Long> idString = rule.getIntDirectIds();
        List<String> kpString = rule.getKpIds();
        for (int i = 0; i < startPos; i++) {
            if (!child.containsQuestion(parent2.getQuestion(i))) {
                child.saveQuestion(i, parent2.getQuestion(i));
            } else {
                int type = getTypeByIndex(i, rule);
                // getQuestionArray()用来选择指定类型和知识点的试题数组
                QuestionManage[] singleArray = geneticAlgorithm.questionManageService.getQuestionArray(type, kpString, idString, qIds,rule.getPdTypes(),rule.getDifficulty());
                child.saveQuestion(i, singleArray[(int) (Math.random() * singleArray.length)]);
            }
        }
        for (int i = endPos; i < parent2.getQuestionSize(); i++) {
            if (!child.containsQuestion(parent2.getQuestion(i))) {
                child.saveQuestion(i, parent2.getQuestion(i));
            } else {
                int type = getTypeByIndex(i, rule);
                QuestionManage[] singleArray = geneticAlgorithm.questionManageService.getQuestionArray(type, kpString, idString, qIds,rule.getPdTypes(),rule.getDifficulty());
                System.out.println(singleArray.length+"..........type"+type+".........kpString"+kpString+"............idString"+idString);
                child.saveQuestion(i, singleArray[(int) (Math.random() * singleArray.length)]);
            }
        }
        if(child.getQuestionList().size() < 6){
            System.out.println(child);
        }
        return child;
    }

    private static int getTypeByIndex(int index, RuleBeanVO rule) {
        int type = 0;
        // 单选
        if (index < rule.getSingleChoiceNum()) {
            type = 1;
        } else if (index < rule.getSingleChoiceNum() + rule.getMultipleChoiceNum()) {
            // 多选
            type = 2;
        } else if(index < rule.getSingleChoiceNum() + rule.getMultipleChoiceNum()
                +rule.getJudgeNum()){
            // 判断
            type = 3;
        } else if(index < rule.getSingleChoiceNum() + rule.getMultipleChoiceNum()
                +rule.getJudgeNum()+rule.getCompleteNum()){
            // 填空
            type = 4;
        }else if(index < rule.getSingleChoiceNum() + rule.getMultipleChoiceNum()
                +rule.getJudgeNum()+rule.getCompleteNum()+rule.getBriefNum()){
            // 简答
            type = 5;
        }else if(index < rule.getSingleChoiceNum() + rule.getMultipleChoiceNum()
                +rule.getJudgeNum()+rule.getCompleteNum()+rule.getBriefNum()
                +rule.getDiscussNum()){
            // 问答
            type = 6;
        }else if(index < rule.getSingleChoiceNum() + rule.getMultipleChoiceNum()
                +rule.getJudgeNum()+rule.getCompleteNum()+rule.getBriefNum()
                +rule.getDiscussNum()+rule.getIntelAnalysisNum()){
            // 实操
            type = 7;
        }else if(index < rule.getSingleChoiceNum() + rule.getMultipleChoiceNum()
                +rule.getJudgeNum()+rule.getCompleteNum()+rule.getBriefNum()
                +rule.getDiscussNum()+rule.getPracticeNum()+rule.getIntelAnalysisNum()){
            // 情析
            type = 8;
        }else if(ObjectUtil.isNotNull(rule.getConnectNum()) && index < rule.getSingleChoiceNum() + rule.getMultipleChoiceNum()
                +rule.getJudgeNum()+rule.getCompleteNum()+rule.getBriefNum()
                +rule.getDiscussNum()+rule.getPracticeNum()+rule.getIntelAnalysisNum()+rule.getConnectNum()){
            // 情析
            type = 9;
        }
        return type;
    }

    /**
     *
     * 突变算子 每个个体的每个基因都有可能突变
     *
     * @param paper
     */
    public static void mutate(Paper paper) {
        QuestionManage tmpQuestion;
        List<QuestionManage> list;
        for (int i = 0; i < paper.getQuestionSize(); i++) {
            if (Math.random() < mutationRate) {
                // 进行突变，第i道
                tmpQuestion = paper.getQuestion(i);
                // 从题库中获取和变异的题目类型一样、知识点相同、情报方向相同的题目（不包含变异题目）
                list = geneticAlgorithm.questionManageService.getQuestionListWithOutSId(tmpQuestion);
                if (list.size() > 0) {
                    for (int index = 0; index < list.size(); index++) {
                        if (paper.containsQuestion(list.get(index))) {
                            continue;
                        }else{
                            // 设置分数
                            list.get(index).setScore(tmpQuestion.getScore());
                            paper.saveQuestion(i, list.get(index));
                            break;
                        }
                    }
                    /*do {
                        // 随机获取一道
                        index = (int) (Math.random() * list.size());
                    }
                    while(!paper.containsQuestion(list.get(index)));*/

                }
            }
        }
    }


    /**
     * 选择算子
     *
     * @param population
     */
    private static Paper select(Population population) {
        Population pop = new Population(tournamentSize);
        for (int i = 0; i < tournamentSize; i++) {
            pop.setPaper(i, population.getPaper((int) (Math.random() * population.getLength())));
        }
        return pop.getFitness();
    }
}
