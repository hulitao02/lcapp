package com.cloud.model.controller;

import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ResultMesEnum;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.dao.StudyTimeDao;
import com.cloud.model.dao.StudyTimeStatisticsDao;
import com.cloud.model.service.StudyTimeService;
import com.cloud.model.user.AppUser;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.model.utils.CommonDate;
import com.cloud.utils.CollectionsCustomer;
import io.swagger.annotations.ApiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/studytime")
@ApiModel(value = "学习时长")
@Slf4j
@RefreshScope
public class StudyTimeController {

    @Autowired
    StudyTimeDao studyTimeDao;
    @Autowired
    StudyTimeService studyTimeService;
    // 时间间隔
    @Value(value = "${time_minute}")
    private int timeMinute;
    @Autowired
    StudyTimeStatisticsDao studyTimeStatisticsDao;
    @Autowired
    SysDepartmentFeign sysDepartmentFeign;


    /**
     * @author:胡立涛
     * @description: TODO 添加知识学习时间
     * @date: 2022/4/11
     *
     * @param: [studyTime]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "saveInfo")
    public ApiResult saveInfo(@RequestBody Map<String, Object> map) {
        try {
            map.put("timeCount", timeMinute);
            map.put("viewState", map.get("viewState") == null ? 2 : Integer.parseInt(map.get("viewState").toString()));
            studyTimeService.saveInfo(map);
            return ApiResultHandler.buildApiResult(ResultMesEnum.SUCCESS.getResultCode(), "操作成功", timeMinute);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 更新学习时间数据
     * @date: 2022/4/11
     * @param: [map]
     *
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "updateInfo")
    public ApiResult updateInfo(@RequestBody Map<String, Object> map) {
        try {
            // 计算数据库表名称
            String tableName = getTableName();
            map.put("tableName", tableName);
            // 根据用户id，知识id查询最新数据
            Map<String, Object> info = studyTimeDao.getInfo(map);
            if (info == null) {
                return ApiResultHandler.buildApiResult(100, "根据用户id，知识id没有查询到对应的数据", null);
            }
//            info = CollectionsCustomer.builder().build().mapToLowerCase(info);
            Timestamp endTime = Timestamp.valueOf(map.get("endTime").toString());
            map.put("id", info.get("id"));
            map.put("updateTime", new Timestamp(System.currentTimeMillis()));
            map.put("endTime", Timestamp.valueOf(map.get("endTime").toString()));
            Timestamp startTime = Timestamp.valueOf(info.get("start_time").toString());
            map.put("timeCount", CommonDate.chaTimes(startTime, endTime));
            studyTimeDao.updateInfo(map);
            return ApiResultHandler.buildApiResult(ResultMesEnum.SUCCESS.getResultCode(), "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 查询所有用户的学习时长
     * @date: 2022/4/11
     * @param: []
     * @return: com.cloud.core.ApiResult
     */
    @GetMapping(value = "getStudyMinutes")
    public ApiResult getStudyMinutes() {
        try {
            Map<String, Object> parMap = new HashMap<>();
            // 数据库表名称计算
            String tableName = getTableName();
            SimpleDateFormat curDate = new SimpleDateFormat("yyyyMMdd");
            String timeDay = curDate.format(new Date());
            int table = studyTimeDao.getTable(tableName);
            if (table > 0) {
                // 根据日期删除统计表中的数据
                parMap.put("tableName", tableName);
                parMap.put("timeDay", timeDay);
                studyTimeService.calculationStudyTime(parMap);
            }
            List<Map<String, Object>> totalInfo = studyTimeStatisticsDao.getTotalInfo();
            if (totalInfo != null && totalInfo.size() > 0) {
                for (Map map : totalInfo) {
                    AppUser appUser = sysDepartmentFeign.findAppUserById(Long.valueOf(map.get("user_id").toString()));
                    map.put("userName", appUser.getNickname());
                }
            }
            return ApiResultHandler.buildApiResult(ResultMesEnum.SUCCESS.getResultCode(), "操作成功", totalInfo);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 查询所有用户的学习时长
     * @date: 2022/4/11
     * @param: []
     * @return: com.cloud.core.ApiResult
     */
    @GetMapping(value = "getPerStudyHoursAndRank")
    public Map<String,Object> getPerStudyHoursAndRank() {
        Map<String, Object> parMap = new HashMap<>();
        Double studyhours = 0.0d;
        Integer rank = 0;
        try {
            Long id = AppUserUtil.getLoginAppUser().getId();
            SimpleDateFormat curDate = new SimpleDateFormat("yyyyMM");
            String timeDay = curDate.format(new Date());

            List<Map<String, Object>> totalInfo = studyTimeStatisticsDao.getPerStudyHoursAndRank(timeDay);
//            totalInfo=CollectionsCustomer.builder().build().listMapToLowerCase(totalInfo);

            if (totalInfo != null && totalInfo.size() > 0) {
                for (Map map : totalInfo) {
                     Long user_id = Long.valueOf(map.get("user_id").toString());
                    if(id.equals(user_id)){
                        Long studyminutes = Long.valueOf(map.get("studyminutes").toString());
                        double hours = studyminutes/60.0;
                        DecimalFormat df = new DecimalFormat("##.#");
                        String format = df.format(hours);
                        studyhours = Double.valueOf(format);
                        rank = Integer.valueOf(map.get("rank").toString());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取用户本月学习时间错误。");
        }
        parMap.put("studyhours",studyhours);
        parMap.put("rank",rank);
        return parMap ;

    }

    /**
     * @author:胡立涛
     * @description: TODO 计算表名称
     * @date: 2022/4/11
     * @param: []
     * @return: java.lang.String
     */
    public String getTableName() {
        SimpleDateFormat curDate = new SimpleDateFormat("yyyyMM");
        String curDateStr = curDate.format(new Date());
        String tableName = "study_time_" + curDateStr;
        return tableName;
    }

}
