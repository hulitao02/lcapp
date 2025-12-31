package com.cloud.exam.controller;

import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.exam.dao.EvalDeptDao;
import com.cloud.exam.dao.EvalDeptHisDao;
import com.cloud.exam.dao.EvalPersonDao;
import com.cloud.exam.dao.EvalPersonHisDao;
import com.cloud.exam.model.eval.*;
import com.cloud.exam.service.EvalNewService;
import com.cloud.exam.utils.Tools;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.common.KnowledgePoints;
import com.cloud.model.user.AppUser;
import com.cloud.model.user.LoginAppUser;
import com.cloud.model.user.SysDepartment;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@Api(value = "考试评估控制器类")
@RestController
@RequestMapping("/eval")
public class EvalNewController {

    @Autowired
    EvalPersonHisDao evalPersonHisDao;
    @Resource
    ManageBackendFeign manageBackendFeign;
    @Resource
    SysDepartmentFeign sysDepartmentFeign;
    @Autowired
    EvalDeptHisDao evalDeptHisDao;
    @Autowired
    EvalPersonDao evalPersonDao;
    @Autowired
    EvalDeptDao evalDeptDao;


    /**
     * @author:胡立涛
     * @description: TODO 个人能力综合评分
     * @date: 2021/12/22
     * @param: [kpId]
     * @return: com.cloud.core.ApiResult<com.cloud.exam.model.eval.EvalDto>
     */
//    @ApiOperation(value = "查询个人能力综合评分", notes = "查询个人能力综合评分")
//    @ApiImplicitParam(name = "kpId", value = "知识点ID", required = false, dataType = "Long")
//    @RequestMapping(value = "/personal/score", method = RequestMethod.GET)
    public ApiResult<EvalDto> getPersonalScore(@RequestParam(value = "kpId", required = false) Long kpId) {
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        Map map = new HashMap();
        map.put("userId", loginAppUser.getId());
        Map avgScore = evalPersonHisDao.getPersonalScore(map);
//        avgScore = CollectionsCustomer.builder().build().mapToLowerCase(avgScore);

        EvalDto dto = new EvalDto();
        dto.setScore(avgScore == null ? 0 : Integer.parseInt(avgScore.get("eval_score").toString()));
        dto.caculateLevel();
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", dto);
    }


    /**
     * @author:胡立涛
     * @description: TODO 能力评估:个人_能力趋势
     * @date: 2021/12/22
     * @param: [kpIds]
     * @return: com.cloud.core.ApiResult<java.util.Map < java.lang.String, java.lang.Object>>
     */
//    @ApiOperation(value = "查询个人能力趋势", notes = "12个月之内的评分")
//    @ApiImplicitParam(name = "kpIds", value = "知识点ID数组", required = false, dataType = "Long[]")
//    @RequestMapping(value = "/personal/history", method = RequestMethod.GET)
    public ApiResult<Map<String, Object>> getPersonalHisScore(@RequestParam(value = "kpIds", required = false) String[] kpIds) {
        try {
            LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
            Map map = new HashMap();
            map.put("userId", loginAppUser.getId());
            map.put("kpIds", kpIds);
            // 月份
            Set<Integer> monthSet = new TreeSet<>();
            List<Map> monthList = evalPersonHisDao.getMonth(map);
//            monthList = CollectionsCustomer.builder().build().listMapToLowerCase(monthList);
            if (monthList != null && monthList.size() > 0) {
                for (Map m : monthList) {
                    monthSet.add(Integer.parseInt(m.get("month").toString()));
                }
            }
            // 知识点
            List<String> legendList = new ArrayList<>();
            // 数据
            List<Map<String, Object>> seriesList = new ArrayList<>();
            if (kpIds == null || kpIds.length == 0) {
                legendList.add("综合能力");
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("name", "综合能力");
                List<Integer> dataList = new ArrayList<>();
                if (monthList != null && monthList.size() > 0) {
                    for (Map bean : monthList) {
                        map.put("kpId", 0);
                        map.put("month", Integer.parseInt(bean.get("month").toString()));
                        Map<String, Object> scoreMap = evalPersonHisDao.getEvalPersonHisInfo(map);
//                        scoreMap = CollectionsCustomer.builder().build().mapToLowerCase(scoreMap);
                        dataList.add(scoreMap == null ? 0 : Integer.parseInt(scoreMap.get("eval_score").toString()));
                        dataMap.put("data", dataList);
                    }
                    Collections.sort(dataList);
                    seriesList.add(dataMap);
                }
            } else {
                for (int i = 0; i < kpIds.length; i++) {
                    legendList.add(getKpName(kpIds[i]));
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("name", getKpName(kpIds[i]));
                    List<Integer> dataList = new ArrayList<>();
                    if (monthList != null && monthList.size() > 0) {
                        for (Map bean : monthList) {
                            map.put("kpId", kpIds[i]);
                            map.put("month", Integer.parseInt(bean.get("month").toString()));
                            Map<String, Object> scoreMap = evalPersonHisDao.getEvalPersonHisInfo(map);
//                            scoreMap = CollectionsCustomer.builder().build().mapToLowerCase(scoreMap);
                            dataList.add(scoreMap == null ? 0 : Integer.parseInt(scoreMap.get("eval_score").toString()));
                            Collections.sort(dataList);
                            dataMap.put("data", dataList);
                        }
                        seriesList.add(dataMap);
                    }
                }
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("legend", legendList);
            resultMap.put("xAxis", monthSet);
            resultMap.put("series", seriesList);
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 能力评估:个人_能力分布
     * @date: 2021/12/22
     * @param: [kpIds]
     * @return: com.cloud.core.ApiResult<java.util.List < com.cloud.exam.model.eval.EvalKpDto>>
     */
//    @ApiOperation(value = "查询个人能力分布", notes = "可通过知识点筛选")
//    @ApiImplicitParam(name = "kpIds", value = "知识点ID数组", required = false, dataType = "Long[]")
//    @RequestMapping(value = "/personal/ability", method = RequestMethod.GET)
    public ApiResult<List<EvalKpDto>> getPersonalAbility(@RequestParam(value = "kpIds", required = false) Long[] kpIds) {
        try {
            // 查询参数
            LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
            Map<String, Object> map = new HashMap<>();
            map.put("userId", loginAppUser.getId());
            map.put("kpIds", kpIds);
            List<Map<String, Object>> list = evalPersonHisDao.getAbility(map);
//            list = CollectionsCustomer.builder().build().listMapToLowerCase(list);
            // 结果构造
            List<EvalKpDto> abilityList = new ArrayList<>();
            if (list != null && list.size() > 0) {
                for (Map bean : list) {
                    EvalKpDto evalKpDto = new EvalKpDto();
                    evalKpDto.setKhNum(Integer.parseInt(bean.get("total_num").toString()));
                    evalKpDto.setKpId(bean.get("kp_id").toString());
                    evalKpDto.setKpName(getKpName(evalKpDto.getKpId()));
                    evalKpDto.setScore(Integer.valueOf(bean.get("eval_score").toString()));
                    evalKpDto.caculateLevel();
                    abilityList.add(evalKpDto);
                }
            }
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", abilityList);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 个人能力：我的排名
     * @date: 2021/12/22
     * @param: [kpId]
     * @return: com.cloud.core.ApiResult<java.util.List < com.cloud.exam.model.eval.EvalDeptDto>>
     */
//    @ApiOperation(value = "查询个人能力我的排名", notes = "单位内的评分情况")
//    @ApiImplicitParam(name = "kpId", value = "知识点ID", required = false, dataType = "Long")
//    @RequestMapping(value = "/personal/dept", method = RequestMethod.GET)
    public ApiResult<List<EvalDeptDto>> getPersonalDeptScore(@RequestParam(value = "kpId", required = false) Long kpId) {
        try {
            List<EvalDeptDto> deptScoreList = new ArrayList<>();
            LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
            Map map = new HashMap();
            Long departmentId = loginAppUser.getDepartmentId();
            // 获取当前部门下的人员
            map.put("deptId", departmentId);
            List<Map> usersList = evalPersonHisDao.getUsersByDeptId(map);
//            usersList = CollectionsCustomer.builder().build().listMapToLowerCase(usersList);
            for (Map userMap : usersList) {
                EvalDeptDto dto = new EvalDeptDto();
                long userId = Long.valueOf(userMap.get("user_id").toString());
                map.put("userId", userId);
                dto.setUserId(userId);
                dto.setUserName(getUserName(dto.getUserId()));
                if (kpId == null) {
                    // 查询综合能力
                    map.put("kpId", 0);
                    Map infoByPar = evalPersonHisDao.getInfoByPar(map);
//                    infoByPar = CollectionsCustomer.builder().build().mapToLowerCase(infoByPar);
                    dto.setScore(infoByPar == null ? 0 : Integer.parseInt(infoByPar.get("eval_score").toString()));
                    // 获取考核的次数
                    dto.setKhNum(infoByPar == null ? 0 : Integer.parseInt(infoByPar.get("total_num").toString()));
                } else {
                    Long[] kpIds = {kpId};
                    map.put("kpIds", kpIds);
                    List<Map> abilityList = evalPersonHisDao.getAbility(map);
//                    abilityList = CollectionsCustomer.builder().build().listMapToLowerCase(abilityList);
                    if (abilityList == null || abilityList.size() == 0) {
                        dto.setScore(0);
                        dto.setKhNum(0);
                    } else {
                        dto.setScore(Integer.valueOf(abilityList.get(0).get("eval_score").toString()));
                        dto.setKhNum(Integer.valueOf(abilityList.get(0).get("total_num").toString()));
                    }
                }
                dto.caculateLevel();
                deptScoreList.add(dto);
            }
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", deptScoreList);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 单位综合能力分值
     * @date: 2021/12/23
     * @param: [kpId]
     * @return: com.cloud.core.ApiResult<com.cloud.exam.model.eval.EvalKpDto>
     */
//    @ApiOperation(value = "查询团体能力综合评分", notes = "查询团体能力综合评分")
//    @ApiImplicitParam(name = "kpId", value = "知识点ID", required = false, dataType = "Long")
//    @RequestMapping(value = "/dept/score", method = RequestMethod.GET)
    public ApiResult<EvalKpDto> getDeptScore(@RequestParam(value = "kpId", required = false) Long kpId) {
        try {
            LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
            Map<String, Object> map = new HashMap<>();
            map.put("deptId", loginAppUser.getDepartmentId());
            Map<String, Object> deptScore = evalDeptHisDao.getDeptScore(map);
            Map avgScore = evalDeptHisDao.getDeptScore(map);
//            avgScore = CollectionsCustomer.builder().build().mapToLowerCase(avgScore);
            int score = 0;
            if (avgScore != null) {
                String avg = avgScore.get("avg").toString();
                score = avgScore == null ? 0 : Integer.parseInt(avg.substring(0, avg.indexOf(".")));
            }
            EvalKpDto dto = new EvalKpDto();
            dto.setScore(score);
            dto.caculateLevel();
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 单位各知识点综合能力
     * @date: 2021/12/23
     * @param: [kpIds]
     * @return: com.cloud.core.ApiResult<java.util.List < com.cloud.exam.model.eval.EvalKpDto>>
     */
//    @ApiOperation(value = "查询团体综合能力", notes = "查询团体综合能力")
//    @ApiImplicitParam(name = "kpIds", value = "知识点ID", required = false, dataType = "Long[]")
//    @RequestMapping(value = "/dept/kpScore", method = RequestMethod.GET)
    public ApiResult<List<EvalKpDto>> getDeptKpScore(@RequestParam(value = "kpIds", required = false) Long[] kpIds) {
        try {
            LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
            List<EvalKpDto> kpList = new ArrayList<>();
            // 查询参数
            Map<String, Object> map = new HashMap<>();
            map.put("deptId", loginAppUser.getDepartmentId());
            map.put("kpIds", kpIds);
            List<Map<String, Object>> deptKpScoreList = evalDeptHisDao.getDeptKpScore(map);
//            deptKpScoreList = CollectionsCustomer.builder().build().listMapToLowerCase(deptKpScoreList);
            for (Map dept : deptKpScoreList) {
                EvalKpDto dto = new EvalKpDto();
                dto.setScore(Integer.parseInt(dept.get("eval_score").toString(  )));
                dto.setKpId(dept.get("kp_id").toString());
                String kpName = getKpName(dto.getKpId());
                dto.setKpName(kpName);
                dto.caculateLevel();
                kpList.add(dto);
            }
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", kpList);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 成员能力排名
     * @date: 2021/12/23
     * @param: [num, kpIds]
     * @return: com.cloud.core.ApiResult<java.util.Map < java.lang.Long, java.util.List < com.cloud.exam.model.eval.EvalDeptDto>>>
     */
//    @ApiOperation(value = "查询团体成员能力排名", notes = "查询团体成员能力排名")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "num", value = "显示人数", required = true, dataType = "Integer"),
//            @ApiImplicitParam(name = "kpIds", value = "知识点ID", required = false, dataType = "Long[]")
//    })
//    @RequestMapping(value = "/dept/deptAbility", method = RequestMethod.GET)
    public ApiResult<Map<Long, List<EvalDeptDto>>> getDeptAbility(
            @RequestParam(value = "num") Integer num,
            @RequestParam(value = "limitNum", required = false) Integer limitNum,
            @RequestParam(value = "kpIds", required = false) Long[] kpIds) {
        try {
            List<String> kpList = new ArrayList<>();
            LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
            List<List<EvalDeptDto>> seriesList = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            map.put("deptId", loginAppUser.getDepartmentId());
            map.put("kpIds", kpIds);
            List<Map<String, Object>> list = new ArrayList<>();
            if (kpIds == null || kpIds.length == 0) {
                // 查询部门能力高的前n个知识点
                map.put("limitNum", 8);
                if (limitNum != null && limitNum > 0) {
                    map.put("limitNum", limitNum);
                }
                list = evalDeptHisDao.getTopKpName(map);
//                list = CollectionsCustomer.builder().build().listMapToLowerCase(list);
            } else {
                for (int i = 0; i < kpIds.length; i++) {
                    Map<String, Object> kpMap = new HashMap<>();
                    kpMap.put("kp_id", kpIds[i]);
                    list.add(kpMap);
                }
            }
            Map<Integer, List<EvalDeptDto>> personMap = new HashMap<>();
            for (int k = 0; k < num; k++) {
                List<EvalDeptDto> dataList = new ArrayList<>();
                personMap.put(k, dataList);
            }
            Map<String, List<EvalDeptDto>> dataMap = new HashMap<>();
            for (Map<String, Object> m : list) {
                String kpId =m.get("kp_id").toString();
                kpList.add(getKpName(kpId));
                // 根据知识点id查询前n名学生信息
                map = new HashMap<>();
                map.put("kpId", kpId);
                map.put("num", num);
                List<Map<String, Object>> topUser = evalPersonHisDao.getTopUser(map);
//                topUser = CollectionsCustomer.builder().build().listMapToLowerCase(topUser);
                if (topUser != null && topUser.size() > 0) {
                    for (int i = 0; i < topUser.size(); i++) {
                        Map userMap = topUser.get(i);
                        EvalDeptDto bean = new EvalDeptDto();
                        bean.setScore(Integer.parseInt(userMap.get("eval_score").toString()));
                        bean.setKhNum(Integer.parseInt(userMap.get("total_num").toString()));
                        bean.setUserId(Long.valueOf(userMap.get("user_id").toString()));
                        if (getUserName(Long.valueOf(userMap.get("user_id").toString()))==""){
                            continue;
                        }
                        bean.setUserName(getUserName(Long.valueOf(userMap.get("user_id").toString())));
                        bean.caculateLevel();
                        // 人员
                        List<EvalDeptDto> evalDeptDtos = personMap.get(i);
                        evalDeptDtos.add(bean);
                        personMap.put(i, evalDeptDtos);
                    }
                    for (int i = topUser.size(); i < num; i++) {
                        List<EvalDeptDto> evalDeptDtos = personMap.get(i);
                        evalDeptDtos.add(null);
                        personMap.put(i, evalDeptDtos);
                    }

                } else {
                    for (int i = 0; i < num; i++) {
                        List<EvalDeptDto> evalDeptDtos = personMap.get(i);
                        evalDeptDtos.add(null);
                        personMap.put(i, evalDeptDtos);
                    }
                }
            }
            for (Integer integer : personMap.keySet()) {
                seriesList.add(personMap.get(integer));
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("xAxis", kpList);//知识点
            resultMap.put("series", seriesList);
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 单位能力：成员能力分布
     * @date: 2021/12/27
     * @param: [kpIds]
     * @return: com.cloud.core.ApiResult<java.util.List < com.cloud.exam.model.eval.EvalDeptDto>>
     */
//    @GetMapping(value = "dept/distribution")
    public ApiResult<List<EvalDeptDto>> distribution(@RequestParam(value = "kpId", required = false) Long kpId) {
        try {
            List<EvalDeptDto> distribution = new ArrayList<>();
            LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
            Map map = new HashMap();
            if (kpId == null) {
                kpId = 0L;
            }
            map.put("kpId", kpId);
            map.put("deptId", loginAppUser.getDepartmentId());
            List<Map<String, Object>> list = evalPersonHisDao.getDistribution(map);
//            list = CollectionsCustomer.builder().build().listMapToLowerCase(list);
            if (list != null && list.size() > 0) {
                for (Map<String, Object> m : list) {
                    EvalDeptDto bean = new EvalDeptDto();
                    bean.setUserId(Long.valueOf(m.get("user_id").toString()));
                    bean.setUserName(getUserName(bean.getUserId()));
                    bean.setScore(Integer.parseInt(m.get("eval_score").toString()));
                    bean.setKhNum(Integer.parseInt(m.get("total_num").toString()));
                    bean.caculateLevel();
                    distribution.add(bean);
                }
            }
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", distribution);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 单次：各人知识点能力
     * @date: 2021/12/28
     * @param: [examId, userId, paperId]
     * @return: com.cloud.core.ApiResult<java.util.List < com.cloud.exam.model.eval.EvalKpDto>>
     */
    @ApiOperation(value = "查询考试个人知识点评分", notes = "查询考试个人知识点评分")
    @ApiImplicitParam(name = "examId", value = "考试活动ID", required = false, dataType = "Long")
    @RequestMapping(value = "/exam/personal/detail", method = RequestMethod.GET)
    public ApiResult<List<EvalKpDto>> getExamPersonalDetail(@RequestParam(value = "examId") Long examId, @RequestParam(value = "userId") Long userId, @RequestParam(value = "paperId", required = false) Long paperId) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("acId", examId);
            map.put("userId", userId);
            List<Map<String, Object>> examPersonalDetail = evalPersonDao.getExamPersonalDetail(map);
//            examPersonalDetail = CollectionsCustomer.builder().build().listMapToLowerCase(examPersonalDetail);

            List<EvalKpDto> kpScoreList = new ArrayList<>();
            if (examPersonalDetail != null && examPersonalDetail.size() > 0) {
                for (Map<String, Object> detail : examPersonalDetail) {
                    EvalKpDto dto = new EvalKpDto();
                    dto.setScore(Integer.parseInt(detail.get("eval_score").toString()));
                    dto.caculateLevel();
                    dto.setKpId(detail.get("kp_id").toString());
                    String kpName = getKpName(dto.getKpId());
                    dto.setKpName(kpName);
                    kpScoreList.add(dto);
                }
            }
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", kpScoreList);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据知识点id获取知识点名称
     * @date: 2021/12/27
     * @param: [kpId]
     * @return: java.lang.String
     */
    private String getKpName(String kpId) {
        String kpName = Tools.getKpCacheValue(kpId);
        if (kpName == null) {
            KnowledgePoints knowledgePointsById = manageBackendFeign.getKnowledgePointsById(kpId);
            if (Objects.nonNull(knowledgePointsById)) {
                kpName = knowledgePointsById.getPointName();
            }
            if (StringUtils.isBlank(kpName)) {
                kpName = "";
            }
            Tools.putKpCacheValue(kpId, kpName);
        }
        return kpName;
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据用户id获取用户名称
     * @date: 2021/12/27
     * @param: [userId]
     * @return: java.lang.String
     */
    private String getUserName(long userId) {
        AppUser user = sysDepartmentFeign.findUserById(userId);
        if (user==null){
            return "";
        }
        String userName = user.getNickname();
        Tools.putUserCacheValue(userId, userName);
        return userName;
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据部门id获取部门名称
     * @date: 2021/12/28
     * @param: [deptId]
     * @return: java.lang.String
     */
    private String getDepartmentName(long deptId) {
        String deptName = Tools.getDeptCacheValue(deptId);
        if (deptName == null) {
            SysDepartment dept = sysDepartmentFeign.findSysDepartmentById(deptId);
            deptName = dept.getDname();
            Tools.putDeptCacheValue(deptId, deptName);
        }
        return deptName;
    }


    /**
     * @author:胡立涛
     * @description: TODO 单次：各单位综合能力评比
     * @date: 2021/12/28
     * @param: [examId, paperId]
     * @return: com.cloud.core.ApiResult<java.util.List < com.cloud.exam.model.eval.DeptEvalDto>>
     */
    @ApiOperation(value = "查询考试综合评分", notes = "查询考试综合评分")
    @ApiImplicitParam(name = "examId", value = "考试活动ID", required = true, dataType = "Long")
    @RequestMapping(value = "/exam/dept/eavl", method = RequestMethod.GET)
    public ApiResult<List<DeptEvalDto>> getExamDeptEval(@RequestParam(value = "examId") Long examId, @RequestParam(value = "paperId", required = false) Long paperId) {
        try {
            List<DeptEvalDto> deptScoreList = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            map.put("acId", examId);
            List<Map<String, Object>> examDeptEval = evalDeptDao.getExamDeptEval(map);
//            examDeptEval = CollectionsCustomer.builder().build().listMapToLowerCase(examDeptEval);


            if (examDeptEval != null && examDeptEval.size() > 0) {
                for (Map<String, Object> bean : examDeptEval) {
                    DeptEvalDto dto = new DeptEvalDto();
                    dto.setScore(Integer.parseInt(bean.get("eval_score").toString()));
                    dto.caculateLevel();
                    dto.setDeptId(Long.valueOf(bean.get("department_id").toString()));
                    dto.setDeptName(getDepartmentName(dto.getDeptId()));
                    // 部门下的人数
                    map.put("deptId", dto.getDeptId());
                    Map<String, Object> userCountDept = evalPersonDao.getUserCountDept(map);
//                    userCountDept = CollectionsCustomer.builder().build().mapToLowerCase(userCountDept);
                    dto.setNum(userCountDept == null ? 0 : Integer.parseInt(userCountDept.get("user_count").toString()));
                    deptScoreList.add(dto);
                }
            }
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", deptScoreList);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 单次：各部门能力评估
     * @date: 2021/12/28
     * @param: [examId, paperId, deptId]
     * @return: com.cloud.core.ApiResult<java.util.List < com.cloud.exam.model.eval.EvalKpDto>>
     */
    @ApiOperation(value = "查询考试知识点评分", notes = "查询考试知识点评分")
    @ApiImplicitParam(name = "examId", value = "考试活动ID", required = false, dataType = "Long")
    @RequestMapping(value = "/exam/dept/detail", method = RequestMethod.GET)
    public ApiResult<List<EvalKpDto>> getExamDeptDetail(@RequestParam(value = "examId") Long examId,
                                                        @RequestParam(value = "paperId", required = false) Long paperId,
                                                        @RequestParam(value = "deptId") Long deptId) {
        try {
            List<EvalKpDto> kpScoreList = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            map.put("acId", examId);
            map.put("deptId", deptId);
            List<Map<String, Object>> deptList = evalDeptDao.getExamDeptDetail(map);
//            deptList = CollectionsCustomer.builder().build().listMapToLowerCase(deptList);

            if (deptList != null && deptList.size() > 0) {
                for (Map<String, Object> bean : deptList) {
                    String kpId = bean.get("kp_id").toString();
                    EvalKpDto dto = new EvalKpDto();
                    dto.setScore(Integer.parseInt(bean.get("eval_score").toString()));
                    dto.caculateLevel();
                    dto.setKpId(kpId);
                    dto.setKpName(getKpName(kpId));
                    kpScoreList.add(dto);
                }
            }
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", kpScoreList);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 单次：部门各人综合能力
     * @date: 2021/12/28
     * @param: [examId, deptId, paperId]
     * @return: com.cloud.core.ApiResult<java.util.List < com.cloud.exam.model.eval.UserEvalDto>>
     */
    @ApiOperation(value = "查询考试个人综合评分", notes = "查询考试个人综合评分")
    @ApiImplicitParam(name = "examId", value = "考试活动ID", required = true, dataType = "Long")
    @RequestMapping(value = "/exam/personal/eval", method = RequestMethod.GET)
    public ApiResult<List<UserEvalDto>> getExamPersonalEval(@RequestParam(value = "examId") Long examId, @RequestParam(value = "deptId") Long deptId, @RequestParam(value = "paperId", required = false) Long paperId) {
        try {
            List<UserEvalDto> userScoreList = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            map.put("acId", examId);
            map.put("deptId", deptId);
            List<Map<String, Object>> examPersonalEval = evalPersonDao.getExamPersonalEval(map);
//            examPersonalEval = CollectionsCustomer.builder().build().listMapToLowerCase(examPersonalEval);


            if (examPersonalEval != null && examPersonalEval.size() > 0) {
                for (Map<String, Object> bean : examPersonalEval) {
                    UserEvalDto dto = new UserEvalDto();
                    dto.setScore(Integer.parseInt(bean.get("eval_score").toString()));
                    dto.caculateLevel();
                    dto.setUserId(Long.valueOf(bean.get("user_id").toString()));
                    dto.setUserName(getUserName(dto.getUserId()));
                    userScoreList.add(dto);
                }
            }
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", userScoreList);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 能力值计算
     * @date: 2021/12/30
     * @param: [examId]
     * @return: com.cloud.core.ApiResult
     */
    @Autowired
    EvalNewService evalNewService;

    @GetMapping(value = "makeEvaluation")
    public ApiResult makeEvaluation(@RequestParam(value = "examId") Long examId) {
        try {
            evalNewService.makeEvaluation(examId);
            return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author: 胡立涛
     * @description: TODO 根据阀值，查询低于该阀值的知识点列表
     * @date: 2022/5/30
     * @param: [evalScore]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    @PostMapping(value = "getPointList")
    public List<Map<String, Object>> getPointList(@RequestBody double evalScore) {
        Map<String, Object> map = new HashMap<>();
        map.put("evalScore", evalScore);
        return evalPersonHisDao.getPointList(map);
    }
}
