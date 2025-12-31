package com.cloud.exam.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.exam.annotation.RepeateRequestAnnotation;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.service.*;
import com.cloud.exam.utils.FileFastdfsUtils;
import com.cloud.exam.utils.ListUtils;
import com.cloud.exam.utils.PageBean;
import com.cloud.exam.utils.image.ImageUtil;
import com.cloud.exam.utils.word.MDoc;
import com.cloud.exam.utils.word.WordPdfUtil;
import com.cloud.exam.vo.PaperVO;
import com.cloud.exam.vo.RuleBeanVO;
import com.cloud.feign.file.FileClientFeign;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.user.AppUser;
import com.cloud.model.user.LoginAppUser;
import com.cloud.utils.ObjectUtils;
import com.cloud.utils.StringUtils;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import javassist.expr.NewExpr;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Phaser;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@Api("试卷管理接口类")
@RefreshScope
public class PaperController {

    // 文件服务器地址
    @Value(value = "${file_server}")
    private String fileServer;
    @Resource
    private PaperService paperService;
    @Resource
    private PaperManageRelService paperManageService;
    @Resource
    private QuestionService questionService;
    @Resource
    private ExamService examService;
    @Resource
    private CompetitionExamPaperRelService competitionExamPaperRelService;
    @Autowired
    private DrawResultService drawResultService;
    @Autowired
    private StudentAnswerService studentAnswerService;
    @Autowired
    private FileClientFeign fileClientFeign;
    @Autowired
    private SysDepartmentFeign sysDepartmentFeign;
    @Autowired
    private QuestionKpRelService questionKpRelService;

    @Value("${file_server}")
    private String fileUploadUrl;
    // 影像缩略图访问地址
    @Value("${localUrlPrefix}")
    private String localUrlPrefix;
    private final static Logger logger = LoggerFactory.getLogger(PaperController.class);

    /**
     * 基于算法的抽题组卷,添加试卷
     *
     * @param rule（总分【totalMark】、期望难度【difficulty】、知识点【kpIds】、情报方向【intDirectIds】、题型以及题量）
     * @param （试卷名称【paperName】、考试时间【totalTime】、试卷描述【describe】、试卷类型【type】、创建人【creator】、创建时间、修改时间)
     * @return 返回试卷信息实体
     */
    @PostMapping(value = "/paper", produces = "application/json;charset=UTF-8")
    @ApiOperation("抽题组卷")
    @RepeateRequestAnnotation(extTimeOut = 1)
    public ApiResult paperSave(@Valid @RequestBody RuleBeanVO rule) throws Exception {
        if (ObjectUtil.isEmpty(rule.getPaperTime()) || rule.getPaperTime() <= 0) {
            throw new IllegalArgumentException("请设置试卷答卷时长。");
        }
        /*if(ObjectUtil.isNotNull(rule.getExamId()) && ObjectUtil.isNotNull(rule.getDepartId())){
            //添加活动或者添加训练跳的页面
            List<ExamDepartUserRel> departAndUserByExamId = examService.getExamDepartRel(rule.getExamId(),rule.getDepartId());
            //学员具有的知识点交集
            List<Set<Long>> sets = new ArrayList<>();
            for (ExamDepartUserRel rel:departAndUserByExamId) {
                if(ObjectUtil.isNotNull(rel.getMemberId())){
                    Set<Long> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(rel.getMemberId());
                    sets.add(kpIdsbyUserId) ;
                }
            }
            //学员具有的知识点交集
            Set<Long> kk  = ListUtils.getSameElementBylists(sets) ;
            kk.add(0L);
            rule.getKpIds().clear();
            kk.stream().forEach(k->rule.getKpIds().add(k));
        }*/
        //校验试卷名称是否重复
        Integer count = paperService.lambdaQuery().eq(Paper::getPaperName, rule.getPaperName()).count();
        if (count > 0) {
            throw new IllegalArgumentException("试卷名称已被使用，请更改。");
        }
        Paper paper = new Paper();
        paper.setType(rule.getPaperType());
        paper.setPaperName(rule.getPaperName());
        paper.setDescribe(rule.getPaperDescribe());
        paper.setTotalTime(rule.getPaperTime());
        paper.setTotalScore(rule.getTotalMark());
        paper.setPaperFlg(rule.getPaperFlg());
        paper = paperService.paperSave(rule, paper);
        return ApiResultHandler.buildApiResult(200, "添加成功", paper);
    }


    /**
     * 修改试卷（修改试卷paperId,questionId)
     *
     * @param paperManage
     * @return 是否修改成功，1：成功。其他不成功
     */
    @PutMapping("/paper")
    @ApiOperation("修改试卷")
    public ApiResult updatePaperManage(@RequestBody PaperManageRel paperManage) {
        return paperManageService.updatePaperManageById(paperManage);
    }

    /**
     * 分页查询试卷
     *
     * @param paper（current，size）
     * @return 返回分页的试卷集合，比如当前页码，一页上有多少的记录，
     */
    @PostMapping("/paperPage")
    @ApiOperation("分页查询试卷")
    public Object findPaperList(@RequestBody PaperVO paper) {
        if (ObjectUtil.isEmpty(paper.getExamId())) {
            IPage byPage = paperService.findByPage(paper);
            return byPage;
        } else {
            List<Set<String>> sets = new ArrayList<>();
            List<ExamDepartUserRel> departAndUserByExamId = examService.getExamDepartRel(paper.getExamId(), paper.getDepartId());
            for (ExamDepartUserRel rel : departAndUserByExamId) {
                Set<String> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(rel.getMemberId());
                sets.add(kpIdsbyUserId);
            }
            //学员具有的知识点交集
            Set<String> kk = ListUtils.getSameElementBylists(sets);
            //所有理论试卷集合
            QueryWrapper<Paper> queryWrapper = new QueryWrapper<>();
            if (ObjectUtil.isNotNull(paper.getType())) {
                queryWrapper.eq("type", paper.getType());
            } else {
                queryWrapper.in("type", Stream.of(0, 1, 2).collect(Collectors.toList()));
            }
            if (ObjectUtil.isNotNull(paper.getStatus())) {
                queryWrapper.eq("status", paper.getStatus());
            }
            if (StringUtils.isNotEmpty(paper.getPaperName())) {
                queryWrapper.like("paper_name", paper.getPaperName());
            }
            if (ObjectUtil.isNotNull(paper.getPaperFlg())) {
                queryWrapper.eq("paper_flg", paper.getPaperFlg());
            }
            queryWrapper.orderByDesc("create_time");
            List<Paper> list = paperService.list(queryWrapper);
            List<Paper> ll = new ArrayList<>();
            Phaser phaser = new Phaser();
            phaser.register();
            ForkJoinPool forkJoinPool = new ForkJoinPool();
            forkJoinPool.submit(new CheckPaperTask(0, list.size(), list, kk, ll, phaser));
            phaser.arriveAndAwaitAdvance();
            //根据size分成j页
            List<Paper> collect1 = ll.stream().skip((paper.getCurrent() - 1) * paper.getSize()).limit(paper.getSize()).collect(Collectors.toList());
            PageBean pageBean1 = PageBean.getPageBean(paper.getCurrent(), paper.getSize(), ll.size(), collect1);
            return pageBean1;
        }
        /*IPage byPage = paperService.findByPage(paper);
        byPage.convert(dr -> {
            Paper d = (Paper) dr;
            PaperVO vo = new PaperVO();
            BeanUtils.copyProperties(d, vo);
            if (d.getStatus() != 1) {
                List<Exam> exam = examService.getEexmByPaperId(d.getId());
                vo.setExamName(exam.get(0)==null?"":exam.get(0).getName());
                vo.setStatus(1);
            }
            return vo;
        });*/
        // return byPage;
    }

    class CheckPaperTask extends RecursiveAction {
        int start;
        int end;
        List<Paper> paperList;
        Set<String> kpIds;
        List<Paper> ll;
        Phaser phaser;

        public CheckPaperTask(int start, int end, List<Paper> paperList, Set<String> kpIds, List<Paper> ll, Phaser phaser) {
            this.start = start;
            this.end = end;
            this.paperList = paperList;
            this.kpIds = kpIds;
            this.ll = ll;
            this.phaser = phaser;
            phaser.register();
        }

        @Override
        public void compute() {
            int count = end - start;
            if (count <= 10) {
                for (int i = start; i < end; i++) {
                    Set<String> kpIdsByPaperId = getKpIdsByPaperId(paperList.get(i).getId());
                    if (kpIds.containsAll(kpIdsByPaperId)) {
                        ll.add(paperList.get(i));
                    }
                }
            } else {
                int index = (start + end) / 2;
                CheckPaperTask work1 = new CheckPaperTask(start, index, paperList, kpIds, ll, phaser);
                work1.fork();
                CheckPaperTask work2 = new CheckPaperTask(index, end, paperList, kpIds, ll, phaser);
                work2.fork();
            }
            phaser.arrive();
        }
    }

    /**
     * 试卷和问题的关联信息集合
     *
     * @param
     * @return 返回是否成功成功，True成功，false失败
     */
    @PutMapping("/paperList")
    @ApiOperation("修改试卷")
    public ApiResult updatePaperManageByList(@RequestBody List<PaperManageRel> paperManageList) {
        //todo 需要优化为批量操作
        for (PaperManageRel paperManageRel : paperManageList) {
            paperManageService.updatePaperManageById(paperManageRel);

        }
        return ApiResultHandler.buildApiResult(200, "保存试卷成功", true);
    }

    /**
     * 查看试卷详情页面
     *
     * @return 返回试卷的详情信息集合
     */
    @GetMapping("/findPaperDetail")
    @ApiOperation("查看试卷详情")
    @ApiImplicitParam(name = "paperId", value = "试卷id", dataType = "数值类型")
    public List<Question> findPaperDetail(@RequestParam Long paperId) {
        return paperManageService.findPaperDetail(paperId);
    }


    @DeleteMapping("/paper/{paperId}")
    @ApiOperation("删除试卷")
    public ApiResult deletePaper(@PathVariable Long paperId) {
        //判断试卷是否被活动绑定，若绑定则不能被删除
        ExamDepartPaperRel examDepartPaperRel = examService.findByPaperId(paperId);
        if (examDepartPaperRel != null) {
            new IllegalArgumentException("该试卷已经被试题绑定，无法删除！");
        }
        boolean b = paperService.removeById(paperId);
        if (b) {
            //删除试卷试题关联表
            QueryWrapper<PaperManageRel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("paper_id", paperId);
            paperManageService.remove(queryWrapper);
        }
        return new ApiResult(200, "删除成功", null);
    }


    /**
     * 查询生成的pdf
     *
     * @param identityCard 准考证号
     * @param var1         有答案 true
     * @param var2         有水印 true
     * @return
     */
    @GetMapping("/question/getUser/pdf")
    public ApiResult getUserQuestionWord(@RequestParam("identityCard") String identityCard,
                                         @RequestParam("var1") Boolean var1,
                                         @RequestParam("var2") Boolean var2) {
        //生成文件的路径
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("identity_card", identityCard);
        //queryWrapper.eq("user_id", loginAppUser.getId());

        DrawResult drawResult = drawResultService.getOne(queryWrapper, true);
        String pdfUrl = "";
        if (ObjectUtils.isNotNull(drawResult)) {
            //有答案
            //有水印
            if (ObjectUtils.isNotNull(var1) && var1) {
                if (StringUtils.isNotEmpty(drawResult.getPdfAnswerUrl()) && drawResult.getPdfAnswerUrl().contains("#")) {
                    if (!var2) {
                        //无水印
                        pdfUrl = drawResult.getPdfAnswerUrl().split("#")[0];
                    } else {
                        //有水印
                        pdfUrl = drawResult.getPdfAnswerUrl().split("#")[1];
                    }
                } else {
                    if (!var2) {
                        pdfUrl = drawResult.getPdfAnswerUrl();
                    }
                }
            } else {
                //无答案
                //有水印
                if (StringUtils.isNotEmpty(drawResult.getPdfNoAnswerUrl()) && drawResult.getPdfNoAnswerUrl().contains("#")) {
                    if (!var2) {
                        pdfUrl = drawResult.getPdfNoAnswerUrl().split("#")[0];
                    } else {
                        pdfUrl = drawResult.getPdfNoAnswerUrl().split("#")[1];
                    }
                } else {
                    if (!var2) {
                        pdfUrl = drawResult.getPdfNoAnswerUrl();
                    }
                }
            }
        }


        return new ApiResult(200, "查询成功", fileServer + pdfUrl);
    }

    @GetMapping("/question/getUser/downloadpdf")
    public void downloadpdf(String pdfUrl, HttpServletResponse response) {
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            byte[] bytes = FileFastdfsUtils.downloadFile(pdfUrl);
            if (bytes != null) {
                outputStream.write(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * pdf的文档添加水印
     *
     * @param identityCard 准考证号
     * @param flag         true 阅卷(老师) false 答卷（学生）
     * @param file         水印图片
     * @return
     * @throws Exception
     */
    @PostMapping("/question/word/generate")
    public ApiResult pdfGenerate(@RequestParam("identityCard") String identityCard,
                                 @RequestParam("flag") Boolean flag,
                                 @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {
        //生成文件的路径
        String root = System.getProperty("user.dir");
        LoginAppUser loginAppUser = sysDepartmentFeign.getLoginAppUser();
        if (ObjectUtils.isNull(loginAppUser)) {
            return new ApiResult(500, "用户未登录", null);
        }

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("identity_card", identityCard);
        //queryWrapper.eq("user_id", loginAppUser.getId());
        DrawResult drawResult = drawResultService.getOne(queryWrapper, true);

        if (ObjectUtils.isNull(drawResult)) {
            return new ApiResult(500, "准考账号输入不对", null);
        }
        //查询用户的做题信息  k试题id  v Object
        Map<Long, StudentAnswer> studentAnswerMap = studentAnswerService.findMapByStuIdAndPaperId(loginAppUser.getId(), drawResult.getPaperId());

        Paper paper = paperService.getById(drawResult.getPaperId());
        if (ObjectUtils.isNull(paper)) {
            return new ApiResult(500, "试卷不存在", null);
        }

        //学员用户开始时间的格式处理
        if (ObjectUtils.isNotNull(drawResult.getLoginDate())) {
            drawResult.setLoginDateStr(DateUtil.format(drawResult.getLoginDate(), "yyyy-MM-dd HH:mm:ss"));
        }
        List<Exam> exam = examService.getEexmByPaperId(drawResult.getPaperId());
        File yfile = new File(root + File.separator + "examFile");
        if (!yfile.exists()) {
            yfile.mkdirs();
        }
        //查询试卷的试题信息
        List<Question> questionList = paperManageService.findPaperDetail(drawResult.getPaperId());
        //单选题
        List<Map<String, Object>> questionList1 = new ArrayList<>();
        //单选分数
        BigDecimal questionScore1 = new BigDecimal(0.0);
        //多选题
        List<Map<String, Object>> questionList2 = new ArrayList<>();
        //多选分数
        BigDecimal questionScore2 = new BigDecimal(0.0);
        //判断题
        List<Map<String, Object>> questionList3 = new ArrayList<>();
        //判断题分数
        BigDecimal questionScore3 = new BigDecimal(0.0);
        //填空题
        List<Map<String, Object>> questionList4 = new ArrayList<>();
        //填空题分数
        BigDecimal questionScore4 = new BigDecimal(0.0);
        //简答题
        List<Map<String, Object>> questionList5 = new ArrayList<>();
        BigDecimal questionScore5 = new BigDecimal(0.0);
        //问答题
        List<Map<String, Object>> questionList6 = new ArrayList<>();
        BigDecimal questionScore6 = new BigDecimal(0.0);
        //引入图片的list
        List<String> picNameList = new ArrayList<>();
        //word图片存储的下标
        int i = 20;
        Map<String, Object> paramMap = null;
        Map<String, Object> questionData = null;
        //图片的各个参数
        List<Map<String, String>> imageData = new ArrayList<>();
        if (ObjectUtils.isNotNull(questionList)) {
            for (Question question : questionList) {
                paramMap = new HashMap<>();
                //处理其他的试题主干,填空题不做处理
                if (StringUtils.isNotEmpty(question.getQuestion())) {
                    questionData = new Gson().fromJson(question.getQuestion(), Map.class);
                    //图片的数据补充
                    i = getImageData(picNameList, questionData, imageData, i);
                    //题干
                    paramMap.put("questionData", questionData);
                }
                //单选题处理
                if (question.getType() == 1) {
                    questionScore1 = new BigDecimal(question.getScore());
                    //评分依据
                    Map<String, Object> scoreBasisData = new Gson().fromJson(question.getScoreBasis(), Map.class);
                    if (ObjectUtils.isNotNull(scoreBasisData)) {
                        for (Map.Entry<String, Object> mapEntry : scoreBasisData.entrySet()) {
                            if (mapEntry.getValue() instanceof Map) {
                                i = getImageData(picNameList, (Map<String, Object>) mapEntry.getValue(), imageData, i);
                            }
                        }
                    }
                    paramMap.put("scoreBasisData", scoreBasisData);

                    Map<String, Map<String, Object>> optionData = new Gson().fromJson(question.getOptions(), Map.class);
                    if (ObjectUtils.isNotNull(optionData)) {
                        //选项的url配置
                        for (Map.Entry<String, Map<String, Object>> mapEntry : optionData.entrySet()) {
                            i = getImageData(picNameList, mapEntry.getValue(), imageData, i);
                        }
                    }
                    paramMap.put("optionData", optionData);

                    if (ObjectUtils.isNotNull(studentAnswerMap)) {
                        StudentAnswer studentAnswer = studentAnswerMap.get(question.getId());
                        if (ObjectUtils.isNotNull(studentAnswer) && StringUtils.isNotEmpty(studentAnswer.getStuAnswer())) {

                            Map<String, String[]> stuAnswerData = new Gson().fromJson(studentAnswer.getStuAnswer(), Map.class);
                            paramMap.put("userAnswer", stuAnswerData);
                        }
                        String result = "错";
                        if (ObjectUtils.isNotNull(studentAnswer) && ObjectUtils.isNotNull(studentAnswer.getActualScore()) && studentAnswer.getActualScore().longValue() > 0) {
                            result = "对";
                        }
                        paramMap.put("result", result);
                    }
                    questionList1.add(paramMap);
                }
                //多选题处理
                if (question.getType() == 2) {
                    questionScore2 = new BigDecimal(question.getScore());
                    Map<String, Map<String, Object>> optionData = new Gson().fromJson(question.getOptions(), Map.class);
                    if (ObjectUtils.isNotNull(optionData)) {
                        //选项的url配置
                        for (Map.Entry<String, Map<String, Object>> mapEntry : optionData.entrySet()) {
                            i = getImageData(picNameList, mapEntry.getValue(), imageData, i);
                        }
                    }
                    paramMap.put("optionData", optionData);

                    //参考答案
                    Map<String, Object> scoreBasisData = new Gson().fromJson(question.getScoreBasis(), Map.class);
                    if (ObjectUtils.isNotNull(scoreBasisData)) {
                        for (Map.Entry<String, Object> mapEntry : scoreBasisData.entrySet()) {
                            if (mapEntry.getValue() instanceof Map) {
                                i = getImageData(picNameList, (Map<String, Object>) mapEntry.getValue(), imageData, i);
                            }
                        }
                    }
                    paramMap.put("scoreBasisData", scoreBasisData);

                    if (ObjectUtils.isNotNull(studentAnswerMap)) {
                        StudentAnswer studentAnswer = studentAnswerMap.get(question.getId());
                        if (ObjectUtils.isNotNull(studentAnswer) && StringUtils.isNotEmpty(studentAnswer.getStuAnswer())) {

                            Map<String, Object> stuAnswerData = new Gson().fromJson(studentAnswer.getStuAnswer(), Map.class);
                            if (ObjectUtils.isNotNull(stuAnswerData.get("text"))) {
                                if (stuAnswerData.get("text") instanceof String) {
                                    continue;
                                }
                                String userAnswer = "";
                                for (String str : (List<String>) stuAnswerData.get("text")) {
                                    userAnswer += str + ",";
                                }
                                paramMap.put("userAnswer", userAnswer.substring(0, userAnswer.length() - 1));
                            }
                        }
                        String result = "错";
                        if (ObjectUtils.isNotNull(studentAnswer) && ObjectUtils.isNotNull(studentAnswer.getActualScore()) && studentAnswer.getActualScore().longValue() > 0) {
                            result = "对";
                        }
                        paramMap.put("result", result);
                    }
                    questionList2.add(paramMap);

                }
                //判断题处理
                if (question.getType() == 3) {
                    questionScore3 = new BigDecimal(question.getScore());
                    if (ObjectUtils.isNotNull(studentAnswerMap)) {
                        StudentAnswer studentAnswer = studentAnswerMap.get(question.getId());
                        if (ObjectUtils.isNotNull(studentAnswer) && StringUtils.isNotEmpty(studentAnswer.getStuAnswer())) {

                            Map<String, Object> stuAnswerData = new Gson().fromJson(studentAnswer.getStuAnswer(), Map.class);
                            if (ObjectUtils.isNotNull(stuAnswerData.get("text")) && StringUtils.isNotEmpty(stuAnswerData.get("text") + "")) {
                                String userAnswer = "错误";
                                if ((Boolean) stuAnswerData.get("text")) {
                                    userAnswer = "正确";
                                }
                                paramMap.put("userAnswer", userAnswer);
                            }
                        }
                        String result = "错";
                        if (ObjectUtils.isNotNull(studentAnswer) && ObjectUtils.isNotNull(studentAnswer.getActualScore()) && studentAnswer.getActualScore().longValue() > 0) {
                            result = "对";
                        }
                        paramMap.put("result", result);
                    }

                    Map<String, Object> scoreBasisData = new Gson().fromJson(question.getScoreBasis(), Map.class);
                    if (ObjectUtils.isNotNull(scoreBasisData) && StringUtils.isNotEmpty(scoreBasisData.get("text") + "")) {
                        String da = "错误";
                        if (scoreBasisData.get("text").equals("true")) {
                            da = "正确";
                        }
                        paramMap.put("scoreBasisData", da);
                    }
                    questionList3.add(paramMap);
                }
                //填空题处理
                if (question.getType() == 4) {
                    questionScore4 = new BigDecimal(question.getScore());
                    if (ObjectUtils.isNotNull(studentAnswerMap)) {
                        StudentAnswer studentAnswer = studentAnswerMap.get(question.getId());
                        if (ObjectUtils.isNotNull(studentAnswer) && StringUtils.isNotEmpty(studentAnswer.getStuAnswer())) {
                            Map<String, Object> stuAnswerData = new Gson().fromJson(studentAnswer.getStuAnswer(), Map.class);
                            if (ObjectUtils.isNotNull(stuAnswerData.get("text"))) {
                                String userAnswer = "";

                                if (stuAnswerData.get("text") instanceof String) {
                                    continue;
                                }

                                for (String str : (List<String>) stuAnswerData.get("text")) {
                                    userAnswer += str + "；";
                                }
                                paramMap.put("userAnswer", userAnswer.substring(0, userAnswer.length() - 1));
                            }
                        }
                        String result = "错";
                        if (ObjectUtils.isNotNull(studentAnswer) && ObjectUtils.isNotNull(studentAnswer.getActualScore()) && studentAnswer.getActualScore().longValue() > 0) {
                            result = "对";
                        }
                        paramMap.put("result", result);
                    }

                    //参考答案
                    Map<String, String> scoreBasisData = new Gson().fromJson(question.getScoreBasis(), Map.class);
                    if (ObjectUtils.isNotNull(scoreBasisData)) {
                        if (ObjectUtils.isNotNull(scoreBasisData.get("text"))) {
                            String[] userAnswer = scoreBasisData.get("text").toString().split("\n");
                            String join = StringUtils.join(userAnswer, ",");
                            //String[] userAnswer = scoreBasisData.get("text").toString().replaceAll("\n","").replaceAll("\n","").split("[\\d]、");
                            paramMap.put("scoreBasisData", join);
                        }
                    }

                    questionList4.add(paramMap);
                }
                if (question.getType() == 5) {
                    questionScore5 = new BigDecimal(question.getScore());
                    if (ObjectUtils.isNotNull(studentAnswerMap)) {
                        StudentAnswer studentAnswer = studentAnswerMap.get(question.getId());
                        if (ObjectUtils.isNotNull(studentAnswer)) {
                            //用户答案
                            if (StringUtils.isNotEmpty(studentAnswer.getStuAnswer())) {
                                Map<String, Object> stuAnswerData = new Gson().fromJson(studentAnswer.getStuAnswer(), Map.class);
                                if (ObjectUtils.isNotNull(stuAnswerData.get("text"))) {
                                    String[] userAnswer = stuAnswerData.get("text").toString().split("\n");
                                    //String[] userAnswer = stuAnswerData.get("text").toString().replaceAll("\n","").replaceAll("\n","").split("[\\d]、");
                                    paramMap.put("userAnswer", userAnswer);
                                }
                            }
                            //判题理由
                            paramMap.put("judgeRemark", studentAnswer.getJudgeRemark());
                        }
                        //分数
                        paramMap.put("result", studentAnswer.getActualScore());
                    }
                    questionList5.add(paramMap);
                }
                if (question.getType() == 6) {
                    questionScore6 = new BigDecimal(question.getScore());
                    if (ObjectUtils.isNotNull(studentAnswerMap)) {
                        StudentAnswer studentAnswer = studentAnswerMap.get(question.getId());
                        if (ObjectUtils.isNotNull(studentAnswer)) {
                            //用户答案
                            if (StringUtils.isNotEmpty(studentAnswer.getStuAnswer())) {
                                Map<String, Object> stuAnswerData = new Gson().fromJson(studentAnswer.getStuAnswer(), Map.class);
                                if (ObjectUtils.isNotNull(stuAnswerData.get("text"))) {
                                    //String[] userAnswer = stuAnswerData.get("text").toString().replaceAll("\n","").replaceAll("\n","").split("[\\d]、");
                                    String[] userAnswer = stuAnswerData.get("text").toString().split("\n");
                                    paramMap.put("userAnswer", userAnswer);
                                }
                            }
                            //判题理由
                            paramMap.put("judgeRemark", studentAnswer.getJudgeRemark());
                        }
                        //分数
                        paramMap.put("result", studentAnswer.getActualScore());
                    }
                    questionList6.add(paramMap);
                }
            }
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("paperName", paper.getPaperName());
        dataMap.put("nickname", loginAppUser.getNickname());
        dataMap.put("drawResult", drawResult);
        String activityType = "";
        if (ObjectUtils.isNotNull(exam)) {
            activityType = "（" + exam.get(0).getName() + "）";
        }
        dataMap.put("activityType", activityType);
        dataMap.put("picNamesList", picNameList);//所有图片名称的list
        dataMap.put("questionList6", questionList6);//问答题
        dataMap.put("questionScore6", questionScore6);//简答题 每题分数
        dataMap.put("questionList5", questionList5);//简答题
        dataMap.put("questionScore5", questionScore5);//简答题 每题分数
        dataMap.put("questionList4", questionList4);//填空题
        dataMap.put("questionScore4", questionScore4);//填空题 每题分数
        dataMap.put("questionList3", questionList3);//判断题
        dataMap.put("questionScore3", questionScore3);//判断题 每题分数
        dataMap.put("questionList2", questionList2);//多选
        dataMap.put("questionScore2", questionScore2);//多选 每题分数
        dataMap.put("questionList1", questionList1);//单选
        dataMap.put("questionScore1", questionScore1);//单选 每题分数
        dataMap.put("flag", flag);
        dataMap.put("imageData", imageData);//图片的数据参数
        String dbUrl = "";//文件的字符串拼接
        String dsfs = "";//有文件的链接
        //word路径
        String wordUrl = root + File.separator + "examFile" + File.separator + loginAppUser.getId() + "_" + System.currentTimeMillis() + ".docx";
        // word转pdf 路径 （无水印的图片）
        String pdfNoUrl = root + File.separator + "examFile" + File.separator + loginAppUser.getId() + "_" + System.currentTimeMillis() + ".pdf";
        // word转pdf 路径 （有水印的图片）
        String pdfUrl = root + File.separator + "examFile" + File.separator + loginAppUser.getId() + "_" + System.currentTimeMillis() + "_watermark" + ".pdf";
        //生成word文件
        new MDoc().createDoc(dataMap, wordUrl);
        //生成无水印的pdf文件
        WordPdfUtil.doc2pdf(wordUrl, pdfNoUrl);
        if (ObjectUtils.isNotNull(file)) {
            //生成有水印添加
//            WordPdfUtil.insertWatermarkImage(pdfNoUrl, pdfUrl, file);
//            dsfs=fileClientFeign.upload(FileUtils.fileDoMultipartFile(new File(pdfUrl)));
        }

        System.out.println(FileFastdfsUtils.uploadFile(fileDoMu(new File(pdfNoUrl))));
        List<String> list = new ArrayList<>();
        list.add(wordUrl);
        list.add(pdfNoUrl);
        list.add(pdfUrl);

//        SystemTest.out.println(fileClientFeign.upload(fileDoMu(new File(pdfNoUrl))));

//        dbUrl=fileClientFeign.upload(FileUtils.fileDoMultipartFile(new File(pdfNoUrl)));
//        if (StringUtils.isNotEmpty(dsfs)){
//            dbUrl+="#"+dsfs;
//        }
//
//        SystemTest.out.println(dbUrl);


//        UpdateWrapper<DrawResult> updateWrapper = new UpdateWrapper<>();
//        if (flag){
//            //有答案     无#有水印
//            updateWrapper.eq("identity_card",identityCard).set("pdf_answer_url","");
//        }else {
//            //无答案     无#有水印
//            updateWrapper.eq("identity_card",identityCard).set("pdf_no_answer_url","");
//        }
//        queryWrapper.eq("user_id", loginAppUser.getId());
//        if (drawResultService.update(null, updateWrapper)){
//            return new ApiResult(200, "生成pdf成功",null);
//        }

//        FileUtil.del(wordUrl);
//        FileUtil.del(pdfNoUrl);
//        FileUtil.del(pdfUrl);
        return new ApiResult(500, "生成成功", list);
    }


    /**
     * pdf文件的水印生成
     *
     * @param identityCard 准考证号
     * @param flag         true 阅卷(老师) false 答卷（学生）
     * @return
     * @throws Exception
     */
    @PostMapping("/question/pdf/logo/generate")
    public ApiResult exportWord(@RequestParam("identityCard") String identityCard,
                                @RequestParam("flag") Boolean flag,
                                @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {

        String root = System.getProperty("user.dir");
        LoginAppUser loginAppUser = sysDepartmentFeign.getLoginAppUser();
        if (ObjectUtils.isNull(loginAppUser)) {
            return new ApiResult(500, "用户未登录", null);
        }

        if (ObjectUtils.isNull(file)) {
            return new ApiResult(500, "水印未上传", null);
        }
        InputStream ins = file.getInputStream();
        String fileName = file.getOriginalFilename();
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public synchronized void run() {
                while (true) {
                    QueryWrapper queryWrapper = new QueryWrapper();
                    queryWrapper.eq("identity_card", identityCard);
                    //queryWrapper.eq("user_id", loginAppUser.getId());
                    DrawResult drawResult = drawResultService.getOne(queryWrapper, true);
                    String answerUrl = "";
                    if (flag) {
                        if (StringUtils.isEmpty(drawResult.getPdfAnswerUrl())) {
                            continue;
                        }
                        //有答案的pdf  无水印
                        answerUrl = drawResult.getPdfAnswerUrl();
                    } else {
                        if (StringUtils.isEmpty(drawResult.getPdfNoAnswerUrl())) {
                            continue;
                        }
                        //无答案的pdf  无水印
                        answerUrl = drawResult.getPdfNoAnswerUrl();
                    }

                    // 将文件服务地址 修改为 配置文件 读取方式
                    //String webUrl = "http://192.168.10.203:8888/" + answerUrl;
                    String webUrl = fileServer + answerUrl;

                    // word转pdf 路径 （有水印的图片）
                    String doUrl = root + File.separator + "examFile" + File.separator + loginAppUser.getId() + "_" + System.currentTimeMillis() + "_watermark" + ".pdf";
                    URL url = new URL(webUrl);
                    HttpURLConnection httpUrl = (HttpURLConnection) url.openConnection();
                    httpUrl.connect();
                    InputStream is = httpUrl.getInputStream();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int let = 0;
                    while ((let = is.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, let);
                    }
                    ByteArrayInputStream swap = new ByteArrayInputStream(outputStream.toByteArray());
                    //生成有水印添加
                    WordPdfUtil.insertWatermarkImage(swap, doUrl, ins, fileName);
                    String ll = answerUrl + "#" + FileFastdfsUtils.uploadFile(fileDoMu(new File(doUrl)));
                    UpdateWrapper<DrawResult> updateWrapper = new UpdateWrapper<>();
                    if (flag) {
                        //有答案     无#有水印
                        updateWrapper.eq("identity_card", identityCard).set("pdf_answer_url", ll);
                    } else {
                        //无答案     无#有水印
                        updateWrapper.eq("identity_card", identityCard).set("pdf_no_answer_url", ll);
                    }
                    drawResultService.update(null, updateWrapper);
                    FileUtil.del(doUrl);
                    break;
                }
            }
        }).start();
        return new ApiResult(200, "带水印的pdf生成成功", null);
    }


    /**
     * 导出pdf的试卷生成无水印的pdf
     *
     * @param identityCard 准考证号
     * @param flag         true 阅卷(老师) false 答卷（学生）
     * @return
     * @throws Exception
     */
    @PostMapping("/question/pdf/generate")
    //@RepeateRequestAnnotation(extTimeOut = 1000)
    public ApiResult exportWord(@RequestParam("identityCard") String identityCard,
                                @RequestParam("flag") Boolean flag) throws Exception {
        //生成文件的路径
        String root = System.getProperty("user.dir");
        LoginAppUser loginAppUser = sysDepartmentFeign.getLoginAppUser();
        if (ObjectUtils.isNull(loginAppUser)) {
            return new ApiResult(500, "用户未登录", null);
        }

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("identity_card", identityCard);
        //queryWrapper.eq("user_id", loginAppUser.getId());
        DrawResult drawResult = drawResultService.getOne(queryWrapper, true);

        if (ObjectUtils.isNull(drawResult)) {
            return new ApiResult(500, "准考账号输入不对", null);
        }
        //查询用户的做题信息  k试题id  v Object
        Long studentId;
        if (flag) {
            studentId = drawResult.getUserId();
        } else {
            studentId = loginAppUser.getId();
        }
        Map<Long, StudentAnswer> studentAnswerMap = studentAnswerService.findMapByStuIdAndPaperId(studentId, drawResult.getPaperId());

        Paper paper = paperService.getById(drawResult.getPaperId());
        if (ObjectUtils.isNull(paper)) {
            return new ApiResult(500, "试卷不存在", null);
        }

        //学员用户开始时间的格式处理
        if (ObjectUtils.isNotNull(drawResult.getLoginDate())) {
            drawResult.setLoginDateStr(DateUtil.format(drawResult.getLoginDate(), "yyyy-MM-dd HH:mm:ss"));
        }
        List<Exam> exam = examService.getEexmByPaperId(drawResult.getPaperId());
        File yfile = new File(root + File.separator + "examFile");
        if (!yfile.exists()) {
            yfile.mkdirs();
        }
        //查询试卷的试题信息
        List<Question> questionList = paperManageService.findPaperDetail(drawResult.getPaperId());
        //单选题
        List<Map<String, Object>> questionList1 = new ArrayList<>();
        //单选分数
        BigDecimal questionScore1 = new BigDecimal(0.0);
        //多选题
        List<Map<String, Object>> questionList2 = new ArrayList<>();
        //多选分数
        BigDecimal questionScore2 = new BigDecimal(0.0);
        //判断题
        List<Map<String, Object>> questionList3 = new ArrayList<>();
        //判断题分数
        BigDecimal questionScore3 = new BigDecimal(0.0);
        //填空题
        List<Map<String, Object>> questionList4 = new ArrayList<>();
        //填空题分数
        BigDecimal questionScore4 = new BigDecimal(0.0);
        //简答题
        List<Map<String, Object>> questionList5 = new ArrayList<>();
        BigDecimal questionScore5 = new BigDecimal(0.0);
        //问答题
        List<Map<String, Object>> questionList6 = new ArrayList<>();
        BigDecimal questionScore6 = new BigDecimal(0.0);
        //引入图片的list
        List<String> picNameList = new ArrayList<>();
        //word图片存储的下标
        int i = 20;
        Map<String, Object> paramMap = null;
        Map<String, Object> questionData = null;
        //图片的各个参数
        List<Map<String, String>> imageData = new ArrayList<>();
        if (ObjectUtils.isNotNull(questionList)) {
            for (Question question : questionList) {
                paramMap = new HashMap<>();
                //处理其他的试题主干,填空题不做处理
                if (StringUtils.isNotEmpty(question.getQuestion())) {
                    questionData = new Gson().fromJson(question.getQuestion(), Map.class);
                    //图片的数据补充
                    i = getImageData(picNameList, questionData, imageData, i);
                    //题干
                    paramMap.put("questionData", questionData);
                }
                //单选题处理
                if (question.getType() == 1) {
                    questionScore1 = new BigDecimal(question.getScore());
                    //评分依据
                    Map<String, Object> scoreBasisData = new Gson().fromJson(question.getScoreBasis(), Map.class);
                    if (ObjectUtils.isNotNull(scoreBasisData)) {
                        for (Map.Entry<String, Object> mapEntry : scoreBasisData.entrySet()) {
                            if (mapEntry.getValue() instanceof Map) {
                                i = getImageData(picNameList, (Map<String, Object>) mapEntry.getValue(), imageData, i);
                            }
                        }
                    }
                    paramMap.put("scoreBasisData", scoreBasisData);

                    Map<String, Map<String, Object>> optionData = new Gson().fromJson(question.getOptions(), Map.class);
                    if (ObjectUtils.isNotNull(optionData)) {
                        //选项的url配置
                        for (Map.Entry<String, Map<String, Object>> mapEntry : optionData.entrySet()) {
                            i = getImageData(picNameList, mapEntry.getValue(), imageData, i);
                        }
                    }
                    paramMap.put("optionData", optionData);

                    if (ObjectUtils.isNotNull(studentAnswerMap)) {
                        StudentAnswer studentAnswer = studentAnswerMap.get(question.getId());
                        if (ObjectUtils.isNotNull(studentAnswer) && StringUtils.isNotEmpty(studentAnswer.getStuAnswer())) {

                            Map<String, String[]> stuAnswerData = new Gson().fromJson(studentAnswer.getStuAnswer(), Map.class);
                            paramMap.put("userAnswer", stuAnswerData);
                        }
                        String result = "错";
                        if (ObjectUtils.isNotNull(studentAnswer) && ObjectUtils.isNotNull(studentAnswer.getActualScore()) && studentAnswer.getActualScore().longValue() > 0) {
                            result = "对";
                        }
                        paramMap.put("result", result);
                    }
                    questionList1.add(paramMap);
                }
                //多选题处理
                if (question.getType() == 2) {
                    questionScore2 = new BigDecimal(question.getScore());
                    Map<String, Map<String, Object>> optionData = new Gson().fromJson(question.getOptions(), Map.class);
                    if (ObjectUtils.isNotNull(optionData)) {
                        //选项的url配置
                        for (Map.Entry<String, Map<String, Object>> mapEntry : optionData.entrySet()) {
                            i = getImageData(picNameList, mapEntry.getValue(), imageData, i);
                        }
                    }
                    paramMap.put("optionData", optionData);

                    //参考答案
                    Map<String, Object> scoreBasisData = new Gson().fromJson(question.getScoreBasis(), Map.class);
                    if (ObjectUtils.isNotNull(scoreBasisData)) {
                        for (Map.Entry<String, Object> mapEntry : scoreBasisData.entrySet()) {
                            if (mapEntry.getValue() instanceof Map) {
                                i = getImageData(picNameList, (Map<String, Object>) mapEntry.getValue(), imageData, i);
                            }
                        }
                    }
                    paramMap.put("scoreBasisData", scoreBasisData);

                    if (ObjectUtils.isNotNull(studentAnswerMap)) {
                        StudentAnswer studentAnswer = studentAnswerMap.get(question.getId());
                        if (ObjectUtils.isNotNull(studentAnswer) && StringUtils.isNotEmpty(studentAnswer.getStuAnswer())) {

                            Map<String, Object> stuAnswerData = new Gson().fromJson(studentAnswer.getStuAnswer(), Map.class);
                            if (ObjectUtils.isNotNull(stuAnswerData.get("text"))) {
                                if (stuAnswerData.get("text") instanceof String) {
                                    continue;
                                }
                                String userAnswer = "";
                                for (String str : (List<String>) stuAnswerData.get("text")) {
                                    userAnswer += str + ",";
                                }
                                paramMap.put("userAnswer", userAnswer.substring(0, userAnswer.length() - 1));
                            }
                        }
                        String result = "错";
                        if (ObjectUtils.isNotNull(studentAnswer) && ObjectUtils.isNotNull(studentAnswer.getActualScore()) && studentAnswer.getActualScore().longValue() > 0) {
                            result = "对";
                        }
                        paramMap.put("result", result);
                    }
                    questionList2.add(paramMap);

                }
                //判断题处理
                if (question.getType() == 3) {
                    questionScore3 = new BigDecimal(question.getScore());
                    if (ObjectUtils.isNotNull(studentAnswerMap)) {
                        StudentAnswer studentAnswer = studentAnswerMap.get(question.getId());
                        if (ObjectUtils.isNotNull(studentAnswer) && StringUtils.isNotEmpty(studentAnswer.getStuAnswer())) {

                            Map<String, Object> stuAnswerData = new Gson().fromJson(studentAnswer.getStuAnswer(), Map.class);
                            if (ObjectUtils.isNotNull(stuAnswerData.get("text")) && StringUtils.isNotEmpty(stuAnswerData.get("text") + "")) {
                                String userAnswer = "错误";
                                if ((Boolean) stuAnswerData.get("text")) {
                                    userAnswer = "正确";
                                }
                                paramMap.put("userAnswer", userAnswer);
                            }
                        }
                        String result = "错";
                        if (ObjectUtils.isNotNull(studentAnswer) && ObjectUtils.isNotNull(studentAnswer.getActualScore()) && studentAnswer.getActualScore().longValue() > 0) {
                            result = "对";
                        }
                        paramMap.put("result", result);
                    }

                    Map<String, Object> scoreBasisData = new Gson().fromJson(question.getScoreBasis(), Map.class);
                    if (ObjectUtils.isNotNull(scoreBasisData) && StringUtils.isNotEmpty(scoreBasisData.get("text") + "")) {
                        String da = "错误";
                        if (scoreBasisData.get("text").equals("true")) {
                            da = "正确";
                        }
                        paramMap.put("scoreBasisData", da);
                    }
                    questionList3.add(paramMap);
                }
                //填空题处理
                if (question.getType() == 4) {
                    questionScore4 = new BigDecimal(question.getScore());
                    if (ObjectUtils.isNotNull(studentAnswerMap)) {
                        StudentAnswer studentAnswer = studentAnswerMap.get(question.getId());
                        if (ObjectUtils.isNotNull(studentAnswer) && StringUtils.isNotEmpty(studentAnswer.getStuAnswer())) {
                            Map<String, Object> stuAnswerData = new Gson().fromJson(studentAnswer.getStuAnswer(), Map.class);
                            if (ObjectUtils.isNotNull(stuAnswerData.get("text"))) {
                                String userAnswer = "";

                                if (stuAnswerData.get("text") instanceof String) {
                                    continue;
                                }

                                for (String str : (List<String>) stuAnswerData.get("text")) {
                                    userAnswer += str + "；";
                                }
                                paramMap.put("userAnswer", userAnswer.substring(0, userAnswer.length() - 1));
                            }
                        }
                        String result = "错";
                        if (ObjectUtils.isNotNull(studentAnswer) && ObjectUtils.isNotNull(studentAnswer.getActualScore()) && studentAnswer.getActualScore().longValue() > 0) {
                            result = "对";
                        }
                        paramMap.put("result", result);
                    }

                    //参考答案
                    Map<String, String> scoreBasisData = new Gson().fromJson(question.getScoreBasis(), Map.class);
                    if (ObjectUtils.isNotNull(scoreBasisData)) {
                        if (ObjectUtils.isNotNull(scoreBasisData.get("text"))) {
                            String[] userAnswer = scoreBasisData.get("text").toString().split("\n");
                            String join = StringUtils.join(userAnswer, ",");
                            //String[] userAnswer = scoreBasisData.get("text").toString().replaceAll("\n","").replaceAll("\n","").split("[\\d]、");
                            paramMap.put("scoreBasisData", join);
                        }
                    }

                    questionList4.add(paramMap);
                }
                if (question.getType() == 5) {
                    questionScore5 = new BigDecimal(question.getScore());
                    if (ObjectUtils.isNotNull(studentAnswerMap)) {
                        StudentAnswer studentAnswer = studentAnswerMap.get(question.getId());
                        if (ObjectUtils.isNotNull(studentAnswer)) {
                            //用户答案
                            if (StringUtils.isNotEmpty(studentAnswer.getStuAnswer())) {
                                Map<String, Object> stuAnswerData = new Gson().fromJson(studentAnswer.getStuAnswer(), Map.class);
                                if (ObjectUtils.isNotNull(stuAnswerData.get("text"))) {
                                    String[] userAnswer = stuAnswerData.get("text").toString().split("\n");
                                    //String[] userAnswer = stuAnswerData.get("text").toString().replaceAll("\n","").replaceAll("\n","").split("[\\d]、");
                                    paramMap.put("userAnswer", userAnswer);
                                }
                            }
                            //判题理由
                            paramMap.put("judgeRemark", studentAnswer.getJudgeRemark());
                            //分数
                            paramMap.put("result", studentAnswer.getActualScore());
                        }
                    }
                    questionList5.add(paramMap);
                }
                if (question.getType() == 6) {
                    questionScore6 = new BigDecimal(question.getScore());
                    if (ObjectUtils.isNotNull(studentAnswerMap)) {
                        StudentAnswer studentAnswer = studentAnswerMap.get(question.getId());
                        if (ObjectUtils.isNotNull(studentAnswer)) {
                            //用户答案
                            if (StringUtils.isNotEmpty(studentAnswer.getStuAnswer())) {
                                Map<String, Object> stuAnswerData = new Gson().fromJson(studentAnswer.getStuAnswer(), Map.class);
                                if (ObjectUtils.isNotNull(stuAnswerData.get("text"))) {
                                    //String[] userAnswer = stuAnswerData.get("text").toString().replaceAll("\n","").replaceAll("\n","").split("[\\d]、");
                                    String[] userAnswer = stuAnswerData.get("text").toString().split("\n");
                                    paramMap.put("userAnswer", userAnswer);
                                }
                            }
                            //判题理由
                            paramMap.put("judgeRemark", studentAnswer.getJudgeRemark());
                            //分数
                            paramMap.put("result", studentAnswer.getActualScore());
                        }
                    }
                    questionList6.add(paramMap);
                }
            }
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("paperName", paper.getPaperName());
        if (flag) {
            Long userId = drawResult.getUserId();
            AppUser appUserById = sysDepartmentFeign.findAppUserById(userId);
            dataMap.put("nickname", ObjectUtil.isNotNull(appUserById) ? appUserById.getNickname() : "");
        } else {
            dataMap.put("nickname", loginAppUser.getNickname());
        }

        dataMap.put("drawResult", drawResult);
        String activityType = "";
        if (ObjectUtils.isNotNull(exam)) {
            activityType = "（" + exam.get(0).getName() + "）";
        }
        dataMap.put("activityType", activityType);
        dataMap.put("picNamesList", picNameList);//所有图片名称的list
        dataMap.put("questionList6", questionList6);//问答题
        dataMap.put("questionScore6", questionScore6);//简答题 每题分数
        dataMap.put("questionList5", questionList5);//简答题
        dataMap.put("questionScore5", questionScore5);//简答题 每题分数
        dataMap.put("questionList4", questionList4);//填空题
        dataMap.put("questionScore4", questionScore4);//填空题 每题分数
        dataMap.put("questionList3", questionList3);//判断题
        dataMap.put("questionScore3", questionScore3);//判断题 每题分数
        dataMap.put("questionList2", questionList2);//多选
        dataMap.put("questionScore2", questionScore2);//多选 每题分数
        dataMap.put("questionList1", questionList1);//单选
        dataMap.put("questionScore1", questionScore1);//单选 每题分数
        dataMap.put("flag", flag);
        dataMap.put("imageData", imageData);//图片的数据参数
        //word路径
        String wordUrl = root + File.separator + "examFile" + File.separator + loginAppUser.getId() + "_" + System.currentTimeMillis() + ".doc";
        // word转pdf 路径 （无水印的图片）
        String pdfNoUrl = root + File.separator + "examFile" + File.separator + loginAppUser.getId() + "_" + System.currentTimeMillis() + ".pdf";

        //生成word文件
        new MDoc().createDoc(dataMap, wordUrl);
        //生成无水印的pdf文件
        WordPdfUtil.doc2pdf(wordUrl, pdfNoUrl);
        //无水印的pdf路径
        String noPdfUrl = FileFastdfsUtils.uploadFile(fileDoMu(new File(pdfNoUrl)));

        UpdateWrapper<DrawResult> updateWrapper = new UpdateWrapper<>();
        if (flag) {
            //有答案     无#有水印
            updateWrapper.eq("identity_card", identityCard).set("pdf_answer_url", noPdfUrl);
        } else {
            //无答案     无#有水印
            updateWrapper.eq("identity_card", identityCard).set("pdf_no_answer_url", noPdfUrl);
        }
        queryWrapper.eq("user_id", loginAppUser.getId());
        if (drawResultService.update(null, updateWrapper)) {
            FileUtil.del(wordUrl);
            FileUtil.del(pdfNoUrl);

            return new ApiResult(200, "生成pdf成功", null);
        }
//        FileUtil.del(pdfUrl);
        return new ApiResult(500, "生成错误", null);
    }


    /**
     * @param picNameList 图片的所有名称
     * @param imgData     图片路径的数组
     * @param imageData   图片的各个参数属性
     * @param i           第几个图片
     * @throws IOException
     */
    public Integer getImageData(List<String> picNameList, Map<String, Object> imgData,
                                List<Map<String, String>> imageData, int i) throws IOException {

        //所有id的集合
        List<String> rIdList = new ArrayList<>();
        Map<String, String> imageMap = null;
        if (ObjectUtils.isNotNull(imgData) && ObjectUtils.isNotNull(imgData.get("url"))) {

            for (Object str : (List<Object>) imgData.get("url")) {
                if (str instanceof String) {
                    if (str.toString().contains(".")) {
                        imageMap = new HashMap<>();
                        imageMap.put("name", "image" + i + "." + ImageUtil.getSuffix(str.toString()));
                        picNameList.add("image" + i + "." + ImageUtil.getSuffix(str.toString()));
                        imageMap.put("type", ImageUtil.getSuffix(str.toString()));
                        // 将图片服务地址 修改为 配置文件 获取
                        String data = ImageUtil.getImageBase64(fileServer + str);
                        if (StringUtils.isNotEmpty(data)) {
                            imageMap.put("data", data);
                            imageData.add(imageMap);
                            rIdList.add(i + "");
                        }
                        i++;
                    }
                } else if (str instanceof Map) {
                    try {
                        Map<String, String> str1 = (Map<String, String>) str;
                        String filename = str1.get("slt");
                        //String filename = slt.substring(slt.lastIndexOf("/") + 1);
                        imageMap = new HashMap<>();
                        imageMap.put("name", "image" + i + "." + ImageUtil.getSuffix(filename));
                        picNameList.add("image" + i + "." + ImageUtil.getSuffix(filename));
                        imageMap.put("type", ImageUtil.getSuffix(filename));
                        String data = ImageUtil.getImageBase64(localUrlPrefix + filename);
                        if (StringUtils.isNotEmpty(data)) {
                            imageMap.put("data", data);
                            imageData.add(imageMap);
                            rIdList.add(i + "");
                        }
                        i++;
                    } catch (Exception e) {
                        logger.error("试卷导出pdf时导出影像试题附件出错", e.getMessage());
                    }
                }

            }

            imgData.put("url", rIdList);
        }
        return i;
    }


    @PostMapping("/question/file")
    public ApiResult exportWord(@RequestParam("file") MultipartFile file) {
        System.out.println(FileFastdfsUtils.uploadFile(file));
        return new ApiResult(200, "成功", null);
    }

    public static MultipartFile fileDoMu(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        return new MockMultipartFile(file.getName(), file.getName(), Files.probeContentType(file.toPath()), inputStream);
    }

    /**
     * 根据试卷id查询试卷试题包含的所有知识点
     *
     * @param paperId
     * @return
     */
    @RequestMapping(value = "getKpIdsByPaperId", method = RequestMethod.GET)
    public Set<String> getKpIdsByPaperId(Long paperId) {
        HashSet<String> set = new HashSet<>();
        List<Question> paperDetail = paperManageService.findPaperDetail(paperId);
        paperDetail.stream().forEach(question -> {
            QueryWrapper<QuestionKpRel> questionKpRelQueryWrapper = new QueryWrapper<>();
            questionKpRelQueryWrapper.eq("question_id", question.getId());
            List<QuestionKpRel> list = questionKpRelService.list(questionKpRelQueryWrapper);
            list.stream().forEach(e -> {
                set.add(e.getKpId());
            });
        });
        return set;
    }
}