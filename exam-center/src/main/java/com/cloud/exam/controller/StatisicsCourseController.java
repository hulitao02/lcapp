package com.cloud.exam.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ExamConstants;
import com.cloud.exam.model.course.CourseStudy;
import com.cloud.exam.service.CourseStudyService;
import com.cloud.exam.service.CourseUserRelService;
import com.cloud.model.utils.AppUserUtil;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by dyl on 2022/02/10.
 * 统计课程数量
 */
@RequestMapping("CourseStudy")
@RestController
public class StatisicsCourseController {

    private final static Logger logger = LoggerFactory.getLogger(StatisicsCourseController.class);

    @Autowired
    private CourseUserRelService courseUserRelService;
    @Autowired
    private CourseStudyService courseStudyService;
    @ApiOperation("获取用户每年每个月已完成的上课数量")
    @RequestMapping(value = "getCourseCountByUser",method = RequestMethod.GET)
    public ApiResult getCourseCountByUser(String paramsDate,Long userId){
        if(ObjectUtil.isEmpty(userId)){
            userId = AppUserUtil.getLoginAppUser().getId();
        }
        List<Integer> list1 = courseUserRelService.getPerMonthCountByUser(paramsDate,userId);
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取成功", list1);
    }

    @ApiOperation("获取每年每个月已完成的上课数量和未上课的数量")
    @RequestMapping(value = "getAllCourseCount",method = RequestMethod.GET)
    public ApiResult getAllCourseCount(String paramsDate){

        try {
            SimpleDateFormat sd1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = paramsDate+"-01-01 00:00:00";
            Date parse = sd1.parse(startTime);
            String endTime = paramsDate+"-12-31 24:00:00";
            Date parse1 = sd1.parse(endTime);
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.between("start_time",parse,parse1);
            List<CourseStudy> list = courseStudyService.list(queryWrapper);
            //long count = list.stream().filter(e -> e.getStatus().equals(ExamConstants.COURSE_AFTER_INIT)).count();
            List<Map<String,Integer>> list1 = new ArrayList();
            for (int i = 0; i < 12; i++) {
                Map<String,Integer> map = new HashMap();
                map.put("ns",0);
                map.put("ys",0);
                list1.add(map);
            }
            //统计每月中上课和未上课的数量
            list.parallelStream().forEach(e->{

                Date st = e.getStartTime();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(st);
                int i = calendar.get(Calendar.MONTH)+1;
                Map<String, Integer> imap = list1.get(i - 1);
                Integer ns = imap.get("ns");
                Integer ys = imap.get("ys");
                if(e.getStatus().equals(ExamConstants.COURSE_AFTER_INIT)){
                    ns++;
                }else {
                    ys++;
                }
                imap.put("ns",ns);
                imap.put("ys",ys);
            });
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "获取成功", list1);
        } catch (ParseException e) {
            logger.error("获取课程数量失败",e.getMessage());
            return ApiResultHandler.buildApiResult(500, "获取失败", null);
        }
    }
}
