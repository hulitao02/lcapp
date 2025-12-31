package com.cloud.exam.controller;


import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.exam.model.ExamKpPersonAvgScore;
import com.cloud.exam.model.eval.*;
import com.cloud.exam.service.ExamKpPersonAvgScoreService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 学员的知识点平均成绩 前端控制器
 * </p>
 *
 * @author tongkesong
 * @since 2023-09-09
 */
@RestController
@RequestMapping
public class ExamKpPersonAvgScoreController {
    @Resource
    private ExamKpPersonAvgScoreService examKpPersonAvgScoreService;

    @GetMapping("/examKpPersonAvgScore/test")
    public ApiResult<List<ExamKpPersonAvgScore>> test(@RequestParam Long examId) {
        List<ExamKpPersonAvgScore> calculate = examKpPersonAvgScoreService.calculate(examId);
        return ApiResultHandler.success(calculate);
    }

    @GetMapping("/eval/personal/score")
    public ApiResult<EvalDto> getPersonalScore() {
        EvalDto evalDto = examKpPersonAvgScoreService.getPersonalScore();
        return ApiResultHandler.success(evalDto);
    }

    @ApiOperation(value = "查询团体能力综合评分", notes = "查询团体能力综合评分")
    @RequestMapping(value = "/eval/dept/score", method = RequestMethod.GET)
    public ApiResult<EvalKpDto> getDeptScore() {
        EvalKpDto evalKpDto = examKpPersonAvgScoreService.getDeptScore();
        return ApiResultHandler.success(evalKpDto);
    }

    @ApiOperation(value = "查询个人能力分布", notes = "可通过知识点筛选")
    @ApiImplicitParam(name = "kpIds", value = "知识点ID数组", required = false, dataType = "Long[]")
    @RequestMapping(value = "/eval/personal/ability", method = RequestMethod.GET)
    public ApiResult<List<EvalKpNewDto>> getPersonalAbility(@RequestParam(value = "kpIds", required = false) Long[] kpIds) {
        List<EvalKpNewDto> evalKpDtoList = examKpPersonAvgScoreService.getPersonalAbility(kpIds);
        return ApiResultHandler.success(evalKpDtoList);
    }

    @ApiOperation(value = "查询个人能力趋势", notes = "年份为筛选条件")
    @ApiImplicitParam(name = "year", value = "查询年份", required = false, dataType = "Integer")
    @RequestMapping(value = "/eval/personal/history", method = RequestMethod.GET)
    public ApiResult<PersonalHistoryDto> getPersonalHisScore(@RequestParam(required = false) Integer year,
                                                             @RequestParam(required = false) Integer month) {
        PersonalHistoryDto result = examKpPersonAvgScoreService.getPersonalHisScore(year, month);
        return ApiResultHandler.success(result);
    }

    @ApiOperation(value = "查询个人能力我的排名", notes = "单位内的评分情况")
    @ApiImplicitParam(name = "kpId", value = "知识点ID", required = false, dataType = "Long")
    @RequestMapping(value = "/eval/personal/dept", method = RequestMethod.GET)
    public ApiResult<List<EvalDeptNewDto>> getPersonalDeptScore(@RequestParam(value = "kpId", required = false) Long kpId) {
        List<EvalDeptNewDto> evalDeptDtoList = examKpPersonAvgScoreService.getPersonalDeptScore(kpId);
        return ApiResultHandler.success(evalDeptDtoList);
    }

//    @GetMapping("/eval/personal/trainSchedule")
//    public ApiResult<List<TrainScheduleDto>> trainSchedule(@RequestParam(required = false) Integer year) {
//        List<TrainScheduleDto> trainScheduleDtos = examKpPersonAvgScoreService.trainSchedule(year);
//        return ApiResultHandler.success(trainScheduleDtos);
//    }

    @ApiOperation(value = "查询团体综合能力", notes = "查询团体综合能力")
    @ApiImplicitParam(name = "kpIds", value = "知识点ID", required = false, dataType = "Long[]")
    @RequestMapping(value = "/eval/dept/kpScore", method = RequestMethod.GET)
    public ApiResult<List<EvalKpNewDto>> getDeptKpScore(@RequestParam(value = "kpIds", required = false) Long[] kpIds) {
        List<EvalKpNewDto> evalKpDtoList = examKpPersonAvgScoreService.getDeptKpScore(kpIds);
        return ApiResultHandler.success(evalKpDtoList);
    }

    @ApiImplicitParam(name = "kpIds", value = "知识点ID", required = false, dataType = "Long[]")
    @RequestMapping(value = "/eval/dept/deptAbility", method = RequestMethod.GET)
    public ApiResult<DeptAbilityDto> deptAbilityBak(@RequestParam(value = "kpIds", required = false) Long[] kpIds,
                                                    @RequestParam(value = "num", required = false) Integer num) {
        DeptAbilityDto deptAbilityDto = examKpPersonAvgScoreService.deptAbilityBak(kpIds, num);
        return ApiResultHandler.success(deptAbilityDto);
    }

    @ApiImplicitParam(name = "kpIds", value = "知识点ID", required = false, dataType = "Long[]")
    @RequestMapping(value = "/eval/dept/deptAbilitybak", method = RequestMethod.GET)
    public ApiResult<DeptAbilityNewDto> deptAbility(@RequestParam(value = "kpIds", required = false) Long[] kpIds,
                                                    @RequestParam(value = "num", required = false) Integer num) {
        DeptAbilityNewDto deptAbilityDto = examKpPersonAvgScoreService.deptAbility(kpIds, num);
        return ApiResultHandler.success(deptAbilityDto);
    }

    @GetMapping(value = "/eval/dept/distribution")
    public ApiResult<List<EvalDeptNewDto>> distribution(@RequestParam(value = "kpId", required = false) Long kpId) {
        List<EvalDeptNewDto> evalDeptNewDtos = examKpPersonAvgScoreService.distribution(kpId);
        return ApiResultHandler.success(evalDeptNewDtos);
    }
}

