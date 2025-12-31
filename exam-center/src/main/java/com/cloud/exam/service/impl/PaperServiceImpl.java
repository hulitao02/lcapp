package com.cloud.exam.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.ExamDao;
import com.cloud.exam.dao.PaperDao;
import com.cloud.exam.dao.PaperManageRelDao;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.service.*;
import com.cloud.exam.utils.GeneticAlgorithm;
import com.cloud.exam.utils.Population;
import com.cloud.exam.vo.PaperVO;
import com.cloud.exam.vo.RuleBeanVO;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.common.ExamConstant;
import com.cloud.model.utils.AppUserUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author meian
 */
@Service
public class PaperServiceImpl extends ServiceImpl<PaperDao, Paper> implements PaperService {

    @Resource
    private PaperDao paperDao;
    @Resource
    private PaperManageRelDao paperManageRelDao;
    @Resource
    private PaperManageRelService paperManageRelService;
    @Resource
    private ExamDao examDao;
    @Resource
    private QuestionManageService questionManageService;
    @Resource
    private QuestionKpRelManageService questionKpRelManageService;
    @Resource
    private SysDepartmentFeign sysDepartmentFeign;
    @Resource
    private QuestionService questionService;
    @Resource
    private QuestionKpRelService questionKpRelService;

    @Override
    public IPage<Paper> findList(Page page) {
        return paperDao.findList(page);
    }


    @Override
    public IPage<Paper> findByPage(PaperVO paper) {
        QueryWrapper<Paper> paperQueryWrapper = new QueryWrapper<>();
        if ("" != paper.getPaperName() && paper.getPaperName() != null) {
            paperQueryWrapper.like("paper_name", paper.getPaperName());
        }
        if (paper.getType() != null) {
            paperQueryWrapper.eq("type", paper.getType());
        }
        if (paper.getStatus() != null) {
            paperQueryWrapper.eq("status", paper.getStatus());
        }
        if (ObjectUtil.isNotNull(paper.getPaperFlg())) {
            paperQueryWrapper.eq("paper_flg", paper.getPaperFlg());
        }
        if (paper.getExamId() != null) {
            HashSet<String> kpIds = new HashSet<>();
            List<ExamDepartUserRel> departAndUserByExamId = examDao.getDepartAndUserByExamId(paper.getExamId());
            for (ExamDepartUserRel rel : departAndUserByExamId) {
                Set<String> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(rel.getMemberId());
                kpIdsbyUserId.stream().forEach(e -> kpIds.add(e));
            }
           /* List<ExamDepartPaperRel> departAndPaperByExamId = examDao.getDepartAndPaperByExamId(paper.getExamId());
            List<Long> ll = new ArrayList<>();
            departAndPaperByExamId.stream().forEach(e->{
                if(e.getPaperId()!=null){
                    ll.add(e.getPaperId());
                }
            });
            if(ll.size()>0){
                paperQueryWrapper.or().in("id",ll);
            }*/
        }
        paperQueryWrapper.select().orderByDesc("create_time", "id");
        Page<Paper> page = new Page();
        page.setCurrent(paper.getCurrent());
        page.setSize(paper.getSize());
        return paperDao.selectPage(page, paperQueryWrapper);
    }


    /**
     * 种群初始化、计算适应度、选择（随机选择）、交叉、变异（概率尽量低）
     *
     * @param rule
     * @param paper
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Paper paperSave(RuleBeanVO rule, Paper paper) {
        Paper resultPaper = null;
        // 迭代计数器
        int count = 0;
        int runCount = 20;
        // 适应度期望值
        double expand = 0.98;
//        rule.setTotalMark(13);//总分
//        rule.setDifficulty(0.6);
//        rule.setPointIds("2#1");
//        rule.setKpIds("");
//        rule.setIntDirectIds("");
        try {
            if (rule != null) {
                // 初始化种群
                Population population = new Population(10, true, rule);
//            SystemTest.out.println("初次适应度" + population.getFitness().getAdaptationDegree());
                while (count < runCount && population.getFitness().getAdaptationDegree() < expand) {
                    count++;
                    //进化种群
                    population = GeneticAlgorithm.evolvePopulation(population, rule);
//                    System.out.println("第 " + count + " 次进化，适应度为： " + population.getFitness().getAdaptationDegree());
                }
//                System.out.println("进化次数： " + count);
//            SystemTest.out.println(population.getFitness().getAdaptationDegree());
                //存入试卷基本信息表后再建立试题与试卷的关联
                paper.setDifficulty(rule.getDifficulty());
                paper.setCreateTime(new Date());
                paper.setCreator(AppUserUtil.getLoginAppUser().getId());
                paper.setUpdateTime(new Date());
                paper.setStatus(1);
                this.save(paper);
                resultPaper = population.getFitness();


                List<QuestionManage> questionManageList = resultPaper.getQuestionList();
                QuestionTransfer questionTransfer = transferQuestion(questionManageList);
                List<Question> questionList = questionTransfer.getQuestionList();
                questionList.sort(Comparator.comparingInt(Question::getType));

                List<PaperManageRel> paperManageRelList = new ArrayList<>(questionList.size());
                Integer i = 1;
                for (Question question : questionList) {
                    PaperManageRel paperManage = new PaperManageRel();
                    paperManage.setPaperId(paper.getId());
                    paperManage.setQuestionId(question.getId());
                    paperManage.setScore(question.getScore());
                    //将试题答案赋值给判分依据
                    paperManage.setScoreBasis(question.getAnswer());
                    //添加试题顺序
                    paperManage.setSort(i);
                    if (rule.getExamId() != null) {
                        //给每道题添加答题时长
                        paperManage.setQuestionTime(paper.getTotalTime());
                    }
                    //详情表
                    paperManageRelList.add(paperManage);
                    i++;
                }
                paperManageRelService.saveBatch(paperManageRelList);
                if (rule.getExamId() != null) {
                    paper.setTotalTime((i - 1) * paper.getTotalTime());
                    this.saveOrUpdate(paper);
                }
                //更改试题状态为已被使用
                questionManageService.lambdaUpdate().set(QuestionManage::getStatus, ExamConstant.QUESTION_USED)
                        .in(QuestionManage::getId, questionTransfer.getQuestionManageIdList()).update();
            }
        } catch (Exception e) {
            log.error("组卷异常", e);
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
        return paper;
    }

    public QuestionTransfer transferQuestion(List<QuestionManage> questionManageList) {
        //从QuestionManage表复制到Question表
        //先构建知识点关联关系
        List<Long> questionManageIdList = questionManageList.stream().map(QuestionManage::getId).collect(Collectors.toList());
        Map<Long, List<String>> questionIdAndKpIdListMap = questionKpRelManageService.getQuestionIdAndKpIdListMap(questionManageIdList);
        List<Question> questionList = questionManageList.stream().map(questionManage -> {
            Question question = new Question();
            BeanUtils.copyProperties(questionManage, question);
            question.setKpIds(questionIdAndKpIdListMap.get(questionManage.getId()));
            question.setId(null);
            return question;
        }).collect(Collectors.toList());
        //保存
        questionService.saveBatch(questionList);
        List<QuestionKpRel> kpRelList2Save = questionList.stream().map(question -> {
            List<String> kpIds = question.getKpIds();
            if (CollectionUtils.isNotEmpty(kpIds)) {
                List<QuestionKpRel> kpRelList = kpIds.stream().map(kpId -> {
                    QuestionKpRel questionKpRel = new QuestionKpRel();
                    questionKpRel.setQuestionId(question.getId());
                    questionKpRel.setKpId(kpId);
                    return questionKpRel;
                }).collect(Collectors.toList());
                return kpRelList;
            } else {
                return new ArrayList<QuestionKpRel>();
            }
        }).reduce(new ArrayList<>(), (acc, item) -> {
            acc.addAll(item);
            return acc;
        }, (o, u) -> {
            o.addAll(u);
            return o;
        });
        questionKpRelService.saveBatch(kpRelList2Save);
        QuestionTransfer questionTransfer = new QuestionTransfer();
        questionTransfer.setQuestionList(questionList);
        questionTransfer.setQuestionManageIdList(questionManageIdList);
        questionTransfer.setQuestionManageList(questionManageList);
        return questionTransfer;
    }

    /**
     * 删除试卷以及试卷关联
     *
     * @param paperId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePaperById(long paperId) {
        //判断试卷是否被活动绑定，若绑定则不能被删除
        ExamDepartPaperRel examDepartPaperRel = examDao.findByPaperId(paperId);
        if (examDepartPaperRel != null) {
            new IllegalArgumentException("该试卷已经被试题绑定，无法删除！");
        }
        try {
            int deleteById = paperDao.deleteById(paperId);
            int deleteByPaperId = paperManageRelDao.deleteByPaperId(paperId);
            if (deleteById != 1 && deleteByPaperId != 1) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return false;
    }
}
