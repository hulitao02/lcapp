package com.cloud.user.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.model.user.*;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.user.ExcelUtil.ExcelUtil;
import com.cloud.user.LcDateUtil;
import com.cloud.user.dao.*;
import com.cloud.user.model.*;
import com.cloud.user.service.LcTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import static com.cloud.user.LcDateUtil.*;

@RestController
@RequestMapping("/lc")
public class LcTaskController {

    @Autowired
    TaskDao taskDao;
    @Autowired
    LcTaskService lcTaskService;
    @Autowired
    ManageBackendFeign manageBackendFeign;
    @Autowired
    TaskUserDao taskUserDao;
    @Autowired
    AppUserDao appUserDao;
    @Autowired
    LcSysRoleUserDao lcSysRoleUserDao;
    @Autowired
    SysDepartmentDao sysDepartmentDao;
    @Autowired
    LcDictClassDao lcDictClassDao;
    @Autowired
    LcDictInfoDao lcDictInfoDao;
    @Autowired
    LcTaskStateDao lcTaskStateDao;
    // 上传文件存储的本地路径
    @Value("${localUrlPrefix}")
    String localUrlPrefix;
    @Value("${localFilePath}")
    private String localFilePath;

    /**
     * @author:胡立涛
     * @description: TODO 添加任务
     * @date: 2025/12/17
     * @param:
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "addTask")
    public ApiResult addTask(@RequestBody LcTask lcTask) {
        try {
            AppUser appUser = AppUserUtil.getLoginAppUser();
            int userId = appUser.getId().intValue();
            if (userId == 0) {
                return ApiResultHandler.buildApiResult(100, "当前登录用户为空", null);
            }
            lcTask.setCreateUser(userId);
            lcTask.setDeptId(appUser.getDepartmentId().intValue());
            lcTaskService.addTask(lcTask);
            return ApiResultHandler.buildApiResult(200, "操作成功", lcTask);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据任务id删除任务
     * @date: 2025/12/17
     * @param:
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "delTask")
    public ApiResult delTask(@RequestBody LcTask lcTask) {
        try {
            lcTaskService.delTask(lcTask);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据任务id查询任务详细信息
     * @date: 2025/12/17
     * @param:
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getTaskDetail")
    public ApiResult getTaskDetail(@RequestBody LcTask lcTask) {
        try {
            LcTask bean = taskDao.selectById(lcTask.getId());
            LcTask userInfo = getUserInfo(bean.getId());
            bean.setUserDeptNames(userInfo.getUserDeptNames());
            bean.setUserIds(userInfo.getUserIds());
            bean.setUserNames(userInfo.getUserNames());
            QueryWrapper<LcTaskState> lcTaskStateQueryWrapper = new QueryWrapper<>();
            lcTaskStateQueryWrapper.eq("task_id", lcTask.getId());
            List<LcTaskState> lcTaskStateList = lcTaskStateDao.selectList(lcTaskStateQueryWrapper);
            if (lcTaskStateList != null && lcTaskStateList.size() > 0) {
                for (LcTaskState lcTaskState : lcTaskStateList) {
                    AppUser byId = appUserDao.findById(Long.valueOf(lcTaskState.getCreateUser()));
                    if (byId != null) {
                        lcTaskState.setCreateUserName(byId.getNickname());
                    }
                }
            }
            Map rMap = new HashMap<>();
            rMap.put("bean", bean);
            rMap.put("lcTaskStateList", lcTaskStateList);
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据任务id修改任务
     * @date: 2025/12/17
     * @param:
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "updateTask")
    public ApiResult updateTask(@RequestBody LcTask lcTask) {
        try {
            return lcTaskService.updateTask(lcTask);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 任务管理列表(部门、个人）
     * @date: 2025/12/17
     * @param:
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getTaskList")
    public ApiResult getTaskList(@RequestBody Map map) {
        try {
            int pageSize = Integer.parseInt(map.get("pageSize").toString());
            int pageNo = Integer.parseInt(map.get("pageNo").toString());
            QueryWrapper<LcTask> q = getTaskQuery(map);
            int totalCount = lcTaskService.count(q);
            q.orderByDesc("update_time");
            Page<LcTask> page = new Page<>(pageNo, pageSize);
            IPage<LcTask> pageList = lcTaskService.page(page, q);
            if (pageList != null && pageList.getRecords().size() > 0) {
                for (LcTask lcTask : pageList.getRecords()) {
                    LcTask lcTaskInfo = getUserInfo(lcTask.getId());
                    lcTask.setUserNames(lcTaskInfo.getUserNames());
                    lcTask.setUserIds(lcTaskInfo.getUserIds());
                    lcTask.setUserDeptNames(lcTaskInfo.getUserDeptNames());
                    // 查询工作类别名称
                    LcDictInfo dictInfo = lcDictInfoDao.selectById(lcTask.getType());
                    if (dictInfo != null) {
                        lcTask.setTypeName(dictInfo.getName());
                    }
                }
            }
            page.setTotal(totalCount);
            return ApiResultHandler.buildApiResult(200, "操作成功", pageList);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 菜单列表
     * @date: 2025/12/18
     * @param: []
     * @return: com.cloud.core.ApiResult
     */
    @GetMapping(value = "getMenumList")
    public ApiResult getMenumList() {
        try {
            String roleIds = "";
            LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
            Set<SysRole> sysRoles = loginAppUser.getSysRoles();
            if (sysRoles == null || sysRoles.size() == 0) {
                return ApiResultHandler.buildApiResult(100, "当前登录用户没有分配权限", null);
            }
            for (SysRole sysRole : sysRoles) {
                roleIds += sysRole.getId() + ",";
            }
            roleIds = roleIds.substring(0, roleIds.length() - 1);
            // 查询当前用户拥有的菜单权限
            List<Map> menuByRoleIds = manageBackendFeign.getMenuByRoleIds(roleIds);
            JSONArray result = listToTree(JSONArray.parseArray(JSON.toJSONString(menuByRoleIds)), "id", "parentid", "children");
            Map rMap = new HashMap();
            rMap.put("result", result);
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 将list集合转换为树形结构
     * @date: 2025/12/18
     * @param: [arr, id, parentid, child]
     * @return: com.alibaba.fastjson.JSONArray
     */
    public static JSONArray listToTree(JSONArray arr, String id, String parentid, String child) {
        JSONArray r = new JSONArray();
        JSONObject hash = new JSONObject();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject json = (JSONObject) arr.get(i);
            hash.put(json.getString(id), json);
        }
        for (int j = 0; j < arr.size(); j++) {
            JSONObject aVal = (JSONObject) arr.get(j);
            JSONObject hashVp = (JSONObject) hash.get(aVal.get(parentid).toString());
            if (hashVp != null) {
                if (hashVp.get(child) != null) {
                    JSONArray ch = (JSONArray) hashVp.get(child);
                    ch.add(aVal);
                    hashVp.put(child, ch);
                } else {
                    JSONArray ch = new JSONArray();
                    ch.add(aVal);
                    hashVp.put(child, ch);
                }
            } else {
                r.add(aVal);
            }
        }
        return r;
    }


    /**
     * @author:胡立涛
     * @description: TODO 查询字典表分类
     * @date: 2025/12/23
     * @param:
     * @return: com.cloud.core.ApiResult
     */
    @GetMapping(value = "getDictClass")
    public ApiResult getDictClass() {
        try {
            QueryWrapper<LcDictClass> queryWrapper = new QueryWrapper<>();
            List<LcDictClass> lcDictClasses = lcDictClassDao.selectList(queryWrapper);
            return ApiResultHandler.buildApiResult(200, "操作成功", lcDictClasses);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据分类id查询字典表信息列表
     * @date: 2025/12/23
     * @param:
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getDictInfos")
    public ApiResult getDictInfos(@RequestBody LcDictClass lcDictClass) {
        try {
            QueryWrapper<LcDictInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("class_id", lcDictClass.getId());
            List<LcDictInfo> lcDictInfos = lcDictInfoDao.selectList(queryWrapper);
            return ApiResultHandler.buildApiResult(200, "操作成功", lcDictInfos);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 添加字典表信息
     * @date: 2025/12/23
     * @param:
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "addDictInfo")
    public ApiResult addDictInfo(@RequestBody LcDictInfo lcDictInfo) {
        try {
            lcDictInfoDao.insert(lcDictInfo);
            return ApiResultHandler.buildApiResult(200, "操作成功", lcDictInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 修改字典表信息
     * @date: 2025/12/23
     * @param:
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "updateDictInfo")
    public ApiResult updateDictInfo(@RequestBody LcDictInfo lcDictInfo) {
        try {
            LcDictInfo bean = lcDictInfoDao.selectById(lcDictInfo.getId());
            bean.setName(lcDictInfo.getName());
            lcDictInfoDao.updateById(bean);
            return ApiResultHandler.buildApiResult(200, "操作成功", bean);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 个人统计分析：部门、个人
     * @date: 2025/12/22
     * @param:
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getTongjiTaktOne")
    public ApiResult getTongjiTaktOne(@RequestBody Map map) {
        try {
            Map rMap = new HashMap<>();
            String startTime = map.get("startTime").toString();
            String endTime = map.get("endTime").toString() + " 23:59:59";
            // 查询任务列表ids  flg:0：个人任务 1：部门任务
            List<Integer> taskIdList = getTaskIdList(map);
            if (taskIdList.size() == 0) {
                return ApiResultHandler.buildApiResult(200, "操作成功", null);
            }

            // 当前进行中工作数量
            QueryWrapper<LcTask> q = new QueryWrapper<>();
            q.in("id", taskIdList);
            q.eq("state", 2);
            q.ge("rel_start_time", startTime);
            q.le("rel_start_time", endTime);
            int jxCount = taskDao.selectCount(q);
            List<LcTask> jxList = taskDao.selectList(q);
            if (jxList != null && jxList.size() > 0) {
                for (LcTask task : jxList) {
                    LcTask lcTask = getUserInfo(task.getId());
                    task.setUserIds(lcTask.getUserIds());
                    task.setUserNames(lcTask.getUserNames());
                    task.setUserDeptNames(lcTask.getUserDeptNames());
                    LcDictInfo lcDictInfo = lcDictInfoDao.selectById(task.getType());
                    task.setTypeName(lcDictInfo.getName());
                }
            }
            rMap.put("jxCount", jxCount);
            rMap.put("jxList", jxList);

            // 上周进行中的工作数量
            String times = getLastWeek();
            String lastWeekStartTime = times.split(",")[0];
            String lastWeekEndTime = times.split(",")[1];
            q = new QueryWrapper<>();
            q.in("id", taskIdList);
            q.eq("state", 2);
            q.ge("rel_start_time", lastWeekStartTime);
            q.le("rel_start_time", lastWeekEndTime);
            int lastWeekJxCount = taskDao.selectCount(q);
            rMap.put("lastWeekJxCount", lastWeekJxCount);

            // 本周需要完成的工作数量（时间段内）
            q = new QueryWrapper<>();
            q.in("id", taskIdList);
            q.ne("state", 3);
            q.ge("plan_end_time", startTime);
            q.le("plan_end_time", endTime);
            int thisWeekCount = taskDao.selectCount(q);
            rMap.put("thisWeekCount", thisWeekCount);

            // 时间段内需要完成的工作数量
            q = new QueryWrapper<>();
            q.in("id", taskIdList);
            q.ne("state", 3);
            q.ge("plan_end_time", startTime);
            q.le("plan_end_time", endTime);
            int doneCount = taskDao.selectCount(q);
            rMap.put("doneCount", doneCount);
            // 时间段内按期完成的任务数量
            q = new QueryWrapper<>();
            q.in("id", taskIdList);
            q.eq("state", 3);
            q.ge("rel_end_time", startTime);
            q.le("rel_end_time", endTime);
            q.eq("flg", 1);
            int onTimeCount = taskDao.selectCount(q);
            rMap.put("onTimeCount", onTimeCount);

            String onTimeRateStr = "0%";
            // 按期完成率
            double onTimeRate = 0.0;
            if (doneCount > 0) {
                onTimeRate = (double) onTimeCount / doneCount;
                onTimeRateStr = String.format("%.1f%%", onTimeRate * 100);
            }
            rMap.put("onTimeRate", onTimeRateStr);

            // 时间段内未按时完成的任务数量(超时/紧急工作）
            q = new QueryWrapper<>();
            q.in("id", taskIdList);
            q.eq("flg", 0);
            q.ne("state", 3);
            q.ge("plan_end_time", startTime);
            q.le("plan_end_time", endTime);
            int notOnTimeCount = taskDao.selectCount(q);
            List<LcTask> notOnTimeTasks = taskDao.selectList(q);
            if (notOnTimeTasks != null && notOnTimeTasks.size() > 0) {
                for (LcTask task : notOnTimeTasks) {
                    LcTask lcTask = getUserInfo(task.getId());
                    task.setUserIds(lcTask.getUserIds());
                    task.setUserNames(lcTask.getUserNames());
                    task.setUserDeptNames(lcTask.getUserDeptNames());
                    LcDictInfo lcDictInfo = lcDictInfoDao.selectById(task.getType());
                    task.setTypeName(lcDictInfo.getName());
                }
            }
            rMap.put("notOnTimeCount", notOnTimeCount);
            rMap.put("notOnTimeTasks", notOnTimeTasks);


            // 时间段内完成任务数量
            q = new QueryWrapper<>();
            q.in("id", taskIdList);
            q.eq("state", 3);
            q.ge("rel_end_time", startTime);
            q.le("rel_end_time", endTime);
            int deptDoneCount = taskDao.selectCount(q);
            rMap.put("deptDoneCount", deptDoneCount);

            // 时间段内计划完成任务数量
            q = new QueryWrapper<>();
            q.in("id", taskIdList);
            q.ge("plan_end_time", startTime);
            q.le("plan_end_time", endTime);
            int deptPlanCount = taskDao.selectCount(q);
            rMap.put("deptPlanCount", deptPlanCount);

            // 整体完成率
            String deptOnTimeRateStr = "0%";
            double deptOnTimeRate = 0.0;
            if (deptPlanCount > 0) {
                deptOnTimeRate = (double) deptDoneCount / deptPlanCount;
                deptOnTimeRateStr = String.format("%.1f%%", deptOnTimeRate * 100);
            }
            rMap.put("deptOnTimeRate", deptOnTimeRateStr);

            // 上月完成任务数量
            String lastMonth = LcDateUtil.getLastMonth();
            String lastMonthStartTime = lastMonth.split(",")[0];
            String lastMonthEndTime = lastMonth.split(",")[1];
            q = new QueryWrapper<>();
            q.in("id", taskIdList);
            q.eq("state", 3);
            q.ge("rel_end_time", lastMonthStartTime);
            q.le("rel_end_time", lastMonthEndTime);
            int lastMonthDoneCount = taskDao.selectCount(q);
            rMap.put("lastMonthDoneCount", lastMonthDoneCount);

            // 上月计划完成任务数量
            q = new QueryWrapper<>();
            q.in("id", taskIdList);
            q.ge("plan_end_time", lastMonthStartTime);
            q.le("plan_end_time", lastMonthEndTime);
            int lastMonthPlanCount = taskDao.selectCount(q);
            rMap.put("lastMonthPlanCount", lastMonthPlanCount);

            // 上月完成任务率
            double lastMonthOnTimeRate = 0.0;
            String lastMonthOnTimeRateStr = "0%";
            if (lastMonthDoneCount > 0) {
                lastMonthOnTimeRate = (double) lastMonthDoneCount / lastMonthPlanCount;
                lastMonthOnTimeRateStr = String.format("%.1f%%", lastMonthOnTimeRate * 100);
            }
            rMap.put("lastMonthOnTimeRate", lastMonthOnTimeRateStr);

            // 较上月完成率比较值
            double percent1 = Double.parseDouble(deptOnTimeRateStr.replace("%", ""));
            double percent2 = Double.parseDouble(lastMonthOnTimeRateStr.replace("%", ""));
            double difference = percent1 - percent2;
            DecimalFormat df = new DecimalFormat("0.0");
            rMap.put("difference", df.format(difference) + "%");

            // 时间段内所有的任务数量
            q = new QueryWrapper<>();
            q.in("id", taskIdList);
            q.ge("plan_start_time", startTime);
            q.le("plan_start_time", endTime);
            int allCount = taskDao.selectCount(q);
            rMap.put("allCount", allCount);

            // 时间段内未开始的任务数量(待办任务)
            q = new QueryWrapper<>();
            q.in("id", taskIdList);
            q.eq("state", 1);
            q.ge("plan_start_time", startTime);
            q.le("plan_start_time", endTime);
            int notStartCount = taskDao.selectCount(q);
            rMap.put("notStartCount", notStartCount);

            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", ex.toString());
        }
    }

    // 查询任务的责任人信息
    public LcTask getUserInfo(int taskId) {
        QueryWrapper<LcTaskUser> q = new QueryWrapper<>();
        q.eq("task_id", taskId);
        List<LcTaskUser> list = taskUserDao.selectList(q);
        if (list == null || list.size() == 0) {
            return null;
        }
        String userNames = "";
        String userIds = "";
        String userDeptNames = "";
        for (LcTaskUser taskUser : list) {
            AppUser byId = appUserDao.findById(Long.valueOf(taskUser.getUserId()));
            if (byId != null) {
                SysDepartment byId1 = sysDepartmentDao.selectById(byId.getDepartmentId());
                if (byId1 != null) {
                    if (taskUser.getFlg() == 1) {
                        userNames += byId.getNickname() + "(创建人)" + ",";
                    } else {
                        userNames += byId.getNickname() + ",";
                        userIds += byId.getId() + ",";
                    }
                    userDeptNames += byId1.getDname() + ",";
                }
            }
        }
        if (userNames != "") {
            userNames = userNames.substring(0, userNames.length() - 1);
            userIds = userIds.substring(0, userIds.length() - 1);
            userDeptNames = userDeptNames.substring(0, userDeptNames.length() - 1);
        }
        LcTask lcTask = new LcTask();
        lcTask.setUserIds(userIds);
        lcTask.setUserNames(userNames);
        lcTask.setUserDeptNames(userDeptNames);
        return lcTask;
    }


    // 查询任务列表ids  flg:0：个人任务 1：部门任务
    public List<Integer> getTaskIdList(Map map) {
        String deptIds = map.get("deptIds") == null ? null : map.get("deptIds").toString();
        int flg = map.get("flg") == "" ? 0 : Integer.parseInt(map.get("flg").toString());
        List<Integer> deptIdListBase = new ArrayList<>();
        if (deptIds != null && !deptIds.equals("")) {
            Map deptMap = new HashMap();
            deptMap.put("deptId", Integer.parseInt(deptIds));
            List<Map> maps = taskDao.selectLcTaskList(deptMap);
            for (Map map1 : maps) {
                deptIdListBase.add(Integer.parseInt(map1.get("id").toString()));
            }
        }
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        QueryWrapper<LcTaskUser> lcTaskUserQueryWrapper = new QueryWrapper<>();
        if (flg == 1) {
            Map m = new HashMap<>();
            m.put("deptId", Integer.parseInt(loginAppUser.getDepartmentId().toString()));
            List<Integer> deptIdList = new ArrayList<>();
            List<Map> list1 = taskDao.selectLcTaskList(m);
            if (list1 == null || list1.size() <= 0) {
                deptIdList.add(-1);
            } else {
                for (Map map1 : list1) {
                    deptIdList.add((Integer) map1.get("id"));
                }
            }
            // 取有权限的部门
            if (deptIdListBase != null && deptIdListBase.size() > 0) {
                deptIdList.retainAll(deptIdListBase);
            }
            if (deptIdList.size()==0){
                deptIdList.add(-1);
            }
            lcTaskUserQueryWrapper.in("dept_id", deptIdList);
        } else {
            lcTaskUserQueryWrapper.eq("user_id", loginAppUser.getId());
        }
        List<Integer> taskIdList = new ArrayList<>();
        List<LcTaskUser> list = taskUserDao.selectList(lcTaskUserQueryWrapper);
        if (list != null & list.size() > 0) {
            for (LcTaskUser taskUser : list) {
                taskIdList.add(taskUser.getTaskId());
            }
        }
        return taskIdList;
    }


    /**
     * @author:胡立涛
     * @description: TODO 统计分析：工作方向分析（个人、部门）
     * @date: 2025/12/25
     * @param:
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getTongjiTakType")
    public ApiResult getTongjiTakType(@RequestBody Map map) {
        try {
            String startTime = map.get("startTime").toString();
            String endTime = map.get("endTime").toString() + " 23:59:59";
            List<Integer> taskIdList = getTaskIdList(map);
            if (taskIdList.size() == 0) {
                return ApiResultHandler.buildApiResult(200, "操作成功", null);
            }
            QueryWrapper<LcTask> q = new QueryWrapper<>();
            q.select("type", "count(*) as count");
            q.in("id", taskIdList);
            q.ge("plan_end_time", startTime);
            q.le("plan_end_time", endTime);
            q.eq("state", 3);
            q.groupBy("type");
            List<Map<String, Object>> maps = taskDao.selectMaps(q);
            if (maps != null && maps.size() > 0) {
                for (Map map1 : maps) {
                    int type = Integer.parseInt(map1.get("type").toString());
                    map1.put("typeName", lcDictInfoDao.selectById(type) == null ? "" : lcDictInfoDao.selectById(type).getName());
                }
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", maps);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", ex.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 统计分析：工作方向分析（个人、部门）
     * @date: 2026/01/04
     * @param:
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getTongjiTaskUser")
    public ApiResult getTongjiTaskUser(@RequestBody Map map){
        try {

            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        }catch (Exception e){
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    // 任务管理、个人任务列表查询条件构造
    public QueryWrapper<LcTask> getTaskQuery(Map map) {
        // 1：部门 0：个人，默认查询个人
        int flg = map.get("flg") == null ? 0 : Integer.parseInt(map.get("flg").toString());
        String deptIds = map.get("deptIds") == null ? null : map.get("deptIds").toString();
        String startTime = map.get("startTime").toString();
        String endTime = map.get("endTime").toString() + " 23:59:59";
        List<Integer> deptIdListBase = new ArrayList<>();
        if (deptIds != null && !deptIds.equals("")) {
            Map deptMap = new HashMap();
            deptMap.put("deptId", Integer.parseInt(deptIds));
            List<Map> maps = taskDao.selectLcTaskList(deptMap);
            for (Map map1 : maps) {
                deptIdListBase.add(Integer.parseInt(map1.get("id").toString()));
            }
        }
        AppUser appUser = AppUserUtil.getLoginAppUser();
        QueryWrapper<LcTaskUser> queryWrapper = new QueryWrapper<>();
        if (flg == 1) {
            Map m = new HashMap<>();
            m.put("deptId", Integer.parseInt(appUser.getDepartmentId().toString()));
            List<Integer> deptIdList = new ArrayList<>();
            List<Map> list1 = taskDao.selectLcTaskList(m);
            if (list1 == null || list1.size() <= 0) {
                deptIdList.add(-1);
            } else {
                for (Map map1 : list1) {
                    deptIdList.add((Integer) map1.get("id"));
                }
            }
            // 取有权限的部门
            if (deptIdListBase != null && deptIdListBase.size() > 0) {
                deptIdList.retainAll(deptIdListBase);
            }
            if (deptIdList.size()==0){
                deptIdList.add(-1);
            }
            queryWrapper.in("dept_id", deptIdList);
        } else {
            queryWrapper.eq("user_id", appUser.getId().intValue());
        }
        List<Integer> taskIdList = new ArrayList<>();
        List<LcTaskUser> userList = taskUserDao.selectList(queryWrapper);
        if (userList == null || userList.size() <= 0) {
            taskIdList.add(0);
        } else {
            for (LcTaskUser lcTaskUser : userList) {
                taskIdList.add(lcTaskUser.getTaskId());
            }
        }
        // 查询任务列表
        QueryWrapper<LcTask> q = new QueryWrapper<>();
        q.in("id", taskIdList);
        String keyWord = map.get("keyWord") == null ? null : map.get("keyWord").toString();
        // 工作类别
        String type = map.get("type") == null ? null : map.get("type").toString();
        if (type != null && !type.equals("")) {
            q.in("type", Integer.parseInt(type));
        }
        if (keyWord != null && !keyWord.equals("")) {
            q.like("name", keyWord);
        }
        // 状态
        String state = map.get("state") == null ? null : map.get("state").toString();
        if (state != null && !state.equals("")) {
            // 未按时完成的任务
            if (Integer.parseInt(state) == 4) {
                q.eq("flg", 0);
                q.ne("state", 3);
                q.ge("plan_end_time", startTime);
                q.le("plan_end_time", endTime);
            } else {
                q.eq("state", Integer.parseInt(state));
                if (Integer.parseInt(state) == 3) {
                    q.ge("rel_end_time", startTime);
                    q.le("rel_end_time", endTime);
                } else {
                    q.ge("plan_start_time", startTime);
                    q.le("plan_start_time", endTime);
                }
            }
        } else {
            q.ge("plan_start_time", startTime);
            q.le("plan_start_time", endTime);
        }
        // 紧急程度
        String jjcd = map.get("jjcd") == null ? null : map.get("jjcd").toString();
        if (jjcd != null && !jjcd.equals("")) {
            List<Integer> jjcdList = Arrays.asList(Integer.parseInt(jjcd.split(",").toString()));
            q.in("jjcd", jjcdList);
        }
        // 优先级别
        String yxj = map.get("yxj") == null ? null : map.get("yxj").toString();
        if (yxj != null && !yxj.equals("")) {
            q.in("yxj", yxj);
        }
        return q;
    }


    /**
     * @author:胡立涛
     * @description: TODO 导出任务excel
     * @date: 2025/12/30
     * @param:
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "exportTaskExcel")
    public ApiResult exportTaskExcel(@RequestBody Map map) {
        try {
            QueryWrapper<LcTask> q = getTaskQuery(map);
            q.orderByDesc("update_time");
            Set<Map<String, Object>> taskSet = new HashSet<>();
            List<LcTask> pageList = lcTaskService.list(q);
            if (pageList != null && pageList.size() > 0) {
                int index = 1;
                for (LcTask lcTask : pageList) {
                    Map<String, Object> taskMap = new LinkedHashMap<>();
                    // 查询工作类别名称
                    LcDictInfo dictInfo = lcDictInfoDao.selectById(lcTask.getType());
                    if (dictInfo != null) {
                        lcTask.setTypeName(dictInfo.getName());
                    }
                    taskMap.put("index", index);
                    taskMap.put("name", lcTask.getName());
                    taskMap.put("typeName", lcTask.getTypeName());
                    taskMap.put("content", lcTask.getContent());
                    taskMap.put("planStartTime", lcTask.getPlanStartTime());
                    taskMap.put("planEndTime", lcTask.getPlanEndTime());
                    // 优先级别 1：紧急 2：普通
                    taskMap.put("yxj", lcTask.getYxj().equals("1") ? "紧急" : "普通");
                    LcTask lcTaskInfo = getUserInfo(lcTask.getId());
                    lcTask.setUserNames(lcTaskInfo.getUserNames());
                    lcTask.setUserDeptNames(lcTaskInfo.getUserDeptNames());
                    taskMap.put("userNames", lcTask.getUserNames());
                    taskMap.put("userDeptNames", lcTask.getUserDeptNames());
                    taskMap.put("wcjd", lcTask.getWcjd() == null ? "0%" : lcTask.getWcjd() + "%");
                    taskMap.put("realStartTime", lcTask.getRelStartTime());
                    taskMap.put("realEndTime", lcTask.getRelEndTime());
                    // 任务状态 1：未开始 2：进行中 3：已完成
                    taskMap.put("state", lcTask.getState() == 1 ? "未开始" : (lcTask.getState() == 2 ? "进行中" : "已完成"));
                    index += 1;
                    taskSet.add(taskMap);
                }
            }
            Map<String, String> dataMap = new LinkedHashMap<>();
            dataMap.put("index", "序号");
            dataMap.put("name", "任务名称");
            dataMap.put("typeName", "任务类别");
            dataMap.put("content", "主要工作内容");
            dataMap.put("planStartTime", "计划开始时间");
            dataMap.put("planEndTime", "计划结束时间");
            dataMap.put("yxj", "优先级别");
            dataMap.put("userNames", "责任人");
            dataMap.put("userDeptNames", "部门");
            dataMap.put("wcjd", "完成进度");
            dataMap.put("realStartTime", "实际开始时间");
            dataMap.put("realEndTime", "实际结束时间");
            dataMap.put("state", "任务状态");

            String uuidName = String.valueOf(System.currentTimeMillis());
            File file = new File(localFilePath + uuidName + ".xls");
            System.out.println("导出文件路径：" + file.getPath());
            FileOutputStream outputStream = new FileOutputStream(file);

            ExcelUtil.exportExcel(dataMap, taskSet, outputStream);
            String filePath = localUrlPrefix + file.getPath().split("movetoFile")[1];
            return ApiResultHandler.buildApiResult(200, "操作成功", filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }
}
