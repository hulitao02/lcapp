package com.cloud.exam.controller;

import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.exam.dao.PgInfoDao;
import com.cloud.exam.model.eval.EvalDeptNewDto;
import com.cloud.exam.model.eval.EvalKpNewDto;
import com.cloud.feign.knowledge.KnowledgeFeign;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.user.AppUser;
import com.cloud.model.utils.AppUserUtil;
import io.swagger.annotations.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Api(value = "考试评估控制器类")
@RestController
@RequestMapping("/pg")
public class PgController {
    @Autowired
    PgInfoDao pgInfoDao;

    /**
     * @author:胡立涛
     * @description: TODO 个人能力评估：能力分布
     * @date: 2024/6/25
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "person/ability")
    public ApiResult ability(@RequestBody Map map) {
        try {
            Map parMap = new HashMap();
            String kpIds = map.get("kpIds") == null ? "" : map.get("kpIds").toString();
            List<String> kpIdList = null;
            if (kpIds != "" && kpIds.trim().length() > 0) {
                kpIdList = new ArrayList<>();
                String[] kpIdArr = kpIds.split(",");
                for (int i = 0; i < kpIdArr.length; i++) {
                    kpIdList.add(kpIdArr[i]);
                }
            }
            String pdTypes = map.get("pdTypes") == null ? "" : map.get("pdTypes").toString();
            List<String> pdTypeList = null;
            if (pdTypes != "" && pdTypes.trim().length() > 0) {
                pdTypeList = new ArrayList<>();
                String[] pdTypeArr = pdTypes.split(",");
                for (int i = 0; i < pdTypeArr.length; i++) {
                    pdTypeList.add("'" + pdTypeArr[i] + "'");
                }
            }
            List<Long> studentList = new ArrayList<>();
            studentList.add(AppUserUtil.getLoginUserId());
            parMap.put("studentIds", studentList);
            parMap.put("startTime", map.get("startTime").toString());
            parMap.put("endTime", map.get("endTime").toString());
            parMap.put("kpIds", kpIdList);
            parMap.put("pdTypes", pdTypeList);
            List<Map> ability = pgInfoDao.ability(parMap);
            List<EvalKpNewDto> evalKpDtoList = new ArrayList<>();
            if (ability != null && ability.size() > 0) {
                for (Map bean : ability) {
                    EvalKpNewDto evalKpNewDto = new EvalKpNewDto();
                    String kpName = bean.get("kp_name").toString();
                    String kpId = bean.get("kp_id").toString();
                    double score = Double.valueOf(bean.get("score").toString());
                    evalKpNewDto.setKpId(kpId);
                    evalKpNewDto.setKpName(kpName);
                    evalKpNewDto.setScore(score);
                    evalKpNewDto.setKhNum(Integer.parseInt(bean.get("total_question").toString()));
                    evalKpDtoList.add(evalKpNewDto);
                }
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", evalKpDtoList);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 知识智能推荐功能所需
     * @date: 2024/9/20
     * @param: [map]
     * @return: java.util.List<java.util.Map>
     */
    @PostMapping(value = "person/abilityzz")
    public List<Map> abilityzz(@RequestBody Map map) {
        try {
            Map parMap = new HashMap();
            String kpIds = map.get("kpIds") == null ? "" : map.get("kpIds").toString();
            List<String> kpIdList = null;
            if (kpIds != "" && kpIds.trim().length() > 0) {
                kpIdList = new ArrayList<>();
                String[] kpIdArr = kpIds.split(",");
                for (int i = 0; i < kpIdArr.length; i++) {
                    kpIdList.add(kpIdArr[i]);
                }
            }
            String pdTypes = map.get("pdTypes") == null ? "" : map.get("pdTypes").toString();
            List<String> pdTypeList = null;
            if (pdTypes != "" && pdTypes.trim().length() > 0) {
                pdTypeList = new ArrayList<>();
                String[] pdTypeArr = pdTypes.split(",");
                for (int i = 0; i < pdTypeArr.length; i++) {
                    pdTypeList.add("'" + pdTypeArr[i] + "'");
                }
            }
            List<Long> studentList = new ArrayList<>();
            studentList.add(AppUserUtil.getLoginUserId());
            parMap.put("studentIds", studentList);
            parMap.put("startTime", null);
            parMap.put("endTime", null);
            parMap.put("kpIds", kpIdList);
            parMap.put("pdTypes", pdTypeList);
            List<Map> ability = pgInfoDao.ability(parMap);
            List<Map> rList = new ArrayList<>();
            if (ability != null && ability.size() > 0) {
                for (Map bean : ability) {
                    String kpId = bean.get("kp_id").toString();
                    Map rMap = new HashMap();
                    rMap.put("kpId", kpId);
                    rMap.put("score", bean.get("score"));
                    rList.add(rMap);
                }
            }
            return rList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 个人能力评估：能力趋势
     * @date: 2024/6/25
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "person/history")
    public ApiResult history(@RequestBody Map map) {
        try {
            Map parMap = new HashMap();
            String kpIds = map.get("kpIds") == null ? "" : map.get("kpIds").toString();
            List<String> kpIdList = null;
            if (kpIds != "" && kpIds.trim().length() > 0) {
                kpIdList = new ArrayList<>();
                String[] kpIdArr = kpIds.split(",");
                for (int i = 0; i < kpIdArr.length; i++) {
                    kpIdList.add(kpIdArr[i]);
                }
            }
            String pdTypes = map.get("pdTypes") == null ? "" : map.get("pdTypes").toString();
            List<String> pdTypeList = null;
            if (pdTypes != "" && pdTypes.trim().length() > 0) {
                pdTypeList = new ArrayList<>();
                String[] pdTypeArr = pdTypes.split(",");
                for (int i = 0; i < pdTypeArr.length; i++) {
                    pdTypeList.add("'" + pdTypeArr[i] + "'");
                }
            }
            parMap.put("studentId", AppUserUtil.getLoginUserId());
            parMap.put("startTime", map.get("startTime").toString());
            parMap.put("endTime", map.get("endTime").toString());
            parMap.put("kpIds", kpIdList);
            parMap.put("pdTypes", pdTypeList);
            List<Map> history = pgInfoDao.history(parMap);
            List<Map> rList = new ArrayList<>();
            if (history != null && history.size() > 0) {
                for (Map bean : history) {
                    Map rMap = new HashMap();
                    String kpId = bean.get("kp_id").toString();
                    String kpName = bean.get("kp_name").toString();
                    String time = bean.get("create_time").toString();
                    double score = Double.valueOf(bean.get("score").toString());
                    rMap.put("kpId", kpId);
                    rMap.put("kpName", kpName);
                    rMap.put("time", time);
                    rMap.put("score", score);
                    rList.add(rMap);
                }
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", rList);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    @Autowired
    SysDepartmentFeign sysDepartmentFeign;


    /**
     * @author:胡立涛
     * @description: TODO 个人能力评估：我的排名
     * @date: 2024/6/26
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "personal/dept")
    public ApiResult dept(@RequestBody Map map) {
        try {
            String kpId = map.get("kpIds") == null ? "" : map.get("kpIds").toString();
            String startTime = map.get("startTime").toString();
            String endTime = map.get("endTime").toString();
            String pdTypes = map.get("pdTypes") == null ? "" : map.get("pdTypes").toString();
            String kpid = "";
            List<String> pdList = null;
            if (pdTypes != "" && pdTypes.trim().length() > 0) {
                pdList = new ArrayList<>();
                String[] pdTypeArr = pdTypes.split(",");
                for (int i = 0; i < pdTypeArr.length; i++) {
                    pdList.add("'" + pdTypeArr[i] + "'");
                }
            }
            Map parMap = new HashMap();
            parMap.put("startTime", startTime);
            parMap.put("endTime", endTime);
            parMap.put("pdTypes", pdList);
            if (kpId == "" || kpId.trim().length() == 0) {
                parMap.put("studentId", AppUserUtil.getLoginUserId());
                Map kpId1 = pgInfoDao.getKpId(parMap);
                if (kpId1 == null) {
                    return ApiResultHandler.buildApiResult(200, "操作成功", null);
                }
                kpid = kpId1.get("kp_id").toString();
            } else {
                kpid = kpId;
            }
            List<String> kpIdList = new ArrayList<>();
            kpIdList.add(kpid);
            parMap.put("kpIds", kpIdList);
            Long deptId = AppUserUtil.getLoginDepartmentId();
            parMap.put("deptId", deptId);
            // 查询当前登录人所在部门的人员信息
            List<Map> list = pgInfoDao.deptUser(parMap);
            List<Long> studentList = new ArrayList<>();
            List<EvalDeptNewDto> evalDeptDtoList = new ArrayList<>();
            for (Map uMap : list) {
                studentList.add(Long.valueOf(uMap.get("student_id").toString()));
                parMap.put("studentIds", studentList);
            }
            List<Map> resultList = pgInfoDao.ability(parMap);
            if (resultList != null && resultList.size() > 0) {
                for (Map resultMap : resultList) {
                    Long studentId = Long.valueOf(resultMap.get("student_id").toString());
                    EvalDeptNewDto bean = new EvalDeptNewDto();
                    bean.setUserId(studentId);
                    AppUser user = sysDepartmentFeign.findUserById(studentId);
                    bean.setUserName(user.getNickname());
                    bean.setScore(Double.valueOf(resultMap.get("score").toString()));
                    evalDeptDtoList.add(bean);
                }
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", evalDeptDtoList);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    @Autowired
    ManageBackendFeign manageBackendFeign;
    @Autowired
    KnowledgeFeign knowledgeFeign;

    /**
     * @author:胡立涛
     * @description: TODO 团体能力评估：单位综合能力
     * @date: 2024/6/26
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "dept/kpScore")
    public ApiResult kpScore(@RequestBody Map map) {
        try {
            String startTime = map.get("startTime").toString();
            String endTime = map.get("endTime").toString();
            String kpIds = map.get("kpIds") == null ? "" : map.get("kpIds").toString();
            String pdTypes = map.get("pdTypes") == null ? "" : map.get("pdTypes").toString();
            String depts = map.get("depts") == null ? "" : map.get("depts").toString();
            if (kpIds == "" || kpIds.trim().length() == 0) {
                List<Map> alltwo = manageBackendFeign.alltwo();
                for (Map m : alltwo) {
                    kpIds += m.get("id") + ",";
                }

                kpIds = kpIds.substring(0, kpIds.length() - 1);
            }
            List<String> kpList = new ArrayList<>();
            String[] kpIdArr = kpIds.split(",");
            for (int i = 0; i < kpIdArr.length; i++) {
                kpList.add(kpIdArr[i]);
            }
            List<String> pdList = null;
            if (pdTypes != "" && pdTypes.trim().length() > 0) {
                pdList = new ArrayList<>();
                String[] pdTypArr = pdTypes.split(",");
                for (int i = 0; i < pdTypArr.length; i++) {
                    pdList.add("'" + pdTypArr[i] + "'");
                }
            }
            List<Long> deptList = new ArrayList<>();
            if (depts != "" && depts.length() > 0) {
                String[] deptArr = depts.split(",");
                for (int i = 0; i < deptArr.length; i++) {
                    deptList.add(Long.valueOf(deptArr[i]));
                }
            } else {
                deptList.add(AppUserUtil.getLoginDepartmentId());
            }
            List<EvalKpNewDto> dataList = null;
            List<Map> rList = new ArrayList<>();
            for (int i = 0; i < deptList.size(); i++) {
                Map<String, EvalKpNewDto> kpMap = new HashMap<>();
                for (int j = 0; j < kpIdArr.length; j++) {
                    EvalKpNewDto evalKpNewDto = new EvalKpNewDto();
                    evalKpNewDto.setKpId(kpIdArr[j]);
                    evalKpNewDto.setKpName("未知");
                    Map knowledgePointsById = knowledgeFeign.getKnowledgePointsById(evalKpNewDto.getKpId());
                    if (knowledgePointsById != null) {
                        evalKpNewDto.setKpName(knowledgePointsById.get("name").toString());
                    }
                    evalKpNewDto.setScore(0.00);
                    kpMap.put(kpIdArr[j].toString(), evalKpNewDto);
                }
                Long deptId = deptList.get(i);
                String deptName = sysDepartmentFeign.findSysDepartmentById(deptId).getDname();
                // 根据部门id查询部门人员
                List<AppUser> deptUserIds = sysDepartmentFeign.getDeptUserIds(deptId);
                if (deptUserIds != null && deptUserIds.size() > 0) {
                    List<Long> studentList = new ArrayList<>();
                    for (AppUser appUser : deptUserIds) {
                        studentList.add(appUser.getId());
                    }
                    Map parMap = new HashMap();
                    parMap.put("startTime", startTime);
                    parMap.put("endTime", endTime);
                    parMap.put("kpIds", kpList);
                    parMap.put("pdTypes", pdList);
                    parMap.put("studentIds", studentList);
                    List<Map> list = pgInfoDao.kpScore(parMap);
                    if (list != null && list.size() > 0) {
                        for (Map m : list) {
                            String kpId = m.get("kp_id").toString();
                            EvalKpNewDto evalKpNewDto = new EvalKpNewDto();
                            evalKpNewDto.setKpId(m.get("kp_id").toString());
                            evalKpNewDto.setKpName(m.get("kp_name").toString());
                            evalKpNewDto.setScore(Double.valueOf(m.get("score").toString()));
                            kpMap.remove(kpId);
                            kpMap.put(kpId, evalKpNewDto);
                        }
                    }
                }
                dataList = new ArrayList<>();
                for (String str : kpMap.keySet()) {
                    EvalKpNewDto evbean = kpMap.get(str);
                    dataList.add(evbean);
                }
                Map deptMap = new HashMap();
                deptMap.put("deptId", deptId);
                deptMap.put("deptName", deptName);
                deptMap.put("dataList", dataList);
                rList.add(deptMap);
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", rList);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 团体能力评估：成员能力排名
     * @date: 2024/6/27
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "dept/deptAbility")
    public ApiResult deptAbility(@RequestBody Map map) {
        try {
            String[] depts = map.get("depts").toString().split(",");
            List<Long> deptList = new ArrayList<>();
            for (int i = 0; i < depts.length; i++) {
                deptList.add(Long.valueOf(depts[i].toString()));
            }
            String pdTypes = map.get("pdTypes") == null ? "" : map.get("pdTypes").toString();
            List<String> pdList = null;
            if (pdTypes != "" && pdTypes.trim().length() > 0) {
                pdList = new ArrayList<>();
                String[] pdTypeArr = pdTypes.split(",");
                for (int i = 0; i < pdTypeArr.length; i++) {
                    pdList.add("'" + pdTypeArr[i] + "'");
                }
            }
            Integer topNum = Integer.parseInt(map.get("topNum").toString());
            String kpIdStr = map.get("kpIds") == null ? "" : map.get("kpIds").toString();
            String kpIdd = "";
            if (kpIdStr == "" || kpIdStr.trim().length() == 0) {
                List<Map> kpTree = manageBackendFeign.alltwo();
                for (Map m : kpTree) {
                    kpIdStr += m.get("id") + ",";
                }

                kpIdStr = kpIdStr.substring(0, kpIdStr.length() - 1);
            }
            String[] kpIds = kpIdStr.split(",");

            List<Map> list1 = new ArrayList<>();
            List<Map> list2 = new ArrayList<>();
            List<Map> list3 = new ArrayList<>();
            List<String> xAxis = new ArrayList<>();
            Map parMap = new HashMap();
            parMap.put("startTime", map.get("startTime").toString());
            parMap.put("endTime", map.get("endTime").toString());
            parMap.put("pdTypes", pdList);
            parMap.put("depts", deptList);
            for (int i = 0; i < kpIds.length; i++) {
                String kpId = kpIds[i].toString();
                parMap.put("kpId", kpId);
                for (int k = 0; k < 3; k++) {
                    int index = k;
                    parMap.put("index", index);
                    Map resultMap = pgInfoDao.deptAbility(parMap);
                    if (resultMap != null) {
                        String kpName = resultMap.get("kp_name").toString();
                        Long studentId = Long.valueOf(resultMap.get("student_id").toString());
                        AppUser appUserById = sysDepartmentFeign.findAppUserById(studentId);
                        Double score = Double.valueOf(resultMap.get("score").toString());
                        Map m = new HashMap();
                        m.put("name", appUserById.getNickname());
                        m.put("value", score);
                        if (k == 0) {
                            list1.add(m);
                            xAxis.add(kpName);
                        } else if (k == 1) {
                            list2.add(m);
                        } else if (k == 2) {
                            list3.add(m);
                        }
                    } else {
                        Map m = new HashMap();
                        m.put("name", null);
                        m.put("value", null);
                        if (k == 0) {
                            break;
                        } else if (k == 1) {
                            list2.add(m);
                        } else if (k == 2) {
                            list3.add(m);
                        }
                    }
                }
            }
            Map map1 = new HashMap();
            map1.put("name", "第1名");
            map1.put("data", list1);
            Map map2 = new HashMap();
            map2.put("name", "第2名");
            map2.put("data", list2);
            Map map3 = new HashMap();
            map3.put("name", "第3名");
            map3.put("data", list3);
            List<Map> rList = new ArrayList<>();
            if (list1 != null && list1.size() > 0) {
                if (topNum == 1) {
                    rList.add(map1);
                } else if (topNum == 2) {
                    rList.add(map1);
                    rList.add(map2);
                } else if (topNum == 3) {
                    rList.add(map1);
                    rList.add(map2);
                    rList.add(map3);
                }
            }
            Map rMap = new HashMap();
            rMap.put("xAxis", xAxis);
            rMap.put("data", rList);
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 团体能力评估：成员能力分布
     * @date: 2024/6/27
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "dept/distribution")
    public ApiResult distribution(@RequestBody Map map) {
        try {
            String[] depts = map.get("depts").toString().split(",");
            List<Long> deptList = new ArrayList<>();
            for (int i = 0; i < depts.length; i++) {
                deptList.add(Long.valueOf(depts[i].toString()));
            }
            String pdTypes = map.get("pdTypes") == null ? "" : map.get("pdTypes").toString();
            List<String> pdList = null;
            if (pdTypes != "" && pdTypes.trim().length() > 0) {
                pdList = new ArrayList<>();
                String[] pdTypeArr = pdTypes.split(",");
                for (int i = 0; i < pdTypeArr.length; i++) {
                    pdList.add("'" + pdTypeArr[i] + "'");
                }
            }
            String kpIds = map.get("kpIds") == null ? "" : map.get("kpIds").toString();
            List<String> kpList = null;
            if (kpIds != "" && kpIds.trim().length() > 0) {
                kpList = new ArrayList<>();
                String[] kpArr = kpIds.split(",");
                for (int i = 0; i < kpArr.length; i++) {
                    kpList.add(kpArr[i].toString());
                }
            }
            Map parMap = new HashMap();
            parMap.put("startTime", map.get("startTime").toString());
            parMap.put("endTime", map.get("endTime").toString());
            parMap.put("depts", deptList);
            parMap.put("pdTypes", pdList);
            parMap.put("kpIds", kpList);
            List<Map> distribution = pgInfoDao.distribution(parMap);
            List<Map> rList = new ArrayList<>();
            if (distribution != null && distribution.size() > 0) {
                for (Map bean : distribution) {
                    Long studentId = Long.valueOf(bean.get("student_id").toString());
                    Long khNum = Long.valueOf(bean.get("count").toString());
                    double score = Double.valueOf(bean.get("score").toString());
                    AppUser appUserById = sysDepartmentFeign.findAppUserById(studentId);
                    Map rMap = new HashMap();
                    rMap.put("userId", studentId);
                    rMap.put("userName", appUserById.getNickname());
                    rMap.put("khNum", khNum);
                    rMap.put("score", score);
                    rList.add(rMap);
                }
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", rList);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }
}


