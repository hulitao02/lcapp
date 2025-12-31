package com.cloud.exam.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.exam.dao.QuestionDao;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.service.*;
import com.cloud.exam.utils.ListUtils;
import com.cloud.exam.utils.exam.QuestionUtils;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.common.Dict;
import com.cloud.model.common.IntDirect;
import com.cloud.model.user.LoginAppUser;
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
public class QuestionServiceImpl extends ServiceImpl<QuestionDao, Question> implements QuestionService {

    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private ManageBackendFeign manageBackendFeign;
    @Resource
    private QuestionKpRelService questionKpRelService;
    @Autowired
    private SysDepartmentFeign sysDepartmentFeign;
    @Autowired
    private ExamService examService;
    @Autowired
    private AnalysisFrameworkService analysisFrameworkService;
    @Autowired
    private CollectionQuestionService collectionQuestionService;
    // 数据库标识 1：达梦数据库 2：pg数据库
    @Value(value = "${db-type}")
    private Integer dbType;
    // 影像缩略图访问地址
    @Value("${localUrlPrefix}")
    private String localUrlPrefix;
    // 文件服务器地址
    @Value(value = "${file_server}")
    private String fileServer;

    @Override
    public IPage<Question> findByPage(Question question) {
        QueryWrapper<Question> questionQueryWrapper = new QueryWrapper<>();
        if (ObjectUtil.isNotEmpty(question.getType())) {
            questionQueryWrapper.eq("type", question.getType());
        }
        if ("" != question.getQuestion() && question.getQuestion() != null) {
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
        if (StringUtils.isBlank(question.getKps()) && CollectionUtils.isNotEmpty(question.getKpIds())) {
            //questionQueryWrapper.in("kp_id",question.getKpIds());
            QueryWrapper<QuestionKpRel> questionKpRelQueryWrapper = new QueryWrapper<>();
            questionKpRelQueryWrapper.in("kp_id", question.getKpIds());
            List<QuestionKpRel> list = questionKpRelService.list(questionKpRelQueryWrapper);
            Set set = new HashSet();
            set.add(0);
            list.stream().forEach(e -> set.add(e.getQuestionId()));
            questionQueryWrapper.in("id", set);
        }
//        if (ObjectUtil.isNotNull(question.getExamId())) {
//            //竞答活动中试卷添加试题时受到活动关联人员知识点权限限制
//            Exam byId = examService.getById(question.getExamId());
//            if (byId.getType() == 1) {
//                List<Set<String>> sets = new ArrayList<>();
//                List<ExamDepartUserRel> departAndUserByExamId = examService.getDepartAndUserByExamId(question.getExamId());
//                departAndUserByExamId.stream().forEach(e -> {
//                    if (ObjectUtil.isNotNull(e.getMemberId())) {
//                        Set<String> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(e.getMemberId());
//                        sets.add(kpIdsbyUserId);
//                    }
//                });
//                //学员具有的相同知识点
//                Set<Long> sameElementBylists = ListUtils.getSameElementBylists(sets);
//                sameElementBylists.add(0L);
//                QueryWrapper<QuestionKpRel> questionKpRelQueryWrapper = new QueryWrapper<>();
//                questionKpRelQueryWrapper.in("kp_id", sameElementBylists);
//                List<QuestionKpRel> list = questionKpRelService.list(questionKpRelQueryWrapper);
//                Set set = new HashSet();
//                set.add(0);
//                list.stream().forEach(e -> set.add(e.getQuestionId()));
//                questionQueryWrapper.in("id", set);
//            }
//        }
        if (StringUtils.isNotBlank(question.getKps())) {
            Set<Long> set = new HashSet<>();
            set.add(0L);
            String[] split = question.getKps().split(",");
            for (String str : split) {
                set.add(Long.valueOf(str));
            }
            QueryWrapper<QuestionKpRel> questionKpRelQueryWrapper = new QueryWrapper<>();
            questionKpRelQueryWrapper.in("kp_id", set);
            List<QuestionKpRel> list = questionKpRelService.list(questionKpRelQueryWrapper);
            Set set1 = new HashSet();
            set1.add(0);
            list.stream().forEach(e -> set1.add(e.getQuestionId()));
            questionQueryWrapper.in("id", set1);
        }

        questionQueryWrapper.select().orderByDesc("create_time", "id");
        Page<Question> page = new Page();
        page.setCurrent(question.getCurrent());
        page.setSize(question.getSize());
        return questionDao.selectPage(page, questionQueryWrapper);
    }


    @Override
    public IPage<Question> findAll(Page<Question> page, Map<String, Object> params) {
        QueryWrapper<Question> questionQueryWrapper = new QueryWrapper<>();
        /*if ( null != params.get("id").toString() && "" != params.get("id").toString()){
            questionQueryWrapper.eq("id",Long.parseLong(params.get("id").toString()));
        }*/
        Object question = params.get("question");
        if (null != question && !"".equals(question.toString())) {
            questionQueryWrapper.like("q.question", question);
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
            List<Long> ll = new ArrayList<>();
            for (int i = 0; i < kpIds.length; i++) {
                ll.add(Long.parseLong(kpIds[i]));
            }
            questionQueryWrapper.in("qr.kp_id", ll);
        }
        questionQueryWrapper.select().orderByDesc("create_time");
        if (dbType == 1) {
            return questionDao.selectQuestionPageDM(page, questionQueryWrapper);
        } else {
            return questionDao.selectQuestionPage(page, questionQueryWrapper);
        }
    }

    @Override
    public IPage<Question> findAllTest(Page<Question> page) {
        return questionDao.findAllTest(page);
    }

    @Override
    public ApiResult findOnlyQuestionId(Long id) {
        Question res = getById(id);
        res.setLocalUrlPrefix(localUrlPrefix);
        QueryWrapper<QuestionKpRel> questionKpRelQueryWrapper = new QueryWrapper<>();
        questionKpRelQueryWrapper.eq("question_id", id);
        List<QuestionKpRel> list = questionKpRelService.list(questionKpRelQueryWrapper);
        List<String> ll = new ArrayList<>();
        list.stream().forEach(e -> {
            ll.add(e.getKpId());
        });
        //根据字典来设置内容
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("dictType", "question_type");
        mapParam.put("dictValue", String.valueOf(res.getType()));
        List<Dict> dictList = manageBackendFeign.findDict(mapParam);
        if (ObjectUtils.isNotNull(dictList)) {
            for (Dict dict : dictList) {
                res.setTypeName(dict.getDictName());
            }
        }
        if (res.getDirectId() == null || res.getDirectId() == 0) {
            res.setDirectName("无");
        } else {
            IntDirect intDirect = manageBackendFeign.findIntDirectById(res.getDirectId());
            if (ObjectUtils.isNotNull(intDirect)) {
                res.setDirectName(intDirect.getName());
            }
        }
        String kpnameByQuestionId = QuestionUtils.getKpNamesByQuestion(res);
        res.setKpName(kpnameByQuestionId);
        res.setKpIds(ll);
        res.setFileAddr(fileServer);
        if (res.getType() == 7 || res.getType() == 11) {
            if (ObjectUtil.isNotNull(res.getModelId())) {
                res.setModelName(analysisFrameworkService.getById(res.getModelId()).getName());
            }
        }
        JSONObject jso = new JSONObject();
        jso.put("text", "");
        if (res.getType() == 4) {
            String options = res.getOptions();
            JSONObject jsonObject = JSONObject.parseObject(options);
            int size = jsonObject.keySet().size();
            String[] s = new String[size];
            for (int i = 0; i < s.length; i++) {
                s[i] = "";
            }
            jso.put("text", s);
        }
        res.setStuAnswer(jso.toJSONString());
        // 查看该试题是否被收藏
        LoginAppUser loginAppUser = sysDepartmentFeign.getLoginAppUser();
        QueryWrapper<CollectionQuestion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginAppUser.getId());
        queryWrapper.eq("question_id", id);
        CollectionQuestion one = collectionQuestionService.getOne(queryWrapper);
        long collectionId = one == null ? 0L : one.getId();
        res.setCollectionId(collectionId);
        return ApiResultHandler.buildApiResult(200, "查询成功", res);
    }

    @Override
    public Double getQuestionScoreById(Long paperId, Long questionId) {
        return questionDao.getQuestionScoreById(paperId, questionId);
    }

    @Override
    public List<Question> getQuestion(List<Long> kpIds, List<Long> intDirectIds, Integer questionType, Integer questionNum, Double difficulty1, Double difficulty2, List<Long> qIds) {
        List<Question> list = questionDao.getQuestions(kpIds, intDirectIds, questionType, questionNum, difficulty1, difficulty2, qIds);
        return list;
    }

    @Override
    public Map<String, List<Question>> getQuestionList() {
        Map<String, List<Question>> resultMap = new HashMap<>();
        List<Question> questionList = questionDao.getQuestionList();
        if (ObjectUtils.isNotNull(questionList)) {
            List<Long> idList = questionList.stream().map(Question::getId).collect(Collectors.toList());
            List<QuestionKpRel> qkrList = questionKpRelService.findByQuestionIdList(idList);
            Map<Long, List<String>> questionIdAndKpIdListMap = qkrList.stream()
                    .collect(Collectors.groupingBy(QuestionKpRel::getQuestionId,
                            Collectors.mapping(QuestionKpRel::getKpId, Collectors.toList())));
            questionList.forEach(question -> {
                question.setKpIds(questionIdAndKpIdListMap.get(question.getId()));
                JSONObject jsonObject = JSONObject.parseObject(question.getQuestion());
                String text = jsonObject.getString("text");
                if (StringUtils.isNotEmpty(text)) {
                    if (ObjectUtils.isNotNull(resultMap.get(text))) {
                        resultMap.get(text).add(question);
                    } else {
                        List<Question> questions = new ArrayList<>();
                        questions.add(question);
                        resultMap.put(text, questions);
                    }
                }
            });
        }
        return resultMap;
    }

    @Override
    public boolean saveQuestion(Question question) {
        int insert = questionDao.insert(question);
        if (insert > 0) {
            List<String> kIds = question.getKpIds();
            kIds.stream().forEach(e -> {
                QuestionKpRel questionKpRel = new QuestionKpRel();
                questionKpRel.setKpId(e);
                questionKpRel.setQuestionId(question.getId());
                questionKpRelService.save(questionKpRel);
            });
            return true;
        }
        return false;
    }


    @Override
    public Question[] getQuestionArray(int type, List<String> kpString, List<Long> substring, List<Long> qIds) {
        List<Question> questionsList = questionDao.getQuestionIn(type, kpString, substring, qIds);
        Question[] questionArrays = new Question[questionsList.size()];
        int i = 0;
        for (Question q : questionsList) {
            questionArrays[i] = q;
            i++;
        }
        return questionArrays;
    }

    @Override
    public List<Question> getQuestionListWithOutSId(Question question) {
        // TODO Auto-generated method stub
        List<Question> questionsList = questionDao.findQuestionEquals(question);
        return questionsList;
    }

    @Override
    public int deleteQuestion(Question question) {
        return questionDao.deleteQuestion(question);
    }


    @Override
    public Question findByHighVersion(String code) {
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
    public boolean updateQuestion(Question question) {
        //修改前先复制一条出来存储，下次创建试卷的时候抽版本最新的试题，组卷后试卷中试题不能修改
        Question oldVersionQuestion = questionDao.selectById(question.getId());
        int version = oldVersionQuestion.getVersion();
        question.setVersion(version + 1);
//        this.save(oldVersionQuestion);
        //查找最新版本的试题
//        Question newVersionQuestion = questionDao.findByHighVersion(oldVersionQuestion.getCode());
//        question.setId(newVersionQuestion.getId());
        boolean ifupdate = this.updateById(question);
        return ifupdate;
    }

    @Override
    public List<Question> selectByDirectId(Long id) {
        return questionDao.selectByDirectId(id);
    }

    /**
     * 保存json文件导入的试题
     *
     * @param qq
     * @param kpIds
     */
    @Override
    public void saveJson(Question qq, List<String> kpIds) {
        try {
            /**
             *  试题中URL 文件目录修改 。
             */


            questionDao.insert(qq);
            kpIds.stream().forEach(e -> {
                QuestionKpRel questionKpRel = new QuestionKpRel();
                questionKpRel.setKpId(e);
                questionKpRel.setQuestionId(qq.getId());
                questionKpRelService.save(questionKpRel);
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
    public void saveJsonAndFiles(Question qq, List<String> kpIds) {
        questionDao.insert(qq);
        kpIds.stream().forEach(e -> {
            QuestionKpRel questionKpRel = new QuestionKpRel();
            questionKpRel.setKpId(e);
            questionKpRel.setQuestionId(qq.getId());
            questionKpRelService.save(questionKpRel);
        });
    }


}
