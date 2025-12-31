package com.cloud.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ExamConstants;
import com.cloud.exam.model.exam.DrawResult;
import com.cloud.exam.service.DrawResultService;
import com.cloud.exception.ResultMesCode;
import com.cloud.redislock.RedisLock;
import com.cloud.utils.RedisUtils;
import com.cloud.utils.Validator;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyl on 2021/07/08.
 * 抢答处理类
 */
@RestController
@RequestMapping("/racequestion")
public class RaceQuestionController {

    @Autowired
    private RedisLock redisLock;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private DrawResultService drawResultService;

    public static final String acesskey = ExamConstants.race_question_key_prefix;

    @ApiOperation(value = "多个用户同时抢答获取答题权限")
    @RequestMapping("/getQuestionAcessKey")
    public ApiResult getQuestionAcessKey(Long examId, Long paperId, Long userId, Long questionId){
        Object  o = redisUtils.get("competition:race:"+examId + ":" + questionId);
        if(!Validator.isEmpty(o) && !o.equals(userId)){
            return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_RACE_ACCESSKEYFAIL.getResultCode(),"抢答失败",false);
        }
        //对当前抢答的试题进行抢锁
        boolean b = redisLock.tryLock( examId + "&" + paperId + "&"+questionId+acesskey);
        if(b) {
            //加锁成功,通知抢到成功的小组，并把信息暂存到redis
            redisUtils.set("competition:race:"+examId + ":" + questionId,userId,1*60*60);
            return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),"抢答成功",true);
        }else{
            //加锁失败则直接返回
            return ApiResultHandler.buildApiResult(ResultMesCode.EXAM_RACE_ACCESSKEYFAIL.getResultCode(),"抢答失败",false);
        }
    }
    @ApiOperation(value = "多个用户同时抢答获取答题权限")
    @RequestMapping("/getAcesskeyTest")
    public ApiResult getAcessKeyTest(){

        System.out.println(redisLock);
        boolean b = redisLock.tryLock( "test:test" + acesskey);
        System.out.println(Thread.currentThread().getName());
        if(b) {
            //加锁成功,通知抢到成功的小组
            return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),b+Thread.currentThread().getName(),null);
        }else{
            //加锁失败则直接返回
            return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),b+Thread.currentThread().getName(),null);
        }
    }

    @ApiOperation(value = "开始抢答发送用户id")
    @RequestMapping("/getUserIds2host")
    public ApiResult getUserIds2host(Long examId){
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("ac_id",examId);
        List<DrawResult> list = drawResultService.list(queryWrapper);
        List<Long> ll = new ArrayList<>();
        list.stream().forEach(e->ll.add(e.getUserId()));
        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(),"",ll);
    }
}
