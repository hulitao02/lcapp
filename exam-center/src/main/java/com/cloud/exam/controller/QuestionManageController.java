package com.cloud.exam.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ExamConstants;
import com.cloud.core.ResultMesEnum;
import com.cloud.exam.dao.QuestionKpRelManageDao;
import com.cloud.exam.dao.QuestionManageDao;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.service.*;
import com.cloud.exam.utils.ExcelUtil.ExcelUtil;
import com.cloud.exam.utils.exam.QuestionManageUtils;
import com.cloud.exam.utils.thread.QuestionDownThread;
import com.cloud.exam.utils.word.WordImportUtil;
import com.cloud.feign.knowledge.KnowledgeFeign;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.common.Dict;
import com.cloud.model.common.ExamConstant;
import com.cloud.model.common.IntDirect;
import com.cloud.model.common.KnowledgePoints;
import com.cloud.model.log.LogAnnotation;
import com.cloud.model.log.constants.LogModule;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.utils.ObjectUtils;
import com.cloud.utils.PageUtil;
import com.cloud.utils.StringUtils;
import com.cloud.utils.Validator;
import com.cloud.utils.excel.ExcelImportByPicture;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RefreshScope
@Slf4j
public class QuestionManageController {

    @Resource
    private QuestionManageService questionManageService;
    @Resource
    private ManageBackendFeign manageBackendFeign;

    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;

    @Autowired
    private QuestionErrorService questionErrorService;

    @Autowired
    private QuestionKpRelManageService questionKpRelManageService;

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private AnalysisFrameworkService analysisFrameworkService;

    @Autowired
    private QuestionService questionService;
    // 文件服务器地址
    @Value(value = "${file_server}")
    private String fileServer;


    // 影像缩略图访问地址
    @Value("${localUrlPrefix}")
    private String localUrlPrefix;

    @Autowired
    SysDepartmentFeign sysDepartmentFeign;
    @Autowired
    CollectionQuestionService collectionQuestionService;


    @GetMapping("/answers/{page}/{size}")
    public ApiResult findAllQuestion(@PathVariable("page") Integer page, @PathVariable("size") Integer size, @RequestParam Map<String, Object> params) {
        Page<QuestionManage> questionPage = new Page<>(page, size);
        IPage<QuestionManage> answerVOIPage = questionManageService.page(questionPage);
//        IPage<QuestionManage> answerVOIPage = questionService.findAllTest(questionPage);
        return ApiResultHandler.buildApiResult(200, "查询所有题库", answerVOIPage);
    }


    /**
     * @param question 参数:试卷的实体类
     * @return 返回试卷的分页信息实体，比如页码， 当前页有多少记录，还有当前页试卷的记录集合
     */
    @PostMapping("/questionPageByParam")
    @ApiOperation("分页查询试题")
    public IPage<QuestionManage> questionPage(@RequestBody QuestionManage question) {
        // 根据活动id查询参赛人员
        Map<String, Object> parMap = new HashMap<>();
        parMap.put("examId", question.getExamId());
        List<Map<String, Object>> studensByExamId = questionManageDao.getStudensByExamId(parMap);
        if (CollectionUtils.isNotEmpty(studensByExamId)) {
            List<String> kpIds = new ArrayList<>();
            Long[] userIds = new Long[studensByExamId.size()];
            for (int i = 0; i < studensByExamId.size(); i++) {
                userIds[i] = Long.valueOf(studensByExamId.get(i).get("member_id").toString());
            }
            List<Map<String, Object>> userKpIds = sysDepartmentFeign.getUserKpIds(userIds);
            for (Map<String, Object> map : userKpIds) {
                kpIds.add(map.get("kp_id").toString());
            }
            question.setKpIds(kpIds);
        }
        IPage<QuestionManage> byPage = questionManageService.findByPage(question);
        List<QuestionManage> records = byPage.getRecords();
        List<Long> questionIdList = records.stream().map(QuestionManage::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(questionIdList)) {
            Map<Long, String> questionIdKnowledgeNameMap = questionManageService.getQuestionIdKnowledgeNameMap(questionIdList);
            records.forEach(e -> {
                e.setKpName(questionIdKnowledgeNameMap.get(e.getId()));
            });
        }
        return byPage;
    }

    @Autowired
    QuestionKpRelManageDao questionKpRelManageDao;
    @Autowired
    QuestionManageDao questionManageDao;


    /**
     * @author:胡立涛
     * @description: TODO 在线交流使用，试题列表
     * @date: 2025/1/14
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/questionmanage/onlinQuestions")
    public ApiResult onlinQuestions(@RequestBody Map map) {
        try {
            int page = Integer.parseInt(map.get("page").toString());
            int size = Integer.parseInt(map.get("size").toString());
            String sencesName = map.get("sencesName") == null ? "" : map.get("sencesName").toString();
            String kpids = map.get("kpIds") == null ? "" : map.get("kpIds").toString();
            List<String> kpIdList = null;
            if (kpids != "") {
                String[] kpIdArr = kpids.split(",");
                kpIdList = new ArrayList<>();
                for (int i = 0; i < kpIdArr.length; i++) {
                    kpIdList.add(kpIdArr[i]);
                }
            }
            QuestionManage questionManage = new QuestionManage();
            questionManage.setDifficulty(null);
            questionManage.setPdType(null);
            questionManage.setType(null);
            if (sencesName != "") {
                questionManage.setQuestion(sencesName);
            }
            if (kpIdList != null) {
                questionManage.setKpIds(kpIdList);
            }
            questionManage.setCurrent(page);
            questionManage.setSize(size);
            IPage<QuestionManage> ipage = questionManageService.findByPage(questionManage);
            if (ipage != null && ipage.getRecords() != null && ipage.getRecords().size() > 0) {
                List<QuestionManage> records = ipage.getRecords();
                List<Long> questionIdList = new ArrayList<>();
                for (QuestionManage bean : records) {
                    questionIdList.add(bean.getId());
                }
                Map<Long, String> questionIdKnowledgeNameMap = questionManageService.getQuestionIdKnowledgeNameMap(questionIdList);
                for (QuestionManage bean : records) {
                    bean.setKpName(questionIdKnowledgeNameMap.get(bean.getId()));
                }
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", ipage);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * 试题分页查询
     *
     * @param params
     * @return
     */
    @GetMapping("/question")
    public IPage<QuestionManage> findQuestionPage(@RequestParam Map<String, Object> params) {
        int start = Integer.parseInt(params.get(PageUtil.START).toString());
        int size = Integer.parseInt(params.get(PageUtil.LENGTH).toString());
        IPage<QuestionManage> questionPage = questionManageService.findAll(new Page<>((start / size) + 1, size), params);
        List<QuestionManage> questionList = questionPage.getRecords();
        if (CollectionUtils.isNotEmpty(questionList)) {
            //提取批量查询条件
            List<Long> directIdList = new ArrayList<>();
            List<Long> questionIdList = new ArrayList<>();
            for (QuestionManage question : questionList) {
                questionIdList.add(question.getId());
                directIdList.add(question.getDirectId());
            }
            //批量查询字典
            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("dictType", "question_type");
            List<Dict> dictList = manageBackendFeign.findDict(mapParam);
            Map<String, String> dictMap = dictList.stream()
                    .collect(Collectors.toMap(Dict::getDictValue, Dict::getDictName, (o, u) -> o));
            //批量查询情报方向
            Map<Long, String> intDirectMap = manageBackendFeign.findIntDirectMap(directIdList);
            intDirectMap.put(null, "无");
            intDirectMap.put(0L, "无");
            Map<Long, String> questionIdKnowledgeNameMap = questionManageService.getQuestionIdKnowledgeNameMap(questionIdList);

            //统一设置
            questionList.forEach(question -> {
                question.setTypeName(dictMap.get(String.valueOf(question.getType())));
                question.setDirectName(intDirectMap.get(question.getDirectId()));
                question.setKpName(questionIdKnowledgeNameMap.get(question.getId()));
            });
        }
        return questionPage;

    }


    @ApiOperation("添加试题")
    @LogAnnotation(module = LogModule.ADD_QUESTION)
    @PostMapping("/question")
    public ApiResult add(@RequestBody QuestionManage question) {
        try {
            if (CollectionUtils.isEmpty(question.getKpIds())) {
                throw new IllegalArgumentException("请选择知识点。。。");
            }
            question.setCreateTime(new Date());
            question.setUpdateTime(new Date());
            question.setCreator(AppUserUtil.getLoginAppUser().getId());
            if (question.getType() == 7 && Validator.isNull(question.getModelUrl())) {
                question.setModelUrl(ExamConstants.model_url);
            }
            String answer = question.getAnswer();
            JSONObject jsonObject = new JSONObject();
            if (question.getType() != 9) {
                jsonObject = JSONObject.parseObject(answer);
                if (ObjectUtil.isEmpty(jsonObject.get("url")) && question.getType() != 4) {
                    JSONObject js = new JSONObject();
                    js.put("text", jsonObject.get("text"));
                    js.put("url", new String[0]);
                    question.setAnswer(js.toJSONString());
                }
            }

            question.setCode(UUID.randomUUID().toString().replaceAll("-", ""));
            if (question.getType() == 4) {
                if (jsonObject.containsKey("fill")) {
                    Object fill = jsonObject.get("fill");
                    question.setAnswer(fill.toString());
                }
            }
            if (question.getType() == 1 || question.getType() == 2) {
                String questionText = question.getOptions();
                JSONObject jsonObject1 = JSONObject.parseObject(questionText);
                if (ObjectUtil.isNotEmpty(jsonObject.get("E"))) {
                    JSONObject obj = JSONObject.parseObject(jsonObject1.get("E") + "");
                    Object text = obj.get("text");
                    Object url = obj.get("url");
                    if (ObjectUtil.isEmpty(text) && ObjectUtil.isEmpty(url)) {
                        jsonObject1.remove("E");
                    }
                }
                if (ObjectUtil.isNotEmpty(jsonObject1.get("F"))) {
                    JSONObject obj = JSONObject.parseObject(jsonObject1.get("F") + "");
                    Object text = obj.get("text");
                    Object url = obj.get("url");
                    if (ObjectUtil.isEmpty(text) && ObjectUtil.isEmpty(url)) {
                        jsonObject1.remove("F");
                    }
                }
                question.setOptions(jsonObject1.toJSONString());
            }
            if (question.getType() == 7) {
                AnalysisFramework ana = analysisFrameworkService.getById(question.getModelId());
                question.setModelUrl(ana.getDetail());
            }
            question.setStatus(ExamConstant.QUESTION_NOT_USED);
            boolean res = questionManageService.saveQuestion(question);
            JSONObject jss = new JSONObject();
            jss.put("success", res);
            if (res) {
                jss.put("id", question.getId());
                return ApiResultHandler.buildApiResult(200, "添加成功", jss);
            } else {
                jss.put("id", null);
                return ApiResultHandler.buildApiResult(400, "添加失败", jss);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 修改问题的思路：不在原来的试题上修改，先复制一条->更新版本->存入数据库->在新版本上进行修改
     * 这样保证使用该试题组卷的试卷不受影响
     *
     * @param question
     * @return
     */
    @LogAnnotation(module = LogModule.UPDATE_QUESTION)
    @PutMapping("/updateQuestion")
    public ApiResult updateQuestion(@RequestBody QuestionManage question) {

        if (ObjectUtils.isNotNull(question) && StringUtils.isNotEmpty(question.getErrorText())) {
            //修改错题的添加
            question.setCreateTime(new Date());
            question.setUpdateTime(new Date());
            question.setCreator(AppUserUtil.getLoginAppUser().getId());
            if (question.getType() == 7 && Validator.isNull(question.getModelUrl())) {
                question.setModelUrl(ExamConstants.model_url);
            }
            question.setCode(UUID.randomUUID().toString().replaceAll("-", ""));


            if (questionManageService.save(question)) {
                questionErrorService.removeById(question.getId());
                return ApiResultHandler.buildApiResult(200, "添加成功", null);
            }
        }
        if (question.getType() == 4) {
            String answer = question.getAnswer();
            JSONObject jsonObject = JSONObject.parseObject(answer);
            if (jsonObject.containsKey("fill")) {
                Object fill = jsonObject.get("fill");
                question.setAnswer(fill.toString());
            }
        }
        if (question.getType() == 7) {
            AnalysisFramework ana = analysisFrameworkService.getById(question.getModelId());
            question.setModelUrl(ana.getDetail());
        }
        if (question.getType() == 1 || question.getType() == 2) {
            String questionText = question.getOptions();
            JSONObject jsonObject = JSONObject.parseObject(questionText);
            if (ObjectUtil.isNotEmpty(jsonObject.get("E"))) {
                JSONObject obj = JSONObject.parseObject(jsonObject.get("E") + "");
                Object text = obj.get("text");
                Object url = obj.get("url");
                if (ObjectUtil.isEmpty(text) && ObjectUtil.isEmpty(url)) {
                    jsonObject.remove("E");
                }
            }
            if (ObjectUtil.isNotEmpty(jsonObject.get("F"))) {
                JSONObject obj = JSONObject.parseObject(jsonObject.get("F") + "");
                Object text = obj.get("text");
                Object url = obj.get("url");
                if (ObjectUtil.isEmpty(text) && ObjectUtil.isEmpty(url)) {
                    jsonObject.remove("F");
                }
            }
            question.setOptions(jsonObject.toJSONString());
        }

        if (question.getType() != 9) {
            String answer = question.getAnswer();
            JSONObject jsonObject = JSONObject.parseObject(answer);
            if (ObjectUtil.isEmpty(jsonObject.get("url")) && question.getType() != 4) {
                JSONObject js = new JSONObject();
                js.put("text", jsonObject.get("text"));
                js.put("url", new String[0]);
                question.setAnswer(js.toJSONString());
            }
        }

        question.setUpdateTime(new Date());
        question.setCreator(AppUserUtil.getLoginAppUser().getId());
        boolean ifupdate = questionManageService.updateQuestion(question);
        if (ifupdate) {
            QueryWrapper<QuestionKpRelManage> questionKpRelQueryWrapper = new QueryWrapper<>();
            questionKpRelQueryWrapper.eq("question_id", question.getId());
            questionKpRelManageService.remove(questionKpRelQueryWrapper);
            List<String> kIds = question.getKpIds();
            kIds.forEach(e -> {
                QuestionKpRelManage questionKpRel = new QuestionKpRelManage();
                questionKpRel.setKpId(e);
                questionKpRel.setQuestionId(question.getId());
                questionKpRelManageService.save(questionKpRel);
            });
        }
        return ApiResultHandler.buildApiResult(200, "修改成功", ifupdate);
    }

    /**
     * 根据查询试题
     *
     * @param id
     * @return
     */
    @GetMapping("/questionId")
    public ApiResult findOnlyQuestionId(@RequestParam Long id, @RequestParam(required = false) Boolean manage) {
        if (manage == null || !manage) {
            return questionService.findOnlyQuestionId(id);
        }
        QuestionManage res = questionManageService.getById(id);
        res.setLocalUrlPrefix(localUrlPrefix);
        QueryWrapper<QuestionKpRelManage> questionKpRelQueryWrapper = new QueryWrapper<>();
        questionKpRelQueryWrapper.eq("question_id", id);
        List<QuestionKpRelManage> list = questionKpRelManageService.list(questionKpRelQueryWrapper);
        List<String> kpIdList = list.stream().map(QuestionKpRelManage::getKpId).collect(Collectors.toList());
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
        String kpnameByQuestionId = QuestionManageUtils.getKpNamesByQuestion(res);
        res.setKpName(kpnameByQuestionId);
        res.setKpIds(kpIdList);
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
            Arrays.fill(s, "");
            jso.put("text", s);
        }
        res.setStuAnswer(jso.toJSONString());
//        // 查看该试题是否被收藏
//        LoginAppUser loginAppUser = sysDepartmentFeign.getLoginAppUser();
//        QueryWrapper<CollectionQuestion> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("user_id", loginAppUser.getId());
//        queryWrapper.eq("question_id", id);
//        CollectionQuestion one = collectionQuestionService.getOne(queryWrapper);
//        long collectionId = one == null ? 0L : one.getId();
//        res.setCollectionId(collectionId);
        return ApiResultHandler.buildApiResult(200, "查询成功", res);
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据试题ids查询试题列表信息
     * @date: 2022/8/12
     * @param: [ids] 格式 1,2,3
     * @return: com.cloud.core.ApiResult
     */
    @GetMapping("/questionIds")
    public ApiResult findQuestionList(@RequestParam String ids) {
        if (ids.isEmpty()) {
            return ApiResultHandler.buildApiResult(100, "参数ids为空", null);
        }
        List<QuestionManage> rList = new ArrayList<>();
        String[] idsArr = ids.split(",");
        for (String s : idsArr) {
            Long id = s == null ? null : Long.valueOf(s);
            QuestionManage res = questionManageService.getById(id);
            if (res == null) {
                continue;
            }
            res.setLocalUrlPrefix(localUrlPrefix);
            QueryWrapper<QuestionKpRelManage> questionKpRelQueryWrapper = new QueryWrapper<>();
            questionKpRelQueryWrapper.eq("question_id", id);
            List<QuestionKpRelManage> list = questionKpRelManageService.list(questionKpRelQueryWrapper);
            List<String> ll = new ArrayList<>();
            list.forEach(e -> {
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
            String kpnameByQuestionId = QuestionManageUtils.getKpNamesByQuestion(res);
            res.setKpName(kpnameByQuestionId);
            res.setKpIds(ll);
            res.setFileAddr(fileServer);
            if (res.getType() == 7 || res.getType() == 11) {
                if (ObjectUtil.isNotNull(res.getModelId())) {
                    res.setModelName(analysisFrameworkService.getById(res.getModelId()).getName());
                }
            }
            rList.add(res);
        }
        return ApiResultHandler.buildApiResult(200, "查询成功", rList);
    }

    /**
     * 参数: 通过试题的ID：试题删除
     * state = 1  删除导入成功列表中的试题 ，0 删除导入失败的试题
     *
     * @return 返回值1：删除成功，0：删除失败
     */
    @LogAnnotation(module = LogModule.DEL_QUESTION)
    @PostMapping("/question/{id}")
    public ApiResult deleteQuestionById(@PathVariable Long id) {

        //      试题中包含的所有的文件 。  试题状态：1：被使用；0:未被使用
        QuestionDownThread findFiles = new QuestionDownThread();
        QuestionManage question = questionManageService.getById(id);
        List<String> filePathList = new ArrayList<>(findFiles.wrapQuestionFiles(question));
        //删除试题下面图片的链接，如果file表里面的useTimes（使用次数）等于1，直接将文件也一并删除，减少服务器空间占用
        int isDelete = questionManageService.deleteQuestion(question);
        //未被使用的，同时删除试题关联文件
        if (isDelete == 1 && !ExamConstant.isQuestionUsed(question.getStatus())) {
            removeFiles(filePathList);
        }
        QueryWrapper<QuestionKpRelManage> questionKpRelQueryWrapper = new QueryWrapper<>();
        questionKpRelQueryWrapper.eq("question_id", question.getId());
        questionKpRelManageService.remove(questionKpRelQueryWrapper);
        return ApiResultHandler.buildApiResult(200, "删除成功", isDelete);
    }

    @PostMapping("/question/deleteBatch")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult deleteBatch(@RequestBody List<Long> idList) {
        if (CollectionUtils.isNotEmpty(idList)) {
            List<QuestionManage> questionManageList = questionManageService.listByIds(idList);
            if (CollectionUtils.isNotEmpty(questionManageList)) {
                //删除未使用的试题附件
                QuestionDownThread findFiles = new QuestionDownThread();
                List<String> filePathList = questionManageList.stream()
                        .filter(e -> !ExamConstant.isQuestionUsed(e.getStatus()))
                        .reduce(new ArrayList<>(), (list, question) -> {
                            list.addAll(findFiles.wrapQuestionFiles(question));
                            return list;
                        }, (o, u) -> {
                            o.addAll(u);
                            return o;
                        });
                removeFiles(filePathList);
                questionManageService.removeByIds(idList);
                LambdaQueryWrapper<QuestionKpRelManage> query = new LambdaQueryWrapper<>();
                query.in(QuestionKpRelManage::getQuestionId, idList);
                questionKpRelManageService.remove(query);
            }
        }
        return ApiResultHandler.buildApiResult(200, "删除成功", null);
    }

    private void removeFiles(List<String> filePathList) {
        if (CollectionUtils.isNotEmpty(filePathList)) {
            filePathList.forEach(path -> {
                try {
                    fastFileStorageClient.deleteFile(path);
                    log.info("删除了试题关联附件：{}", path);
                } catch (Exception e) {
                    log.error("删除相关附件异常，附件地址:[{}] ,异常信息:{}", path, e.getMessage());
                    log.error("删除相关附件异常", e);
                }
            });
        }
    }

    /**
     * 批量导出试题成excle
     *
     * @param ids 试题的ID值
     *            返回值：EXcle流信息
     */
    @PostMapping("/question/exportExcel")
    public void exportExcel(@RequestParam Long[] ids) throws FileNotFoundException {
        Set<Map<String, Object>> questionSet = new HashSet<>();
        //根据id查找试题列表
        for (long id : ids) {
            Map<String, Object> questionMap = new HashMap<>();
            QuestionManage question = questionManageService.getById(id);
            questionMap.put("question", question.getQuestion());
            questionMap.put("options", question.getOptions());
            questionMap.put("difficulty", question.getDifficulty());
            questionMap.put("type", question.getType());
            questionMap.put("answer", question.getAnswer());
            questionMap.put("analysis", question.getAnalysis());

            String kpnameByQuestionId = QuestionManageUtils.getKpNamesByQuestion(question);
            questionMap.put("kpName", kpnameByQuestionId);
            /*if (null !=question.getKpId() && null != manageBackendFeign.getKnowledgePointsById(question.getKpId())) {
                questionMap.put("kpName",manageBackendFeign.getKnowledgePointsById(question.getKpId()).getPointName());
            }*/

            if (null != question.getDirectId() && null != manageBackendFeign.findIntDirectById(question.getDirectId()).getName()) {
                questionMap.put("directName", manageBackendFeign.findIntDirectById(question.getDirectId()).getName());
            }
            questionSet.add(questionMap);
        }
        // String uuidName = UUID.randomUUID().toString();
        String exportFileUrl = configurableApplicationContext.getEnvironment().getProperty("exportFileUrl");
        String uuidName = String.valueOf(System.currentTimeMillis());
        File file = new File(exportFileUrl + uuidName + ".xls");


        FileOutputStream outputStream = new FileOutputStream(file);
        Map<String, String> map = new HashMap<>();
        //map.put("id","id");
        map.put("question", "题目");
        map.put("options", "选项");
        map.put("difficulty", "难度");
        map.put("type", "题型");
        map.put("answer", "答案");
        map.put("analysis", "解析");
        map.put("kpName", "知识点");
        map.put("directName", "情报方向");
        ExcelUtil.exportExcel(map, questionSet, outputStream);
    }

    @Autowired
    KnowledgeFeign knowledgeFeign;

    /**
     * 从excel导入试题
     *
     * @param multipartFile
     * @return
     */
    @PostMapping("/uploadExcel")
    public ApiResult uploadExcel(@RequestParam(name = "file") MultipartFile multipartFile,
                                 @RequestParam(value = "use", defaultValue = "0") Integer use) {
        ExcelImportByPicture<QuestionExcel> importer = new ExcelImportByPicture<>(QuestionExcel.class);
        try {
            Map<String, List<QuestionExcel>> quesionMap = importer.readExcelImageAndData(multipartFile, 0);
            //查询所有的知识点信息
            Map<String, String> knowledgePointNameMap = new HashMap<>();
            List<Map> list = knowledgeFeign.getKnowledgePointListFeign();
            if (list != null && list.size() > 0) {
                for (Map map : list) {
                    knowledgePointNameMap.put(map.get("pointname").toString(), map.get("id").toString());
                }
            }
            Map<String, String> nameValueMap = manageBackendFeign.getNameValueMap("question_type");
            Map<String, List<QuestionManage>> questionContextList = questionManageService.getQuestionList();
            String typeNames = nameValueMap.keySet().stream().collect(Collectors.joining(","));
            List<QuestionManage> questions = new ArrayList<>();
            for (Map.Entry<String, List<QuestionExcel>> entry : quesionMap.entrySet()) {
                if (!nameValueMap.containsKey(entry.getKey())) {
                    return ApiResultHandler.buildApiResult(500, "不支持的题型：" + entry.getKey() + "，仅支持：" + typeNames, null);
                }
                String type = nameValueMap.get(entry.getKey());
                List<QuestionExcel> questionExcels = entry.getValue();
                //用于校验文件中存在重复的题干
                Map<String, Map<String, Integer>> findRepeat = new HashMap<>();
                for (int i = 0; i < questionExcels.size(); i++) {
                    QuestionExcel questionExcel = questionExcels.get(i);
                    QuestionManage questionManage = new QuestionManage();
                    questionManage.setType(Integer.valueOf(type));
                    if (StringUtils.isBlank(questionExcel.getQuestion())) {
                        return ApiResultHandler.buildApiResult(500, entry.getKey() + "第" + (i + 2) + "行，缺少题干", null);
                    }
                    //校验答案
                    if (StringUtils.isBlank(questionExcel.getAnswer())) {
                        return ApiResultHandler.buildApiResult(500, entry.getKey() + "第" + (i + 2) + "行，缺少答案", null);
                    }
                    if (!questionExcel.validAnswer(type)) {
                        return ApiResultHandler.buildApiResult(500, entry.getKey() + "第" + (i + 2) + "行，答案不符合规范", null);
                    }
                    questionManage.setAnswer(questionExcel.convertAnswer(type));
                    if ("1".equals(type) || "2".equals(type)) {
                        //校验选择题选项
                        if (questionExcel.isOptionsBlank()) {
                            return ApiResultHandler.buildApiResult(500, entry.getKey() + "第" + (i + 2) + "行，至少应该包含一个选项", null);
                        }
                    }
                    questionManage.setOptions(questionExcel.convertOptions(type));

                    if (StringUtils.isBlank(questionExcel.getKnowledgePoints())) {
                        return ApiResultHandler.buildApiResult(500, entry.getKey() + "第" + (i + 2) + "行，缺少知识点", null);
                    }
                    //将知识点转化为id
                    String[] knowledgePoints = questionExcel.getKnowledgePoints().split("[,，]");
                    List<String> kpIds = new ArrayList<>(knowledgePoints.length);
                    for (String kp : knowledgePoints) {
                        if (!knowledgePointNameMap.containsKey(kp)) {
                            return ApiResultHandler.buildApiResult(500, entry.getKey() + "第" + (i + 2) + "行，系统中不存在知识点：" + kp, null);
                        }
                        String kpid = knowledgePointNameMap.get(kp);
                        kpIds.add(kpid);
                        //校验excel本身题干重复（知识点和题干相同）
                        Integer oldIndex = findExcelRepeatQuestion(findRepeat, kp, questionExcel.getQuestion(), i);
                        if (oldIndex != null) {
                            return ApiResultHandler.buildApiResult(500, entry.getKey() + "第" + (i + 2) + "," + (oldIndex + 2) + "行，题目重复", null);
                        }
                        //校验题干与库中重复（知识点、题干、题型相同）
                        boolean databaseRepeatQuestion = findDatabaseRepeatQuestion(questionContextList, kpid, questionExcel.getQuestion(), type);
                        if (databaseRepeatQuestion) {
                            return ApiResultHandler.buildApiResult(500, entry.getKey() + "第" + (i + 2) + "行，系统中已存在相同的题目", null);
                        }
                    }
                    questionManage.setKpIds(kpIds);
                    //校验判读类型，否则必须为“可见光、红外、SAR和其他”的其中一个
                    if (StringUtils.isBlank(questionExcel.getPdType())) {
                        return ApiResultHandler.buildApiResult(500, entry.getKey() + "第" + (i + 2) + "行，缺少判读类型", null);
                    }
                    if (!questionExcel.validPdType()) {
                        return ApiResultHandler.buildApiResult(500, entry.getKey() + "第" + (i + 2) + "行，非法的判读类型，只能为可见光、红外、SAR或其他", null);
                    }
                    questionManage.setPdType(questionExcel.getPdType());
                    questionManage.setQuestion(questionExcel.convertQuestion());
                    questionManage.setAnalysis(questionExcel.convertAnalysis());
                    questionManage.setDifficulty(questionExcel.convertDifficulty());
                    questions.add(questionManage);
                }
            }
            log.info("从excel中读到{}道题", questions.size());
            saveQuestions(use, questions, multipartFile.getOriginalFilename());
            return ApiResultHandler.success();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("从excel导入试题失败", e);
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    private Integer findExcelRepeatQuestion(Map<String, Map<String, Integer>> findRepeat, String kp, String question, int index) {
        Map<String, Integer> questionMap = findRepeat.computeIfAbsent(kp, k -> new HashMap<>());
        Integer oldIndex = questionMap.put(question, index);
        return oldIndex;
    }

    private boolean findDatabaseRepeatQuestion(Map<String, List<QuestionManage>> questionContextList, String kpid, String question, String type) {
        List<QuestionManage> questionManages = questionContextList.get(question);
        if (CollectionUtils.isNotEmpty(questionManages)) {
            for (QuestionManage q : questionManages) {
                if (q.getType() == Integer.parseInt(type)) {
                    return q.getKpIds().contains(kpid);
                }
            }
        }
        return false;
    }

    /**
     * 查看用户导入的错题
     * 参数：试题ID
     * 返回值：返回错误试题实体
     *
     * @throws IOException
     */
    @RequestMapping(value = "/question/error", method = RequestMethod.GET)
    public ApiResult questionErrorList(Long id) throws IOException {

        QueryWrapper<QuestionError> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        QuestionError questionError = questionErrorService.getOne(queryWrapper, true);
        return new ApiResult(200, "查询成功", questionError);

    }

    /**
     * 查看用户导入的错题
     *
     * @throws IOException
     */
    @GetMapping("/question/error/list")
    public IPage<QuestionError> questionErrorList(@RequestParam Map<String, Object> params) {
        int start = Integer.parseInt(params.get(PageUtil.START).toString());
        int size = Integer.parseInt(params.get(PageUtil.LENGTH).toString());
        IPage<QuestionError> questionPage = questionErrorService.findAll(new Page<>((start / size) + 1, size), params);

        List<QuestionError> reQuestionPageList = new ArrayList<>();


        List<QuestionError> questionList = questionPage.getRecords();


        for (QuestionError question : questionList) {
            QueryWrapper<QuestionKpRelManage> questionKpRelQueryWrapper = new QueryWrapper<>();
            questionKpRelQueryWrapper.eq("question_id", question.getId());
            List<QuestionKpRelManage> list = questionKpRelManageService.list(questionKpRelQueryWrapper);
            StringBuilder kname = new StringBuilder();
            for (QuestionKpRelManage ql : list) {
                KnowledgePoints knowledgePoints = manageBackendFeign.getKnowledgePointsById(ql.getKpId());
                if (ObjectUtils.isNotNull(knowledgePoints)) {
                    kname.append(knowledgePoints.getPointName());
                }
            }
            question.setKpName(kname.toString());
            //根据字典来设置内容
            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("dictType", "question_type");
            mapParam.put("dictValue", String.valueOf(question.getType()));
            List<Dict> dictList = manageBackendFeign.findDict(mapParam);
            if (ObjectUtils.isNotNull(dictList)) {
                for (Dict dict : dictList) {
                    question.setTypeName(dict.getDictName());
                }
            }
            if (question.getDirectId() == null || question.getDirectId() == 0) {
                question.setDirectName("无");
            } else {
                IntDirect intDirect = manageBackendFeign.findIntDirectById(question.getDirectId());
                if (ObjectUtils.isNotNull(intDirect)) {
                    question.setDirectName(intDirect.getName());
                }
            }
            reQuestionPageList.add(question);
        }

        return questionPage.setRecords(reQuestionPageList);
    }


    /**
     * 将word文件导入到系统中
     *
     * @param multipartFile
     * @throws IOException
     */
    @PostMapping("/question/importWord")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult importWord(@RequestParam(name = "file") MultipartFile multipartFile,
                                @RequestParam(value = "use", defaultValue = "0") Integer use) {
        try {
            Map<String, String> KnowledgePoints = new HashMap<>();
            List<Map> list = knowledgeFeign.getKnowledgePointListFeign();
            if (list != null && list.size() > 0) {
                for (Map map : list) {
                    KnowledgePoints.put(map.get("pointname").toString(), map.get("id").toString());
                }
            }
            Map<String, List<QuestionManage>> questionContextList = questionManageService.getQuestionList();
            //情报方向暂不设置
            InputStream inputStream;
            List<QuestionManage> questions = new ArrayList<>();
            String markedFileUrl;
            String originalFilename = multipartFile.getOriginalFilename();
            try {
                inputStream = multipartFile.getInputStream();
                markedFileUrl = WordImportUtil.importWordAndMark(inputStream, originalFilename, KnowledgePoints, questionContextList, questions);
            } catch (Exception e) {
                log.error("解析word异常", e);
                return ApiResultHandler.buildApiResult(500, "解析word异常", null);
            }
            //将list对象存入数据库中 cv
            if (StringUtils.isEmpty(markedFileUrl)) {
                saveQuestions(use, questions, originalFilename);
                return new ApiResult(200, "导入完成", null, true);
            } else {
                return new ApiResult(101, "文档中包含不能导入的试题", fileServer + markedFileUrl, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResult(500, "操作异常", e.toString());
        }
    }

    private void saveQuestions(Integer use, List<QuestionManage> questions, String originalFilename) {
        Date now = new Date();
        Long userid = AppUserUtil.getLoginAppUser().getId();
        for (QuestionManage question : questions) {
            question.setUpdateTime(now);
            question.setCreateTime(now);
            question.setCreator(userid);
            question.setCode(UUID.randomUUID().toString().replaceAll("-", ""));
            question.setVersion(1);
            //用途
            question.setUse(use);
            question.setDirectId(0L);
            question.setStatus(ExamConstant.QUESTION_NOT_USED);
            question.setBatch(originalFilename);
            if (StringUtils.isBlank(question.getPdType())) {
                question.setPdType("其他");
            }
            //难度默认为一般
            if (question.getDifficulty() == null) {
                question.setDifficulty(0.3);
            }
        }
        questionManageService.saveBatch(questions);
        List<QuestionKpRelManage> questionKpRelList = new ArrayList<>();
        questions.forEach(question -> {
            List<String> kpIds = question.getKpIds();
            if (CollectionUtils.isNotEmpty(kpIds)) {
                Long questionId = question.getId();
                kpIds.forEach(e -> {
                    QuestionKpRelManage questionKpRel = new QuestionKpRelManage();
                    questionKpRel.setQuestionId(questionId);
                    questionKpRel.setKpId(e);
                    questionKpRelList.add(questionKpRel);
                });
            }
        });
        questionKpRelManageService.saveBatch(questionKpRelList);
    }

    @ApiOperation(value = "查询所有重复试题")
    @GetMapping("/getRepeatQuestionBytext")
    public ApiResult getRepeatQuestionBytext(@RequestParam(required = false) Long id, @RequestParam(required = true) String text) {
        String qustionText = "";
        /*if(!Validator.isEmpty(id)){
            QuestionError questionError = questionErrorService.getById(id);
            String question1 = questionError.getQuestion();
            qustionText = JSONObject.parseObject(question1).getString("text");

        }else{
        }*/
        qustionText = text;
        List<QuestionManage> ll = new ArrayList<>();
        List<QuestionManage> list = questionManageService.list();
        for (QuestionManage e : list) {
            String question = e.getQuestion();
            try {
                JSONObject jsonObject = JSONObject.parseObject(question);
                String text1 = jsonObject.getString("text");
                if (text1.equals(qustionText)) {
                    ll.add(e);
                }
            } catch (Exception error) {
                log.error("解析试题question失败，id==" + e.getId());
            }
        }
        if (!Validator.isEmpty(id)) {
            ll.removeIf(e -> e.getId().equals(id));
        }

        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询所有重复试题成功", ll);
    }

    @GetMapping("selectByDirectId/{id}")
    public Boolean selectByDirectId(@PathVariable Long id) {
        return questionManageService.selectByDirectId(id).size() > 0;
    }

//    @ApiOperation(value = "根据条件导出试题")
//    @RequestMapping(value = "exportQuestionByParam", method = RequestMethod.POST)
//    public void exportQuestionByParam(@RequestBody Map<String, List<Object>> map, HttpServletResponse response, HttpServletRequest request) {
//        List<Object> types = map.get("types");
//        List<Object> kpIds = map.get("kpIds");
//        List<Integer> l1 = new ArrayList<>();
//        types.stream().forEach(e -> {
//            l1.add(Integer.parseInt(e.toString()));
//        });
//        List<Long> l2 = new ArrayList<>();
//        kpIds.stream().forEach(e -> {
//            l2.add(Long.parseLong(e.toString()));
//        });
//        QueryWrapper<QuestionManage> questionQueryWrapper = new QueryWrapper<>();
//        questionQueryWrapper.in("type", l1);
//        List<QuestionManage> qlist = questionManageService.list(questionQueryWrapper);
//        QueryWrapper<QuestionKpRelManage> queryWrapper = new QueryWrapper<>();
//        queryWrapper.in("kp_id", l2);
//        List<QuestionKpRelManage> klist = questionKpRelManageService.list(queryWrapper);
//        List<QuestionManage> collect = qlist.stream().filter((question) -> klist.stream().map(QuestionKpRelManage::getQuestionId).collect(Collectors.toList()).contains(question.getId())).collect(Collectors.toList());
//        collect.stream().forEach(e -> {
//            e.setCreateTime(null);
//            e.setUpdateTime(null);
//            QueryWrapper<QuestionKpRelManage> questionKpRelQueryWrapper = new QueryWrapper<>();
//            questionKpRelQueryWrapper.eq("question_id", e.getId());
//            List<QuestionKpRelManage> list1 = questionKpRelManageService.list(questionKpRelQueryWrapper);
//            list1.stream().forEach(q -> e.getKpIds().add(q.getKpId()));
//        });
//
//        /**
//         *  最后导入的ZIP文件的位置
//         */
//        String targetFolder = fastdfsConfig.DIR + FastdfsConfig.TARGET_FOLDER_PREFIX + System.currentTimeMillis();
//
//        /**
//         *  1. 线程池的方式，去下载相关的文件
//         *  3. 生成试题中所有的附件信息
//         */
//        String tempZipPath = targetFolder + ".zip";
//        try {
//            final CountDownLatch downLatch = new CountDownLatch(collect.size());
//            WorkorderExecutors executors = WorkorderExecutors.getInstance();
//            if (CollectionUtils.isNotEmpty(collect)) {
//                collect.stream().forEach(q -> {
//                    QuestionDownThread thread = new QuestionDownThread(targetFolder, q,
//                            downLatch, fastFileStorageClient, fileClientFeign);
//                    executors.customerService.execute(thread);
//                });
//            }
//            downLatch.await();
//            /**
//             *
//             * 再导出问题JSON
//             */
//            Gson gson = new Gson();
//            String question_JSON = gson.toJson(collect);
//            String jsonFilePath = targetFolder + "/";
//            OutJsonUtils.createJsonFile(question_JSON, jsonFilePath, fileName, false);
//            logger.info("[SUCCESS 试题附件生成本地文件结束 ]");
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("[FAIL 试题附件生成本地文件结束 ]");
//            return;
//        }
//        /**
//         *  生成ZIP文件
//         */
//        BufferedInputStream bufferIn = null;
//        OutputStream outputStream = null;
//        try {
//            /**
//             *  服务器端本地 先生成ZIP文件
//             */
//            createZip(targetFolder, tempZipPath, "", response);
//            /**
//             *  最后以流的方式 返回给前端
//             */
//            logger.info("START >>>>>>>>>>>>>>>>>> 浏览器");
//            File tempZipFile = new File(tempZipPath);
//            String tempZipFileName = tempZipFile.getName();
//            String userAgent = request.getHeader("User-Agent").toUpperCase();
//            String fileNameUnEncode = URLEncoder.encode(tempZipFileName, "UTF-8");
//            if (userAgent.indexOf("MSIE") > -1 || (userAgent.indexOf("GECKO") > 0 && userAgent.indexOf("RV:11") > 0)) {
//                fileNameUnEncode = URLEncoder.encode(tempZipFileName, "UTF-8");
//            } else {
//                fileNameUnEncode = new String(fileNameUnEncode.getBytes("UTF-8"), "iso-8859-1");
//            }
//            //显示响应头,将文件名传给前端
//            response.reset();
//            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
//            response.setCharacterEncoding("UTF-8");
//            // 解决前端访问跨域问题
////            response.setHeader("Access-Control-Allow-Origin", "*");
//            response.setContentType("multipart/form-data");
//            response.addHeader("Content-Disposition", "attachment;filename=" + fileNameUnEncode);
//            bufferIn = new BufferedInputStream(new FileInputStream(tempZipPath));
//            outputStream = new BufferedOutputStream(response.getOutputStream());
//            byte[] buff = new byte[1024 * 1024 * 10];
//            int n;
//            while ((n = bufferIn.read(buff)) != -1) {
//                outputStream.write(buff, 0, n);
//            }
//            logger.info("SUCCESS >>>>>>>>>>>>>>>>>> 浏览器");
//        } catch (Exception e) {
//            logger.error("生产ZIP文件异常:{}", e.getMessage());
//            e.printStackTrace();
//            throw new IllegalArgumentException("试题迁出ZIP异常：" + e.getMessage());
//        } finally {
//            try {
//                if (Objects.nonNull(outputStream)) {
//                    outputStream.flush();
//                    outputStream.close();
//                }
//                if (Objects.nonNull(bufferIn)) {
//                    bufferIn.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    /**
     * 创建ZIP文件
     *
     * @param sourcePath
     * @param filePathAndName
     * @param httpResponse
     */
    public void createZip(String sourcePath, String tempZipPath, String filePathAndName, HttpServletResponse httpResponse) throws Exception {
        ZipOutputStream zos = null;
        OutputStream outstream = null;
        try {
            long startMilis = System.currentTimeMillis();
            log.info("开始生成压缩文件 开始时间: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            outstream = new FileOutputStream(tempZipPath);
            zos = new ZipOutputStream(outstream);
            writeZip(new File(sourcePath), "", zos);
            log.info("[SUCCESS  ZIP 生成本地文件结束 ] {} ", tempZipPath);
            log.info("SUCCESS生成压缩文件 结束时间: {}， 耗时[毫秒]：{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), (System.currentTimeMillis() - startMilis));
        } catch (Exception e) {
            throw new RuntimeException("ZIP 创建失败");
        } finally {
            try {
                if (Objects.nonNull(zos)) {
                    zos.close();
                }
                if (Objects.nonNull(outstream)) {
                    outstream.close();
                }
            } catch (IOException e) {
                log.error("创建ZIP文件失败", e);
                throw new RuntimeException("ZIP 创建失败");
            }
        }
    }


    /**
     * 压缩zip,循环压缩子目录文件
     */
    private static void writeZip(File file, String parentPath, ZipOutputStream zos) {
        if (file.exists()) {
            //处理文件夹
            if (file.isDirectory()) {
                parentPath += file.getName() + File.separator;
                File[] files = file.listFiles();
                if (files.length != 0) {
                    for (File f : files) {
                        writeZip(f, parentPath, zos);
                    }
                } else {       //空目录则创建当前目录
                    try {
                        zos.putNextEntry(new ZipEntry(parentPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    //创建压缩文件
                    ZipEntry ze = new ZipEntry(parentPath + file.getName());
                    //添加压缩文件
                    zos.putNextEntry(ze);
                    byte[] content = new byte[1024];
                    int len;
                    while ((len = fis.read(content)) != -1) {
                        zos.write(content, 0, len);
                        zos.flush();
                    }
                } catch (IOException e) {
                    log.error("创建ZIP文件失败", e);
                } finally {
                    try {
                        if (fis != null) {
                            fis.close();
                        }
                    } catch (IOException e) {
                        log.error("创建ZIP文件失败", e);
                    }
                }
            }
        }

    }


    /*@ApiOperation(value = "根据条件导出试题")
    @RequestMapping(value = "exportQuestionByParam",method = RequestMethod.POST)
    public void exportQuestionByParam(@RequestBody Map<String,List<Object>> map, HttpServletResponse response, HttpServletRequest request){
        List<Object> types = map.get("types");
        List<Object> kpIds = map.get("kpIds");
        List<Integer> l1 = new ArrayList<>();
        types.stream().forEach(e->{
            l1.add(Integer.parseInt(e.toString()));
        });
        List<Long> l2 = new ArrayList<>();
        kpIds.stream().forEach(e->{
            l2.add(Long.parseLong(e.toString()));
        });
        QueryWrapper<QuestionManage> questionQueryWrapper = new QueryWrapper<>();
        questionQueryWrapper.in("type",l1);
        List<QuestionManage> qlist = questionService.list(questionQueryWrapper);
        QueryWrapper<QuestionKpRelManage> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("kp_id",l2);
        List<QuestionKpRelManage> klist = questionKpRelService.list(queryWrapper);
        List<QuestionManage> collect = qlist.stream().filter((question) -> klist.stream().map(QuestionKpRelManage::getQuestionId).collect(Collectors.toList()).contains(question.getId())).collect(Collectors.toList());
        collect.stream().forEach(e->{
            e.setCreateTime(null);
            e.setUpdateTime(null);
            QueryWrapper<QuestionKpRelManage> questionKpRelQueryWrapper = new QueryWrapper<>();
            questionKpRelQueryWrapper.eq("question_id",e.getId());
            List<QuestionKpRelManage> list1 = questionKpRelService.list(questionKpRelQueryWrapper);
            list1.stream().forEach(q->e.getKpIds().add(q.getKpId()));
        });
        Gson gson = new Gson();
        String s = gson.toJson(collect);
        //每个json文件1500条数据
        //计算拆分次数
        int j =( collect.size() + maxNum -1 )/ maxNum ;
        List<List<QuestionManage>> mgList = new ArrayList<>();
        Stream.iterate(0,n->n+1).limit(j).forEach(i->{
            mgList.add(collect.stream().skip(i*maxNum).limit(maxNum).collect(Collectors.toList()));
        });
        CountDownLatch countDownLatch  = new CountDownLatch(mgList.size());
        ExecutorService es = Executors.newFixedThreadPool(mgList.size());
        try {
            mgList.parallelStream().forEach(ll->{
                Question2JsonTask task = new Question2JsonTask(countDownLatch, ll);
                es.submit(task);
            });
            countDownLatch.await();
            System.out.println("生成json文件结束");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            es.shutdown();
        }
        //将生成的所有json文件打包成zip文件
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null ;
        ZipOutputStream zipOutputStream = null ;
        BufferedInputStream bufferedInputStream = null ;
        try {
            fileOutputStream = new FileOutputStream("/questionzip/question.zip");
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
            byte[] bytes = new byte[1024 * 10];
            File file = new File("/questionJson/");
            if(file.isDirectory()){
                File[] list = file.listFiles();
                System.out.println("开始将生成json文件打zip包共"+list.length+"个文件");
                for (File fes : list) {
                    System.out.println("=================="+fes.getName());
                    //File jsonFile = new File(filePath+list[i]);
                    ZipEntry zipEntry = new ZipEntry(fes.getName());
                    zipOutputStream.putNextEntry(zipEntry);
                    //读取待压缩的文件并写进压缩包
                    fileInputStream = new FileInputStream(fes);
                    bufferedInputStream = new BufferedInputStream(fileInputStream,1024*10);
                    int read = 0 ;
                    while ((read = bufferedInputStream.read(bytes,0,1024*10))!=-1){
                        zipOutputStream.write(bytes,0,read);
                    }

                }
                zipOutputStream.closeEntry();
            }
            System.out.println("完成生成json文件打zip包。。。。。。");
        }catch (IOException e){
            logger.error("json文件压缩失败");
        }finally {
            try {
                if(fileInputStream!=null){
                    fileInputStream.close();
                }
                if(bufferedInputStream!=null){
                    bufferedInputStream.close();
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }
        FileInputStream in = null ;
        BufferedOutputStream outputStream = null ;
        try {
            File zipFile = new File("/questionzip/question.zip");
            response.setContentType("APPLICATION/OCTET-STREAM");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(("question.zip").getBytes(), "iso-8859-1"));
            in = new FileInputStream(zipFile);
            outputStream = new BufferedOutputStream(response.getOutputStream());
            byte[] buff = new byte[1024*10];
            int n ;
            while ((n = in.read(buff))!=-1){
                outputStream.write(buff,0,n);
            }
            outputStream.flush();
            outputStream.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
               *//* File file = new File(filePath);
                File[] files = file.listFiles();
                for (File f :files) {
                    f.delete();
                }*//*
                if(in!=null){
                    in.close();
                }
                if(outputStream!=null){
                    outputStream.close();
                }
            }catch (Exception f){
                f.printStackTrace();
                logger.error("导出试题zip文件失败");
            }

        }
    }

    class Question2JsonTask implements Runnable{
        private CountDownLatch countDownLatch;
        private List<QuestionManage> list;
        public Question2JsonTask(CountDownLatch countDownLatch,List<QuestionManage> list) {
            this.countDownLatch = countDownLatch;
            this.list = list;
        }
        @Override
        public void run() {
            Gson gson = new Gson();
            String s = gson.toJson(list);
            OutJsonUtils.createJsonFile(s, filePath, System.currentTimeMillis()+"");
            countDownLatch.countDown();
        }
    }
*/
//    @RequestMapping(value = "/importQuestionByjsonfile", method = RequestMethod.POST)
//    public ApiResult importQuestionByjsonfile(@RequestParam(name = "file") MultipartFile multipartFile) {
//
//        String fileName = multipartFile.getOriginalFilename();
//        // 上传文件为空
//        if (org.apache.commons.lang3.StringUtils.isEmpty(fileName)) {
//            throw new BussinessException(ResultMesCode.Bad_Request.getResultCode(), "没有导入文件");
//        }
//        //上传文件大小为1000条数据
//        if (multipartFile.getSize() > 1024 * 1024 * 10) {
//            throw new BussinessException(ResultMesCode.Bad_Request.getResultCode(), "上传失败: 文件大小不能超过10M!");
//        }
//        // 上传文件名格式不正确
//        if (fileName.lastIndexOf(".") != -1 && !".json".equals(fileName.substring(fileName.lastIndexOf(".")))) {
//            throw new BussinessException(ResultMesCode.Bad_Request.getResultCode(), "文件名格式不正确, 请使用后缀名为json的文件");
//        }
//        InputStreamReader inputStreamReader = null;
//        BufferedReader bufferedReader = null;
//        List<QuestionManage> list = new ArrayList<>();
//        try {
//
//            inputStreamReader = new InputStreamReader(multipartFile.getInputStream(), "utf-8");
//
//            bufferedReader = new BufferedReader(inputStreamReader);
//
//            JSONReader jsonReader = new JSONReader(bufferedReader);
//            jsonReader.startArray();
//
//            while (jsonReader.hasNext()) {
//
//                JSONObject jsonObject = (JSONObject) jsonReader.readObject();
//                QuestionManage q = JSON.toJavaObject(jsonObject, Question.class);
//                List<Long> kpIds = q.getKpIds();
//                QuestionManage qq = new Question();
//                BeanUtils.copyProperties(q, qq, "id");
//                Pattern pattern = Pattern.compile("\\s*|\t|\n");
//                Matcher m1 = pattern.matcher(qq.getQuestion());
//                String s1 = m1.replaceAll("");
//                qq.setQuestion(s1);
//                Matcher m2 = pattern.matcher(qq.getAnswer());
//                m2.replaceAll("");
//                String s2 = m2.replaceAll("");
//                qq.setAnswer(s2);
//                Matcher m3 = pattern.matcher(qq.getAnalysis());
//                m3.replaceAll("");
//                String s3 = m3.replaceAll("");
//                qq.setAnalysis(s3);
//                Matcher m4 = pattern.matcher(qq.getOptions());
//                m4.replaceAll("");
//                String s4 = m4.replaceAll("");
//                qq.setOptions(s4);
//                qq.setUpdateTime(new Date());
//                qq.setCreateTime(new Date());
//                qq.setKpIds(kpIds);
//                qq.setCreator(AppUserUtil.getLoginAppUser().getId());
////                TODO 赋值JSON中试题的ID，方便查找附件
//                list.add(qq);
//
//            }
//            jsonReader.endArray();
//        } catch (IOException e) {
//            logger.error("解析迁入的json文件失败");
//        } finally {
//            try {
//                if (inputStreamReader != null) {
//                    inputStreamReader.close();
//                }
//                if (bufferedReader != null) {
//                    bufferedReader.close();
//                }
//            } catch (IOException ee) {
//                ee.printStackTrace();
//            }
//        }
//        int j = (list.size() + maxNum - 1) / maxNum;
//        List<List<QuestionManage>> mgList = new ArrayList<>();
//        Stream.iterate(0, n -> n + 1).limit(j).forEach(i -> {
//            mgList.add(list.stream().skip(i * maxNum).limit(maxNum).collect(Collectors.toList()));
//        });
//        ThreadPoolUtils threadPoolUtils = new ThreadPoolUtils();
//        ThreadPoolExecutor threadPoolExecutor = threadPoolUtils.getThreadPool(taskThreadPoolConfig.getCorePoolSize(),
//                taskThreadPoolConfig.getMaxPoolSize(), taskThreadPoolConfig.getKeepAliveSeconds(), taskThreadPoolConfig.getQueueCapacity());
//        try {
//            mgList.parallelStream().forEach(ll -> {
//                ImportQuestionTask task = new ImportQuestionTask(ll);
//                threadPoolExecutor.submit(task);
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            threadPoolExecutor.shutdown();
//        }
//        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "导入试题成功", null);
//    }

    class ImportQuestionTask implements Runnable {

        private List<QuestionManage> list;

        public ImportQuestionTask(List<QuestionManage> list) {
            this.list = list;
        }

        @Override
        public void run() {
            list.forEach(e -> {
                questionManageService.saveJson(e, e.getKpIds());
            });
        }
    }

    /**
     * 修改标注题标注
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "editImageAnnotionQuestion", method = RequestMethod.POST)
    public ApiResult editImageAnnotionQuestion(@RequestBody Map<String, Object> map) {
        Object id = map.get("id");
        Object imageAnnotion = map.get("imageAnnotion");
        QuestionManage question = questionManageService.getById(Long.valueOf(id + ""));
        question.setImageAnnotion(imageAnnotion + "");
        questionManageService.saveOrUpdate(question);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "修改成功", null);
    }

    /**
     * 获取标注题标注
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "getImageAnnotionQuestion", method = RequestMethod.GET)
    public ApiResult getImageAnnotionQuestion(Long id) {
        try {
            QuestionManage byId = questionManageService.getById(id);
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取标注题标注成功", byId.getImageAnnotion());
        } catch (NullPointerException e) {
            log.error("getImageAnnotionQuestion获取标注题标注为空：id=={}", id);
            return ApiResultHandler.buildApiResult(500, "该标注试题不存在", "");
        }


    }

}
