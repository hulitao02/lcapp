package com.cloud.exam.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ExamConstants;
import com.cloud.exam.model.course.CourseStudy;
import com.cloud.exam.service.CourseStudyService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyl on 2021/5/15.
 */
@Component
public class UpdateCoureStudyStatus {


    public static final Logger logger = LoggerFactory.getLogger(UpdateCoureStudyStatus.class);
    @Autowired
    private CourseStudyService courseStudyService;
    /**
     * 课程开始时间一到 修改课程状态为上课开始
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void startCourseStudy(){

        try {
//            QueryWrapper<CourseStudy>  qw = new QueryWrapper<CourseStudy>();
//            List<Integer>  ll  = new ArrayList<>();
//            ll.add(ExamConstants.COURSE_AFTER_INIT);
//            ll.add(ExamConstants.COURSE_AFTER_SUBMIT);
//            qw.in("status", ll);
//            List<CourseStudy> objects =courseStudyService.list(qw);
//            if(CollectionUtils.isNotEmpty(objects)){
//                objects.stream().forEach(e->{
//                    if(e.getStatus().equals(ExamConstants.COURSE_AFTER_INIT)){
//                        if(e.getStartTime().getTime()<=System.currentTimeMillis() && e.getEndTime().getTime()>System.currentTimeMillis()){
//                            e.setStatus(ExamConstants.COURSE_STARTING);
//                        }else if(e.getEndTime().getTime()<=System.currentTimeMillis()){
//                            e.setStatus(ExamConstants.COURSE_ENDING);
//                        }
//                    }else if(e.getStatus().equals(ExamConstants.COURSE_AFTER_SUBMIT)){
//                        if(e.getEndTime().getTime()<=System.currentTimeMillis()){
//                            e.setStatus(ExamConstants.COURSE_ENDING);
//                        }
//                    }
//                });
//                courseStudyService.saveOrUpdateBatch(objects);
//            }
        }catch (Exception e){
            logger.error("定时修改课程状态失败。。",e);
        }
    }

}
