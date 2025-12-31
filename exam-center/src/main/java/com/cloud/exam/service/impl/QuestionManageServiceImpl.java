package com.cloud.exam.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.QuestionManageDao;
import com.cloud.exam.model.exam.QuestionKpRelManage;
import com.cloud.exam.model.exam.QuestionManage;
import com.cloud.exam.service.ExamService;
import com.cloud.exam.service.QuestionKpRelManageService;
import com.cloud.exam.service.QuestionManageService;
import com.cloud.feign.knowledge.KnowledgeFeign;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.utils.ObjectUtils;
import com.cloud.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author meidan
 */
@Service
@Transactional
@Slf4j
public class QuestionManageServiceImpl extends ServiceImpl<QuestionManageDao, QuestionManage> implements QuestionManageService {

    @Autowired
    private QuestionManageDao questionManageDao;
    @Autowired
    private ManageBackendFeign manageBackendFeign;
    @Resource
    private QuestionKpRelManageService questionKpRelManageService;
    @Autowired
    private SysDepartmentFeign sysDepartmentFeign;
    @Autowired
    private ExamService examService;
    // 数据库标识 1：达梦数据库 2：pg数据库
    @Value(value = "${db-type}")
    private Integer dbType;


    @Override
    public IPage<QuestionManage> findByPage(QuestionManage question) {
        QueryWrapper<QuestionManage> questionQueryWrapper = new QueryWrapper<>();
        if (ObjectUtil.isNotEmpty(question.getType())) {
            questionQueryWrapper.eq("type", question.getType());
        }
        if (StringUtils.isNotEmpty(question.getQuestion())) {
            questionQueryWrapper.like("question", question.getQuestion());
        }
        if (question.getDifficulty() != null) {
            if (question.getDifficulty() >= 0 && question.getDifficulty() <= 0.2) {
                questionQueryWrapper.between("difficulty", 0, 0.2);
            } else if (question.getDifficulty() > 0.2 && question.getDifficulty() <= 0.4) {
                questionQueryWrapper.gt("difficulty", 0.2);
                questionQueryWrapper.le("difficulty", 0.4);
            } else if (question.getDifficulty() > 0.4 && question.getDifficulty() <= 0.6) {
                questionQueryWrapper.gt("difficulty", 0.4);
                questionQueryWrapper.le("difficulty", 0.6);
            } else if (question.getDifficulty() > 0.6 && question.getDifficulty() <= 0.8) {
                questionQueryWrapper.gt("difficulty", 0.6);
                questionQueryWrapper.le("difficulty", 0.8);
            } else if (question.getDifficulty() > 0.8 && question.getDifficulty() <= 1) {
                questionQueryWrapper.gt("difficulty", 0.8);
                questionQueryWrapper.le("difficulty", 1);
            }
        }
       /* if (question.getKpId() != null){
            questionQueryWrapper.eq("kp_id",question.getKpId());
        }*/
        if (!question.isFlag()) {
            questionQueryWrapper.between("type", 0, 4);
        }
        if (question.getPdTypeStr() != null && question.getPdTypeStr().trim().length() > 0) {
            List<String> pdList = new ArrayList<>();
            String[] pdArr = question.getPdTypeStr().split(",");
            for (int i = 0; i < pdArr.length; i++) {
                pdList.add(pdArr[i]);
            }
            questionQueryWrapper.in("pd_type", pdList);
        }
        if (StringUtils.isBlank(question.getKps()) && CollectionUtils.isNotEmpty(question.getKpIds())) {
            //questionQueryWrapper.in("kp_id",question.getKpIds());
            QueryWrapper<QuestionKpRelManage> questionKpRelQueryWrapper = new QueryWrapper<>();
            questionKpRelQueryWrapper.in("kp_id", question.getKpIds());
            List<QuestionKpRelManage> list = questionKpRelManageService.list(questionKpRelQueryWrapper);
            Set set = new HashSet();
            set.add(0);
            list.forEach(e -> set.add(e.getQuestionId()));
            questionQueryWrapper.in("id", set);
        }
        if (StringUtils.isNotBlank(question.getKps())) {
            Set<Long> set = new HashSet<>();
            set.add(0L);
            String[] split = question.getKps().split(",");
            for (String str : split) {
                set.add(Long.valueOf(str));
            }
            QueryWrapper<QuestionKpRelManage> questionKpRelQueryWrapper = new QueryWrapper<>();
            questionKpRelQueryWrapper.in("kp_id", set);
            List<QuestionKpRelManage> list = questionKpRelManageService.list(questionKpRelQueryWrapper);
            Set set1 = new HashSet();
            set1.add(0);
            list.forEach(e -> set1.add(e.getQuestionId()));
            questionQueryWrapper.in("id", set1);
        }
        questionQueryWrapper.select().orderByDesc("create_time", "id");
        Page<QuestionManage> page = new Page<>();
        page.setCurrent(question.getCurrent());
        page.setSize(question.getSize());
        return questionManageDao.selectPage(page, questionQueryWrapper);
    }

    @Override
    public IPage<QuestionManage> findAll(Page<QuestionManage> page, Map<String, Object> params) {
        QueryWrapper<QuestionManage> questionQueryWrapper = new QueryWrapper<>();
        Object question = params.get("question");
        if (null != question && !"".equals(question.toString())) {
            questionQueryWrapper.like("q.question", question);
        }
        String pdType = params.get("pdType") == null ? "" : params.get("pdType").toString();
        if (pdType != "" && pdType.trim().length() > 0) {
            questionQueryWrapper.eq("pd_type", pdType);
        }
        Object batch = params.get("batch");
        if (null != batch && !"".equals(batch.toString())) {
            questionQueryWrapper.like("q.batch", batch.toString());
        }
        Object type = params.get("type");
        if (null != type && !"".equals(type.toString())) {
            questionQueryWrapper.eq("q.type", Integer.parseInt(type.toString()));
        }
        Object kpIdsObj = params.get("kpIds");
        if (null != kpIdsObj && !"".equals(kpIdsObj.toString())) {
            String[] kpIds = kpIdsObj.toString().split(",");
            List<String> ll = new ArrayList<>();
            for (String kpId : kpIds) {
                ll.add(kpId);
            }
            questionQueryWrapper.in("qr.kp_id", ll);
        }
        questionQueryWrapper.select().orderByDesc("create_time");
        if (dbType == 1) {
            return questionManageDao.selectQuestionPageDM(page, questionQueryWrapper);
        } else {
            return questionManageDao.selectQuestionPage(page, questionQueryWrapper);
        }
    }

    @Override
    public IPage<QuestionManage> findAllTest(Page<QuestionManage> page) {
        return questionManageDao.findAllTest(page);
    }

    @Override
    public QuestionManage findOnlyQuestionId() {
        return questionManageDao.findOnlyQuestionId();
    }

    @Override
    public Double getQuestionScoreById(Long paperId, Long questionId) {
        return questionManageDao.getQuestionScoreById(paperId, questionId);
    }

    @Override
    public List<QuestionManage> getQuestion(List<Long> kpIds, List<Long> intDirectIds, Integer questionType, Integer questionNum, Double difficulty1, Double difficulty2, List<Long> qIds) {
        return questionManageDao.getQuestions(kpIds, intDirectIds, questionType, questionNum, difficulty1, difficulty2, qIds);
    }

    @Override
    public Map<String, List<QuestionManage>> getQuestionList() {
        Map<String, List<QuestionManage>> resultMap = new HashMap<>();
        List<QuestionManage> questionList = questionManageDao.getQuestionList();
        if (ObjectUtils.isNotNull(questionList)) {
            List<Long> idList = questionList.stream().map(QuestionManage::getId).collect(Collectors.toList());
            Map<Long, List<String>> questionIdAndKpIdListMap = questionKpRelManageService.getQuestionIdAndKpIdListMap(idList);

            questionList.forEach(question -> {
                question.setKpIds(questionIdAndKpIdListMap.get(question.getId()));
                JSONObject jsonObject = JSONObject.parseObject(question.getQuestion());
                String text = jsonObject.getString("text");
                if (StringUtils.isNotEmpty(text)) {
                    if (ObjectUtils.isNotNull(resultMap.get(text))) {
                        resultMap.get(text).add(question);
                    } else {
                        List<QuestionManage> questions = new ArrayList<>();
                        questions.add(question);
                        resultMap.put(text, questions);
                    }
                }
            });
        }
        return resultMap;
    }

    @Override
    public boolean saveQuestion(QuestionManage question) {
        int insert = questionManageDao.insert(question);
        if (insert > 0) {
            List<String> kIds = question.getKpIds();
            kIds.forEach(e -> {
                QuestionKpRelManage questionKpRel = new QuestionKpRelManage();
                questionKpRel.setKpId(e);
                questionKpRel.setQuestionId(question.getId());
                questionKpRelManageService.save(questionKpRel);
            });
            return true;
        }
        return false;
    }


    @Override
    public QuestionManage[] getQuestionArray(int type, List<String> kpString, List<Long> substring, List<Long> qIds, List<String> pdTypes,Double diff) {
        // TODO Auto-generated method stub
//        int[] kpStringList = Arrays.asList(kpString.split(",")).stream().mapToInt(Integer::parseInt).toArray();
//        List kpStringList = Arrays.asList();
//        int[] substringList = Arrays.asList(substring.split(",")).stream().mapToInt(Integer::parseInt).toArray();
//        List substringList = Arrays.asList();
        List<QuestionManage> questionsList = questionManageDao.getQuestionIn(type, kpString, substring, qIds, pdTypes,diff);
        QuestionManage[] questionArrays = new QuestionManage[questionsList.size()];
        int i = 0;
        for (QuestionManage q : questionsList) {
            questionArrays[i] = q;
            i++;
        }
        return questionArrays;
    }

    @Override
    public List<QuestionManage> getQuestionListWithOutSId(QuestionManage question) {
        // TODO Auto-generated method stub
        return questionManageDao.findQuestionEquals(question);
    }

    @Override
    public int deleteQuestion(QuestionManage question) {
        return questionManageDao.deleteQuestion(question);
    }


    @Override
    public QuestionManage findByHighVersion(String code) {
        return findByHighVersion(code);
    }

    /**
     * 修改问题的思路：不在原来的试题上修改，先复制一条->更新版本->存入数据库->在新版本上进行修改
     * 这样保证使用该试题组卷的试卷不受影响
     *
     * @param question
     * @return
     */
    @Override
    public boolean updateQuestion(QuestionManage question) {
        //修改前先复制一条出来存储，下次创建试卷的时候抽版本最新的试题，组卷后试卷中试题不能修改
        QuestionManage oldVersionQuestion = questionManageDao.selectById(question.getId());
        int version = oldVersionQuestion.getVersion();
        question.setVersion(version + 1);
//        this.save(oldVersionQuestion);
        //查找最新版本的试题
//        Question newVersionQuestion = questionDao.findByHighVersion(oldVersionQuestion.getCode());
//        question.setId(newVersionQuestion.getId());
        return this.updateById(question);
    }

    @Override
    public List<QuestionManage> selectByDirectId(Long id) {
        return questionManageDao.selectByDirectId(id);
    }

    /**
     * 保存json文件导入的试题
     *
     * @param qq
     * @param kpIds
     */
    @Override
    public void saveJson(QuestionManage qq, List<String> kpIds) {
        try {
            /**
             *  试题中URL 文件目录修改 。
             */
            questionManageDao.insert(qq);
            kpIds.forEach(e -> {
                QuestionKpRelManage questionKpRel = new QuestionKpRelManage();
                questionKpRel.setKpId(e);
                questionKpRel.setQuestionId(qq.getId());
                questionKpRelManageService.save(questionKpRel);
            });
        } catch (Exception e) {
            e.printStackTrace();
            //手动回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

    }


    /**
     * 保存json文件导入的试题
     *
     * @param qq
     * @param kpIds
     */
    @Transactional
    @Override
    public void saveJsonAndFiles(QuestionManage qq, List<String> kpIds) {
        questionManageDao.insert(qq);
        kpIds.forEach(e -> {
            QuestionKpRelManage questionKpRel = new QuestionKpRelManage();
            questionKpRel.setKpId(e);
            questionKpRel.setQuestionId(qq.getId());
            questionKpRelManageService.save(questionKpRel);
        });
    }

    @Autowired
    KnowledgeFeign knowledgeFeign;

    @Override
    public Map<Long, String> getQuestionIdKnowledgeNameMap(List<Long> questionIdList) {
        if (CollectionUtils.isEmpty(questionIdList)) {
            return Collections.emptyMap();
        }
        //批量查询知识点关联
        List<QuestionKpRelManage> questionKpRelList = questionKpRelManageService.findByQuestionIdList(questionIdList);
        //批量查询知识点
        List<String> kpIdList = questionKpRelList.stream().map(QuestionKpRelManage::getKpId).distinct().collect(Collectors.toList());
        Map<String, String> knowledgePointsMap = new HashMap<>();
        Map parMap = new HashMap();
        parMap.put("kpIds", kpIdList);
        List<Map> knowledgePointsMapByIdList = knowledgeFeign.getKnowledgePointsMapByIdList(parMap);
        if (knowledgePointsMapByIdList != null && knowledgePointsMapByIdList.size() > 0) {
            for (Map map : knowledgePointsMapByIdList) {
                knowledgePointsMap.put(map.get("id").toString(), map.get("name").toString());
            }
        }
        //组装问题id与知识点名称映射
        return questionKpRelList.stream().collect(Collectors.groupingBy(QuestionKpRelManage::getQuestionId,
                Collectors.mapping(qr -> knowledgePointsMap.get(qr.getKpId()),
                        Collectors.joining(" "))));
    }
}
