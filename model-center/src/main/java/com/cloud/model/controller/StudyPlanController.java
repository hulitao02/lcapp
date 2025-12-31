package com.cloud.model.controller;


import com.alibaba.fastjson.JSON;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ResultMesEnum;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.bean.vo.StudyPlanVo;
import com.cloud.model.bean.vo.es.EsKnowlegdeJsonBean;
import com.cloud.model.model.StudyKnowledge;
import com.cloud.model.model.StudyPlan;
import com.cloud.model.service.StudyPlanService;
import com.cloud.model.utils.AppUserUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tool.PooledHttpClientAdaptor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/studyPlan")
@ApiModel(value = "学习计划")
@RefreshScope
@Slf4j
public class StudyPlanController {
    //    ES 域名
    @Value(value = "${es.domain}")
    private String ES_DOMAIN;
    //   ES 请求
    @Value(value = "${es.X_Access_Token}")
    private String X_Access_Token;
    @Autowired
    private StudyPlanService studyPlanService;
    @Autowired
    private SysDepartmentFeign sysDepartmentFeign;


    @ApiOperation("保存或者更新学习计划")
    @PostMapping("/saveOrUpdateStudyPlan")
    public ApiResult saveStudyPlan(@RequestBody StudyPlan studyPlan) {
        log.info("studyPlan# saveOrUpdateStudyPlan-->  params:{} ", JSON.toJSONString(studyPlan));
        if (Objects.isNull(studyPlan.getId())) {
            studyPlan.setCreateTime(new Date());
            studyPlan.setUpdateTime(new Date());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String format_str = format.format(studyPlan.getPlanStartTime());
            format_str = format_str + " 23:59:59";
            SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date planTimestamp = defaultFormat.parse(format_str);
                studyPlan.setPlanStartTime(planTimestamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            studyPlan.setUpdateTime(new Date());
        }
        try {
            this.studyPlanService.saveStudyPlanAndKpList(studyPlan);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作学习计划异常:{}", e.getMessage());
        }
        return ApiResultHandler.buildApiResult(ResultMesEnum.SUCCESS.getResultCode(), "操作学习计划成功", null);
    }


    @ApiOperation("删除学习计划")
    @GetMapping("/deleteStudyPlanById")
    public ApiResult deleteStudyPlanById(@RequestParam(value = "id", required = true) Integer id) {
        log.info("studyPlan# deleteStudyPlanById-->  params:{} ", id);
        try {
            if (Objects.isNull(id)) {
                return ApiResultHandler.buildApiResult(400, "删除学习计划参数异常！", null);
            }
            StudyPlan studyPlan = new StudyPlan();
            studyPlan.setId(id);
            this.studyPlanService.deleteStudyPlanAndKpList(studyPlan);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "删除学习计划异常", e.getMessage());
        }
        return ApiResultHandler.buildApiResult(ResultMesEnum.SUCCESS.getResultCode(), "删除学习计划以及知识成功!", null);
    }


    @ApiOperation("学习计划中分页查询所有的知识")
    @GetMapping("/getPageForStudyPlanKnowledge")
    public ApiResult getPageKnowledge(@RequestParam(value = "searchContent", required = false) String searchContent,
                                      @RequestParam("pageNo") Integer pageNo,
                                      @RequestParam("pageSize") Integer pageSize) {
        log.info("studyPlan# getPageForStudyPlanKnowledge-->  params, searchContent:{} ", searchContent);
        String baseUrl = ES_DOMAIN + "/jeecg-boot/enc/encSenses/searchSenses";
        if (StringUtils.isNoneBlank(searchContent)) {
            baseUrl += "?searchContent=" + searchContent + "&pageNo=" + pageNo + "&pageSize=" + pageSize + "";
        } else {
            baseUrl = ES_DOMAIN + "jeecg-boot/enc/encSenses/queryDataPageList?" + "pageNo=" + pageNo + "&pageSize=" + pageSize + "";
        }
        PooledHttpClientAdaptor adaptor = new PooledHttpClientAdaptor();
        Map<String, String> headMap = new HashMap<>();
        headMap.put("X-Access-Token", X_Access_Token);
        /**
         *  请求ES知识接口
         */
        String esKnowledgeJSON = adaptor.doGet(baseUrl, headMap, Collections.emptyMap());
        EsKnowlegdeJsonBean knowledgeResultBean = JSON.parseObject(esKnowledgeJSON, EsKnowlegdeJsonBean.class);
        return ApiResultHandler.buildApiResult(ResultMesEnum.SUCCESS.getResultCode(), "返回知识列表", knowledgeResultBean);
    }


    @ApiOperation("查询学习计划信息")
    @GetMapping("/getStudyPlanList")
    public ApiResult getStudyPlanList(@RequestParam("userId") Integer userId,
                                      @RequestParam(value = "paramStudyStartTime", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date paramStudyStartTime,
                                      @RequestParam(value = "paramStudyEndTime", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date paramStudyEndTime) {
        log.info("studyPlan# getStudyPlanList-->  params:  userId:{} ,paramStudyStartTime:{} paramStudyEndTime:{}",
                userId, paramStudyStartTime, paramStudyEndTime);
        Map<String, List<StudyPlanVo>> resultMap = new HashMap<>();
        /**
         *
         * 查询 学习计划相关的所有的知识点
         */
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("status", 1);
        paramsMap.put("userId", userId);
        paramsMap.put("paramStudyStartTime", paramStudyStartTime);
        paramsMap.put("paramStudyEndTime", paramStudyEndTime);
        // 学习计划中，关联已学习知识点
        List<StudyPlanVo> dbStudyPlanVOList = this.studyPlanService.getStudyPlansWithStudyStatus(paramsMap);
        /**
         *  分组得到计划相关的知识
         */
        Map<Integer, Map<Integer, List<StudyPlanVo>>> resultPlanMap = new HashMap<>();
        if (CollectionUtils.isEmpty(dbStudyPlanVOList)) {
            return ApiResultHandler.buildApiResult(ResultMesEnum.SUCCESS.getResultCode(), "学习计划返回为空", Collections.emptyMap());
        }

        /**
         *  学习计划中，学习的知识和未学习的知识分组 。
         *  当前计划ID下，关联的知识是否已 学习
         */
        Map<Integer, List<StudyPlanVo>> groupPlanMap = dbStudyPlanVOList.stream().collect(Collectors.groupingBy(StudyPlanVo::getId));
        for (Map.Entry<Integer, List<StudyPlanVo>> entry : groupPlanMap.entrySet()) {
//          当前计划内，学习和为学的分组
            Map<Integer, List<StudyPlanVo>> planStudyMap = new HashMap<>();
//          是否在计划内完成的知识学习
            List<StudyPlanVo> isFinishedList = new ArrayList<>();
            List<StudyPlanVo> unstudyedList = new ArrayList<>();
            Integer planId = entry.getKey();
//          于学习计划 关联的所有的知识 // 对比学习的时间和 学习计划的时间，看是否是在学习计划时间欸
            List<StudyPlanVo> plan_kn_List = entry.getValue();
            plan_kn_List.stream().forEach(knVo -> {
                StudyPlanVo newVO = new StudyPlanVo();
                BeanUtils.copyProperties(knVo, newVO);
                try {
                    if(Objects.nonNull(newVO.getStudyStatus()) && 2 == newVO.getStudyStatus().intValue()){
                        isFinishedList.add(newVO);
                    }else{
                        unstudyedList.add(newVO);
                    }
                } catch (Exception e) {
                    unstudyedList.clear();
                    unstudyedList.addAll(plan_kn_List);
                    log.error(" 学习计划查询 ，判断关联的知识是否在计划时间内完成，异常", e.getMessage());
                    e.printStackTrace();
                }
            });
            planStudyMap.put(1, isFinishedList);
            planStudyMap.put(0, unstudyedList);
//          一个学习计划，已学的知识，和未学的知识
            resultPlanMap.put(planId, planStudyMap);
        }
        /**
         *  学习计划排重 ，
         *  每个学习计划VO中 记录了 关联的知识 是否已在计划时间内完成 学习
         */
        List<StudyPlanVo> distinctStudyPlanList = dbStudyPlanVOList.stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(t -> t.getId()))), ArrayList::new));
        for (int i = 0; i < distinctStudyPlanList.size(); i++) {
            StudyPlanVo distincPlan = distinctStudyPlanList.get(i);
            Integer id = distincPlan.getId();
//          是否有 此学习计划
            if (resultPlanMap.containsKey(id)) {
                Map<Integer, List<StudyPlanVo>> knMap = resultPlanMap.get(id);
//              再得到该学习计划中 ，学习计划内学习的 。
                if (CollectionUtils.isNotEmpty(knMap.get(1))) {
                    List<StudyPlanVo> studyKnList = knMap.get(1);
                    distincPlan.setStudyedList(studyKnList);
                } else {
                    distincPlan.setStudyedList(Collections.emptyList());
                }
                //  再得到该学习计划中 ，学习计划内未完成的。 。
                if (CollectionUtils.isNotEmpty(knMap.get(0))) {
                    List<StudyPlanVo> unStudyList = knMap.get(0);
                    distincPlan.setUnstudyedList(unStudyList);
                } else {
                    distincPlan.setUnstudyedList(Collections.emptyList());
                }
            }
//          判断是否可以修改
            LocalDate localDateNow = LocalDate.now();
            Date planStartTime = distincPlan.getPlanStartTime();
//          如果当前时间 > 结束时间不允许修改
            if (planStartTime.getTime() >= localDateNow.atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli()) {
                distincPlan.setUpdate(true);
            } else {
                distincPlan.setUpdate(false);
            }
            /**
             *  当前学习计划下所有的知识
             */
            Set<String> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(AppUserUtil.getLoginAppUser().getId());
            if (groupPlanMap.containsKey(distincPlan.getId())) {
                List<StudyPlanVo> planVoList = groupPlanMap.get(distincPlan.getId());
                List<StudyKnowledge> knowledgeList = new ArrayList<>();
                planVoList.stream().forEach(k -> {
                    StudyKnowledge sk = new StudyKnowledge();
                    sk.setKnowledgeName(k.getKnowledgeName());
                    sk.setKnowledgeId(k.getKnowledgeId());
                    sk.setStudyStatus(Objects.nonNull(k.getStudyDate()) ? 1 : 0);
                    sk.setPlanId(k.getId());
                    sk.setStatus(k.getStatus());
                    sk.setKpId(k.getKpId());
                    if(kpIdsbyUserId.contains(k.getKpId())){
                        sk.setFlag(true);
                    }else {
                        sk.setFlag(false);
                    }
                    knowledgeList.add(sk);
                });
                List<StudyKnowledge> collect = knowledgeList.stream()
                        .collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(t -> t.getKnowledgeId()))), ArrayList::new));
//              得到所有的知识点 。
                distincPlan.setStudyKnowledgeList(collect);
            }
        }
        resultMap = distinctStudyPlanList.stream().sorted(Comparator.comparing(StudyPlan::getPlanStartTime)).collect(Collectors.groupingBy(StudyPlan::getFormatTime));
        return ApiResultHandler.buildApiResult(ResultMesEnum.SUCCESS.getResultCode(), "学习计划返回列表", resultMap);
    }


}
