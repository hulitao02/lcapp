package com.cloud.exam.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ExamConstants;
import com.cloud.core.ResultMesEnum;
import com.cloud.exam.annotation.EditActivityAnnotation;
import com.cloud.exam.annotation.RepeateRequestAnnotation;
import com.cloud.exam.dao.*;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.service.*;
import com.cloud.exam.strategy.CompetitionlExamDraw;
import com.cloud.exam.strategy.DrawResultContext;
import com.cloud.exam.strategy.NomalExamDraw;
import com.cloud.exam.strategy.TrainExamDraw;
import com.cloud.exam.utils.CommonPar;
import com.cloud.exam.utils.FileFastdfsUtils;
import com.cloud.exam.utils.ListUtils;
import com.cloud.exam.utils.Tools;
import com.cloud.exam.utils.exam.QuestionUtils;
import com.cloud.exam.vo.*;
import com.cloud.exception.ResultMesCode;
import com.cloud.feign.exam.ExamFeign;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.log.LogAnnotation;
import com.cloud.model.log.constants.LogModule;
import com.cloud.model.user.*;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.utils.*;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Created by dyl on 2021/03/25.
 * 考试活动类
 */
@Api(value = "考试活动类")
@RestController
@RefreshScope
public class ExamController {
    public final static Logger logger = LoggerFactory.getLogger(ExamController.class);
    @Resource
    private ExamService examService;
    @Resource
    private StudentAnswerService studentAnswerService;
    @Resource
    private DrawResultService drawResultService;
    @Resource
    private SysDepartmentFeign sysDepartmentFeign;
    @Resource
    private ManageGroupService manageGroupService;
    @Resource
    private QuestionService questionService;
    @Resource
    private ExamPlaceareaService examPlaceareaService;
    @Resource
    private PaperService paperService;
    @Resource
    private ExamPlaceService examPlaceService;
    @Resource
    private FileFastdfsUtils fileFastdfsUtils;
    @Resource
    private CompetitionExamPaperRelService competitionExamPaperRelService;
    @Resource
    private PaperManageRelService paperManageRelService;
    @Resource
    private ExamFeign examFeign;
    @Resource
    private RedisUtils redisUtils;
    // 错误试题收录
    @Resource
    private ErrorQuestionDao errorQuestionDao;
    @Resource
    private CollectionQuestionService collectionQuestionService;
    @Resource(name = "executor")
    private Executor executor;
    @Resource
    private EvalNewService evalNewService;
    @Resource
    private ExamKpPersonAvgScoreService examKpPersonAvgScoreService;
    @Value("${file.upload_max_size}")
    private String uploadMaxSize;
    // 影像缩略图访问地址
    @Value("${localUrlPrefix}")
    private String localUrlPrefix;
    // 文件服务器地址
    @Value(value = "${file_server}")
    private String fileServer;

    // 开始录屏 调用容器接口参数
    @Value(value = "${uploadPath}")
    private String uploadPath;
    @Value(value = "${framesNum}")
    private Integer framesNum;

    @Resource
    DrawResultDao drawResultDao;
    @Value("${tifInterface}")
    private String tifInterface;

    public static boolean isOverlapping(Date start1, Date end1, Date start2, Date end2) {
        return start1.before(end2) && start2.before(end1);
    }

    @GetMapping(value = "makeEvaluation")
    public ApiResult makeEvaluation(@RequestBody Map<String, Object> map) {
        try {
//            evalNewService.makeEvaluation(Long.valueOf(map.get("acId").toString()));
            examKpPersonAvgScoreService.calculate(Long.valueOf(map.get("acId").toString()));
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }

    /**
     * 返回活动的信息
     *
     * @param exam
     * @return
     */
    @ApiOperation(value = "新建活动", notes = "新建活动")
    @ApiImplicitParams({@ApiImplicitParam(name = "exam", value = "封装活动实体", required = true, dataType = "exam"),
            @ApiImplicitParam(name = "parentId", value = "父活动id", dataType = "String")})
    @LogAnnotation(module = LogModule.ADD_EXAM)
    @RequestMapping(value = "/createActivity", method = RequestMethod.POST)
    @RepeateRequestAnnotation(extTimeOut = 1)
    public ApiResult createExam(@Valid @RequestBody Exam exam) {
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        exam.setCreateTime(new Date());
        if (!Validator.isEmpty(exam.getParentId())) {
            exam.setParentId(Long.valueOf(exam.getParentId()));
        } else {
            exam.setParentId(0L);
        }
        exam.setCreator(loginAppUser.getId());
        exam.setCreateDepartName(loginAppUser.getDepartmentName());
        if (ObjectUtils.isNotNull(exam.getScorePercent())) {
            exam.setScorePercent(NumberUtils.toFloat(exam.getScorePercent(), 100));
        }
        exam.setExamStatus(ExamConstants.ACTIVITY_NOT_LAUNCH);
        String message = "活动创建成功";
        if (exam.getId() != null) {
            Exam byId = examService.getById(exam.getId());
            if (byId.getExamStatus() > ExamConstants.ACTIVITY_NOT_LAUNCH) {
                return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "活动已经启动，无法修改。", byId);
            }
            byId.setUpdateTime(new Date());
            message = "活动修改成功";
        }
        if (exam.getParentId() > 0) {

            Exam pExam = examService.getById(exam.getParentId());
            Date startTime = pExam.getStartTime();
            Date endTime = pExam.getEndTime();
            Date startTime1 = exam.getStartTime();
            Date endTime1 = exam.getEndTime();
            boolean overlapping = isOverlapping(startTime, endTime, startTime1, endTime1);
            if (!overlapping) {
                return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_EXTTIME_LESS.getResultCode(), ResultMesCode.EXAM_EXTTIME_LESS.getResultMsg(), null);
            }
        }

        examService.saveOrUpdate(exam);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), message, exam);
    }


    @RequestMapping(value = "uploadExamDescribleFiles", method = RequestMethod.POST)
    public ApiResult uploadExamDescribleFiles(@RequestParam(value = "file", required = false) MultipartFile[] pictureFiles) {
        JSONArray jsonArray = new JSONArray();
        if (pictureFiles != null && pictureFiles.length > 0) {
            for (MultipartFile pictureFile : pictureFiles) {
                if (pictureFile.getSize() > Long.valueOf(uploadMaxSize) * 1024) {
                    throw new IllegalArgumentException("文件大小超出" + uploadMaxSize + "M限制。。。");
                } else {
                    String s = FileFastdfsUtils.uploadFile(pictureFile);
                    jsonArray.add(s);
                }
            }
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "上传成功", jsonArray);
    }

    @ApiOperation(value = "创建竞答活动保存结束提示")
    @RequestMapping(value = "saveExamEndRemark", method = RequestMethod.POST)
    public ApiResult saveExamEndRemark(@RequestBody HashMap<String, Object> map) {
        Object examRemark = map.get("examRemark");
        Object id = map.get("id");
        if (!Validator.isEmpty(id)) {
            Exam exam1 = examService.getById(Long.valueOf(String.valueOf(id)));
            exam1.setExamRemark(examRemark + "");
            examService.saveOrUpdate(exam1);
        }

        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), ApiResultHandler.success().getMessage(), null);
    }

    @Autowired
    ExamDao examDao;
    @Autowired
    ExamDepartPaperRelDao examDepartPaperRelDao;
    @Autowired
    ExamDepartUserRelDao examDepartUserRelDao;
    @Autowired
    ExamPlaceRelDao examPlaceRelDao;
    @Autowired
    ExamManageGroupRelDao examManageGroupRelDao;

    /**
     * @author:胡立涛
     * @description: TODO 根据活动id删除活动
     * @date: 2024/10/30
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "/delExam")
    public ApiResult delExam(@RequestBody Map map) {
        try {
            Long examId = Long.valueOf(map.get("examId").toString());
            // 删除活动
            examDao.deleteById(examId);
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("ac_id", examId);
            examDepartPaperRelDao.delete(queryWrapper);
            examDepartUserRelDao.delete(queryWrapper);
            examPlaceRelDao.delete(queryWrapper);
            examManageGroupRelDao.delete(queryWrapper);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 判断当前登录用户是否为本次考核阅卷人员或监考人员
     * @date: 2024/10/30
     * @param: [map]
     * @return: com.cloud.core.ApiResult 返回值=0为否，其他情况为是
     */
    @PostMapping(value = "/manageFlg")
    public ApiResult manageFlg(@RequestBody Map map) {
        try {
            LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
            Integer managegroupId = Integer.valueOf(map.get("managegroupId").toString());
            Long examId = Long.valueOf(map.get("examId").toString());
            QueryWrapper<ExamManageGroupRel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("managegroup_id", managegroupId);
            queryWrapper.eq("ac_id", examId);
            queryWrapper.eq("member_id", loginAppUser.getId());
            Integer count = examManageGroupRelDao.selectCount(queryWrapper);
            return ApiResultHandler.buildApiResult(200, "操作成功", count);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @return 返回信息 所有的主活动LIST集合
     */
    @ApiOperation(value = "查询所有主活动", notes = "查询所有主活动")
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "页数", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "每页数量", required = true, dataType = "Integer")})
    @RequestMapping(value = "/getAllExams", method = RequestMethod.GET)
    public ApiResult getAllExams(String name, String examStatus, Integer type, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") Date startTime, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") Date endTime, Integer page, Integer size) {
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        //查询用户角色
        Set<SysRole> rolesByUserId = sysDepartmentFeign.findRolesByUserId(loginAppUser.getId());
        //是否超级管理员
        boolean b1 = rolesByUserId.stream().anyMatch(sr -> sr.getCode().equals(ExamConstants.SYSTEM_SUPER_ADMIN));
        Page<Exam> pg = new Page<>(page, size);
        QueryWrapper<Exam> entityWrapper = new QueryWrapper<>();
        if (Validator.isNull(name)) {
            entityWrapper.eq("parent_id", 0L);
        }
        if (Validator.isNotNull(name)) {
            entityWrapper.like("name", name);
        }
        if (!Validator.isEmpty(startTime)) {
            entityWrapper.ge("start_time", startTime);
        }
        if (!Validator.isEmpty(endTime)) {
            entityWrapper.le("end_time", endTime);
        }
        if (Validator.isNotNull(examStatus)) {
            List ll = new ArrayList();
            String[] split = examStatus.split(",");
            for (int i = 0; i < split.length; i++) {
                ll.add(Integer.valueOf(split[i]));
            }
            entityWrapper.in("exam_status", ll);
        }
        if (!Validator.isEmpty(type)) {
            if (type == 10) {
                List ll = new ArrayList();
                ll.add(ExamConstants.EXAM_TYPE_JINGDA);
                ll.add(ExamConstants.EXAM_TYPE_LILUN);
                entityWrapper.in("type", ll);
            } else {
                entityWrapper.eq("type", type);
            }
        } else {
            List ll = new ArrayList();
            ll.add(ExamConstants.EXAM_TYPE_JINGDA);
            ll.add(ExamConstants.EXAM_TYPE_LILUN);
            entityWrapper.in("type", ll);
        }
        //根据当前用户角色选择需要展示的数据
        boolean is_judge = false;
        boolean is_monitor = false;
        boolean is_host = false;
        List<ExamManageGroupRel> ll = new ArrayList<>();
        if (!b1) {
            //判断是否是监考或者阅卷人员
            List<ExamManageGroupRel> collect3 = new ArrayList<>();
            Set<Long> examIds = new HashSet<>();
            ll = examService.getAllExamManageRel(loginAppUser.getId());
            collect3 = ll.stream().filter(e -> e.getManagegroupId() == 1).collect(Collectors.toList());
            if (!Validator.isEmpty(collect3) && collect3.size() > 0) {
                is_monitor = true;
                collect3.stream().forEach(e -> examIds.add(e.getAcId()));
            }
            collect3 = ll.stream().filter(e -> e.getManagegroupId() == 2).collect(Collectors.toList());
            if (!Validator.isEmpty(collect3) && collect3.size() > 0) {
                is_judge = true;
                collect3.stream().forEach(e -> examIds.add(e.getAcId()));
            }
            //可以看到竞答活动主持人是当前用户的活动
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("host_id", loginAppUser.getId());
            List<Exam> list = examService.list(queryWrapper);
            if (list.size() > 0) {
                is_host = true;
                list.stream().forEach(e -> examIds.add(e.getId()));
            }

            //只显示本部门的活动列表
            if (!is_judge && !is_monitor && !is_host) {
                Set<Long> userIds = new HashSet<>();
                Long departmentId = loginAppUser.getDepartmentId();
                List<AppUser> allUsers = sysDepartmentFeign.getAllUsers();
                List<AppUser> collect = allUsers.stream().filter(e -> e.getDepartmentId().equals(departmentId)).collect(Collectors.toList());
                collect.stream().forEach(e -> userIds.add(e.getId()));
                /*if(is_judge){
                    List<ExamManageGroupRel> collect1 = ll.stream().filter(l -> l.getMemberId() == loginAppUser.getId()).collect(Collectors.toList());
                    collect1.stream().forEach(ls->userIds.add(ls.getMemberId()));
                }
                if(is_monitor){
                    List<ExamManageGroupRel> collect2 = ll.stream().filter(l -> l.getMemberId() == loginAppUser.getId()).collect(Collectors.toList());
                    collect2.stream().forEach(ls->userIds.add(ls.getMemberId()));
                }*/
                entityWrapper.in("creator", userIds);
            } else {
                entityWrapper.in("id", examIds);
            }
            //排除当前登录用户参加的活动
            Set<Long> allExamRelByCurrentUser = examService.getAllExamRelByCurrentUser(loginAppUser.getId());
            if (allExamRelByCurrentUser.size() > 0) {
                entityWrapper.notIn("id", allExamRelByCurrentUser);
            }

        }
        entityWrapper.orderByDesc("create_time");
        IPage<Exam> answerVOIPage = examService.page(pg, entityWrapper);
        for (Exam am : answerVOIPage.getRecords()) {
            QueryWrapper query = new QueryWrapper<>();
            query.eq("ac_id", am.getId());
            List<ExamDepartPaperRel> examList = examDepartPaperRelDao.selectList(query);
            am.setPaperFlg(0);
            if (examList != null && examList.size() > 0) {
                for (ExamDepartPaperRel bean : examList) {
                    if (bean.getPaperId() != null && bean.getPaperId() > 0) {
                        am.setPaperFlg(1);
                        break;
                    }
                }
            }
            Long creatorId = am.getCreator();
            if (1 == am.getType()) {
                //竞答活动返回主持人
                AppUser appUserById = sysDepartmentFeign.findAppUserById(am.getHostId());
                am.setHostName(appUserById.getNickname());
                if (am.getExamStatus() > 0 && !am.getExamStatus().equals(ExamConstants.ACTIVITY_CONCELL)) {
                    //返回准考证号
                    QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("ac_id", am.getId());
                    List<DrawResult> list = drawResultService.list(queryWrapper);
                    am.setIdentityCard(list.get(0).getIdentityCard());
                }

            }
            AppUser appUserById = sysDepartmentFeign.findAppUserById(creatorId);
            am.setCreateDepartName(appUserById.getDepartmentName());
            if (b1) {
                am.setJudge(true);
                am.setMonitor(true);
            } else {
                ll.stream().forEach(e -> {
                    if (e.getAcId().equals(am.getId())) {
                        if (e.getManagegroupId() == 2) {
                            am.setJudge(true);
                        }
                        if (e.getManagegroupId() == 1) {
                            am.setMonitor(true);
                        }
                    }
                });
            }
        }
        QueryWrapper<Exam> qw = new QueryWrapper<>();
        qw.ne("parent_id", 0L);
        List<Exam> list1 = examService.list(qw);
        getExamTree(answerVOIPage.getRecords(), list1);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询所有活动成功", answerVOIPage);
    }

    public void getExamTree(List<Exam> records, List<Exam> exams) {
        exams.stream().forEach(e -> {
                    records.stream().forEach(ee -> {
                        if (!Validator.isEmpty(e.getParentId()) && e.getParentId().equals(ee.getId())) {
                            Long creatorId = e.getCreator();
                            AppUser appUserById = sysDepartmentFeign.findAppUserById(creatorId);
                            e.setCreateDepartName(appUserById.getDepartmentName());
                            if (ee.isJudge()) {
                                e.setJudge(true);
                            }
                            if (ee.isMonitor()) {
                                e.setMonitor(true);
                            }
                            ee.getChildList().add(e);
                            if (1 == e.getType()) {
                                //竞答活动返回主持人
                                AppUser hostUserById = sysDepartmentFeign.findAppUserById(e.getHostId());
                                e.setHostName(hostUserById.getNickname());
                                if (e.getExamStatus() > 0 && !e.getExamStatus().equals(ExamConstants.ACTIVITY_CONCELL)) {
                                    //返回准考证号
                                    QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
                                    queryWrapper.eq("ac_id", ee.getId());
                                    List<DrawResult> list = drawResultService.list(queryWrapper);
                                    e.setIdentityCard(list.get(0).getIdentityCard());
                                }
                            }
                        }
                    });
                }
        );
    }

    /**
     * @return 返回单个活动实体
     */
    @ApiOperation(value = "根据准考证号查询活动", notes = "根据id查询某个活动")
    @ApiImplicitParam(name = "identityCard", value = "准考证号", required = true, dataType = "Long")
    @RequestMapping(value = "/getExamDescribeByIdentityCard", method = RequestMethod.GET)
    public ApiResult getExamDescribeByIdentityCard(String identityCard) {
        Exam exam = examService.getExamDescribeByIdentityCard(identityCard);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询活动成功", exam);
    }

    /**
     * @return 根据活动Id查询活动信息，返回活动实体
     */
    @ApiOperation(value = "修改活动，根据id查询活动", notes = "根据id查询某个活动")
    @ApiImplicitParam(name = "examId", value = "活动id", required = true, dataType = "Long")
    @RequestMapping(value = "/getExamById", method = RequestMethod.GET)
    public ApiResult getExamById(Long examId) {
        Exam exam = examService.getById(examId);
       /* LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        Set<SysRole> rolesByUserId = sysDepartmentFeign.findRolesByUserId(loginAppUser.getId());
        boolean b1 = rolesByUserId.stream().anyMatch(sr -> sr.getId().equals(ExamConstants.SYS_SUPER_ADMIN));
        if (!b1) {
            if (!exam.getCreator().equals(loginAppUser.getId())) {
                return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "不能修改不是自己创建的活动。。。", null);
            }
        }*/
        //竞答活动返回主持人
        if (!Validator.isEmpty(exam.getHostId())) {
            AppUser appUserById = sysDepartmentFeign.findAppUserById(exam.getHostId());
            exam.setHostName(appUserById.getNickname());
        }
        if (!Validator.isEmpty(exam.getScorePercent())) {
            exam.setScorePercent(NumberUtils.toDouble(exam.getScorePercent(), 100));
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询活动成功", exam);
    }


    /**
     * @return 返回某个活动下的子活动信息
     */
    @ApiOperation(value = "查询某个活动下的子活动", notes = "查询某个活动下的子活动")
    @ApiImplicitParam(name = "parentId", value = "活动id", required = true, dataType = "String")
    @RequestMapping(value = "/getClildExam", method = RequestMethod.GET)
    public ApiResult getClildExam(@RequestParam String parentId) {
        QueryWrapper<Exam> entityWrapper = new QueryWrapper<Exam>();
        entityWrapper.eq("parent_id", Integer.valueOf(parentId));
        List<Exam> list = examService.list(entityWrapper);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询所有子活动", list);
    }

    @ApiOperation(value = "删除活动", notes = "删除活动")
    @ApiImplicitParam(name = "examId", value = "活动id", required = true, dataType = "Long")
    @RequestMapping(value = "/deleteExamById", method = RequestMethod.GET)
    public ApiResult deleteExamById(Long examId) {
        Exam exam = examService.getById(examId);
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        Set<SysRole> rolesByUserId = sysDepartmentFeign.findRolesByUserId(loginAppUser.getId());
        boolean b1 = rolesByUserId.stream().anyMatch(sr -> sr.getCode().equals(ExamConstants.SYSTEM_SUPER_ADMIN));
        if (!b1) {
            if (!exam.getCreator().equals(loginAppUser.getId())) {
                return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "您没有权限删除此活动。", null);
            }
        }
        QueryWrapper<Exam> entityWrapper = new QueryWrapper<Exam>();
        entityWrapper.eq("parent_id", Long.valueOf(examId));
        List<Exam> list = examService.list(entityWrapper);
        boolean b = list.stream().anyMatch(e -> e.getExamStatus().equals(ExamConstants.EXAM_START));
        if (b) {
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "该活动下子活动正在考试，无法删除。", b);
        } else {
            if (list.size() > 0) {
                for (Exam e : list) {
                    examService.removeById(e.getId());
                }
            }
        }
        examService.removeById(examId);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "删除活动成功。", b);
    }

    /**
     * @return 返回true或者false
     */
    @ApiOperation(value = "修改活动", notes = "修改活动")
    @ApiImplicitParam(name = "exam", value = "封装实体类", dataType = "exam")
    @RequestMapping(value = "/editExam")
    public ApiResult editExam(@RequestBody Exam exam) {
        Exam e = examService.getById(exam.getId());
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        Set<SysRole> rolesByUserId = sysDepartmentFeign.findRolesByUserId(loginAppUser.getId());
        boolean b1 = rolesByUserId.stream().anyMatch(sr -> sr.getCode().equals(ExamConstants.SYSTEM_SUPER_ADMIN));
        if (!b1) {
            if (!e.getCreator().equals(loginAppUser.getId())) {
                return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "不能修改非自己创建的活动。。。", null);
            }
        }
        boolean b = examService.saveOrUpdate(exam);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "修改活动成功。。。", b);
    }

    @ApiOperation(value = "更改活动状态(5-取消，4，判卷完成，活动完成，3考试结束 6 开始考试)", notes = "更改活动状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "examId", value = "活动id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "status", value = "状态值", required = true, dataType = "Integer")})
    @RequestMapping(value = "/updateExamStatus", method = RequestMethod.GET)
    public ApiResult updateExamStatus(Long examId, Integer status) {
        Exam exam = examService.getById(examId);
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        Set<SysRole> rolesByUserId = sysDepartmentFeign.findRolesByUserId(loginAppUser.getId());
        boolean b1 = rolesByUserId.stream().anyMatch(sr -> sr.getCode().equals(ExamConstants.SYSTEM_SUPER_ADMIN));
        /*if (!b1) {
            if (!exam.getCreator().equals(loginAppUser.getId())) {
                return ApiResultHandler.buildApiResult(ResultMesCode.ACTIVITY_NOT_PERMISSION.getResultCode(), ResultMesCode.ACTIVITY_NOT_PERMISSION.getResultMsg(), null);
            }
        }*/
        QueryWrapper<Exam> entityWrapper = new QueryWrapper<Exam>();
        entityWrapper.eq("parent_id", Long.valueOf(examId));
        List<Exam> list = examService.list(entityWrapper);
        if (ExamConstants.ACTIVITY_CONCELL.equals(status)) {
            boolean b = list.stream().anyMatch(e -> e.getExamStatus().equals(ExamConstants.ACTIVITY_EXAM_START));
            if (b) {
                return ApiResultHandler.buildApiResult(ResultMesCode.ACTIVITY_CHILEREN_NOT_FINISH.getResultCode(), ResultMesCode.ACTIVITY_CHILEREN_NOT_FINISH.getResultMsg(), b);
            }
        }
        QueryWrapper qw = new QueryWrapper();
        qw.eq("ac_id", examId);
        List<DrawResult> ll = drawResultService.list(qw);
        if (ExamConstants.ACTIVITY_FINISH.equals(status)) {
            if (ll.stream().anyMatch(dr -> dr.getUserStatus().equals(ExamConstants.EXAM_WAIT_JUDGE))) {
                return ApiResultHandler.buildApiResult(ResultMesCode.ACTIVITY_PAPER_WAITING_JUDGE.getResultCode(), ResultMesCode.ACTIVITY_PAPER_WAITING_JUDGE.getResultMsg(), null);
            }

        }
        //手动点击考试结束
        if (status.equals(ExamConstants.EXAM_WAIT_JUDGE)) {
            if (ll.stream().anyMatch(dr -> dr.getUserStatus().equals(ExamConstants.EXAM_START))) {
                return ApiResultHandler.buildApiResult(ResultMesCode.ACTIVITY_USER_NOT_FINISH.getResultCode(), ResultMesCode.ACTIVITY_USER_NOT_FINISH.getResultMsg(), null);
            }
            exam.setExamStatus(status);
            examService.saveOrUpdate(exam);
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", null);
        }
        //手动点击开始考试
        if (status.equals(ExamConstants.ACTIVITY_EXAM_START)) {
            if (exam.getExamStatus().equals(ExamConstants.ACTIVITY_EXAM_START)) {
                return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "活动已经开始。。。", null);
            }
            exam.setExamStatus(status);
            exam.setExamTime(new Date());
            examService.saveOrUpdate(exam);
            /*QueryWrapper<DrawResult> q = new QueryWrapper<>();
            q.eq("ac_id", examId);
            List<DrawResult> ls = drawResultService.list(qw);
            for (DrawResult dr : ls) {
                dr.setUserStatus(status);
                drawResultService.saveOrUpdate(dr);
            }*/
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", null);
        }
        exam.setExamStatus(status);
        examService.updateById(exam);
        for (Exam ee : list) {
            ee.setExamStatus(status);
            examService.saveOrUpdate(ee);
        }
        if (ll.size() > 0) {
            for (DrawResult dr : ll) {
                if (status == 5) {
                    drawResultService.removeById(dr.getId());
                } else {
                    dr.setUserStatus(status);
                    drawResultService.saveOrUpdate(dr);
                }
            }
        }
        if (status == 4 || status == 5) {
            //删除redis里面相关缓存
            redisUtils.delKeys("exam:" + examId + ":*");
            QueryWrapper<DrawResult> queryWrapper = new QueryWrapper();
            queryWrapper.eq("ac_id", examId);
            List<DrawResult> list1 = drawResultService.list(queryWrapper);
            list1.stream().forEach(e -> {
                redisUtils.delKeys("exam:" + e.getIdentityCard());
            });
        }
        if (status == 4) {
            try {
//                evalService.makeEvaluation(examId);
                evalNewService.makeEvaluation(examId);
//                examKpPersonAvgScoreService.calculate(examId);
            } catch (Exception e) {
                logger.error("点击活动结束，调用评价接口出错。。。");
            }

        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", null);
    }

    @ApiOperation(value = "查询所有比武单位", notes = "查询所有比武单位")
    @RequestMapping(value = "/getAllSysDepartMent", method = RequestMethod.GET)
    public ApiResult getAllSysDepartMent(HttpServletRequest request) {
        String departId = request.getParameter("departId");

        List<SysDepartment> sysDepartmentList = sysDepartmentFeign.findAll();
        List<SysDepartment> list = new ArrayList<>();
        if (StringUtils.isNotBlank(departId)) {
            Long id = Long.valueOf(departId);
            SysDepartment sysDepartmentById = sysDepartmentFeign.findSysDepartmentById(id);

            setSortTable(id, sysDepartmentList, list);
            list.add(sysDepartmentById);
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询所有比武单位成功", list);
        } else {
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询所有比武单位成功", sysDepartmentList);
        }
    }

    private void setSortTable(Long parentId, List<SysDepartment> all, List<SysDepartment> list) {
        all.forEach(a -> {
            if (a.getParentId().longValue() == parentId.longValue()) {
                list.add(a);
                setSortTable(a.getId(), all, list);
            }
        });
    }

    @ApiOperation(value = "添加考试活动单位", notes = "添加考试活动单位")
    @ApiImplicitParams({@ApiImplicitParam(name = "examId", value = "活动id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "departId", value = "单位id", required = true, dataType = "String")})
    @RequestMapping(value = "/addDepartByexamId", method = RequestMethod.GET)
    @EditActivityAnnotation(parameterType = ExamConstants.PARAMETER_STR)
    public ApiResult addDepartByexamId(@RequestParam("examId") Long examId, @RequestParam("departId") String departId) {
        String[] strings = departId.split(",");
        for (String deId : strings) {
            List<ExamDepartUserRel> list = examService.getExamDepartRel(examId, Long.valueOf(deId));
            if (list.size() < 1) {
                examService.addDepartByexamId(examId, Long.valueOf(deId));
                examService.addDepartUserRel(examId, Long.valueOf(deId));
            }
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "添加单位成功", null);
    }

    @ApiOperation(value = "获取考试活动单位需要参加人员", notes = "获取考试活动单位需要参加人员")
    @ApiImplicitParams({@ApiImplicitParam(name = "examId", value = "活动id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "departId", value = "单位id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "name", value = "姓名", dataType = "String"),
            @ApiImplicitParam(name = "kps", value = "课程对应的知识点id", dataType = "String")})
    @RequestMapping(value = "/getAllDepartUsers", method = RequestMethod.GET)
    public ApiResult getAllDepartUsers(Long examId, Long departId, String name, Integer flag, String kps) {
        List<AppUser> allUserVOs = sysDepartmentFeign.getAllUsers();
        List<SysDepartment> sysDepartmentList = sysDepartmentFeign.findSysDepartmentList();
        //获取指定单位下所有的子单位
        Set<Long> set = new HashSet<>();
        set.add(departId);
        Set<Long> childDepart = getChildDepart(departId, sysDepartmentList, set);
        List<AppUser> data = allUserVOs.stream().filter(au -> childDepart.contains(au.getDepartmentId())).collect(Collectors.toList());

        //根据flag判断是查询参加考试的单位人员还是添加管理小组人员(0 添加参考人员 1 添加管理小组人员)
        if (flag == 0) {
            Exam exam = examService.getById(examId);
            Long hostId = exam.getHostId();
            if (!Validator.isEmpty(hostId)) {
                data.removeIf(ap -> ap.getId().equals(hostId));
            }
            List<ExamManageGroupRel> allExamManageRelByExamId = examService.getAllExamManageRelByExamId(examId);
            if (CollectionUtil.isNotEmpty(allExamManageRelByExamId)) {
                for (ExamManageGroupRel egr : allExamManageRelByExamId) {
                    data.removeIf(ap -> ap.getId().equals(egr.getMemberId()));
                }
            }
        } else if (flag == 2) {
            // 添加监考组成员
            data = sysDepartmentFeign.getAppUserByRole("jiankaozu");
            data = data.stream().filter(d -> d.getDepartmentId().equals(departId)).collect(Collectors.toList());
        } else if (flag == 3) {
            // 添加阅卷组成员
            data = sysDepartmentFeign.getAppUserByRole("yuejuanzu");
            data = data.stream().filter(d -> d.getDepartmentId().equals(departId)).collect(Collectors.toList());
        }

        List<ExamDepartUserRel> departAndUserByExamId = examService.getDepartAndUserByExamId(examId);

        //排除已经添加考试的人员
        for (ExamDepartUserRel rel : departAndUserByExamId) {
            if (rel.getMemberId() != null) {
                data.removeIf(record -> record.getId().equals(rel.getMemberId()));
            }
        }
        //课程添加人员时选择具有相同知识点的学员
//        if (StringUtils.isNotBlank(kps)) {
//            List<Long> list = new ArrayList<>();
//            List<AppUser> appUsers = new ArrayList<>();
//            String[] split = kps.split(",");
//            for (String str : split) {
//                list.add(Long.valueOf(str));
//            }
//            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
//            data.parallelStream().forEach(e -> {
//                //设置请求原线程下的参数
//                RequestContextHolder.setRequestAttributes(requestAttributes);
//                Set<Long> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(e.getId());
//                if (kpIdsbyUserId.containsAll(list)) {
//                    appUsers.add(e);
//                }
//            });
//            data = appUsers;
//        }

        if (Validator.isNotNull(name)) {
            data = data.stream().filter(u -> u.getNickname().contains(name)).collect(Collectors.toList());
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取单位人员成功", data);
    }

    public Set<Long> getChildDepart(Long parentId, List<SysDepartment> allDepart, Set set) {
        allDepart.stream().forEach(e -> {
            if (e.getParentId().equals(parentId)) {
                set.add(e.getId());
                getChildDepart(e.getId(), allDepart, set);
            }
        });
        return set;
    }

    @ApiOperation(value = "添加考试活动单位人员", notes = "添加考试活动单位人员")
    @ApiImplicitParams({@ApiImplicitParam(name = "examId", value = "活动id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "departId", value = "单位id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "memberId", value = "人员id", required = true, dataType = "String"),
            @ApiImplicitParam(name = "flag", value = "是否直接添加人员", required = false, dataType = "Boolean")})
    @RequestMapping(value = "/addMemberByDepartmentId", method = RequestMethod.GET)
    @EditActivityAnnotation(parameterType = ExamConstants.PARAMETER_STR)
    public ApiResult addMemberByDepartmentId(Long examId, Long departId, String memberId, boolean flag) {
        Exam exam = examService.getById(examId);

        String[] split = memberId.split(",");
        List<ExamDepartPaperRel> departAndPaperByExamId = examService.findPaperByexamIdanddepartId(examId, departId);

        //flag = true 代表选择的考生与原有试卷数据权限不一致 ，选择添加考生，需要删除之前的关联试卷关系
        if (flag) {
            //竞答活动处理
            if (exam.getType() == 1) {
                //添加人员删除试卷
                Object competitionPaper = Tools.getDeptCache("competitionPaper" + examId);
                if (competitionPaper != null) {
                    List<CompetitionExamPaperRel> list = (List<CompetitionExamPaperRel>) competitionPaper;
                    list.stream().forEach(competitionExamPaperRel -> {
                        competitionExamPaperRelService.removeById(competitionExamPaperRel.getId());
                    });
                }
                examService.delDepart(departId, examId);
                for (int i = 0; i < split.length; i++) {
                    examService.addMemberByDepartmentId(examId, departId, Long.valueOf(split[i]));
                }
            } else {
                //删除活动与试卷的关系
                examService.deleteDepartPaperRel(examId, departId);
                examService.addExamPaperRelations(examId, departId, null);
                examService.delDepart(departId, examId);
                for (int i = 0; i < split.length; i++) {
                    examService.addMemberByDepartmentId(examId, departId, Long.valueOf(split[i]));
                }
            }

            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "添加单位人员成功", null);
        }
        if (exam.getType() == 1) {
            //竞答活动处理
            QueryWrapper<CompetitionExamPaperRel> qw = new QueryWrapper<>();
            qw.eq("ac_id", examId);
            List<CompetitionExamPaperRel> list = competitionExamPaperRelService.list(qw);
            if (CollectionUtils.isEmpty(list)) {
                examService.delDepart(departId, examId);
                for (int i = 0; i < split.length; i++) {
                    examService.addMemberByDepartmentId(examId, departId, Long.valueOf(split[i]));
                }
            } else {
                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
                List<CompetitionExamPaperRel> ll = new ArrayList<>();
                list.parallelStream().forEach(competitionExamPaperRel -> {
                    RequestContextHolder.setRequestAttributes(requestAttributes);
                    Set<Long> kpIdsByPaperId = examFeign.getKpIdsByPaperId(competitionExamPaperRel.getPaperId());
//                    for (int i = 0; i < split.length; i++) {
//                        Set<Long> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(Long.valueOf(split[i]));
//                        if (!kpIdsbyUserId.containsAll(kpIdsByPaperId)) {
//                            ll.add(competitionExamPaperRel);
//                        }
//                    }
                });
                if (CollectionUtils.isNotEmpty(ll)) {
                    Tools.putDeptCache("competitionPaper" + examId, ll);
                    return ApiResultHandler.buildApiResult(ResultMesEnum.EXAM_USER_NOT_MATCH.getResultCode(), ResultMesEnum.EXAM_USER_NOT_MATCH.getResultMsg(), false);
                } else {
                    examService.delDepart(departId, examId);
                    for (int i = 0; i < split.length; i++) {
                        examService.addMemberByDepartmentId(examId, departId, Long.valueOf(split[i]));
                    }
                }
            }
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "添加单位人员成功", null);
        }
        if (CollectionUtils.isEmpty(departAndPaperByExamId)) {
            examService.delDepart(departId, examId);
            for (int i = 0; i < split.length; i++) {
                examService.addMemberByDepartmentId(examId, departId, Long.valueOf(split[i]));
            }
        } else {
            //在线考核活动一个单位下只能有一个试卷
            List<Object> ll = new ArrayList<>();
            Set<Long> kpIdsByPaperId = examFeign.getKpIdsByPaperId(ObjectUtil.isNull(departAndPaperByExamId.get(0).getPaperId()) ? 0L : departAndPaperByExamId.get(0).getPaperId());
            for (int i = 0; i < split.length; i++) {
                Set<String> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(Long.valueOf(split[i]));
                if (!kpIdsbyUserId.containsAll(kpIdsByPaperId)) {
                    ll.add(Long.valueOf(split[i]));
                    //examService.delDepart(departId, examId);
                    //examService.addMemberByDepartmentId(examId, departId, Long.valueOf(split[i]));
                }
            }
            if (CollectionUtils.isNotEmpty(ll)) {
                return ApiResultHandler.buildApiResult(ResultMesEnum.EXAM_USER_NOT_MATCH.getResultCode(), ResultMesEnum.EXAM_USER_NOT_MATCH.getResultMsg(), false);
            } else {
                examService.delDepart(departId, examId);
                for (int i = 0; i < split.length; i++) {
                    examService.addMemberByDepartmentId(examId, departId, Long.valueOf(split[i]));
                }
            }
        }

        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "添加单位人员成功", null);
    }


    @ApiOperation(value = "增加单位活动试卷", notes = "增加单位活动试卷")
    @ApiImplicitParams(value = {@ApiImplicitParam(name = "examId", value = "活动id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "map", value = "活动单位 试卷id map集合", dataType = "Map"),
            @ApiImplicitParam(name = "flag", value = "true通过添加试卷添加关系，false 通过单选框更改关联关系", dataType = "Map")})
    @RequestMapping(value = "/addExamPaperRelations", method = RequestMethod.POST)
    @EditActivityAnnotation(parameterType = ExamConstants.PARAMETER_STR)
    public ApiResult addExamPaperRelations(@RequestParam Long examId, @RequestBody Map<Long, String> map, @RequestParam boolean flag) {
        Set<Map.Entry<Long, String>> entries = map.entrySet();
        Iterator<Map.Entry<Long, String>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Exam exam = examService.getById(examId);
            Map.Entry<Long, String> next = iterator.next();
            Long departId = next.getKey();
            String paperIds = next.getValue();
            List<ExamDepartPaperRel> list = examService.findPaperByexamIdanddepartId(examId, departId);
            if (exam.getType() == 0 && flag) {
                if (CollectionUtils.isNotEmpty(list) && list.size() == 1 && ObjectUtil.isNotNull(list.get(0).getPaperId())) {
                    throw new IllegalArgumentException("一个单位只能绑定一张试卷");
                }
                List<ExamDepartUserRel> userByByexamIdanddepartId = examService.findUserByByexamIdanddepartId(examId, departId);
                if (CollectionUtils.isEmpty(userByByexamIdanddepartId)) {
                    throw new IllegalArgumentException("请先选择考试人员");
                } else if (userByByexamIdanddepartId.size() == 1) {
                    userByByexamIdanddepartId.stream().forEach(examDepartUserRel -> {
                        if (ObjectUtil.isNull(examDepartUserRel.getMemberId())) {
                            throw new IllegalArgumentException("请先选择考试人员");
                        }
                    });
                }

            }
            if (Validator.isNull(paperIds)) {
                examService.deleteDepartPaperRel(examId, departId);
                examService.addExamPaperRelations(examId, departId, null);
                continue;
            }
            String[] split = paperIds.split(",");
            if (!Validator.isEmpty(flag) && flag) {
                examService.deleteExtDepartPaperRel(examId, departId);
                if (split.length > 0) {
                    for (int i = 0; i < split.length; i++) {
                        ExamDepartPaperRel relations = examService.findRelations(examId, departId, Long.valueOf(split[i]));
                        if (Validator.isEmpty(relations)) {
                            examService.addExamPaperRelations(examId, departId, Long.valueOf(split[i]));
                        }
                    }
                }
            } else if (Validator.isEmpty(flag) || !flag) {
                examService.deleteDepartPaperRel(examId, departId);
                if (split.length > 0) {
                    for (int i = 0; i < split.length; i++) {
                        ExamDepartPaperRel relations = examService.findRelations(examId, departId, Long.valueOf(split[i]));
                        if (Validator.isEmpty(relations)) {
                            examService.addExamPaperRelations(examId, departId, Long.valueOf(split[i]));
                        }
                    }
                } else {
                    examService.addExamPaperRelations(examId, departId, null);
                }
            }
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "保存单位试卷成功", map);
    }

    @ApiOperation(value = "删除单位", notes = "删除单位")
    @ApiImplicitParam(name = "map", value = "", required = true, dataType = "Map")
    @RequestMapping(value = "/delDerpart", method = RequestMethod.DELETE)
    @EditActivityAnnotation(parameterType = ExamConstants.PARAMETER_MAP)
    public ApiResult delDerpart(@RequestBody Map<String, Long> map) {
        Long examId = map.get("examId");
        Long departId = map.get("departId");
        Integer res2 = examService.delDepartRel(departId, examId);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "删除单位成功", res2);
    }

    @ApiOperation(value = "删除单位成员", notes = "删除单位成员")
    @ApiImplicitParam(name = "map", value = "", dataType = "Map")
    @RequestMapping(value = "/delDerpartUser", method = RequestMethod.DELETE)
    @EditActivityAnnotation(parameterType = ExamConstants.PARAMETER_MAP)
    public ApiResult delDerpartUser(@RequestBody Map<String, Long> map) {
        Long examId = map.get("examId");
        Long departId = map.get("departId");
        Long userId = map.get("userId");
        List<ExamDepartUserRel> examDepartRel = examService.getExamDepartRel(examId, departId);
        if (examDepartRel.size() > 1) {
            examService.delDepartUser(departId, examId, userId);
        } else {
            examService.updateDepartUser(departId, examId, userId);
        }

        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "删除单位成员成功", null);
    }

    @ApiOperation(value = "获取活动下的单位", notes = "获取活动下的单位")
    @ApiImplicitParam(name = "examId", value = "活动id", dataType = "Long")
    @RequestMapping(value = "/getDepartmentByExamId", method = RequestMethod.GET)
    public ApiResult getDepartmentByExamId(Long examId) {
        List<SysDepartment> sysDepartmentList = sysDepartmentFeign.findSysDepartmentList();
        List<ExamDepartUserRel> list = examService.getSysDepartmemtByExamId(examId);
        Map<Long, List<ExamDepartUserRel>> collect1 = list.stream().collect(Collectors.groupingBy(ExamDepartUserRel::getDepartId));
        List<ExamDepartPaperRel> list1 = examService.getDepartAndPaperByExamId(examId);
        Map<Long, List<ExamDepartPaperRel>> collect = list1.stream().collect(Collectors.groupingBy(ExamDepartPaperRel::getDepartId));
        List<SysDepartmentVO> ls = new ArrayList<>();
        for (Long departId : collect.keySet()) {
            List<PaperVO> paperList = new ArrayList<>();
            List<Long> paperIds = new ArrayList<>();
            SysDepartmentVO vo = new SysDepartmentVO();
            List<AppUser> au = new ArrayList<>();
            collect.get(departId).stream().forEach(ep -> {
                if (!Validator.isEmpty(ep.getPaperId())) {
                    PaperVO pv = new PaperVO();
                    Paper byId = paperService.getById(ep.getPaperId());
                    BeanUtils.copyProperties(byId, pv);
                    pv.setPaperId(ep.getPaperId());
                    pv.setPaperName(byId.getPaperName());
                    pv.setDepartId(departId);
                    paperList.add(pv);
                    paperIds.add(ep.getPaperId());
                }
            });
            vo.setPaperIds(paperIds);
            vo.setPaperList(paperList);
            List<ExamDepartUserRel> examDepartUserRels = collect1.get(departId);
            if (!Validator.isEmpty(examDepartUserRels)) {
                for (ExamDepartUserRel eu : examDepartUserRels) {
                    if (!Validator.isEmpty(eu.getMemberId())) {
                        AppUser userById = sysDepartmentFeign.findAppUserById(eu.getMemberId());
                        au.add(userById);
                    }
                }
            }
            vo.setAppUserList(au);
            for (SysDepartment sysDepartment : sysDepartmentList) {
                if (sysDepartment.getId().equals(departId)) {
                    vo.setDepartment(sysDepartment);
                }
            }
            ls.add(vo);
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取活动下的单位成功", ls);
    }

    @ApiOperation(value = "查询某个活动下的管理小组", notes = "查询某个活动下的管理小组")
    @ApiImplicitParam(name = "examId", value = "活动id", required = true, dataType = "Long")
    @RequestMapping(value = "/getManageByExamId", method = RequestMethod.GET)
    public ApiResult getManageByExamId(Long examId) {
        List<ManageGroup> list1 = manageGroupService.list();
        boolean a = list1.stream().anyMatch(e -> e.getId().equals(1));
        boolean b = list1.stream().anyMatch(e -> e.getId().equals(2));
        if (!a) {
            ManageGroup mg = new ManageGroup();
            mg.setId(1);
            mg.setName("监考组");
            mg.setDescribe("负责考试过程监考");
            manageGroupService.save(mg);
        }
        if (!b) {
            ManageGroup mg = new ManageGroup();
            mg.setId(2);
            mg.setName("阅卷组");
            mg.setDescribe("负责考试过程监考");
            manageGroupService.save(mg);
        }
        Exam exam = examService.getById(examId);
        List<ExamManageGroupRel> list = examService.getAllExamManageRelByExamId(examId);
        TreeSet<ManageGroupVO> ls = new TreeSet<ManageGroupVO>((Comparator<ManageGroupVO>) (o1, o2) -> {
            return o1.getMg().getId() - o2.getMg().getId();
        });
        if (exam.getType().equals(ExamConstants.EXAM_TYPE_LILUN)) {
            //竞答活动不用添加监考和阅卷组
            boolean b1 = list.stream().anyMatch(ss -> ss.getManagegroupId() == 1L);
            if (!b1) {
                ManageGroupVO mgo = new ManageGroupVO();
                ManageGroup mg = new ManageGroup();
                mg.setId(1);
                mg.setName("监考组");
                mg.setDescribe("负责考试过程监考");
                mgo.setMg(mg);
                mgo.setAppUserVOList(new ArrayList<>());
                ls.add(mgo);
            }
            boolean b2 = list.stream().anyMatch(ss -> ss.getManagegroupId() == 2L);
            if (!b2) {
                ManageGroupVO mgo = new ManageGroupVO();
                ManageGroup mg = new ManageGroup();
                mg.setId(2);
                mg.setName("阅卷组");
                mg.setDescribe("负责阅卷");
                mgo.setMg(mg);
                mgo.setAppUserVOList(new ArrayList<>());
                ls.add(mgo);
            }
        } else if (exam.getType().equals(ExamConstants.EXAM_TYPE_XUNLIAN)) {
            //训练活动
            boolean b2 = list.stream().anyMatch(ss -> ss.getManagegroupId() == 2L);
            if (!b2) {
                ManageGroupVO mgo = new ManageGroupVO();
                ManageGroup mg = new ManageGroup();
                mg.setId(2);
                mg.setName("阅卷组");
                mg.setDescribe("负责阅卷");
                mgo.setMg(mg);
                mgo.setAppUserVOList(new ArrayList<>());
                ls.add(mgo);
            }
        }
        Map<Long, List<ExamManageGroupRel>> collect = list.stream().collect(Collectors.groupingBy(ExamManageGroupRel::getManagegroupId));
        for (Long mgId : collect.keySet()) {
            List<AppUser> ll = new ArrayList<>();
            ManageGroupVO mgo = new ManageGroupVO();
            ManageGroup mg = manageGroupService.getById(mgId);
            mgo.setMg(mg);
            List<ExamManageGroupRel> examManageGroupRels = collect.get(mgId);
            for (ExamManageGroupRel em : examManageGroupRels) {
                if (!Validator.isEmpty(em.getMemberId())) {
                    AppUser userById = sysDepartmentFeign.findAppUserById(em.getMemberId());
                    ll.add(userById);
                }
            }
            mgo.setAppUserVOList(ll);
            ls.add(mgo);
        }
        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(), "查询所有管理小组成功...", ls);
    }

    @ApiOperation(value = "增加小组", notes = "增加小组")
    @ApiImplicitParams({@ApiImplicitParam(name = "examId", value = "活动id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "mg", value = "封装小组实体类", required = true, dataType = "mg")})
    @RequestMapping(value = "/addManageGroup", method = RequestMethod.POST)
    @EditActivityAnnotation(parameterType = ExamConstants.PARAMETER_STR)
    public ApiResult addManageGroup(@RequestParam("examId") Long examId, @Valid @RequestBody ManageGroup mg) {
        manageGroupService.save(mg);
        examService.addManageGroupRel(mg, examId);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "保存管理小组成功", mg);
    }

    @ApiOperation(value = "查询具有监考或者判卷的人员", notes = "查询具有监考或者判卷的人员")
    @ApiImplicitParams({@ApiImplicitParam(name = "flag", value = "添加人员标识(0 监考组 1 判卷组 2其他)", required = true, dataType = "String"),
            @ApiImplicitParam(name = "name", value = "名字", dataType = "String")})
    @RequestMapping(value = "/getPermissionUser", method = RequestMethod.GET)
    public ApiResult getPermissionUser(String flag, String name) {
        AppUser appUser = new AppUser();
        List<AppUser> data = sysDepartmentFeign.getUsers(appUser);
        List<AppUser> list1 = new ArrayList<>();
        for (AppUser ap : data) {
            Set<SysPermission> permissions = sysDepartmentFeign.getPermissonByUserId(ap.getId());
            if ("0".equals(flag)) {
                if (permissions.stream().anyMatch(pp -> pp.getPermission().equals(ExamConstants.IS_JUDGE))) {
                    list1.add(ap);
                }
            } else if ("1".equals(flag)) {
                if (permissions.stream().anyMatch(pp -> pp.getPermission().equals(ExamConstants.IS_MONITOR))) {
                    list1.add(ap);
                }
            } else {
                list1 = data;
            }
        }
        if (Validator.isNotNull(name)) {
            list1 = list1.stream().filter(user -> user.getNickname().contains(name)).collect(Collectors.toList());
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询管理小组成员成功", list1);
    }

    @ApiOperation(value = "增加小组成员(id=1 监考组 id=2阅卷组)", notes = "增加小组成员")
    @ApiImplicitParams({@ApiImplicitParam(name = "examId", value = "活动id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "mgId", value = "小组id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "userId", value = "人员id", required = true, dataType = "String")})
    @RequestMapping(value = "/addManageGroupUsers", method = RequestMethod.GET)
    @EditActivityAnnotation(parameterType = ExamConstants.PARAMETER_STR)
    public ApiResult addManageGroupUsers(@RequestParam("examId") Long examId, @RequestParam("mgId") Long mgId, @RequestParam("userId") String userId) {
        String[] split = userId.split(",");
        examService.delManageGroupRel(examId, mgId);
        for (int i = 0; i < split.length; i++) {
            Long uid = Long.valueOf(split[i]);
            examService.delManageGroupMember(examId, mgId, uid);
            examService.updateManageGroupRel(mgId, examId, uid);
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "保存管理小组成员成功", null);
    }

    @ApiOperation(value = "删除小组", notes = "删除小组")
    @ApiImplicitParam(name = "map", value = "小组id,活动id", dataType = "Map")
    @RequestMapping(value = "/delManageGroup", method = RequestMethod.DELETE)
    @EditActivityAnnotation(parameterType = ExamConstants.PARAMETER_MAP)
    public ApiResult delManageGroup(@RequestBody Map<String, Long> map) {
        Long examId = map.get("examId");
        Long mgId = map.get("mgId");
        if (mgId == 1 || mgId == 2) {
            throw new IllegalArgumentException("该小组不能删除。。。");
        }
        Integer res = examService.delManageGroup(examId, mgId);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "删除管理小组成功", res);
    }

    @ApiOperation(value = "删除小组成员", notes = "删除小组成员")
    @ApiImplicitParam(name = "map", value = "成员id,活动id,小组id", dataType = "Map")
    @RequestMapping(value = "/delManageGroupMember", method = RequestMethod.DELETE)
    @EditActivityAnnotation(parameterType = ExamConstants.PARAMETER_MAP)
    public ApiResult delManageGroupMember(@RequestBody Map<String, Long> map) {
        Long examId = map.get("examId");
        Long mgId = map.get("mgId");
        Long userId = map.get("userId");
        List<ExamManageGroupRel> examManageGroupRelList = examService.getManageRel(examId, mgId);
        if (examManageGroupRelList.size() > 1) {
            examService.delManageGroupMember(examId, mgId, userId);
        } else {
            examService.updateManageGroupMember(examId, mgId, userId);
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "删除管理小组成员成功", null);
    }
    /*@ApiOperation(value = "设置小组组长", notes = "设置小组组长")
    @ApiImplicitParam(name = "map", value = "成员id,活动id,小组id", dataType = "Map")
    @RequestMapping(value = "/addManageGroupMaster", method = RequestMethod.POST)
    @EditActivityAnnotation(parameterType = ExamConstants.PARAMETER_MAP)
    public ApiResult addManageGroupMaster(@RequestBody ExamManageGroupRel examManageGroupRel) {
        UpdateWrapper<ExamManageGroupRel> queryWrapper = new UpdateWrapper<>();
        queryWrapper.eq("ac_id",examManageGroupRel.getAcId());
        queryWrapper.eq("managegroup_id",examManageGroupRel.getManagegroupId());
        List<ExamManageGroupRel> list = examManageGroupRelService.list(queryWrapper);

        list.stream().forEach(e->{
            e.setMaster(0);
            examManageGroupRelService.update(e,queryWrapper);
        });

        queryWrapper.eq("member_id",examManageGroupRel.getMemberId());
        examManageGroupRelService.update(examManageGroupRel,queryWrapper);

        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "删除管理小组成员成功", null);
    }
*/

    @ApiOperation(value = "查询活动考试场地", notes = "查询考试场地")
    @ApiImplicitParam(name = "examId", value = "活动id", required = true, dataType = "Long")
    @RequestMapping(value = "/searchExamPlaceByExamId", method = RequestMethod.GET)
    public ApiResult searchExamPlaceByExamId(Long examId) {
        List<ExamPlace> list = examService.searchExamPlaceByExamId(examId);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询所有场地成功", list);
    }

    @ApiOperation(value = "添加考试场地", notes = "添加考试场地")
    @ApiImplicitParams({@ApiImplicitParam(name = "examId", value = "活动id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "examPlace", value = "场地封装实体", required = true, dataType = "examPlace")})
    @RequestMapping(value = "/addExamPlace", method = RequestMethod.POST)
    @EditActivityAnnotation(parameterType = ExamConstants.PARAMETER_STR)
    public ApiResult addExamPlace(@RequestParam Long examId, @Valid @RequestBody ExamPlace examPlace) {


//        List<Exam> list = examService.getExamPlaceRelations(examPlace.getPlaceName());
//        if (list.size() > 0) {
//            for (Exam ee : list) {
//                if (isOverlapping(e.getStartTime(), e.getEndTime(), ee.getStartTime(), ee.getEndTime())) {
//                    return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "该场地已被占用", null);
//                }
//            }
//        }*/
        Exam e = examService.getById(examId);
        if (e.getType() != 1) {
            if (StringUtils.isEmpty(examPlace.getPlaceArea())) {
                return ApiResultHandler.buildApiResult(101, "考场区域不能为空", null);
            }

        }
        examPlaceService.save(examPlace);
        Integer res = examService.addExamPlace(examPlace.getId(), examId);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "保存场地成功", res);
    }

    @ApiOperation(value = "查询单位下考试区域", notes = "查询单位下考试区域")
    @RequestMapping(value = "/getExamPlacearea", method = RequestMethod.GET)
    public ApiResult getExamPlacearea() {
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        QueryWrapper qw = new QueryWrapper();
        qw.eq("depart_id", loginAppUser.getDepartmentId());
        List<ExamPlacearea> list = examPlaceareaService.list(qw);
        Collections.sort(list, new Comparator<ExamPlacearea>() {
            @Override
            public int compare(ExamPlacearea o1, ExamPlacearea o2) {
                return o2.getId().intValue() - o1.getId().intValue();
            }
        });
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询单位下考试区域成功", list);
    }

    @ApiOperation(value = "添加考试区域", notes = "添加考试区域")
    @ApiImplicitParam(name = "examPlacearea", value = "考试区域实体", required = true, dataType = "examPlacearea")
    @RequestMapping(value = "/addExamPlacearea", method = RequestMethod.POST)
    public ApiResult addExamPlacearea(@RequestBody ExamPlacearea examPlacearea) {
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        examPlacearea.setDepartId(loginAppUser.getDepartmentId());
        examPlaceareaService.save(examPlacearea);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "添加考试区域", null);
    }

    @ApiOperation(value = "删除考试场地", notes = "删除考试场地")
    @ApiImplicitParam(name = "map", value = "场地id,活动id", dataType = "Map")
    @RequestMapping(value = "/delExamPlace", method = RequestMethod.DELETE)
    @EditActivityAnnotation(parameterType = ExamConstants.PARAMETER_MAP)
    public ApiResult delExamPlace(@RequestBody Map<String, Long> map) {
        Long examId = map.get("examId");
        Long epId = map.get("epId");
        Integer res = examService.delExamPlace(epId, examId);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "删除场地成功", res);
    }

    @ApiOperation(value = "启动活动，生成抽签结果", notes = "启动活动，生成抽签结果")
    @RequestMapping(value = "/getDrawResult", method = RequestMethod.POST)
    public synchronized ApiResult getDrawResult(@RequestBody Map<String, Long> map) {
        Long examId = map.get("examId");
        Exam exam = examService.getById(examId);
        if (!exam.getExamStatus().equals(ExamConstants.ACTIVITY_NOT_LAUNCH) && !exam.getExamStatus().equals(ExamConstants.ACTIVITY_CONCELL)) {
            return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_LAUNCH_YES.getResultCode(), ResultMesCode.EXAM_LAUNCH_YES.getResultMsg(), null);
        }
        if (exam.getParentId() == 0) {
            QueryWrapper<Exam> wp = new QueryWrapper();
            wp.eq("parent_id", examId);
            List<Exam> exams = examService.list(wp);
            boolean b = exams.stream().anyMatch(es -> es.getExamStatus().equals(ExamConstants.ACTIVITY_EXAM_START));
            if (b) {
                return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_CHILD_NOT_FINISH.getResultCode(), ResultMesCode.EXAM_CHILD_NOT_FINISH.getResultMsg(), null);
            }
        }
        Date endTime = exam.getEndTime();
        if (endTime.getTime() < System.currentTimeMillis()) {
            return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_LAUNCH_PASS_TIME.getResultCode(), ResultMesCode.EXAM_LAUNCH_PASS_TIME.getResultMsg(), null);
        }

        //竞答活动忽略管理小组关联判断,训练活动只有阅卷判断
        if (!exam.getType().equals(ExamConstants.EXAM_TYPE_JINGDA)) {
            List<ExamManageGroupRel> allExamManageRelByExamId = examService.getAllExamManageRelByExamId(examId);
            boolean b3 = allExamManageRelByExamId.stream().anyMatch(sys -> Validator.isEmpty(sys.getMemberId()));
            if (Validator.isEmpty(allExamManageRelByExamId) || b3) {
                return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_NOT_MANAGER_USER_REL.getResultCode(), ResultMesCode.EXAM_NOT_MANAGER_USER_REL.getResultMsg(), null);
            }
            Map<Long, List<ExamManageGroupRel>> collect = allExamManageRelByExamId.stream().collect(Collectors.groupingBy(ExamManageGroupRel::getManagegroupId));
            Set<Long> longs = collect.keySet();
            List<Long> list = new ArrayList();
            if (exam.getType().equals(ExamConstants.EXAM_TYPE_LILUN)) {
                list.add(1L);
            }
            list.add(2L);
            if (!longs.containsAll(list)) {
                return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_NOT_MANAGER_REL.getResultCode(), ResultMesCode.EXAM_NOT_MANAGER_REL.getResultMsg(), null);
            }
        }

        List<ExamDepartUserRel> list2 = examService.getDepartAndUserByExamId(examId);
        boolean b2 = list2.stream().anyMatch(ss -> Validator.isEmpty(ss.getMemberId()));
        if (Validator.isEmpty(list2) || b2) {
            return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_NOT_MEMBER_REL.getResultCode(), ResultMesCode.EXAM_NOT_MEMBER_REL.getResultMsg(), null);
        }


        Map<Long, List<Long>> m1 = new HashMap<>();
        if (0 == exam.getType() || 2 == exam.getType()) {
            List<ExamDepartPaperRel> list1 = examService.getDepartAndPaperByExamId(examId);
            boolean b1 = list1.stream().anyMatch(ss -> Validator.isEmpty(ss.getPaperId()));
            if (b1 || Validator.isEmpty(list1)) {
                return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_NOT_PAPER_REL.getResultCode(), ResultMesCode.EXAM_NOT_PAPER_REL.getResultMsg(), null);
            }
            Map<Long, List<ExamDepartPaperRel>> collect1 = list1.stream().collect(Collectors.groupingBy(ExamDepartPaperRel::getDepartId));
            for (Long departId : collect1.keySet()) {
                List<Long> list = new ArrayList<>();
                collect1.get(departId).stream().forEach(ss -> list.add(ss.getPaperId()));
                m1.put(departId, list);
            }
        } else if (1 == exam.getType()) {
            QueryWrapper<CompetitionExamPaperRel> qw = new QueryWrapper<>();
            qw.eq("ac_id", examId);
            List<CompetitionExamPaperRel> list = competitionExamPaperRelService.list(qw);
            boolean b1 = list.stream().anyMatch(ss -> Validator.isEmpty(ss.getPaperId()));
            if (b1 || Validator.isEmpty(list)) {
                return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_NOT_PAPER_REL.getResultCode(), ResultMesCode.EXAM_NOT_PAPER_REL.getResultMsg(), null);
            }
        }
        Map<Long, List<Long>> m2 = new HashMap<>();
        Map<Long, List<ExamDepartUserRel>> collect2 = list2.stream().collect(Collectors.groupingBy(ExamDepartUserRel::getDepartId));
        int userCount = 0;
        for (Long departId : collect2.keySet()) {
            List<Long> list = new ArrayList<>();
            collect2.get(departId).stream().forEach(ss -> list.add(ss.getMemberId()));
            if (exam.getType() == 1 && exam.getUnit() == 0) {
                userCount++;
            } else {
                userCount += list.size();
            }

            m2.put(departId, list);
        }
        List<String> placesIds = new ArrayList<>();
        if (exam.getType() == 1 || exam.getType() == 0) {
            List<ExamPlaceRel> list3 = examService.getExamPlaceRelByExamId(examId);
            if (Validator.isEmpty(list3)) {
                return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_NOT_PLACE_REL.getResultCode(), ResultMesCode.EXAM_NOT_PLACE_REL.getResultMsg(), null);
            }
            int totalPlaceNum = 0;
            for (ExamPlaceRel pr : list3) {
                totalPlaceNum += pr.getSeatCount();
                for (int i = 1; i < pr.getSeatCount() + 1; i++) {
                    placesIds.add(pr.getPlaceId() + "&" + i);
                }
            }
            if (userCount > placesIds.size()) {
                return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_PLACENUM_LESS.getResultCode(), ResultMesCode.EXAM_PLACENUM_LESS.getResultMsg(), userCount);
            }
        }

        List<Paper> papers = examService.getAllPaper(examId);
        List<ExamPlace> places = examService.getAllExamPlace(examId);

        List<DrawResultVO> drawResultVOS = new ArrayList<>();
        DrawResultContext drawResultContext = null;
        if (exam.getType() == 1) {
            drawResultContext = new DrawResultContext(new CompetitionlExamDraw());
        } else if (exam.getType() == 0) {
            drawResultContext = new DrawResultContext(new NomalExamDraw());
        } else {
            drawResultContext = new DrawResultContext(new TrainExamDraw());
        }
        drawResultVOS = drawResultContext.executeStrategy(examId, m1, m2, placesIds, papers, places);

        exam.setExamStatus(ExamConstants.ACTIVITY_LAUNCH);
        if (exam.getStartTime().getTime() <= System.currentTimeMillis()) {
            exam.setExamStatus(ExamConstants.ACTIVITY_START);
        }
        examService.saveOrUpdate(exam);
        //将抽签对应的试题缓存到redis
        if (exam.getType().equals(ExamConstants.EXAM_TYPE_LILUN) || exam.getType().equals(ExamConstants.EXAM_TYPE_XUNLIAN)) {
            executor.execute(new SaveDrawResultQuestions(examId));
        }
        //将抽签结果缓存到redis,用于下载抽签结果
        redisUtils.lSet("exam:" + examId + ":drawResult", drawResultVOS, ExamConstants.EXAM_DRAWRESULT_DETAILS_TIME);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "成功启动活动", drawResultVOS);
    }

    @RequestMapping("/getDrawResultDetails")
    public ApiResult getDrawResultDetails(Long examId) {
        List<Object> objects = redisUtils.lGet("exam:" + examId + ":drawResult", 0, -1);
        if (CollectionUtils.isNotEmpty(objects)) {
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "SUCCESS", (List<DrawResultVO>) objects.get(0));
        }
        logger.error(" 考试:{}  redis缓存中，相关数据为空，排查原因 ", examId);
        return ApiResultHandler.buildApiResult(ApiResultHandler.error_data(examId).getCode(), "数据异常！！",
                Collections.emptyList());
    }

    @ApiOperation(value = "更改当前学员考试状态（ 0未登录 1 已登录 2 考试开始 3 考试结束 5 考试取消）", notes = "更改当前学员考试状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "examId", value = "活动id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "status", value = "更改状态", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "userId", value = "学员id", required = true, dataType = "Long")})
    @RequestMapping(value = "/updateDrawResult", method = RequestMethod.GET)
    public ApiResult updateDrawResult(Long examId, Integer status, Long userId) {
        DrawResult d = new DrawResult();
        d.setAcId(examId);
        d.setUserStatus(status);
        //httpSession.setAttribute(userId + "", status);
        examService.updateDrawResult(d);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "更改当前学员考试状态成功。。。", null);
    }


    @ApiOperation(value = "点击阅卷进入阅卷页面", notes = "点击阅卷")
    @ApiImplicitParam(name = "identityId", value = "准考证号", dataType = "String")
    @RequestMapping(value = "/getStudentAnswerDetails", method = RequestMethod.GET)
    public ApiResult getStudentAnswerDetails(@RequestParam String identityId) {
        QueryWrapper<DrawResult> q = new QueryWrapper<>();
        q.eq("identity_card", identityId);
        DrawResult one = drawResultService.getOne(q);
        Long paperId = one.getPaperId();
        Paper byId1 = paperService.getById(paperId);
        if (ObjectUtils.isNotNull(byId1) && byId1.getType() == 1) {
            QueryWrapper<StudentAnswer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("student_id", one.getUserId());
            queryWrapper.eq("paper_id", one.getPaperId());
            List<StudentAnswer> list = studentAnswerService.list(queryWrapper);
            List<StudentAnswerVO> ll = new ArrayList<>();
            for (StudentAnswer stu : list) {
                StudentAnswerVO vo = new StudentAnswerVO();
                BeanUtils.copyProperties(stu, vo);
                Question byId = questionService.getById(vo.getQuestionId());
                List<HashMap<String, Object>> hashMapList = QuestionUtils.getKpDetailsByQuestion(byId);
                vo.setKpDetails(hashMapList);
                vo.setQuestion(byId.getQuestion());
                JSONObject js = new JSONObject();
                js.put("text", "");
                js.put("url", "");
                QueryWrapper<PaperManageRel> qw = new QueryWrapper<>();
                qw.eq("paper_id", one.getPaperId());
                qw.eq("question_id", vo.getQuestionId());
                PaperManageRel paperManageRel = paperManageRelService.getOne(qw);
                vo.setKeywords(byId.getKeywords() == null ? js.toJSONString() : byId.getKeywords());
                vo.setAnalysis(byId.getAnalysis() == null ? js.toJSONString() : byId.getAnalysis());
                vo.setAnswer(byId.getAnswer() == null ? js.toJSONString() : byId.getAnswer());
                vo.setQuestionScore(paperManageRel.getScore());
                vo.setStuAnswers(stu.getStuAnswer());
                vo.setCostTime(one.getCostTime());
                vo.setLocalUrlPrefix(localUrlPrefix);
                // 增加是否收藏逻辑
                QueryWrapper<CollectionQuestion> collectionQuestionQueryWrapper = new QueryWrapper<>();
                collectionQuestionQueryWrapper.eq("user_id", vo.getStudentId());
                collectionQuestionQueryWrapper.eq("question_id", vo.getQuestionId());
                CollectionQuestion collectionQuestion = collectionQuestionService.getOne(collectionQuestionQueryWrapper);
                long collectionId = collectionQuestion == null ? 0l : collectionQuestion.getId();
                vo.setCollectionId(collectionId);
                vo.setFileAddr(fileServer);
                vo.setLocalUrlPrefix(localUrlPrefix);
                ll.add(vo);
            }
            return ApiResultHandler.buildApiResult(200, "获取成功", ll);
        }
        //List<Object> objects = redisUtils.lGet("exam:" + one.getAcId()+":"+identityId, 0, -1);
        List<Object> objects = null;
        List<StudentAnswerVO> ll = new ArrayList<>();
        if (one.getExamType() == 2 || one.getExamType() == 3 || objects == null || objects.size() < 1) {
            QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("identity_card", identityId);
            DrawResult drawResult = drawResultService.getOne(queryWrapper);
            QueryWrapper<StudentAnswer> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("student_id", drawResult.getUserId());
            queryWrapper1.eq("paper_id", drawResult.getPaperId());
            queryWrapper1.eq("ac_id", drawResult.getAcId());
            queryWrapper1.orderByAsc("type");
            List<StudentAnswer> list = studentAnswerService.list(queryWrapper1);

            for (StudentAnswer studentAnswer : list) {
                StudentAnswerVO vo = new StudentAnswerVO();
                BeanUtils.copyProperties(studentAnswer, vo);
                Question byId = questionService.getById(vo.getQuestionId());
                vo.setQuestion(byId.getQuestion());
                vo.setOptions(byId.getOptions());
                JSONObject js = new JSONObject();
                js.put("text", "");
                js.put("url", "");
                QueryWrapper<PaperManageRel> qw = new QueryWrapper<>();
                qw.eq("paper_id", drawResult.getPaperId());
                qw.eq("question_id", vo.getQuestionId());
                PaperManageRel paperManageRel = paperManageRelService.getOne(qw);
                vo.setKeywords(byId.getKeywords() == null ? js.toJSONString() : byId.getKeywords());
                vo.setAnalysis(byId.getAnalysis() == null ? js.toJSONString() : byId.getAnalysis());
                vo.setAnswer(byId.getAnswer() == null ? js.toJSONString() : byId.getAnswer());

                if (byId.getType() == 4) {
                    String answer = byId.getAnswer();
                    JSONObject jsonObject = JSONObject.parseObject(answer);
                    Set<Map.Entry<String, Object>> entries = jsonObject.entrySet();
                    List ls = new ArrayList();
                    for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                        ls.add(entry.getValue());
                    }
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("text", ls);
                    vo.setAnswer(jsonObject1.toJSONString());
                }

                vo.setQuestionScore(paperManageRel.getScore());
                vo.setStuAnswers(studentAnswer.getStuAnswer());
                vo.setCostTime(one.getCostTime());
                vo.setLocalUrlPrefix(localUrlPrefix);
                if (studentAnswer.getActualScore() != null && studentAnswer.getActualScore() > 0) {
                    vo.setFlag(true);
                }

                // 增加是否收藏逻辑
                QueryWrapper<CollectionQuestion> collectionQuestionQueryWrapper = new QueryWrapper<>();
                collectionQuestionQueryWrapper.eq("user_id", vo.getStudentId());
                collectionQuestionQueryWrapper.eq("question_id", vo.getQuestionId());
                CollectionQuestion collectionQuestion = collectionQuestionService.getOne(collectionQuestionQueryWrapper);
                long collectionId = collectionQuestion == null ? 0l : collectionQuestion.getId();
                vo.setCollectionId(collectionId);
                vo.setFileAddr(fileServer);
                vo.setLocalUrlPrefix(localUrlPrefix);
                ll.add(vo);
            }
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取考生答案成功。。。", ll);
//        List<StudentAnswerVO> list = (List<StudentAnswerVO>) objects.get(0);
//        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取考生答案成功。。。", list);
    }

    @ApiOperation(value = "提交上传非理论考试答案文件", notes = "上传非理论考试答案文件")
    @ApiImplicitParam(name = "sa", value = "准考证号", dataType = "studentAnswer")
    @RequestMapping(value = "/uploadStudentAnswerFile", method = RequestMethod.POST)
    public ApiResult uploadStudentAnswerFile(@RequestParam Long questionId, @RequestParam String identityId, @RequestParam MultipartFile file) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("identity_card", identityId);
        DrawResult one = drawResultService.getOne(qw);
        String path = fileFastdfsUtils.uploadAnalyseFile(file);
        StudentAnswer sa = new StudentAnswer();
        sa.setPaperId(one.getPaperId());
        sa.setStudentId(one.getUserId());
        sa.setQuestionId(questionId);
        sa.setStuAnswer(path);
        List list = new LinkedList();
        list.add(path);
        JSONObject jso = new JSONObject();
        jso.put("text", "");
        jso.put("url", list.toArray());
        sa.setStuAnswer(jso.toJSONString());
        sa.setAcId(one.getAcId());
        Question byId = questionService.getById(questionId);
        sa.setPdType(byId.getPdType());
        studentAnswerService.saveOrUpdate(sa);
        one.setUserStatus(ExamConstants.ACTIVITY_WAIT_JUDGE);
        drawResultService.saveOrUpdate(one);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "保存结果成功", null);
    }

    @ApiOperation(value = "下载非理论考试答案文件", notes = "下载非理论考试答案文件")
    @ApiImplicitParam(name = "sa", value = "准考证号", dataType = "studentAnswer")
    @RequestMapping(value = "/uploadStudentAnswer", method = RequestMethod.POST)
    public ApiResult dowloadStudentAnswerFile(@RequestBody StudentAnswer sa, MultipartFile file) {
        String path = fileFastdfsUtils.uploadAnalyseFile(file);
        sa.setStuAnswer(path);
        studentAnswerService.saveOrUpdate(sa);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "保存结果成功", null);
    }

    @ApiOperation(value = "点击监考，获取活动监考页面信息", notes = "获取活动监考页面信息")
    @ApiImplicitParam(name = "examId", value = "活动id", required = true, dataType = "Long")
    @RequestMapping(value = "/minitorResult", method = RequestMethod.GET)
    public ApiResult getMinitorResult(Long examId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("ac_id", examId);
        Exam exam = examService.getById(examId);
        List<DrawResultVO> ls = new ArrayList<>();
        List<DrawResult> ll = drawResultService.list(qw);
        for (DrawResult dr : ll) {
            DrawResultVO vo = new DrawResultVO();
            BeanUtils.copyProperties(dr, vo);
            AppUser appUserById = sysDepartmentFeign.findAppUserById(dr.getUserId());
            vo.setUserName(appUserById.getNickname());
            vo.setDepartName(appUserById.getDepartmentName());
            vo.setPositionName(appUserById.getPositionName());
            ExamPlace byId = examPlaceService.getById(dr.getPlaceId());
            vo.setPlaceName(byId == null ? "" : byId.getPlaceName());
            vo.setExamName(exam.getName());
            vo.setExamDate(DateConvertUtils.date2Str(exam.getStartTime()));
            Integer totalTime = paperService.getById(dr.getPaperId()).getTotalTime();
            vo.setExamTime(String.valueOf(totalTime));
            vo.setExamStatus(exam.getExamStatus());
            ls.add(vo);
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "进入监考页面", ls);
    }

    @RequestMapping(value = "/getMessageByUserId", method = RequestMethod.GET)
    @ApiOperation(value = "获取当前用户考试信息", notes = "获取当前用户考试信息")
    public ApiResult getMessageByUserId() {
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        QueryWrapper qw = new QueryWrapper();
        qw.eq("user_id", loginAppUser.getId());
        List<DrawResult> list = drawResultService.list(qw);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "", list);
    }

    @ApiOperation(value = "获取判卷所有考试列表", notes = "获取判卷所有考试列表")
    @ApiImplicitParam(name = "examId", value = "活动id", required = true, dataType = "Long")
    @RequestMapping(value = "/getListPapers", method = RequestMethod.GET)
    public ApiResult getListPapers(String paperName, Integer paperType, Integer examStatus, Long examId) {
        QueryWrapper<DrawResult> entityWrapper = new QueryWrapper<>();
        entityWrapper.eq("ac_id", examId);
        List<Integer> as = new ArrayList<Integer>();
        as.add(ExamConstants.EXAM_WAIT_JUDGE);
        as.add(ExamConstants.EXAM_FINISH);
        if (!Validator.isEmpty(paperType)) {
            entityWrapper.eq("paper_type", paperType);
        }
        if (!Validator.isEmpty(examStatus)) {
            as.clear();
            as.add(examStatus);
            //entityWrapper.eq("user_status", examStatus);
        }
        entityWrapper.in("user_status", as);
        List<DrawResult> list = drawResultService.list(entityWrapper);
        List<DrawResultVO> ll = new ArrayList<>();
        List<Paper> paperList = paperService.list();
        for (DrawResult dr : list) {
            DrawResultVO vo = new DrawResultVO();
            BeanUtils.copyProperties(dr, vo);
            Optional<Paper> first = paperList.stream().filter(paper -> String.valueOf(paper.getId()).equals(String.valueOf(dr.getPaperId()))).findFirst();
            if (first.isPresent()) {
                vo.setPaperName(first.get().getPaperName());
                ll.add(vo);
            }
        }
        if (!Validator.isEmpty(paperName)) {
            ll = ll.stream().filter(v -> v.getPaperName().contains(paperName)).collect(Collectors.toList());

        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取考试列表信息成功...", ll);
    }

    @ApiOperation(value = "理论判卷", notes = "理论判卷")
    @RequestMapping(value = "/judgePage", method = RequestMethod.POST)
    public synchronized ApiResult judgePage(@RequestParam String identityId, @RequestBody List<StudentAnswerVO> list) {

        Double totalScore = 0d;
        QueryWrapper wq = new QueryWrapper();
        wq.eq("identity_card", identityId);
        DrawResult dr = drawResultService.getOne(wq);
        //DrawResult dr = drawResultService.getById(drawResultId);
        Integer paperType = dr.getPaperType();
        if (!Validator.isEmpty(dr.getJudgePerson())) {
            throw new IllegalArgumentException("判卷已完成。");
        }
        try {
            if (ExamConstants.PAPER_QINGXI.equals(paperType)) {
                judgeAnalysisPaper(identityId, list);
            }
            for (StudentAnswerVO st : list) {
                Question one = null;
                StudentAnswer byId = studentAnswerService.getById(st.getId());
                if (ExamConstants.PAPER_LILUN.equals(paperType)) {
                    QueryWrapper<Question> qw = new QueryWrapper<>();
                    qw.eq("id", st.getQuestionId());
                    one = questionService.getOne(qw);
                    Integer type = one.getType();
                    if (type == 1 || type == 3 || type == 2 || type == 4) {

//                        if (!Validator.isNull(st.getJudgeRemark())) {
//                            //if(byId.getStuAnswer().equals(one.getAnswer())){
//                            if (st.getFlag()) {
//                                byId.setActualScore(0d);
//                            } else {
//                                byId.setActualScore(st.getQuestionScore());
//                                //totalScore += st.getQuestionScore();
//                            }
//
//                        } else {
//                            if (st.getFlag()) {
//                                byId.setActualScore(st.getActualScore());
//                                //totalScore += st.getQuestionScore();
//                            } else {
//                                byId.setActualScore(0d);
//                            }
//                        }
                        byId.setActualScore(st.getActualScore());
                    } else {
                        //totalScore += st.getActualScore();
                        byId.setActualScore(st.getActualScore());
                    }
                    totalScore += st.getActualScore();
                } else {
                    if (!Validator.isEmpty(st.getActualScore())) {
                        byId.setActualScore(st.getActualScore());
                        totalScore += st.getActualScore();
                    }
                }
                byId.setJudgeRemark(st.getJudgeRemark());
                byId.setPaperId(dr.getPaperId());
                byId.setUpdateTime(new Date());
                byId.setStudentId(dr.getUserId());
                byId.setPdType(one.getPdType());
                studentAnswerService.updateById(byId);
            }
            dr.setScore(totalScore);
            dr.setJudgePerson(AppUserUtil.getLoginAppUser().getId());
            dr.setUserStatus(ExamConstants.EXAM_FINISH);
            drawResultService.saveOrUpdate(dr);
            QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ac_id", dr.getAcId());
            List<DrawResult> list1 = drawResultService.list(queryWrapper);
            if (list1.stream().allMatch(e -> e.getUserStatus().equals(ExamConstants.EXAM_FINISH))) {
                Exam exam = examService.getById(dr.getAcId());
                exam.setExamStatus(ExamConstants.EXAM_FINISH);
                examService.saveOrUpdate(exam);
                examKpPersonAvgScoreService.calculate(exam.getId());
            }

            // 错误试题收录
            Map<String, Object> map = new HashMap<>();
            map.put("studentId", dr.getUserId());
            map.put("paperId", dr.getPaperId());
            List<Map<String, Object>> errorList = errorQuestionDao.errorList(map);
            if (errorList != null && errorList.size() > 0) {
                Map<String, Object> parMap = new HashMap<>();
                for (Map<String, Object> m : errorList) {
                    // 根据用户id和试题id查询是否已记录
                    parMap.put("userId", dr.getUserId());
                    parMap.put("questionId", Long.valueOf(m.get("question_id").toString()));
                    List<Map<String, Object>> byPar = errorQuestionDao.findByPar(parMap);
                    if (byPar == null || byPar.size() == 0) {
                        parMap.put("question", m.get("question"));
                        parMap.put("type", CommonPar.question_type.get(m.get("type").toString()));
                        parMap.put("createTime", new Timestamp(System.currentTimeMillis()));
                        errorQuestionDao.saveInfo(parMap);
                    } else {
                        parMap.put("id", Long.valueOf(byPar.get(0).get("id").toString()));
                        parMap.put("createTime", new Timestamp(System.currentTimeMillis()));
                        errorQuestionDao.updateInfo(parMap);
                    }
                }
            }
            // 计算知识点能力
            new Thread(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    evalNewService.makeEvaluation(dr.getAcId());
                }
            }).start();
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "判卷完成。。。", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 情析和整编判卷
     */
    public ApiResult judgeAnalysisPaper(String identityId, List<StudentAnswerVO> list) {
        QueryWrapper<DrawResult> q = new QueryWrapper<>();
        q.eq("identity_card", identityId);
        DrawResult dr = drawResultService.getOne(q);
        Double totalScore = 0d;
        for (StudentAnswerVO vo : list) {
            StudentAnswer byId = studentAnswerService.getById(vo.getId());
            totalScore += vo.getActualScore();
            byId.setJudgeRemark(vo.getJudgeRemark());
            byId.setPaperId(dr.getPaperId());
            byId.setUpdateTime(new Date());
            byId.setStudentId(dr.getUserId());
            //byId.setKpDetails(vo.getKpDetails());
            byId.setKpScores(JSON.toJSONString(vo.getKpDetails()));

            studentAnswerService.saveOrUpdate(byId);
        }
        dr.setScore(totalScore);
        dr.setJudgePerson(AppUserUtil.getLoginAppUser().getId());
        dr.setUserStatus(ExamConstants.EXAM_FINISH);
        drawResultService.saveOrUpdate(dr);
        return ApiResultHandler.buildApiResult(200, "情析判卷成功", null);
    }

    @ApiOperation(value = "导出抽签结果", notes = "导出抽签结果")
    @ApiImplicitParam(name = "examId", value = "活动id", dataType = "Long", required = true)
    @RequestMapping(value = "/exportDrawResultByExamId", method = RequestMethod.GET)
    public void exportDrawResultByExamId(Long examId, HttpServletResponse rs) {
        Exam exam = examService.getById(examId);
        List<Object> objects = redisUtils.lGet("exam:" + examId + ":drawResult", 0, -1);
        List<DrawResultVO> list = (List<DrawResultVO>) objects.get(0);
        List<Map<String, Object>> set = new ArrayList<>();
        Map map = MapUtil.newHashMap(true);
        if (exam.getType() == 0) {
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> drawResultMap = new LinkedHashMap<>();
                Object o = list.get(i);
                DrawResultVO vo = (DrawResultVO) o;
                drawResultMap.put("单位名称", vo.getDepartName());
                drawResultMap.put("学员名称", vo.getUserName());
                if (vo.getPaperType() == 0) {
                    drawResultMap.put("考试类型", "理论考试");
                } else if (vo.getPaperType() == 1) {
                    drawResultMap.put("考试类型", "情析考试");
                } else if (vo.getPaperType() == 2) {
                    drawResultMap.put("考试类型", "实操考试");
                } else {
                    drawResultMap.put("考试类型", "");
                }
                drawResultMap.put("考试场地", vo.getPlaceName());
                drawResultMap.put("座位号", vo.getPlaceNum());
                drawResultMap.put("准考证号", vo.getIdentityCard());
                drawResultMap.put("考试时间", vo.getExamDate());
                set.add(drawResultMap);
            }
            String[] columns = {"单位", "姓名", "类型", "考场", "座位", "准考证号", "时间"};
            String[] fields = {"departName", "userName", "examType", "placeName", "placeNum", "identityCard", "examDate"};
            for (int i = 0; i < fields.length; i++) {
                map.put(fields[i], columns[i]);
            }
        } else if (exam.getType() == 1) {
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> drawResultMap = new LinkedHashMap<>();
                Object o = list.get(i);
                DrawResultVO vo = (DrawResultVO) o;
                drawResultMap.put("单位名称", vo.getDepartName());
                drawResultMap.put("参赛人数", vo.getUserCount());
                drawResultMap.put("学员名称", vo.getUserName());
                drawResultMap.put("座位号", vo.getPlaceNum());
                drawResultMap.put("准考证号", vo.getIdentityCard());
                drawResultMap.put("考试时间", vo.getExamDate());
                set.add(drawResultMap);
            }
            String[] columns = {"单位", "参赛人数", "姓名", "座位", "准考证号", "时间"};
            String[] fields = {"departName", "userCount", "userName", "placeNum", "identityCard", "examDate"};
            for (int i = 0; i < fields.length; i++) {
                map.put(fields[i], columns[i]);
            }
        }
        String name = exam.getName() + "抽签信息";
        try {
            //ExcelUtil.exportExcel(map,set,rs);
            ExcelFileUtils.exportFile(set, map, name, rs);
        } catch (Exception e) {
            logger.error("导出文件失败...", e.getMessage());
        }
    }

    class SaveDrawResultQuestions implements Runnable {
        private Long examId;

        public SaveDrawResultQuestions(Long examId) {
            this.examId = examId;
        }

        @Override
        public void run() {
            List<ExamDepartPaperRel> list1 = examService.getDepartAndPaperByExamId(examId);
            Map<Long, List<ExamDepartPaperRel>> collect1 = list1.stream().collect(Collectors.groupingBy(ExamDepartPaperRel::getPaperId));
            Set<Long> integers = collect1.keySet();
            for (Long paperId : integers) {
                List<Question> list = examService.getAllQuestionsBypaperId(paperId);
                List<StudentAnswerVO> ll = new ArrayList<>();
                for (Question q : list) {
                    StudentAnswerVO vo = new StudentAnswerVO();
                    vo.setQuestionId(q.getId());
                    vo.setQuestion(q.getQuestion());
                    vo.setOptions(q.getOptions());
                    vo.setType(q.getType());
                    vo.setPaperId(paperId);
                    vo.setModelUrl(q.getModelUrl());
                    vo.setLocalUrlPrefix(localUrlPrefix);
                    vo.setFileAddr(fileServer);
                    JSONObject jso = new JSONObject();
                    jso.put("text", "");
                    if (q.getType() == 4) {
                        String options = q.getOptions();
                        JSONObject jsonObject = JSONObject.parseObject(options);
                        int size = jsonObject.keySet().size();
                        String[] s = new String[size];
                        for (int i = 0; i < s.length; i++) {
                            s[i] = "";
                        }
                        jso.put("text", s);
                    }
                    vo.setStuAnswer(jso.toJSONString());
                    Double questionScoreById = questionService.getQuestionScoreById(paperId, q.getId());
                    vo.setQuestionScore(questionScoreById);
                    ll.add(vo);
                }
                redisUtils.lSet("exam:" + examId + ":" + paperId, ll);
            }
        }
    }

    @ApiOperation(value = "导出模版excel文件")
    @RequestMapping("/exportModelExcel")
    public void exportModelExcel(HttpServletResponse rs) {
        List<SysDepartment> all = sysDepartmentFeign.findAll();
        List<String> ll = new ArrayList<>();
        all.stream().forEach(e -> ll.add(e.getDname()));
        List<Map<String, String>> set = new ArrayList<>();
        HashMap<String, String> hashMap = new LinkedHashMap<>();
        hashMap.put("部门", "");
        hashMap.put("姓名", "");
        hashMap.put("性别", "");
        hashMap.put("身份证号", "");
        hashMap.put("手机号", "");
        set.add(hashMap);
        try {
            ExcelFileUtils.exportModelExcel(rs, "人员信息模版", set, ll);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ApiOperation(value = "导入参赛人员")
    @RequestMapping("/importAppUserExcel")
    public void importAppUserExcel(MultipartFile file) throws IOException {
        ExcelReader reader = ExcelUtil.getReader(file.getInputStream(), 0);
        List<List<Object>> read = reader.read(1, reader.getRowCount());

        List<SysDepartment> all = sysDepartmentFeign.findAll();
        String[] columNames = new String[]{"departmentName", "nickname", "sex", "rankNum", "phone"};
        try {
            //List<Map<String, Object>> leading = ExcelFileUtils.leading(file, columNames);
            for (List<Object> list : read) {
                AppUser appUser = new AppUser();
                Object departmentName = list.get(0);
                SysDepartment sysDepartment = all.stream().filter(e -> e.getDname().equals(departmentName)).findFirst().get();
                appUser.setRankNum(list.get(3) == null ? "" : list.get(3).toString());
                appUser.setNickname(list.get(1).toString());
                appUser.setPassword(ExamConstants.password);
                appUser.setUsername(PinyinUtils.getAllPinyin(list.get(1).toString()));
                appUser.setPhone(Validator.isEmpty(list.get(4)) ? "" : list.get(4).toString());
                appUser.setDepartmentId(sysDepartment.getId());
                appUser.setSex(list.get(2).equals("男") ? 1 : 0);
                appUser.setCreateTime(new Date());
                appUser.setUpdateTime(new Date());
//                appUser.setEnabled(1);
                appUser.setStatus(1);
                appUser.setType("BACKEND");
                sysDepartmentFeign.register(appUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "getKpListByExamAndType", method = RequestMethod.GET)
    public Set<String> getKpListByExamAndType(Long examId, Long departId, Integer type) {
        try {
            List<Set<String>> sets = new ArrayList<>();
            if (type == 1) {
                List<ExamDepartUserRel> departAndUserByExamId = examService.getExamDepartRel(examId, departId);
                for (ExamDepartUserRel rel : departAndUserByExamId) {
                    if (ObjectUtil.isNotNull(rel.getMemberId())) {
                        Set<String> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(rel.getMemberId());
                        sets.add(kpIdsbyUserId);
                    }
                }
            } else if (type == 2) {
                List<ExamDepartUserRel> departAndUserByExamId = examService.getDepartAndUserByExamId(examId);
                departAndUserByExamId.stream().forEach(e -> {
                    if (ObjectUtil.isNotNull(e.getMemberId())) {
                        Set<String> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(e.getMemberId());
                        sets.add(kpIdsbyUserId);
                    }
                });
            } else if (type == 3) {
                Set<String> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(AppUserUtil.getLoginAppUser().getId());
                return kpIdsbyUserId;
            }
            //学员具有的知识点交集
            return ListUtils.getSameElementBylists(sets);
        } catch (Exception e) {
            logger.error("ExamController-->getKpListByExamAndType出错：{}", e);
            return new HashSet<>();
        }
    }

    @RequestMapping(value = "testM", method = RequestMethod.GET)
    public String testM() {
        try {
            LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
            return loginAppUser.getArmServices();
        } catch (Exception e) {
            logger.error("测试打印日志：{}", e);
        }
        return "";
    }


    /**
     * @author:胡立涛
     * @description: TODO 录屏 调用容器接口传递参数
     * @date: 2022/10/26
     * @param: []
     * @return: com.cloud.core.ApiResult
     */
    @GetMapping(value = "getUploadPath")
    public ApiResult getUploadPath() {
        try {
            Map<String, Object> rMap = new HashMap<>();
            rMap.put("uploadPath", uploadPath);
            rMap.put("framesNum", framesNum);
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 查看学员端桌面 操作（web端发送请求）
     * @date: 2022/11/7
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "jpWbe")
    public ApiResult jpWbe(@RequestBody Map<String, Object> map) {
        try {
            Long examId = map.get("examId") == null ? null : Long.valueOf(map.get("examId").toString());
            Long studentId = map.get("studentId") == null ? null : Long.valueOf(map.get("studentId").toString());
            if (examId == null) {
                return ApiResultHandler.buildApiResult(100, "参数 examId 为空", null);
            }
            if (studentId == null) {
                return ApiResultHandler.buildApiResult(100, "参数 studentId 为空", null);
            }

            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            logger.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 影像处理 开始答题逻辑
     * @date: 2022/11/18
     * @param: []
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "tifProcesExam")
    public ApiResult tifProcesExam() {
        try {
            Map<String, Object> rMap = new HashMap<>();
            rMap.put("filePath", fileServer);
            rMap.put("tifInterface", tifInterface);
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    @PostMapping("/isUserRelated")
    public Map<Long, String> isUserRelated(@RequestBody List<Long> userIdList) {
        return examService.isUserRelated(userIdList);
    }
}
