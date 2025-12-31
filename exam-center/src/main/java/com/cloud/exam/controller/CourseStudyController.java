package com.cloud.exam.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ExamConstants;
import com.cloud.core.ResultMesEnum;
import com.cloud.exam.dao.CourseUserRelDao;
import com.cloud.exam.dao.ErrorQuestionDao;
import com.cloud.exam.model.course.*;
import com.cloud.exam.model.exam.CollectionQuestion;
import com.cloud.exam.model.exam.Question;
import com.cloud.exam.model.exam.QuestionKpRel;
import com.cloud.exam.model.exam.StudentAnswerDetails;
import com.cloud.exam.service.*;
import com.cloud.exam.utils.CommonPar;
import com.cloud.exam.utils.exam.HandleAnswerUtils;
import com.cloud.exam.vo.CourseStudentAnswerVO;
import com.cloud.exam.vo.CourseStudyVO;
import com.cloud.exam.vo.StudentAnswerVO;
import com.cloud.exception.ResultMesCode;
import com.cloud.feign.model.ModelFeign;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.user.AppUser;
import com.cloud.model.user.LoginAppUser;
import com.cloud.model.user.SysRole;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.thread.TaskThreadPoolConfig;
import com.cloud.thread.ThreadPoolUtils;
import com.cloud.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.bytedeco.javacpp.presets.opencv_core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * Created by dyl on 2022/01/10.
 * 课程管理类
 */
@Api(value = "课程管理类")
@RequestMapping("CourseStudy")
@RestController
public class CourseStudyController {

    public static final Logger logger = LoggerFactory.getLogger(CourseStudyController.class);
    @Autowired
    private CourseStudyService courseStudyService;
    @Autowired
    private CourseUserRelService courseUserRelService;
    @Autowired
    private CourseKpRelService courseKpRelService;
    @Autowired
    private CourseQuestionRelService courseQuestionRelService;
    @Autowired
    private SysDepartmentFeign sysDepartmentFeign;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private CourseStudentAnswerService courseStudentAnswerService;
    @Autowired
    private CourseKpService courseKpService;
    @Autowired
    private QuestionKpRelService questionKpRelService;
    @Autowired
    private CollectionQuestionService collectionQuestionService;
    @Autowired
    private CourseUserRelDao courseUserRelDao;
    @Autowired
    private ErrorQuestionDao errorQuestionDao;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ModelFeign modelFeign;
    @Autowired
    private TaskThreadPoolConfig taskThreadPoolConfig;
    // 影像缩略图访问地址
    @Value("${localUrlPrefix}")
    private String localUrlPrefix;
    // 文件服务器地址
    @Value(value = "${file_server}")
    private String fileServer;
    @ApiOperation(value = "创建课程基本信息")
    @RequestMapping(value = "addCourseStudyBaseDetails",method = RequestMethod.POST)
    public ApiResult addCourseStudyBaseDetails(@Valid @RequestBody CourseStudyVO courseStudyVO){

        if(ObjectUtil.isNotNull(courseStudyVO.getStartTime()) && System.currentTimeMillis()>courseStudyVO.getStartTime().getTime()){
            throw new IllegalArgumentException("开始时间不能小于当前时间,请修改");
        }
        if(ObjectUtil.isNotNull(courseStudyVO.getEndTime()) && System.currentTimeMillis()>courseStudyVO.getEndTime().getTime()){
            throw new IllegalArgumentException("结束时间不能小于当前时间,请修改");
        }
        CourseStudy courseStudy = new CourseStudy();
        BeanUtils.copyProperties(courseStudyVO,courseStudy);
        courseStudy.setCreator(AppUserUtil.getLoginAppUser().getId());
        courseStudy.setCreateTime(new Date());
        courseStudy.setStatus(ExamConstants.COURSE_INIT);
        if(courseStudyService.saveOrUpdate(courseStudy)){
            Long id = courseStudy.getId();
            QueryWrapper<CourseKp> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("course_id",id);
            courseKpService.remove(queryWrapper);
            List<Long> kpIdList = new ArrayList<>();
            String[] split = courseStudyVO.getKps().split(",");
            for (int i = 0; i < split.length; i++) {
                kpIdList.add(Long.valueOf(split[i]+""));
            }
            kpIdList.stream().forEach(e->{
                CourseKp courseKp = new CourseKp();
                courseKp.setCourseId(courseStudy.getId());
                courseKp.setKpId(e);
                courseKpService.save(courseKp);
            });
        };
        return  ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "保存课程基本信息和知识点成功", courseStudy.getId());
    }
    @ApiOperation(value = "课程添加教员")
    @RequestMapping(value = "addCourseStudyTeacher",method = RequestMethod.POST)
    public ApiResult addCourseStudyTeacher(@Valid @RequestBody CourseStudyVO courseStudyVO){

        CourseStudy courseStudy = new CourseStudy();
        BeanUtils.copyProperties(courseStudyVO,courseStudy);
        courseStudyService.saveOrUpdate(courseStudy);
        return  ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "添加教员成功", courseStudy.getId());

    }



    @ApiOperation(value = "更改知识点，校验当前知识点与已经关联的数据是否匹配")
    @RequestMapping(value = "changeCourseStudyKp",method = RequestMethod.POST)
    public ApiResult changeCourseStudyKp(@RequestBody CourseStudyVO courseStudyVO){
        try{
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            //选择的课程知识点
            List<Long> kpIdList = new ArrayList<>();
            String[] split = courseStudyVO.getKps().split(",");
            for (int i = 0; i < split.length; i++) {
                kpIdList.add(Long.valueOf(split[i]+""));
            }
            Long teacherId = courseStudyVO.getTeacherId();

            //教师的数据权限包含课程的知识点
            boolean f2 = true ;
            if(ObjectUtil.isNotNull(teacherId)){
                Set<String> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(teacherId);
                if(!kpIdsbyUserId.containsAll(kpIdList)){
                    f2 = false ;
                }
            }

           List<CourseKpRel> kpList = courseStudyVO.getKpIds();
           List<Long> kpIds = new ArrayList<>();
           List<Long> qIds1List = courseStudyVO.getQIds1();
           List<Long> qIds1 = new ArrayList<>();
           List<Long> studentIdList = courseStudyVO.getStudentIds();
           List<Long> studentIds = new ArrayList<>();
           List<Question> qIds2List = courseStudyVO.getQIds2();
           List<Long> qIds2 = new ArrayList<>();

           if(CollectionUtils.isNotEmpty(kpList)){
               CountDownLatch countDownLatch = new CountDownLatch(kpList.size());
               kpList.stream().forEach(e->{
                   CompletableFuture.runAsync(()->{
                       try{
                           if(ObjectUtil.isNotNull(e.getKpId()) && !kpIdList.contains(e.getKpId())){
                               kpIds.add(e.getKpId());
                           }
                       }finally {
                           countDownLatch.countDown();
                       }
                   });
               });
           }

            if(CollectionUtils.isNotEmpty(qIds1List)){
                CountDownLatch countDownLatch = new CountDownLatch(qIds1List.size());
                qIds1List.stream().forEach(e->{
                   CompletableFuture.runAsync(()->{
                       try{
                           Set<String> set = new HashSet<>();
                           QueryWrapper<QuestionKpRel> questionKpRelQueryWrapper = new QueryWrapper<>();
                           questionKpRelQueryWrapper.eq("question_id",e);
                           List<QuestionKpRel> list = questionKpRelService.list(questionKpRelQueryWrapper);
                           list.stream().forEach(questionKpRel->set.add(questionKpRel.getKpId()));
                           if(!kpIdList.containsAll(set)){
                               qIds1.add(e);
                           }
                       }finally {
                           countDownLatch.countDown();
                       }
                   });
               });
                countDownLatch.await();
            }
           if(CollectionUtils.isNotEmpty(studentIdList)){
               CountDownLatch countDownLatch = new CountDownLatch(studentIdList.size());
               studentIdList.stream().forEach(e->{
                   CompletableFuture.runAsync(()->{
                       //设置请求原线程下的参数
                       RequestContextHolder.setRequestAttributes(requestAttributes);
                       try{
                           Set<String> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(e);
                           if(!kpIdsbyUserId.containsAll(kpIdList)){
                               studentIds.add(e) ;
                           }
                       }catch (Exception ex){
                           ex.printStackTrace();
                       }finally {
                           countDownLatch.countDown();
                       }
                   });
               });
               countDownLatch.await();
           }
           if(CollectionUtils.isNotEmpty(qIds2List)){
               CountDownLatch countDownLatch = new CountDownLatch(qIds2List.size());
               qIds2List.stream().forEach(e->{
                   CompletableFuture.runAsync(()->{
                       try{
                           Set<String> set = new HashSet<>();
                           QueryWrapper<QuestionKpRel> questionKpRelQueryWrapper = new QueryWrapper<>();
                           questionKpRelQueryWrapper.eq("question_id",e.getId());
                           List<QuestionKpRel> list = questionKpRelService.list(questionKpRelQueryWrapper);
                           list.stream().forEach(questionKpRel->set.add(questionKpRel.getKpId()));
                           if(!kpIdList.containsAll(kpIdList)){
                               qIds2.add(e.getId());
                           }
                       }finally {
                           countDownLatch.countDown();
                       }
                   });
               });
               countDownLatch.await();
           }
           if(CollectionUtils.isNotEmpty(kpIds) ||CollectionUtils.isNotEmpty(qIds1) ||CollectionUtils.isNotEmpty(qIds2) ||CollectionUtils.isNotEmpty(studentIds)||!f2){
               JSONObject jsonObject  = new JSONObject();
               jsonObject.put("kpIds",kpIds);
               jsonObject.put("qIds1",qIds1);
               jsonObject.put("qIds2",qIds2);
               jsonObject.put("studentIds",studentIds);
               if(!f2){
                   jsonObject.put("teacherId",teacherId);
               }else{
                   jsonObject.put("teacherId",null);
               }

               return ApiResultHandler.buildApiResult(ResultMesEnum.COURSE_KP_NOT_MATCH.getResultCode(), ResultMesEnum.COURSE_KP_NOT_MATCH.getResultMsg(), jsonObject);
           }else {
               return ApiResultHandler.buildApiResult(ResultMesEnum.SUCCESS.getResultCode(), ResultMesEnum.SUCCESS.getResultMsg(), null );
           }

            /*
            QueryWrapper<CourseKpRel> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("course_id",courseId);
            List<CourseKpRel> list1 = courseKpRelService.list(queryWrapper1);
            QueryWrapper<CourseUserRel> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.eq("course_id",courseId);
            List<CourseUserRel> list2 = courseUserRelService.list(queryWrapper2);
            QueryWrapper<CourseQuestionRel> queryWrapper3 = new QueryWrapper<>();
            queryWrapper3.eq("course_id",courseId);
            List<CourseQuestionRel> list3 = courseQuestionRelService.list(queryWrapper3);
            if(flag){
                List<Object> list11 = redisUtils.lGet("course:createkp:" + courseId, 0, -1);
                List<CourseKpRel> ll1 = (List<CourseKpRel>)list11.get(0);
                List<Object> list22 = redisUtils.lGet("course:createuser:" + courseId, 0, -1);
                List<CourseUserRel> ll2 = (List<CourseUserRel>)list22.get(0);
                List<Object> list33 = redisUtils.lGet("course:createquestion:" + courseId, 0, -1);
                List<CourseQuestionRel> ll3 = (List<CourseQuestionRel>)list33.get(0);
                courseKpRelService.remove(queryWrapper1);
                courseUserRelService.remove(queryWrapper2);
                courseQuestionRelService.remove(queryWrapper3);
                ll1.stream().forEach(e->{
                    courseKpRelService.saveOrUpdate(e);
                });
                ll2.stream().forEach(e->{
                    courseUserRelService.saveOrUpdate(e);
                });
                ll3.stream().forEach(e->{
                    courseQuestionRelService.saveOrUpdate(e);
                });
                if(f2){
                    CourseStudy byId = courseStudyService.getById(courseStudyVO.getId());
                    BeanUtils.copyProperties(courseStudyVO,byId);
                    byId.setTeacherId(null);
                    courseStudyService.saveOrUpdate(byId);
                }
                return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "保存课程基本信息和知识点成功", null);
            }


            if(CollectionUtils.isEmpty(list1) && CollectionUtils.isEmpty(list2) && CollectionUtils.isEmpty(list3)&&ObjectUtil.isNull(teacherId)){
                addCourseStudyBaseDetails(courseStudyVO);
            }else {

                boolean f1  = true ;
                List<CourseKpRel> l1 = new ArrayList<>();
                //课堂知识点
                list1.stream().forEach(e->{
                    if(!kpIdList.contains(e.getKpId())){
                        l1.add(e) ;
                    }
                });
                if(list1.size()>0 && l1.size()>0){
                    f1 = false ;
                    redisUtils.lSet("course:createkp:"+courseId,l1);
                }
                //学员
                CountDownLatch countDownLatch1 = new CountDownLatch(list2.size());
                List<CourseUserRel> ll = new ArrayList<>();
                for (CourseUserRel courseRel:list2) {
                    CompletableFuture.runAsync(()->{
                        try{
                            Set<Long> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(courseRel.getUserId());
                            if(!kpIdsbyUserId.containsAll(kpIdList)){
                                ll.add(courseRel);
                            }
                        }finally {
                            countDownLatch1.countDown();
                        }
                    });
                }
                countDownLatch1.await();
                //试题
                CountDownLatch countDownLatch2 = new CountDownLatch(list3.size());
                List<CourseQuestionRel> ls = new ArrayList<>();
                for (CourseQuestionRel courseQuestionRel :list3) {
                    CompletableFuture.runAsync(()->{
                        try{
                            Set<Long> set = new HashSet<>();
                            QueryWrapper<QuestionKpRel> questionKpRelQueryWrapper = new QueryWrapper<>();
                            questionKpRelQueryWrapper.eq("question_id",courseQuestionRel.getQuestionId());
                            List<QuestionKpRel> list = questionKpRelService.list(questionKpRelQueryWrapper);
                            list.stream().forEach(e->set.add(e.getKpId()));
                            if(!kpIdList.containsAll(set)){
                                ls.add(courseQuestionRel);
                            }
                        }finally {
                            countDownLatch2.countDown();
                        }
                    });
                }
                countDownLatch2.await();
                if(list2.size()>0){
                    redisUtils.lSet("course:createuser:"+courseId,ll);
                }
                if(list3.size()>0){
                    redisUtils.lSet("course:createquestion:"+courseId,ls);
                }
                if(list2.size()>0){
                    return ApiResultHandler.buildApiResult(ResultMesEnum.COURSE_STUDENT_NOT_MATCH.getResultCode(), ResultMesEnum.COURSE_STUDENT_NOT_MATCH.getResultMsg(), "");
                }
                if(list3.size()>0){
                    return ApiResultHandler.buildApiResult(ResultMesEnum.COURSE_QUESTION_NOT_MATCH.getResultCode(), ResultMesEnum.COURSE_QUESTION_NOT_MATCH.getResultMsg(), "");
                }
                if(!f2){
                    return ApiResultHandler.buildApiResult(ResultMesEnum.COURSE_KPID_NOT_MATCH.getResultCode(), ResultMesEnum.COURSE_KPID_NOT_MATCH.getResultMsg(), "");
                }
                if(!f1){
                    return ApiResultHandler.buildApiResult(ResultMesEnum.COURSE_TEACHER_NOT_MATCH.getResultCode(), ResultMesEnum.COURSE_TEACHER_NOT_MATCH.getResultMsg(), "");
                }
                //说明选择的知识点全部符合当前选择的关联数据，直接删除原有的关联知识点，重新添加知识点
                QueryWrapper<CourseKp> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("course_id",courseStudyVO.getId());
                courseKpService.remove(queryWrapper);
                kpIdList.stream().forEach(e->{
                    CourseKp courseKp = new CourseKp();
                    courseKp.setKpId(e);
                    courseKp.setCourseId(courseStudyVO.getId());
                    courseKpService.save(courseKp);
                });
                return  ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "保存课程基本信息和知识点成功", null);
            }*/
        }catch (Exception e){
            logger.error("修改课程知识点校验是否符合数据权限失败：",e);
            return ApiResultHandler.buildApiResult(ResultMesEnum.SUCCESS.getResultCode(), ResultMesEnum.SUCCESS.getResultMsg(), null );
        }

    }

    @ApiOperation(value = "创建课程添加学员课堂知识试题")
    @RequestMapping(value = "createCourseStudy",method = RequestMethod.POST)
    public ApiResult createCourseStudy(@Valid @RequestBody CourseStudyVO courseStudyVO){
        if(CollectionUtils.isEmpty(courseStudyVO.getKpIds())){
            throw new IllegalArgumentException("请选择知识点。");
        }
        if(CollectionUtils.isEmpty(courseStudyVO.getStudentIds())){
            throw new IllegalArgumentException("请选择学生。");
        }
        if(CollectionUtils.isEmpty(courseStudyVO.getQIds2()) || CollectionUtils.isEmpty(courseStudyVO.getQIds1())){
            throw new IllegalArgumentException("请选择试题。");
        }
        if(ObjectUtil.isNotNull(courseStudyVO.getStartTime()) && System.currentTimeMillis()>courseStudyVO.getStartTime().getTime()){
            throw new IllegalArgumentException("开始时间不能小于当前时间,请修改后重新提交。");
        }
        if(ObjectUtil.isNotNull(courseStudyVO.getEndTime()) && System.currentTimeMillis()>courseStudyVO.getEndTime().getTime()){
            throw new IllegalArgumentException("结束时间不能小于当前时间,请修改后重新提交。");
        }

        //保存信息
        try{
            CourseStudy courseStudy = new CourseStudy();
            BeanUtils.copyProperties(courseStudyVO,courseStudy);
            if(ObjectUtil.isNotEmpty(courseStudyVO.getId())){
                //当修改课程时先删除关联关系
                QueryWrapper<CourseKpRel> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("course_id",courseStudyVO.getId());
                QueryWrapper<CourseUserRel> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.eq("course_id",courseStudyVO.getId());
                QueryWrapper<CourseQuestionRel> queryWrapper3 = new QueryWrapper<>();
                queryWrapper3.eq("course_id",courseStudyVO.getId());
                QueryWrapper<CourseKp> queryWrapper4 = new QueryWrapper<>();
                queryWrapper4.eq("course_id",courseStudyVO.getId());
                courseKpRelService.remove(queryWrapper1);
                courseUserRelService.remove(queryWrapper2);
                courseQuestionRelService.remove(queryWrapper3);
                courseKpService.remove(queryWrapper4);
            }else {
                courseStudy.setCreator(AppUserUtil.getLoginAppUser().getId());
                courseStudy.setCreateTime(new Date());
                courseStudy.setStatus(ExamConstants.COURSE_INIT);
            }

            if(courseStudyService.saveOrUpdate(courseStudy)){
                CompletableFuture.runAsync(()->{
                    Set<Long> kpIdList = new HashSet<>();
                    String[] split = courseStudyVO.getKps().split(",");
                    for (int i = 0; i < split.length; i++) {
                        kpIdList.add(Long.valueOf(split[i]+""));
                    }
                    List<CourseKp> ll  = new ArrayList();
                    kpIdList.stream().forEach(e->{
                        CourseKp courseKp = new CourseKp();
                        BeanUtils.copyProperties(e,courseKp,"id");
                        courseKp.setKpId(e);
                        courseKp.setCourseId(courseStudy.getId());
                        ll.add(courseKp);
                    });
                    courseKpService.saveBatch(ll);
                }).exceptionally(exception->{
                    logger.error("插入课程知识点关系失败",exception);
                    //手动回滚事务
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return null;
                });
                CompletableFuture.runAsync(()->{
                    List<Long> studentIds = courseStudyVO.getStudentIds();
                    List<CourseUserRel> ll  = new ArrayList();
                    studentIds.stream().forEach(e->{
                        CourseUserRel courseUserRel = new CourseUserRel();
                        courseUserRel.setUserId(e);
                        courseUserRel.setCourseId(courseStudy.getId());
                        ll.add(courseUserRel);
                    });
                    courseUserRelService.saveBatch(ll);
                }).exceptionally(exception->{
                     logger.error("插入课程学员关系失败",exception);
                     //手动回滚事务
                     TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                     return null;
                 });
                CompletableFuture.runAsync(()->{
                    List<CourseKpRel> kpIds = courseStudyVO.getKpIds();
                    List<CourseKpRel> ll  = new ArrayList();
                    kpIds.stream().forEach(e->{
                        CourseKpRel courseKpRel = new CourseKpRel();
                        BeanUtils.copyProperties(e,courseKpRel,"id");
                        courseKpRel.setKpId(e.getKpId());
                        courseKpRel.setCourseId(courseStudy.getId());
                        courseKpRel.setSort(ll.size()+1);
                        ll.add(courseKpRel);
                    });
                    courseKpRelService.saveBatch(ll);
                }).exceptionally(exception->{
                    logger.error("插入课堂知识点关系失败",exception);
                    //手动回滚事务
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return null;
                });
                CompletableFuture.runAsync(()->{
                    List<Long> qIds1 = courseStudyVO.getQIds1();
                    List<CourseQuestionRel> ll  = new ArrayList();
                    qIds1.stream().forEach(e->{
                        CourseQuestionRel courseQuestionRel = new CourseQuestionRel();
                        courseQuestionRel.setQuestionId(e);
                        courseQuestionRel.setCourseId(courseStudy.getId());
                        courseQuestionRel.setType(ExamConstants.COURSE_QUESTION_BEFORE);
                        courseQuestionRel.setSort(ll.size()+1);
                        ll.add(courseQuestionRel);
                    });
                    courseQuestionRelService.saveBatch(ll);
                }).exceptionally(exception->{
                    logger.error("插入课程课堂试题关系失败",exception);
                    //手动回滚事务
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return null;
                });
                CompletableFuture.runAsync(()->{
                    List<Question> qIds1 = courseStudyVO.getQIds2();
                    List<CourseQuestionRel> ll  = new ArrayList();
                    qIds1.stream().forEach(e->{
                        CourseQuestionRel courseQuestionRel = new CourseQuestionRel();
                        courseQuestionRel.setQuestionId(e.getId());
                        courseQuestionRel.setCourseId(courseStudy.getId());
                        courseQuestionRel.setType(ExamConstants.COURSE_QUESTION_AFTER);
                        courseQuestionRel.setScore(e.getScore()==null?1:e.getScore());
                        courseQuestionRel.setSort(ll.size()+1);
                        ll.add(courseQuestionRel);
                    });
                    courseQuestionRelService.saveBatch(ll);
                }).exceptionally(exception->{
                    logger.error("插入课程课后练习试题关系失败",exception);
                    //手动回滚事务
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return null;
                });
            }
        }catch (Exception e){
            logger.error("课程创建失败",e);
            //手动回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ApiResultHandler.otherError(ResultMesCode.INTERNAL_SERVER_ERROR);
        };
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "创建成功", null);
    }
    @ApiOperation("修改试题分数")
    @RequestMapping(value = "editQuestionScore",method = RequestMethod.GET)
    public ApiResult editQuestionScore(@RequestParam Long courseId,@RequestParam Long id,@RequestParam Integer score){
        QueryWrapper<CourseQuestionRel> questionRelQueryWrapper = new QueryWrapper<>();
        questionRelQueryWrapper.eq("course_id",courseId);
        questionRelQueryWrapper.eq("question_id",id);
        questionRelQueryWrapper.eq("type",ExamConstants.COURSE_AFTER_SUBMIT);
        CourseQuestionRel byId = courseQuestionRelService.getOne(questionRelQueryWrapper);
        byId.setScore(Double.parseDouble(score+""));
        courseQuestionRelService.saveOrUpdate(byId);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "修改成功", null);
    }
    @ApiOperation("查询课程信息")
    @RequestMapping(value = "getCourseDetailsById",method = RequestMethod.GET)
    public ApiResult getCourseDetailsById(@RequestParam Long id){
        CourseStudyVO courseStudyDetail = getCourseStudyDetail(id);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取课程信息成功", courseStudyDetail);
    }
    public CourseStudyVO getCourseStudyDetail(Long id){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<List<Long>> c0 = CompletableFuture.supplyAsync(()->{
            //设置请求原线程下的参数
            RequestContextHolder.setRequestAttributes(requestAttributes);
            QueryWrapper<CourseKp> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("course_id",id);
            List<CourseKp> list = courseKpService.list(queryWrapper);
            List<Long> ll  = new ArrayList();
            list.stream().forEach(e->{
                ll.add(e.getKpId());
            });
            return  ll;
        }).exceptionally(exception->{
            logger.error("查询课程学员关系失败",exception);
            //手动回滚事务
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ArrayList<>();
        });

        CompletableFuture<List<AppUser>> c1 = CompletableFuture.supplyAsync(()->{
            //设置请求原线程下的参数
            RequestContextHolder.setRequestAttributes(requestAttributes);
            QueryWrapper<CourseUserRel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("course_id",id);
            List<CourseUserRel> list = courseUserRelService.list(queryWrapper);
            List<AppUser> ll  = new ArrayList();
            list.stream().forEach(e->{
                AppUser appUserById = sysDepartmentFeign.findAppUserById(e.getUserId());
                ll.add(appUserById);
            });
            return  ll;
        }).exceptionally(exception->{
            logger.error("查询课程学员关系失败",exception);
            //手动回滚事务
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ArrayList<>();
        });
        CompletableFuture<List<CourseKpRel>> c2 = CompletableFuture.supplyAsync(()->{
            RequestContextHolder.setRequestAttributes(requestAttributes);
            QueryWrapper<CourseKpRel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("course_id",id);
            List<CourseKpRel> list = courseKpRelService.list(queryWrapper);
            List<CourseKpRel> ll  = new ArrayList();
            list.stream().forEach(e->{
                CourseKpRel courseKpRel = new CourseKpRel();
                BeanUtils.copyProperties(e,courseKpRel);
                ll.add(courseKpRel);
            });
            return ll;
        }).exceptionally(exception->{
            logger.error("查询课程知识点关系失败",exception);
            //手动回滚事务
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ArrayList<>();
        });
        CompletableFuture<List<Question>> c3 = CompletableFuture.supplyAsync(()->{
            RequestContextHolder.setRequestAttributes(requestAttributes);
            QueryWrapper<CourseQuestionRel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("course_id",id);
            queryWrapper.eq("type",0);
            List<CourseQuestionRel> list = courseQuestionRelService.list(queryWrapper);
            List<Question> ll  = new ArrayList();
            list.stream().forEach(e->{
                Question byId1 = questionService.getById(e.getQuestionId());
                ll.add(byId1);
            });
            return ll;
        }).exceptionally(exception->{
            logger.error("查询课程课堂试题关系失败",exception);
            //手动回滚事务
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ArrayList<>();
        });
        CompletableFuture<List<Question>> c4 = CompletableFuture.supplyAsync(()->{
            RequestContextHolder.setRequestAttributes(requestAttributes);
            QueryWrapper<CourseQuestionRel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("course_id",id);
            queryWrapper.eq("type",1);
            List<CourseQuestionRel> list = courseQuestionRelService.list(queryWrapper);
            List<Question> ll  = new ArrayList();
            list.stream().forEach(e->{
                Question byId1 = questionService.getById(e.getQuestionId());
                byId1.setScore(e.getScore());
                ll.add(byId1);
            });
            return ll;
        }).exceptionally(exception->{
            logger.error("查询课程课堂试题关系失败",exception);
            //手动回滚事务
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ArrayList<>();
        });
        CompletableFuture.allOf(c0,c1,c2,c3,c4);
        List<Long> join0 = c0.join();
        List<AppUser> join = c1.join();
        List<CourseKpRel> join1 = c2.join();
        List<Question> join2 = c3.join();
        List<Question> join3 = c4.join();
        CourseStudyVO vo = new CourseStudyVO();
        CourseStudy courseStudy = courseStudyService.getById(id);
        BeanUtils.copyProperties(courseStudy,vo);
        vo.setKpList(join0);
        vo.setBeforeDetails(join2);
        vo.setKpDetails(join1);
        vo.setStudentDetails(join);
        vo.setAfterDetails(join3);
        vo.setTeacherName(sysDepartmentFeign.findAppUserById(courseStudy.getTeacherId()).getNickname());
        return vo ;
    }
    @ApiOperation("修改课程基本信息")
    @RequestMapping(value = "editCourseBase",method = RequestMethod.POST)
    public ApiResult  editCourseBase(@Valid @RequestBody CourseStudy courseStudy){
        if(ObjectUtil.isNotNull(courseStudy.getStartTime()) && System.currentTimeMillis()>courseStudy.getStartTime().getTime()){
            throw new IllegalArgumentException("开始时间不能小于当前时间,请修改后重试");
        }
        if(ObjectUtil.isNotNull(courseStudy.getEndTime()) && System.currentTimeMillis()>courseStudy.getEndTime().getTime()){
            throw new IllegalArgumentException("结束时间不能小于当前时间,请修改后重试");
        }
        courseStudyService.saveOrUpdate(courseStudy);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "修改成功", courseStudy.getId());
    }
    @ApiOperation(value = "获取具有教师权限用户")
    @RequestMapping(value = "getTeachers",method = RequestMethod.GET)
    public ApiResult getTeachers(@RequestParam(required = false) String name,@RequestParam(required = false) String kps){

        Set<Long> kpIdList = new HashSet<>();
        if(StringUtils.isNotBlank(kps)){
            String[] split = kps.split(",");
            for (String str:split) {
                kpIdList.add(Long.valueOf(str)) ;
            }
        }
        List<AppUser> data=sysDepartmentFeign.getAppUserByRole("teacher");
        List<AppUser> ll = new ArrayList<>();
        ThreadPoolUtils threadPoolUtils = new ThreadPoolUtils();
        ThreadPoolExecutor threadPoolExecutor = threadPoolUtils.getThreadPool(taskThreadPoolConfig.getCorePoolSize(), taskThreadPoolConfig.getMaxPoolSize(), taskThreadPoolConfig.getKeepAliveSeconds(), taskThreadPoolConfig.getQueueCapacity());
        CountDownLatch countDownLatch = new CountDownLatch(data.size());
        try{
            for (AppUser appUser:data) {
                Mytask mytask = new Mytask(appUser,kpIdList.stream().collect(Collectors.toSet()), ll,countDownLatch);
                threadPoolExecutor.submit(mytask);
            }
            countDownLatch.await();
            if(ObjectUtil.isNotEmpty(name)){
                ll = ll.stream().filter(e->e.getNickname().contains(name)).collect(Collectors.toList());
            }
            List<AppUser> collect = ll.stream().filter(appUser -> ObjectUtil.isNotNull(appUser)).collect(Collectors.toList());
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取教员成功",collect);
        }catch (Exception e){
            logger.error("获取教师权限用户失败：",e);
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取教员失败", data);
        }finally {
            threadPoolExecutor.shutdown();
        }
    }

    class Mytask implements Runnable{
        private AppUser appUser ;
        private Set<Long> kpIdList ;
        private List<AppUser> ll ;
        private CountDownLatch countDownLatch ;
        public Mytask(AppUser appUser, Set<Long> kpIdList,List<AppUser> ll,CountDownLatch countDownLatch) {
            this.appUser = appUser;
            this.kpIdList = kpIdList;
            this.ll = ll ;
            this.countDownLatch = countDownLatch ;
        }
        @Override
        public void run() {
            try {
                Set<String> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(appUser.getId());
                if(kpIdsbyUserId.containsAll(kpIdList)){
                    ll.add(appUser);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                countDownLatch.countDown();
            }
        }
    }
    @ApiOperation(value = "添加学员")
    @RequestMapping(value = "addUserRel",method = RequestMethod.POST)
    public ApiResult addUserRel(@RequestBody Map<String,Object> map){
        Object kps = map.get("kps");
        Object courseId = map.get("courseId");
        List<Object> userIds = (List<Object>)map.get("userIds");
        if(ObjectUtil.isNotEmpty(kps)){
            //先删除后保存知识点
            QueryWrapper<CourseKp> qw = new QueryWrapper<>();
            qw.eq("course_id",Long.valueOf(String.valueOf(courseId)));
            courseKpService.remove(qw);
            String[] split = kps.toString().split(",");
            Set<Long> set = new HashSet<>();
            for (int i = 0; i < split.length; i++) {
                set.add(Long.valueOf(split[i])) ;
            }
            for (Long kid:set) {
                CourseKp courseKp = new CourseKp();
                courseKp.setKpId(kid);
                courseKp.setCourseId(Long.valueOf(String.valueOf(courseId)));
                courseKpService.save(courseKp) ;
            }
        }
        if(CollectionUtils.isEmpty(userIds)){
            throw new IllegalArgumentException("请选择至少一位学员");
        }
        List<CourseUserRel>  ll = new ArrayList<>();
        userIds.stream().forEach(e->{
            QueryWrapper<CourseUserRel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("course_id",Long.valueOf(String.valueOf(e)));
            queryWrapper.eq("user_id",Long.valueOf(String.valueOf(courseId)));
            List<CourseUserRel> list = courseUserRelService.list(queryWrapper);
            if(CollectionUtils.isEmpty(list)){
                CourseUserRel c = new CourseUserRel();
                c.setUserId(Long.valueOf(String.valueOf(e)));
                c.setCourseId(Long.valueOf(String.valueOf(courseId)));
                ll.add(c);
            }
        });
        courseUserRelService.saveBatch(ll);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", null);
    }

    @ApiOperation(value = "删除学员")
    @RequestMapping(value = "delUserRel",method = RequestMethod.POST)
    public ApiResult delUserRel(@RequestParam Long courseId, @RequestParam Long userId){
        QueryWrapper<CourseUserRel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id",courseId);
        queryWrapper.eq("user_id",userId);
        courseUserRelService.remove(queryWrapper);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", null);
    }
    @ApiOperation(value = "添加知识点，flag=0添加、1排序")
    @RequestMapping(value = "updateKpIds",method = RequestMethod.POST)
    public ApiResult updateKpIds(@RequestBody Map<String,Object> map){
        Object kps = map.get("kps");
        Object  courseId = map.get("courseId");
        if(ObjectUtil.isNotEmpty(kps)){
            //先删除后保存知识点
            QueryWrapper<CourseKp> qw = new QueryWrapper<>();
            qw.eq("course_id",Long.valueOf(String.valueOf(courseId)));
            courseKpService.remove(qw);
            String[] split = kps.toString().split(",");
            Set<Long> set = new HashSet<>();
            for (int i = 0; i < split.length; i++) {
                set.add(Long.valueOf(split[i])) ;
            }
            for (Long kid:set) {
                CourseKp courseKp = new CourseKp();
                courseKp.setKpId(kid);
                courseKp.setCourseId(Long.valueOf(String.valueOf(courseId)));
                courseKpService.save(courseKp) ;
            }
        }

        Integer flag = Integer.valueOf(String.valueOf(map.get("flag")));
        List<HashMap<String,Object>> courseKpRelList = (List<HashMap<String,Object>>)map.get("kpIds");
        if(0==flag && CollectionUtils.isEmpty(courseKpRelList)){
            throw new IllegalArgumentException("请至少选择一个知识添加");
        }

        int i = 0;
        QueryWrapper<CourseKpRel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id",Long.valueOf(String.valueOf(courseId)));
        List<CourseKpRel> list  = courseKpRelService.list(queryWrapper);
        if(Integer.parseInt(flag.toString())==0){
            i = list.size();
        }/*else {
            courseKpRelService.remove(queryWrapper);
        }*/
        final int j = i;
        List<CourseKpRel>  ll = new ArrayList<>();
        final int k = 1;
        courseKpRelList.stream().forEach(e -> {
            if (0 == flag) {
                boolean sensesId = list.stream().anyMatch(record -> record.getSensesId().equals(e.get("sensesId")));
                if (!sensesId) {
                    CourseKpRel c = new CourseKpRel();
                    c.setKpId(Long.valueOf(String.valueOf(e.get("kpId"))));
                    c.setSensesId(e.get("sensesId") + "");
                    c.setBkClassLabel(e.get("bkClassLabel") + "");
                    c.setSensesName(e.get("sensesName") + "");
                    c.setCourseId(Long.valueOf(String.valueOf(courseId)));
                    c.setSort(j + ll.size() + 1);
                    ll.add(c);
                }
            }else if(1==flag){
                CourseKpRel courseKpRel = list.stream().filter(record -> record.getSensesId().equals(e.get("sensesId"))).findFirst().get();
                courseKpRel.setSort(k+ll.size());
                ll.add(courseKpRel);
            }
        });
        courseKpRelService.saveOrUpdateBatch(ll);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", null);
    }

    @ApiOperation(value = "删除知识点")
    @RequestMapping(value = "delKpIdRel",method = RequestMethod.POST)
    public ApiResult delKpIdRel(@RequestParam Long id){
        courseKpRelService.removeById(id);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", null);
    }
    @ApiOperation(value = "添加试题或者排序试题,flag=0添加、1排序,type=0 课堂练习、1 课后练习")
    @RequestMapping(value = "updateQIds",method = RequestMethod.POST)
    public ApiResult updateQIds(@RequestBody Map<String,Object> map){

        Object courseId = map.get("courseId");
        Object type = map.get("type");
        Object flag = map.get("flag");
        Object kps = map.get("kps");
        if(ObjectUtil.isNotEmpty(kps)){
            //先删除后保存知识点
            QueryWrapper<CourseKp> qw = new QueryWrapper<>();
            qw.eq("course_id",Long.valueOf(String.valueOf(courseId)));
            courseKpService.remove(qw);
            String[] split = kps.toString().split(",");
            Set<Long> set = new HashSet<>();
            for (int i = 0; i < split.length; i++) {
                set.add(Long.valueOf(split[i])) ;
            }
            for (Long kid:set) {
                CourseKp courseKp = new CourseKp();
                courseKp.setKpId(kid);
                courseKp.setCourseId(Long.valueOf(String.valueOf(courseId)));
                courseKpService.save(courseKp) ;
            }
        }
        if(Integer.valueOf(flag+"")==0 && CollectionUtils.isEmpty((List<HashMap<String,Object>>)map.get("qIds"))){
            throw new IllegalArgumentException("请至少选择一个试题");
        }
        Integer i = 0;
        QueryWrapper<CourseQuestionRel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id",Long.valueOf(String.valueOf(courseId)));
        queryWrapper.eq("type",Integer.parseInt(String.valueOf(type)));
        List<CourseQuestionRel> list  = courseQuestionRelService.list(queryWrapper);
        if(Integer.parseInt(String.valueOf(flag))==0){
            i = list.size();
        }/*else{
            courseQuestionRelService.remove(queryWrapper);
        }*/
        final Integer j = i;
        List<CourseQuestionRel> ll = new ArrayList<>();
        if (0 == Integer.parseInt(flag.toString())) {
            List<HashMap<String, Object>> qIds = (List<HashMap<String, Object>>) map.get("qIds");
            // 添加试题
            qIds.stream().forEach(e -> {
                boolean b = list.stream().anyMatch(record -> record.getQuestionId().equals(e.get("id")));
                if (!b) {
                    CourseQuestionRel c = new CourseQuestionRel();
                    c.setQuestionId(Long.valueOf(String.valueOf(e.get("id"))));
                    c.setCourseId(Long.valueOf(String.valueOf(courseId)));
                    c.setType(Integer.parseInt(String.valueOf(type)));
                    c.setScore(1d);
                    c.setSort(j + ll.size() + 1);
                    ll.add(c);
                }
            });
        }else {
            //排序试题
            List<Integer> qIds = (List<Integer>)map.get("qIds");
            for (int k = 0; k < qIds.size() ; k++) {
                Long aLong = Long.valueOf(qIds.get(k)+"");
                CourseQuestionRel courseQuestionRel = list.stream().filter(record -> record.getQuestionId().equals(aLong)).findFirst().get();
                courseQuestionRel.setSort(k+1);
                ll.add(courseQuestionRel);
            }
        }
        courseQuestionRelService.saveOrUpdateBatch(ll);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", null);
    }
    @ApiOperation(value = "删除试题，type=0 课堂练习、1 课后练习")
    @RequestMapping(value = "delQIdRel",method = RequestMethod.POST)
    public ApiResult delQIdRel(@RequestParam Long courseId, @RequestParam Long qId,@RequestParam Integer type){
        QueryWrapper<CourseQuestionRel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id",courseId);
        queryWrapper.eq("question_id",qId);
        queryWrapper.eq("type",type);
        courseQuestionRelService.remove(queryWrapper);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", null);
    }

    @ApiOperation(value = "管理者获取课程管理列表")
    @RequestMapping(value = "getAllCourses",method = RequestMethod.GET)
    public ApiResult getAllCourses(@RequestParam String startTime){
        List<Date> week = DateConvertUtils.getWeek(startTime);
        Date d1 = week.get(0);
        Date d2 = week.get(week.size()-1);
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        //是否超级管理员
        boolean b = UserUtils.ifAdmin();
        QueryWrapper<CourseStudy> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("start_time",d1,d2);
        queryWrapper.orderByAsc("start_time");
        List<CourseStudy> list = new ArrayList<>();
        /*if(!b){
            queryWrapper.eq("creator",loginAppUser.getId());
        }*/
        list = courseStudyService.list(queryWrapper);
        List<Map<String,List<CourseStudy>>> map = getObjectListMap(list);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", map);
    }

    @ApiOperation(value = "教员获取我的授课列表")
    @RequestMapping(value = "getTeacherCourses",method = RequestMethod.GET)
    public ApiResult getTeacherCourses(String startTime){
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        List<Date> week = DateConvertUtils.getWeek(startTime);
        Date d1 = week.get(0);
        Date d2 = week.get(week.size()-1);
        QueryWrapper<CourseStudy> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("start_time",d1,d2);
        queryWrapper.orderByAsc("id");
        List<CourseStudy> list = new ArrayList<>();
        //是否是教员
        Set<SysRole> rolesByUserId = sysDepartmentFeign.findRolesByUserId(loginAppUser.getId());
        boolean b1 = rolesByUserId.stream().anyMatch(e -> e.getCode().equals(ExamConstants.SYSTEM_TEACHER));
        if(b1){
            queryWrapper.eq("teacher_id",loginAppUser.getId());
            list = courseStudyService.list(queryWrapper);
        }

        List<Map<String,List<CourseStudy>>> objectListMap = getObjectListMap(list);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", objectListMap);
    }

    @ApiOperation(value = "学员获取我的课程列表")
    @RequestMapping(value = "getStudentCourses",method = RequestMethod.GET)
    public ApiResult getStudentCourses(String startTime){
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        List<Date> week = DateConvertUtils.getWeek(startTime);
        Date d1 = week.get(0);
        Date d2 = week.get(week.size()-1);
        List<CourseStudy> courseByStudentId = courseStudyService.findCourseByStudentId(d1, d2, loginAppUser.getId());
        courseByStudentId.stream().forEach(e->{
            QueryWrapper<CourseUserRel> qw = new QueryWrapper<>();
            qw.eq("user_id",loginAppUser.getId());
            qw.eq("course_id",e.getId());
            CourseUserRel one = courseUserRelService.getOne(qw);
            e.setUserStatus(one.getStatus());
        });
        List<Map<String,List<CourseStudy>>> objectListMap = getObjectListMap(courseByStudentId);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", objectListMap);
    }
    private List<Map<String,List<CourseStudy>>>  getObjectListMap(List<CourseStudy> list) {
        List<Map<String,List<CourseStudy>>> ls  =  new ArrayList();

        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
        list.stream().forEach(e->{
            e.setCourseTime(getCourseExtTime(e.getStartTime(),e.getEndTime()));
            AppUser appUserById = sysDepartmentFeign.findAppUserById(e.getTeacherId());
            e.setTeacherName(ObjectUtil.isNull(appUserById)?"":appUserById.getNickname());
        });
        Map<Object,List<CourseStudy>> map = MapUtil.newHashMap(true);
        List<String>  ll = new ArrayList<>();
        for (int i = 1;i<8;i++) {
            List<CourseStudy>  morning = new ArrayList<>();
            List<CourseStudy>  afternoon = new ArrayList<>();
            Map<String,List<CourseStudy>> mp = MapUtil.newHashMap(true);
            mp.put("morning",morning);
            mp.put("afternoon",afternoon);
            ls.add(mp);
           /* map.put(sd.format(week.get(i-1)),new ArrayList<CourseStudy>());
            ll.add(sd.format(week.get(i-1)));*/
        }

        list.stream().forEach(e->{
            Map<String, List<CourseStudy>> stringListMap;
            boolean b = ifMorning(e.getStartTime());
            int dayOfWeek = DateConvertUtils.getDayOfWeek(e.getStartTime());
            if(dayOfWeek==0){
                stringListMap = ls.get(6);
            }else {
                stringListMap = ls.get(dayOfWeek-1);
            }
            if(b){
                stringListMap.get("morning").add(e);
            }else {
                stringListMap.get("afternoon").add(e);
            }


            /*if(dayOfWeek==0){
                map.get(ll.get(ll.size()-1)).add(e);
            }else {
                map.get(ll.get(dayOfWeek-1)).add(e);
            }*/
        });
        return ls;
    }

    public boolean ifMorning(Date startTime){
        boolean flag = true;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        int i = calendar.get(Calendar.HOUR_OF_DAY);
        if(i>=12) {
            flag = false;
        }
        return flag;
    }
    public String getCourseExtTime(Date startTime,Date endTime){
        return DateConvertUtils.getHourAndMin(startTime)+"~"+DateConvertUtils.getHourAndMin(endTime);
    }

    @ApiOperation("删除课程")
    @RequestMapping(value = "delCourseById",method = RequestMethod.GET)
    public ApiResult  delCourseById(@RequestParam Long id){
        courseStudyService.removeById(id);
        QueryWrapper<CourseUserRel> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("course_id",id);
        courseUserRelService.remove(queryWrapper1);
        QueryWrapper<CourseQuestionRel> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("course_id",id);
        courseQuestionRelService.remove(queryWrapper2);
        QueryWrapper<CourseKpRel> queryWrapper3 = new QueryWrapper<>();
        queryWrapper3.eq("course_id",id);
        courseKpRelService.remove(queryWrapper3);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", id);
    }

    @ApiOperation("更改课程状态")
    @RequestMapping(value = "updateCourseStatuById",method = RequestMethod.GET)
    public ApiResult  updateCourseStatuById(@RequestParam Long id,@RequestParam Integer status){
        CourseStudy byId = courseStudyService.getById(id);
        if(1==status && System.currentTimeMillis()<byId.getStartTime().getTime()){
            throw new IllegalArgumentException("还未到上课时间。");
        }
        //只有教师或者具有管理员权限用户能够更改课程状态
        if(UserUtils.ifAdmin() || AppUserUtil.getLoginAppUser().getId().equals(byId.getTeacherId())){
            byId.setStatus(status);
            byId.setUpdateTime(new Date());
            courseStudyService.saveOrUpdate(byId);
        }

        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "改变课程状态成功", id);
    }

    @ApiOperation("课堂练习预览或者课后练习预览 type=0课堂 1 课后")
    @RequestMapping(value = "getBeforeDetails",method = RequestMethod.GET)
    public ApiResult getBeforeDetails(Long courseId, Integer type){
        LoginAppUser loginAppUser = sysDepartmentFeign.getLoginAppUser();
        QueryWrapper<CourseQuestionRel> questionRelQueryWrapper = new QueryWrapper<>();
        questionRelQueryWrapper.eq("course_id",courseId);
        questionRelQueryWrapper.eq("type",type);
        questionRelQueryWrapper.orderByAsc("sort");
        List<CourseQuestionRel> list = courseQuestionRelService.list(questionRelQueryWrapper);
        List<Question> ll = new ArrayList<>();
        list.stream().forEach(record->{
            Question byId = questionService.getById(record.getQuestionId());
            if(byId.getType()==4){
                JSONObject jsonObject = JSONObject.parseObject(byId.getOptions());
                int size = jsonObject.size();
                String[] str = new String[size];
                JSONObject js = new JSONObject();
                js.put("text",str);
                byId.setStuAnswer(js.toJSONString());
            }
            byId.setScore(record.getScore());
            byId.setStuAnswer(byId.getOptions());
            // 增加是否收藏逻辑
            QueryWrapper<CollectionQuestion> collectionQuestionQueryWrapper = new QueryWrapper<>();
            collectionQuestionQueryWrapper.eq("user_id", loginAppUser.getId());
            collectionQuestionQueryWrapper.eq("question_id", record.getQuestionId());
            CollectionQuestion collectionQuestion = collectionQuestionService.getOne(collectionQuestionQueryWrapper);
            long collectionId = collectionQuestion == null ? 0l : collectionQuestion.getId();
            byId.setCollectionId(collectionId);
            byId.setFileAddr(fileServer);
            byId.setLocalUrlPrefix(localUrlPrefix);
            ll.add(byId);
        });
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取成功", ll);
    }
    @ApiOperation("学员点击进去课后练习")
    @RequestMapping(value = "getAfterCourseQuestions",method = RequestMethod.GET)
    public ApiResult  getAfterCourseQuestions(@RequestParam Long courseId){
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        //List objects = redisUtils.lGet("course:"+courseId+":"+AppUserUtil.getLoginAppUser(), 0, -1);
        //if (!Validator.isEmpty(objects)) {
        //    return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "预览试题成功。。。", objects.get(0));
        //}
        QueryWrapper<CourseQuestionRel> questionRelQueryWrapper = new QueryWrapper<>();
        questionRelQueryWrapper.eq("course_id",courseId);
        questionRelQueryWrapper.eq("type",ExamConstants.COURSE_QUESTION_AFTER);
        questionRelQueryWrapper.orderByAsc("sort");
        List<CourseQuestionRel> list = courseQuestionRelService.list(questionRelQueryWrapper);
        List<CourseStudentAnswerVO> ll = new ArrayList<>();
        list.stream().forEach(e->{
            CourseStudentAnswerVO vo = new CourseStudentAnswerVO();
            Question q = questionService.getById(e.getQuestionId());
            vo.setQuestionId(q.getId());
            vo.setQuestion(q.getQuestion());
            vo.setOptions(q.getOptions());
            vo.setType(q.getType());
            vo.setQuestionScore(e.getScore()==null?1:e.getScore());
            vo.setDifficulty(q.getDifficulty());
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
            // 增加是否收藏逻辑
            QueryWrapper<CollectionQuestion> collectionQuestionQueryWrapper = new QueryWrapper<>();
            collectionQuestionQueryWrapper.eq("user_id", loginAppUser.getId());
            collectionQuestionQueryWrapper.eq("question_id", vo.getQuestionId());
            CollectionQuestion collectionQuestion = collectionQuestionService.getOne(collectionQuestionQueryWrapper);
            long collectionId = collectionQuestion == null ? 0L : collectionQuestion.getId();
            vo.setCollectionId(collectionId);
            vo.setLocalUrlPrefix(localUrlPrefix);
            vo.setFileAddr(fileServer);
            ll.add(vo);
        });
        redisUtils.lSet("course:" + courseId , ll,ExamConstants.EXAM_OTHER_DETAILS_TIME7);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", ll);
    }
    /**
     * 定时将学员考试结果存入redis
     */
    @ApiOperation(value = "定时保存学员考试过程", notes = "保存学员考试过程缓存信息")
    @RequestMapping(value = "/saveStudentAnswer", method = RequestMethod.POST)
    public ApiResult saveStudentAnswer(@RequestParam Long courseId, @RequestBody List<CourseStudentAnswerVO> courseStudentAnswerVO) {

        for (CourseStudentAnswerVO vo : courseStudentAnswerVO) {
            Question byId = questionService.getById(vo.getQuestionId());
            vo.setStuAnswer(vo.getStuAnswers().toString());
            vo.setCourseId(courseId);
            vo.setStudentId(AppUserUtil.getLoginAppUser().getId());
            vo.setPaperType(byId.getType());
            vo.setQuestion(byId.getQuestion());
            vo.setOptions(byId.getOptions());
        }
        redisUtils.delKeys("course:" + courseId+":"+AppUserUtil.getLoginAppUser().getId());
        redisUtils.lSet("course:" + courseId+":"+AppUserUtil.getLoginAppUser().getId(), courseStudentAnswerVO,ExamConstants.EXAM_STUDENT_ANSWER_DETAILS_TIME);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "提交成功。", null);
    }

    @ApiOperation("学生提交课后练习答案或者保存学生答题记录")
    @RequestMapping(value = "finishCourseStudentAnswer",method = RequestMethod.POST)
    public ApiResult finishCourseStudentAnswer(@RequestBody Map<String,Object> hashMap){

        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        Long courseId = Long.valueOf(String.valueOf(hashMap.get("courseId")));
        QueryWrapper<CourseUserRel> qw = new QueryWrapper<>();
        qw.eq("course_id",courseId);
        qw.eq("user_id",loginAppUser.getId());
        CourseUserRel cr = courseUserRelService.getOne(qw);
        if(cr.getStatus().equals(ExamConstants.COURSE_AFTER_SUBMIT)){
            throw new IllegalArgumentException("您已提交答案，请勿再重复提交");
        }
        List<Map<String,Object>> courseStudentAnswerVOList = (List<Map<String,Object>>)hashMap.get("courseStudentAnswerVOList");
        double totalScore = 0L;
        Map<Object,Object> map = new LinkedHashMap<>();
        List<StudentAnswerDetails> list2 = new ArrayList();
        list2.add(new StudentAnswerDetails());
        list2.add(new StudentAnswerDetails());
        list2.add(new StudentAnswerDetails());
        map.put("list",list2);
        //map.put("name",exam.getName());
        for (Map<String,Object> map2: courseStudentAnswerVOList) {
            CourseStudentAnswerVO courseStudentAnswerVO = JSON.parseObject(JSON.toJSONString(map2),CourseStudentAnswerVO.class);

            StudentAnswerVO studentAnswerVO = new StudentAnswerVO();
            BeanUtils.copyProperties(courseStudentAnswerVO,studentAnswerVO);
            CourseStudentAnswer csa = new CourseStudentAnswer();
            csa.setQuestionId(courseStudentAnswerVO.getQuestionId());
            csa.setStudentId(loginAppUser.getId());
            csa.setCourseId(courseId);
            csa.setPaperType(ExamConstants.PAPER_LILUN);
            csa.setStuAnswer(courseStudentAnswerVO.getStuAnswer().toString());
            csa.setType(courseStudentAnswerVO.getType());
            //courseStudentAnswerVO.setStuAnswer(courseStudentAnswerVO.getStuAnswer().toString());
            studentAnswerVO.setStuAnswer(courseStudentAnswerVO.getStuAnswer().toString());
            studentAnswerVO.setQuestionId(courseStudentAnswerVO.getQuestionId());
            studentAnswerVO.setQuestionScore(map2.get("questionScore")==null?0:Double.parseDouble(String.valueOf(map2.get("questionScore"))));
            csa.setCreateTime(new Date());
            try{
                double v = HandleAnswerUtils.judgeStudentAnswer(studentAnswerVO,map);
                totalScore += v;
                csa.setActualScore(v);
            }catch (Exception e){
                e.printStackTrace();
                logger.error("考生提交答案后判断客观题失败。。。");
            }
            courseStudentAnswerService.save(csa);
            QueryWrapper<CourseUserRel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("course_id",courseId);
            queryWrapper.eq("user_id",loginAppUser.getId());
            CourseUserRel courseUserRel = courseUserRelService.getOne(queryWrapper);
            courseUserRel.setScore(totalScore);
            courseUserRel.setStatus(ExamConstants.COURSE_AFTER_SUBMIT);
            courseUserRelService.updateById(courseUserRel);
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", null);
    }

    @ApiOperation("获取批改作业列表status=1 等待批阅 2=批阅完成")
    @RequestMapping(value = "getStudentWorkList",method = RequestMethod.GET)
    public ApiResult getStudentWorkList(@RequestParam Long courseId){
        CourseStudy courseStudy = courseStudyService.getById(courseId);
        String s = DateConvertUtils.assembleTime(courseStudy.getStartTime(), courseStudy.getEndTime());
        QueryWrapper<CourseUserRel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id",courseId);
        queryWrapper.ne("status",0);
        List<CourseUserRel> ll = courseUserRelService.list(queryWrapper);
        ll.stream().forEach(e->{
            e.setUserName(courseStudy.getName());
            e.setCourseTime(s);
            e.setUserName(sysDepartmentFeign.findAppUserById(e.getUserId()).getNickname());
        });
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", ll);
    }

    @ApiOperation(value = "教员批阅", notes = "教员批阅")
    @RequestMapping(value = "/judgeStudentAnswer", method = RequestMethod.POST)
    public synchronized ApiResult judgeStudentAnswer(@RequestBody Map<String, Object> hashMap) {
        //@RequestParam Long courseId,@RequestParam Long userId, @RequestBody List<CourseStudentAnswerVO> list
        Long id = Long.valueOf(String.valueOf(hashMap.get("id")));
        //Long userId = Long.valueOf(String.valueOf(hashMap.get("userId")));
        List<Map<String, Object>> list = (List<Map<String, Object>>) hashMap.get("list");
        Double totalScore = 0d;
        /*QueryWrapper wq = new QueryWrapper();
        wq.eq("course_id", courseId);
        wq.eq("user_id", userId);*/
        CourseUserRel dr = courseUserRelService.getById(id);
        if (dr.getStatus().equals(ExamConstants.COURSE_AFTER_FINISH)) {
            throw new IllegalArgumentException("批阅已完成，请勿重复批阅。");
        }

        for (Map<String, Object> map2 : list) {
            CourseStudentAnswerVO st = JSON.parseObject(JSON.toJSONString(map2), CourseStudentAnswerVO.class);
            CourseStudentAnswer byId = courseStudentAnswerService.getById(st.getId());
            QueryWrapper<Question> qw = new QueryWrapper<>();
            qw.eq("id", st.getQuestionId());
            Question one = questionService.getOne(qw);
            Integer type = one.getType();
            if (type == 1 || type == 3 || type == 2 || type == 4) {
                if (!Validator.isNull(st.getJudgeRemark())) {
                    if (st.getFlag()) {
                        byId.setActualScore(0d);
                    } else {
                        byId.setActualScore(st.getQuestionScore());
                    }
                } else {
                    if (st.getFlag()) {
                        byId.setActualScore(st.getActualScore());
                    } else {
                        byId.setActualScore(0d);
                    }
                }
            } else {
                byId.setActualScore(st.getActualScore());
            }
            totalScore += st.getActualScore();
            byId.setJudgeRemark(st.getJudgeRemark());
            byId.setCourseId(dr.getCourseId());
            byId.setUpdateTime(new Date());
            byId.setStudentId(dr.getUserId());
            byId.setActualScore(st.getActualScore());
            courseStudentAnswerService.saveOrUpdate(byId);
        }
        dr.setScore(totalScore);
        dr.setStatus(ExamConstants.COURSE_AFTER_FINISH);
        courseUserRelService.saveOrUpdate(dr);
        QueryWrapper wq = new QueryWrapper();
        wq.eq("course_id", dr.getCourseId());
        List<CourseUserRel> userList = courseUserRelService.list(wq);
        if(userList.stream().allMatch(e->e.getStatus().equals(ExamConstants.COURSE_AFTER_FINISH))){
            CourseStudy courseStudy = courseStudyService.getById(dr.getCourseId());
            courseStudy.setStatus(ExamConstants.COURSE_FINISH);
            courseStudyService.saveOrUpdate(courseStudy);
        }

        // 错题收录
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("studentId", dr.getUserId());
        errorMap.put("courseId", dr.getCourseId());
        List<Map<String, Object>> errorList = courseUserRelDao.errorList(errorMap);
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
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "批阅完成。。。", null);
    }
    public Set<SysRole> getUserRole(){
        Set<SysRole> rolesByUserId = sysDepartmentFeign.findRolesByUserId(AppUserUtil.getLoginAppUser().getId());
        return rolesByUserId;
    }

    /**
     * 查看课程阅卷详情
     * @param id
     * @return
     */
    @ApiOperation(value = "获取考生活动的阅卷详情")
    @ApiImplicitParam(name = "id",value = "作业列表id",dataType = "Long")
    @RequestMapping(value = "/getJudgeDetails",method = RequestMethod.GET)
    public ApiResult getJudgeDetails(Long id){
        CourseUserRel one = courseUserRelService.getById(id);
        Long studentId = one.getUserId();
        Long courseId = one.getCourseId();
        QueryWrapper<CourseStudentAnswer> qw1 = new QueryWrapper<>();
        qw1.eq("student_id",studentId);
        qw1.eq("course_id",courseId);
        List<CourseStudentAnswerVO> ll  = new ArrayList<>();
        List<CourseStudentAnswer> list = courseStudentAnswerService.list(qw1);
        for(CourseStudentAnswer stu:list){
            CourseStudentAnswerVO sa = new CourseStudentAnswerVO();
            BeanUtils.copyProperties(stu,sa);
            Question byId = questionService.getById(stu.getQuestionId());
            sa.setQuestion(byId.getQuestion());
            sa.setOptions(byId.getOptions());
            QueryWrapper<CourseQuestionRel> qw2 = new QueryWrapper<>();
            qw2.eq("course_id",courseId);
            qw2.eq("question_id",byId);
            Double questionScoreById = courseQuestionRelService.getOne(qw2).getScore();
            sa.setQuestionScore(questionScoreById);
            JSONObject  js = new JSONObject();
            js.put("text","");
            js.put("url","");
            sa.setKeywords(byId.getKeywords()==null?js.toJSONString():byId.getKeywords());
            sa.setAnalysis(byId.getAnalysis()==null?js.toJSONString():byId.getAnalysis());
            sa.setAnswer(byId.getAnswer()==null?js.toJSONString():byId.getAnswer());

            sa.setActualScore(stu.getActualScore());
            if(stu.getType() == 4){
                String answer = byId.getAnswer();
                List s = new LinkedList();
                LinkedHashMap<String, String> jsonmap = JSON.parseObject(answer, new TypeReference<LinkedHashMap<String, String>>() {
                });
                for (Map.Entry<String,String> entry:jsonmap.entrySet()) {
                    s.add(entry.getValue());
                }
                JSONObject  jso = new JSONObject(true);
                jso.put("text",s.toArray());
                sa.setAnswer(jso.toJSONString());
            }
            if(Validator.isEmpty(stu.getActualScore()) || stu.getActualScore()==0 ){
                sa.setFlag(false);
            }else if (stu.getActualScore()>0){
                sa.setFlag(true);
            }
            sa.setTotalScore(one.getScore());
            ll.add(sa);
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取用户考试信息成功。。。", ll);
    }

    @ApiOperation(value = "点击阅卷进入阅卷页面", notes = "点击阅卷")
    @RequestMapping(value = "/getStudentAnswerDetails", method = RequestMethod.GET)
    public ApiResult getStudentAnswerDetails(@RequestParam Long id,@RequestParam Integer flag) {
        Long courseUserRelId ;
        if(flag==0){
            //教师阅卷课后练习
            courseUserRelId = id;
        }else {
            //学生查看批改情况
            //CourseStudy byId = courseStudyService.getById(id);
            QueryWrapper<CourseUserRel> q = new QueryWrapper<>();
            q.eq("user_id",AppUserUtil.getLoginAppUser().getId());
            q.eq("course_id",id);
            CourseUserRel one = courseUserRelService.getOne(q);
            courseUserRelId = one.getId();
        }
        CourseUserRel one = courseUserRelService.getById(courseUserRelId);
        Long studentId = one.getUserId();
        Long courseId = one.getCourseId();
        QueryWrapper<CourseStudentAnswer> qw1 = new QueryWrapper<>();
        qw1.eq("student_id",studentId);
        qw1.eq("course_id",courseId);
        List<CourseStudentAnswerVO> ll  = new ArrayList<>();
        List<CourseStudentAnswer> list = courseStudentAnswerService.list(qw1);
        for (CourseStudentAnswer studentAnswer : list) {
            CourseStudentAnswerVO vo = new CourseStudentAnswerVO();
            BeanUtils.copyProperties(studentAnswer, vo);
            Question byId = questionService.getById(vo.getQuestionId());
            vo.setQuestion(byId.getQuestion());
            vo.setOptions(byId.getOptions());
            JSONObject js = new JSONObject();
            js.put("text", "");
            js.put("url", "");
            QueryWrapper<CourseQuestionRel> qw = new QueryWrapper<>();
            qw.eq("course_id", courseId);
            qw.eq("question_id", vo.getQuestionId());
            CourseQuestionRel paperManageRel = courseQuestionRelService.getOne(qw);
            vo.setKeywords(byId.getKeywords() == null ? js.toJSONString() : byId.getKeywords());
            vo.setAnalysis(byId.getAnalysis() == null ? js.toJSONString() : byId.getAnalysis());
            vo.setAnswer(byId.getAnswer() == null ? js.toJSONString() : byId.getAnswer());

            if(studentAnswer.getType() == 4){
                String answer = byId.getAnswer();
                List s = new LinkedList();
                LinkedHashMap<String, String> jsonmap = JSON.parseObject(answer, new TypeReference<LinkedHashMap<String, String>>() {
                });
                for (Map.Entry<String,String> entry:jsonmap.entrySet()) {
                    s.add(entry.getValue());
                }
                JSONObject  jso = new JSONObject(true);
                jso.put("text",s.toArray());
                vo.setAnswer(jso.toJSONString());
            }
            if(Validator.isEmpty(studentAnswer.getActualScore()) || studentAnswer.getActualScore()==0 ){
                vo.setFlag(false);
            }else if (studentAnswer.getActualScore()>0){
                vo.setFlag(true);
            }
            vo.setTotalScore(one.getScore());
            vo.setQuestionScore(paperManageRel.getScore());
            if (studentAnswer.getActualScore() != null && studentAnswer.getActualScore() > 0) {
                vo.setFlag(true);
            }
            // 增加是否收藏逻辑
            QueryWrapper<CollectionQuestion> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", vo.getStudentId());
            queryWrapper.eq("question_id", vo.getQuestionId());
            CollectionQuestion collectionQuestion = collectionQuestionService.getOne(queryWrapper);
            long collectionId = collectionQuestion == null ? 0L: collectionQuestion.getId();
            vo.setCollectionId(collectionId);
            ll.add(vo);
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取考生答案成功。。。", ll);

    }

    @ApiOperation("查询课程相关的知识")
    @RequestMapping(value = "listKnowledgePageByCourse",method = RequestMethod.GET)
    public ApiResult listKnowledgePageByCourse(Long courseId){
        Integer page = 1;
        Integer size = 100;
        QueryWrapper<CourseKpRel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id",courseId);
        List<CourseKpRel> list1 = courseKpRelService.list(queryWrapper);
        Set set = new HashSet();
        list1.stream().forEach(e->set.add(e.getSensesId()));
        Long id = AppUserUtil.getLoginAppUser().getId();
        List<Map> ll = new ArrayList<>();

        Map map = modelFeign.listKnowledgePage(1, 100, Integer.parseInt(id + ""),"");
        Map<String,Object> data = (Map<String,Object>)map.get("data");
        Map<String,Object> res =(Map<String,Object>) data.get("result");
        List<Map> list = (List) res.get("records");
        for (Map m:list) {
            if(set.contains(m.get("sensesId"))){
                ll.add(m);
            }
        }
        Map mp = new HashMap();
        mp.put("current",page);
        mp.put("size",size);
        int pages = ll.size()%size==0?ll.size()/size:(ll.size()/size)+1;
        mp.put("pages",pages);
        mp.put("total",ll.size());
        if( size*(page-1)>= ll.size()){
            mp.put("records",new ArrayList<>());
        }else{
            mp.put("records",ll.subList(size*(page-1),(size*page>ll.size()?ll.size():(size*page))));
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取成功", mp);
    }

    @ApiOperation(value = "复制课程")
    @RequestMapping(value = "copyCourseStudy1",method = RequestMethod.GET)
    public ApiResult copyCourseStudy1(Long id) {

        CourseStudy courseStudyDetail = courseStudyService.getById(id);
        try {
            CourseStudy courseStudy = new CourseStudy();
            BeanUtils.copyProperties(courseStudyDetail, courseStudy, "id", "createTime");
            courseStudy.setCreateTime(new Date());
            courseStudyService.save(courseStudy);
            CourseStudy b = courseStudyService.copyCourse(courseStudyDetail, courseStudy.getId());
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "复制成功", null);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "复制失败", null);
        }

    }
    @ApiOperation(value = "复制课程")
    @RequestMapping(value = "copyCourseStudy", method = RequestMethod.GET)
    public ApiResult copyCourseStudy (Long id){

        try {
            CourseStudy courseStudyDetail = courseStudyService.getById(id);
            courseStudyService.copyCourse1(courseStudyDetail);
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "复制成功", null);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "复制失败", null);
        }
    }


    @RequestMapping(value = "getKpByCourseId",method = RequestMethod.GET)
    public Set<Long> getKpByCourseId(Long courseId){
        Set<Long> set = new HashSet<>();
        QueryWrapper<CourseKp> courseKpQueryWrapper = new QueryWrapper<>();
        courseKpQueryWrapper.eq("course_id",courseId);
        List<CourseKp> list = courseKpService.list(courseKpQueryWrapper);
        list.stream().forEach(e->set.add(e.getKpId()));
        return set ;
    }

}
