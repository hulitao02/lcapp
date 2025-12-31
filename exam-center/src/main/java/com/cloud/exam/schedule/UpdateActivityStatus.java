package com.cloud.exam.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ExamConstants;
import com.cloud.exam.model.exam.DrawResult;
import com.cloud.exam.model.exam.Exam;
import com.cloud.exam.service.DrawResultService;
import com.cloud.exam.service.ExamKpPersonAvgScoreService;
import com.cloud.exam.service.ExamService;
import com.cloud.utils.RedisUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyl on 2021/5/15.
 */
@Component
public class UpdateActivityStatus {
    @Resource
    private ExamService examService;
    @Resource
    private DrawResultService drawResultService;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private ExamKpPersonAvgScoreService examKpPersonAvgScoreService;

    /**
     * 活动开始时间一到 修改状态为考试开始
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void startActivity() {
        QueryWrapper<Exam> qw = new QueryWrapper<Exam>();
        List<Integer> ll = new ArrayList<>();
        ll.add(ExamConstants.ACTIVITY_LAUNCH);
        ll.add(ExamConstants.ACTIVITY_START);
        qw.in("exam_status", ll);
        qw.ne("type", 3);
        List<Exam> objects = examService.list(qw);
        objects.stream().forEach(e -> {
            if (e.getStartTime().getTime() <= System.currentTimeMillis()) {
                e.setExamStatus(ExamConstants.ACTIVITY_EXAM_START);
                examService.saveOrUpdate(e);
            }
        });
    }


    /**
     * 活动结束时间一到 修改状态为待阅卷
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void judgeActivity(){
        QueryWrapper<Exam>  qw = new QueryWrapper<Exam>();
        qw.eq("exam_status", ExamConstants.ACTIVITY_EXAM_START);
        //qw.ne("type",ExamConstants.ACTIVITY_WAIT_JUDGE);
        List<Exam> objects =examService.list(qw);
        objects.stream().forEach(e->{
            if(e.getEndTime().getTime()<=System.currentTimeMillis()){
                QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("ac_id",e.getId());
                List<DrawResult> list = drawResultService.list(queryWrapper);
                if(CollectionUtils.isEmpty(list)){
                    e.setExamStatus(ExamConstants.EXAM_CONCELL);
                    examService.saveOrUpdate(e);
                }else {
                    if(list.stream().allMatch(drawResult -> drawResult.getUserStatus() < ExamConstants.ACTIVITY_WAIT_JUDGE) || list.stream().allMatch(drawResult -> drawResult.getUserStatus().equals(ExamConstants.ACTIVITY_FINISH))){
                        e.setExamStatus(ExamConstants.EXAM_FINISH);
                        examService.saveOrUpdate(e);
                        examKpPersonAvgScoreService.calculate(e.getId());
                    }else {
                        e.setExamStatus(ExamConstants.ACTIVITY_WAIT_JUDGE);
                        examService.saveOrUpdate(e);
                    }
                }

            }
        });
    }

    /**
     * 活动结束时间一到 未启动的活动改为取消
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void concellActivity(){
        QueryWrapper<Exam>  qw = new QueryWrapper<Exam>();
        qw.eq("exam_status", ExamConstants.ACTIVITY_NOT_LAUNCH);
        try {
            List<Exam> objects =examService.list(qw);
            objects.stream().forEach(e->{
                if(e.getEndTime().getTime()<=System.currentTimeMillis()){
                    e.setExamStatus(ExamConstants.EXAM_CONCELL);
                    examService.saveOrUpdate(e);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
