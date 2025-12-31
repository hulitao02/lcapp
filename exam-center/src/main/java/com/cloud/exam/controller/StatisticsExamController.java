package com.cloud.exam.controller;

import cn.hutool.core.util.ObjectUtil;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.exam.dao.QuestionDao;
import com.cloud.exam.model.exam.DrawResult;
import com.cloud.exam.service.ExamService;
import com.cloud.exam.utils.Tools;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.exam.ExamStatisticsDto;
import com.cloud.model.user.AppUser;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.utils.DateConvertUtils;
import com.cloud.utils.StringUtils;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

//import com.cloud.utils.CollectionsCustomer;

@RestController
@RefreshScope
public class StatisticsExamController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsExamController.class);

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private ExamService examService;

    @Autowired
    private SysDepartmentFeign sysDepartmentFeign;


    @GetMapping("/statistics/groupbyType")
    public List<Map> getQuestionStatisticsByType() {
        List<Map> mapList = this.questionDao.getQuestionStatisticsByType();
//        mapList = CollectionsCustomer.builder().build().listMapToLowerCase(mapList);
        return mapList;
    }

    ;

    @GetMapping("/statistics/groupbyTypeAndDifficulty")
    public List<Map> getQuestionStatisticsByTypeAndDifficulty() {
        List<Map> mapList = this.questionDao.getQuestionStatisticsByTypeAndDifficulty();
//        mapList = CollectionsCustomer.builder().build().listMapToLowerCase(mapList);
//        11	0.10	其他
//        93	0.20	单选
//        2	    0.04	情报分析
//        1	    0.09	判断
        return mapList;
    }

    ;

    @GetMapping("/statistics/questionsIsUsed")
    public List<Map> getQuestionStatisticsByIsUsed() {
        List<Map> mapList = this.questionDao.getQuestionStatisticsByIsUsed();
//        mapList = CollectionsCustomer.builder().build().listMapToLowerCase(mapList);
        return mapList;
    }

    ;


    /**
     * 自测 和 完成训练的数量
     *
     * @param examDTO
     * @return
     */
    @ApiOperation(value = "以年为单位，每个月的自测的数量和完成训练任务的数量")
    @RequestMapping(value = "/statistics/getFinishedExamList", method = RequestMethod.POST)
    public ApiResult getFinishedSelfAndTranCount(@RequestBody ExamStatisticsDto examDTO) {
//        时间查询年份
        String timeParamPre = examDTO.getQueryDateYy();
//      每个月的自测数量
        ExamStatisticsDto selfDto = new ExamStatisticsDto();
        selfDto.setQueryDateYy(examDTO.getQueryDateYy());
        List<Integer> selfFinishedStatus = new ArrayList<>();
        selfFinishedStatus.add(4);
        List<Integer> selfTypeList = new ArrayList<>();
        selfTypeList.add(3);
        selfDto.setExamTypeList(selfTypeList);
        selfDto.setExamStatusList(selfFinishedStatus);
        List<Map<String, Object>> selfCountList = this.examService.getStatisticsExamCountByTypeAndStatus(selfDto);
//        TODO 大小写敏感
//        selfCountList = CollectionsCustomer.builder().build().listMapToLowerCase(selfCountList);
//      每个月完成训练的数量
        ExamStatisticsDto trainDto = new ExamStatisticsDto();
        selfDto.setQueryDateYy(examDTO.getQueryDateYy());
        List<Integer> trainTypeList = new ArrayList<>();
        trainTypeList.add(2);
        trainDto.setExamTypeList(selfTypeList);
//       完成状态
        List<Integer> trainFinishedStatus = new ArrayList<>();
        trainFinishedStatus.add(4);
        trainDto.setExamTypeList(trainTypeList);
        trainDto.setExamStatusList(trainFinishedStatus);
        List<Map<String, Object>> trainCountList = this.examService.getStatisticsExamCountByTypeAndStatus(trainDto);
//        trainCountList = CollectionsCustomer.builder().build().listMapToLowerCase(trainCountList);
        /**
         * 补全12个月，调整数据结构 给前端用
         */
        Map resultMap = new HashMap();

        List<String> legend = new ArrayList<>();
        List<String> xAxis = new ArrayList<>();
        List<Map> series = new LinkedList<>();
        legend.add("自测数量");
        legend.add("完成训练数量");
//       两个Map
        Map selfMap = new HashMap();
        List selfDataList = new ArrayList();
//        训练
        Map trainMap = new HashMap();
        List trainDataList = new ArrayList();
        for (int i = 1; i <= 12; i++) {
            String time_key = i + "月";
//            加入时间
            xAxis.add(time_key);
//          数据
            String db_time = String.format("%s-%s", timeParamPre, (i < 10 ? "0" + i : i));
            Optional<Map<String, Object>> optionTrain = trainCountList.stream().filter(map -> {
                String time = map.get("date").toString();
                return db_time.equals(time);
            }).findFirst();
            if (optionTrain.isPresent()) {
                Object count = optionTrain.get().get("count");
                trainDataList.add(count);
            } else {
                trainDataList.add(0);
            }
//          自测
            Optional<Map<String, Object>> optionSelf = selfCountList.stream().filter(map -> {
                String time = map.get("date").toString();
                return db_time.equals(time);
            }).findFirst();
            if (optionSelf.isPresent()) {
                Object count = optionSelf.get().get("count");
                selfDataList.add(count);
            } else {
                selfDataList.add(0);
            }
        }
        selfMap.put("name", "自测数量");
        selfMap.put("data", selfDataList);
        trainMap.put("name", "完成训练数量");
        trainMap.put("data", trainDataList);
        series.add(selfMap);
        series.add(trainMap);
//        最后整合 数据
        resultMap.put("series", series);
        resultMap.put("xAxis", xAxis);
        resultMap.put("legend", legend);
        return ApiResultHandler.buildApiResult(200, "自测和完成训练折线统计", resultMap);
    }


    /**
     * 今年完成了多少场考试，参与了多少场竞答
     *
     * @param examStatisticsDto
     * @return TODO 类型写死 ，目前只有考核和竞答
     */
    @ApiOperation(value = "今年完成了多少场考试，参与了多少场竞答")
    @RequestMapping(value = "/statistics/getFinishedExamAndCompiting", method = RequestMethod.POST)
    public ApiResult getFinishedExamAndCompiting(@RequestBody ExamStatisticsDto examStatisticsDto) {
        /**
         *  已人的维度进行 统计
         *  考试
         */
        ExamStatisticsDto examDto = new ExamStatisticsDto();
        List<Integer> exTypeList = new ArrayList<>();
        exTypeList.add(0);
        examDto.setExamTypeList(exTypeList);
        examDto.setExamStatusList(examStatisticsDto.getExamStatusList());
        examDto.setUserId(examStatisticsDto.getUserId());
        examDto.setQueryDateYy(examStatisticsDto.getQueryDateYy());
        List<Map<String, Object>> exCountList = this.examService.getStatisticsExCountGroupUserId(examDto);
//        exCountList =  CollectionsCustomer.builder().build().listMapToLowerCase(exCountList);
        /**
         *  竞答
         */
        ExamStatisticsDto jdDto = new ExamStatisticsDto();
        List<Integer> jdDtoTypeList = new ArrayList<>();
        jdDtoTypeList.add(1);
        jdDto.setExamTypeList(jdDtoTypeList);
        jdDto.setExamStatusList(examStatisticsDto.getExamStatusList());
        jdDto.setUserId(examStatisticsDto.getUserId());
        jdDto.setQueryDateYy(examStatisticsDto.getQueryDateYy());
        List<Map<String, Object>> jdCountList1 = this.examService.getStatisticsExCountGroupUserId(jdDto);
//        List<Map<String, Object>> jdCountList_dm = CollectionsCustomer.builder().build().listMapToLowerCase(jdCountList1);
        /**
         *  考核和竞答 组装数据
         */
//      最终返回结果
//      0(在线考核)、1(竞答)、2(训练)、3(自测)
        Map<String, Integer> resultMap = new HashMap<>();
        if (CollectionUtils.isEmpty(exCountList)) {
            resultMap.put("ks", 0);
            if (CollectionUtils.isNotEmpty(jdCountList1)) {
                resultMap.put("jd", Integer.valueOf(jdCountList1.get(0).get("count").toString()));
            } else {
                resultMap.put("jd", 0);
            }
        } else {
            exCountList.stream().forEach(exMap -> {
                Integer userid = Integer.valueOf(exMap.get("userid").toString());
//              考试
                resultMap.put("ks", Integer.valueOf(exMap.get("count").toString()));
                if (CollectionUtils.isNotEmpty(jdCountList1)) {
                    Optional<Map<String, Object>> jdOption = jdCountList1.stream().filter(jdMap -> {
                        Integer userid1 = Integer.valueOf(jdMap.get("userid").toString());
                        return userid.equals(userid1);
                    }).findFirst();
                    if (jdOption.isPresent()) {
                        Map<String, Object> stringObjectMap = jdOption.get();
                        Integer count = Integer.valueOf(stringObjectMap.get("count").toString());
                        resultMap.put("jd", count);
                    } else {
                        resultMap.put("jd", 0);
                    }
                } else {
                    resultMap.put("jd", 0);
                }
            });
        }
        return ApiResultHandler.buildApiResult(200, "完成的考试和竞答活动.[ks:考试 jd:竞答]", resultMap);
    }


    /**
     * 查询 训练type = 2
     * TODO参数类型， 修改，考试的状态，和类型 会出现LIST的情况
     *  考生状态(0 未登录 1 已登录 3 考试结束待判卷  4 已完成 5 考试取消 6 考试开始)
     *  活动状态
     *  0:待启动、1已启动、2活动开始、6开始考试、3考试结束待阅卷、4完成、5:取消 (0,5)
     * @param examDTO
     * @return
     */
    @ApiOperation(value = "以年为单位，统计每个月开展训练任务数量、参加训练人数。【名称：各月训练人员数量和参训人数统计")
    @RequestMapping(value = "/statistics/getStatisticsExamCountAndUserCount", method = RequestMethod.POST)
    public List<Map> getStatisticsExamCountAndUserCount(@RequestBody ExamStatisticsDto examDTO) {

        String paramsDate = examDTO.getQueryDateYy();
        // 增加考生状态 。
        Integer[] userStatusArray = new Integer[]{1,3,4,6};
        List<Integer> userStatusList = Arrays.asList(userStatusArray);
        examDTO.setUserStatusList(userStatusList);

        // 增加活动状态
        Integer[] examStatusArray = new Integer[]{1,2,3,4,6};
        List<Integer> examStatusList = Arrays.asList(examStatusArray);
        examDTO.setExamStatusList(examStatusList);

        /**
         *  补齐一年中12个月，每个月 训练的总数 ，和参加的人原总数
         */
        List<Map<String, Object>> examCountList = this.examService.getStatisticsExamCountByTypeAndStatus(examDTO);
//        examCountList = CollectionsCustomer.builder().build().listMapToLowerCase(examCountList);

        List<Map<String, Object>> userCountList = this.examService.getStatisticsExUserCountByTypeAndStatus(examDTO);
//        userCountList = CollectionsCustomer.builder().build().listMapToLowerCase(userCountList);
        List<Map> resultList = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String date_key = paramsDate + "-" + (i < 10 ? "0" + i : i);
            String date_month_key = String.format("%s月", i);
//          重新赋值的MAP
            Map<String, Object> newMap = new LinkedHashMap<>();
            newMap.put("date", date_month_key);
            Optional<Map<String, Object>> returnExamMap = examCountList.stream().filter(excountMap -> {
                return excountMap.values().contains(date_key);
            }).findFirst();
//            如果有值，那么赋值
            if (returnExamMap.isPresent()) {
                Map<String, Object> stringObjectMap = returnExamMap.get();
                newMap.put("examCount", stringObjectMap.get("count"));
            } else {
                newMap.put("examCount", 0);
            }
//          参加考试人数
            Optional<Map<String, Object>> returnUserMap = userCountList.stream().filter(userCountMap -> {
                return userCountMap.values().contains(date_key);
            }).findFirst();
            if (returnUserMap.isPresent()) {
                Map<String, Object> stringObjectMap = returnUserMap.get();
                newMap.put("userCount", stringObjectMap.get("count"));
            } else {
                newMap.put("userCount", 0);
            }
            resultList.add(newMap);
        }
        return resultList;
    }


    /**
     *
     * 活动类型:
     * 0(在线考核)、1(竞答)、2(训练)、3(自测)
     *  自测类型 type = 3
     * 自测数量按照 从高到底排序，已自测的数量为基础
     *      *  考生状态(0 未登录 1 已登录 3 考试结束待判卷  4 已完成 5 考试取消 6 考试开始)
     *      *  活动状态
     *      *  0:待启动、1已启动、2活动开始、6开始考试、3考试结束待阅卷、4完成、5:取消 (0,5)
     * @param examDTO
     * @return
     */
    @ApiOperation(value = "以年为单位，排名自测数量最多的前10个学员，展示姓名、自测次数和自测题数")
    @RequestMapping(value = "/statistics/getStatisticsUsersExamAndQuestionCount", method = RequestMethod.POST)
    public Map getStatisticsUsersExamAndQuestionCount(@RequestBody ExamStatisticsDto examDTO) {


        List<Map<String, Object>> examCountList = new ArrayList<>();
        List<Map<String, Object>> questionsCountList = new ArrayList<>();
        /**
         *  根 据考试类型的不同 3 和 其他
         */
        Integer[] userStatusArray = new Integer[]{1,3,4,6};
        List<Integer> userStatusList = Arrays.asList(userStatusArray);
        examDTO.setUserStatusList(userStatusList);
        // 增加活动状态
        Integer[] examStatusArray = new Integer[]{1,2,3,4,6};
        List<Integer> examStatusList = Arrays.asList(examStatusArray);
        examDTO.setExamStatusList(examStatusList);

        List<Integer> examTypeList = examDTO.getExamTypeList();
        if (CollectionUtils.isNotEmpty(examTypeList) && examTypeList.contains(3)) {
            examCountList.addAll(
                    this.examService.getStatisticsSelfExCountGroupUserId(examDTO)
            );
            questionsCountList.addAll(
            this.examService.getStatisticsSelfQesCountGroupUserId(examDTO)
            );
        } else {
            examCountList.addAll(
                    this.examService.getStatisticsExCountGroupUserId(examDTO)
            );
            questionsCountList.addAll(
                    this.examService.getStatisticsQesCountGroupUserId(examDTO)
            );
        }
        /**
         *  用户ID 并集
         */
        List<Long> userIdAllList = new ArrayList<>();
        examCountList.stream().forEach(map -> {
            Object userId1 = map.get("userid");
            if (Objects.nonNull(userId1)) {
                Long userId = Long.valueOf(userId1.toString());
                userIdAllList.add(userId);
            }
        });
        questionsCountList.stream().forEach(map -> {
            Object userId1 = map.get("userid");
            if (Objects.nonNull(userId1)) {
                Long userId = Long.valueOf(userId1.toString());
                userIdAllList.add(userId);
            }
        });
        //  去掉重复的人员ID
        List<Long> userIdDistinct = new ArrayList<>(userIdAllList.stream().distinct().collect(Collectors.toList()));
//       得到学员的信息
        List<AppUser> appUserListByUserIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userIdDistinct)) {
            Long[] userIdArray = userIdDistinct.toArray(new Long[userIdDistinct.size()]);
            appUserListByUserIdList.addAll(this.sysDepartmentFeign.getAppUserListByUserIdList(userIdArray));
        }
        /**
         *  封装 数据 前台需要的
         */
        List<String> legend = new ArrayList<>();
        legend.add("自测次数");
        legend.add("自测题数");
        List<String> xaxis = new ArrayList<>();
        List<String> ziceDataList = new ArrayList<>();
        List<String> questionsDataList = new ArrayList<>();
        Map<String, Object> resultMap = new LinkedHashMap<>();
        examCountList.stream().forEach(map -> {
            Object userId1 = map.get("userid");
            if (Objects.nonNull(userId1)) {
                Long userId = Long.valueOf(userId1.toString());
                Long examCount = Long.valueOf(map.get("count").toString());
                String username = "";
                Long questionCount = 0L;
                Optional<AppUser> first = appUserListByUserIdList.stream().filter(user -> {
                    Long id = user.getId();
                    return userId.equals(id);
                }).findFirst();
                if (first.isPresent()) {
//                    把userName 修改为 用户姓名
                    username = StringUtils.isNotBlank(first.get().getNickname()) ? first.get().getNickname() : first.get().getUsername();
                }
//              再判断考题中是否有该 学员
                Optional<Map<String, Object>> firstQuestion = questionsCountList.stream().filter(questionMap -> {
                    Object userId2 = questionMap.get("userid");
                    if (Objects.nonNull(userId2)) {
                        Long questionUserId = Long.valueOf(userId2.toString());
                        return userId.equals(questionUserId);
                    }
                    return false;
                }).findFirst();
                if (firstQuestion.isPresent()) {
                    questionCount = Long.valueOf(firstQuestion.get().get("count").toString());
                }
                ziceDataList.add(examCount.toString());
                questionsDataList.add(questionCount.toString());
                xaxis.add(StringUtils.isBlank(username) ? userId.toString() : username);
            }
        });
        resultMap.put("ziceDataList", ziceDataList);
        resultMap.put("questionsDataList", questionsDataList);
        resultMap.put("legend", legend);
        resultMap.put("xaxis", xaxis);
        return resultMap;
    }

    /**
     * 列表列出，当前有哪些人正在自测。【开始自测，但还未结束都算】【名称：正在训练的学员】【排序：开始时间倒序】
     *
     * @param
     * @return
     */
    @ApiOperation(value = "列表列出，当前有哪些人正在自测。【开始自测，但还未结束都算】【名称：正在训练的学员】【排序：开始时间倒序】")
    @RequestMapping(value = "/statistics/getStatisticsExamingUserList", method = RequestMethod.POST)
    public List<Map> getStatisticsExamingUserList(@RequestBody ExamStatisticsDto examDTO) {

//      考试人员ID集合 LIST
        List<Long> userIdList = new ArrayList<>();
        /**
         *   查询正在考试的学u按
         */
//        TODO 增加人员状态
        List<Integer> userStatusList = new ArrayList<>();
        userStatusList.add(6);
        examDTO.setUserStatusList(userStatusList);

        List<Map> userMapList = this.examService.getExamingUserList(examDTO);
//        userMapList = CollectionsCustomer.builder().build().listMapToLowerCase(userMapList);

        userMapList.stream().forEach(map -> {
            Object userIdObject = map.get("userid");
            if (Objects.nonNull(userIdObject)) {
                Long userId = Long.valueOf(userIdObject.toString());
                userIdList.add(userId);
            }
        });
        if (!CollectionUtils.isEmpty(userIdList)) {
//            排除相同的 考试人员
            List<Long> collect = userIdList.stream().distinct().collect(Collectors.toList());
            Long[] userIdArray = userIdList.toArray(new Long[collect.size()]);
            List<AppUser> appUserListByUserIdList = this.sysDepartmentFeign.getAppUserListByUserIdList(userIdArray);
            userMapList.stream().forEach(map -> {
                Object userid = map.get("userid");
                if (Objects.nonNull(userid)) {
                    Long userId = Long.valueOf(userid.toString());
                    Optional<AppUser> first = appUserListByUserIdList.stream().filter(user -> {
                        return user.getId().equals(userId);
                    }).findFirst();
                    if (first.isPresent()) {
                        map.put("userName", StringUtils.isNotBlank(first.get().getNickname()) ? first.get().getNickname() : first.get().getUsername());
                    }
                }
            });
        }
        return userMapList;
    }


    /**
     *  TODO  自测相关的信息 可以在 drawresult 上查询
     * 进入训练还未结束就算），正在训练人数，点击人数，弹出展示正在训练的人名
     * 正在进行的考核
     * 活动状态：
     * 0:待启动、1已启动、2活动开始、6开始考试、3考试结束待阅卷、4完成、5:取消 (0,5)
     *
     * @param
     * @return
     */
    @ApiOperation(value = "当前有哪些训练任务有人在训练（只有有人进入训练还未结束就算），正在训练人数，点击人数，弹出展示正在训练的人名")
    @RequestMapping(value = "/statistics/getStatisticsExamListByTypeAndStatus", method = RequestMethod.POST)
    public List<Map> getStatisticsExamListByTypeAndStatus(@RequestBody ExamStatisticsDto examDTO) {
        /**
         *  TODO(以后优化) 增加参加活动人员状态，人员状态和活动状态是分开的，所有需要单独的分开。
         *
         */
        List<Integer> userStatusList = new ArrayList<>();
        userStatusList.add(6);
        examDTO.setUserStatusList(userStatusList);
        /**
         *  最后组装结果
         */
        List<Map> userCountMapList = new ArrayList<>();
        userCountMapList.addAll(this.examService.getStaticExUserBystatusAndType(examDTO));
        return userCountMapList;
    }


    /**
     * 正在进行中的 训练
     *
     * @param
     * @return
     */
    @ApiOperation(value = "通过考试ID查询 正在参数活动人员信息")
    @RequestMapping(value = "/statistics/getUserListByExamId", method = RequestMethod.POST)
    public List<AppUser> getExamingUserList(@RequestBody ExamStatisticsDto examDTO) {


//        TODO 增加考试人员状态，正在考试的人员信息
        List<Integer> userStatusList = new ArrayList<>();
        userStatusList.add(6);
        examDTO.setUserStatusList(userStatusList);
        /**
         *   查询正在考试的学u按
         */
        List<Map> userMapList = this.examService.getExamingUserList(examDTO);
//        userMapList = CollectionsCustomer.builder().build().listMapToLowerCase(userMapList);

        ArrayList<Long> userIdList = new ArrayList<>();
        userMapList.stream().forEach(map -> {
            Object userid = map.get("userid");
            if (Objects.nonNull(userid)) {
                Long userId = Long.valueOf(userid.toString());
                userIdList.add(userId);
            }
        });
        List<AppUser> appUserListByUserIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userIdList)) {
            Long[] userIdArray = userIdList.toArray(new Long[userIdList.size()]);
            appUserListByUserIdList.addAll(this.sysDepartmentFeign.getAppUserListByUserIdList(userIdArray));
        }
        return appUserListByUserIdList;
    }


    ///////////考核和试卷相关////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     *
     *  试卷也分类型，考试也有类型
     *  试卷类型 4 5 6 是只有JD活动才有的 。
     * @param examDTO
     * @return
     */
    @ApiOperation(value = "以年为单位，统计每个月在线考核试卷创建数量和使用数量")
    @RequestMapping(value = "/statistics/getStatisticsPaperCount", method = RequestMethod.POST)
    public List<Map> getStatisticsPaperCount(@RequestBody ExamStatisticsDto examDTO) {

        String paramDate = examDTO.getQueryDateYy();
        /**
         *   统计考试试卷 每个月创建 和使用
         *   5月12号，增加试卷的类型
         */
        List<Integer> examTypeList = examDTO.getExamTypeList();
        List<Integer> paperTypeList = new ArrayList<>();
//        如果是JD活动
        if(CollectionUtils.isNotEmpty(examTypeList) && examTypeList.contains(1)){
            paperTypeList.add(4);
            paperTypeList.add(5);
            paperTypeList.add(6);
        }else{
            paperTypeList.add(0);
            paperTypeList.add(1);
            paperTypeList.add(2);
        }
        examDTO.setPaperTypeList(paperTypeList);
        List<Map<String, Object>> paperCreatedMap = this.examService.getStatisticsPaperCountCreated(examDTO);
//        paperCreatedMap = CollectionsCustomer.builder().build().listMapToLowerCase(paperCreatedMap);

        List<Map<String, Object>> PaperUsedMap = this.examService.getStatisticsPaperCountUsed(examDTO);
//        PaperUsedMap = CollectionsCustomer.builder().build().listMapToLowerCase(PaperUsedMap);
//        最后返回
        List<Map> resultMapList = new ArrayList<>();
//        补全12个月，每个月的情况
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> newMap = new HashMap<>();
            String time_pre = paramDate + "-" + (i < 10 ? "0" + i : i);
            String time_key = i + "月";
            newMap.put("date", time_key);
            Optional<Map<String, Object>> createMap_first = paperCreatedMap.stream().filter(map -> {
                if (Objects.nonNull(map.get("date"))) {
                    String date = map.get("date").toString();
                    return date.equals(time_pre);
                }
                return false;
            }).findFirst();
            if (createMap_first.isPresent()) {
                newMap.put("createCount", createMap_first.get().get("count"));
            } else {
                newMap.put("createCount", 0);
            }
//          使用情况
            Optional<Map<String, Object>> first_1 = PaperUsedMap.stream().filter(map -> {
                if (Objects.nonNull(map.get("date"))) {
                    String date = map.get("date").toString();
                    return date.equals(time_pre);
                }
                return false;
            }).findFirst();
            if (first_1.isPresent()) {
                newMap.put("usedCount", first_1.get().get("count"));
            } else {
                newMap.put("usedCount", 0);
            }
            resultMapList.add(newMap);
        }
        return resultMapList;
    }


    /**
     * 以年为单位，统计每个月在线考核活动数量、使用试卷数量（被活动关联的）、参考人数")
     * 年度考核情况
     *
     * @param examDTO
     * @return
     */
    @ApiOperation(value = "以年为单位，统计每个月在线考核活动数量、使用试卷数量（被活动关联的）、参考人数 [年度考核情况]")
    @RequestMapping(value = "/statistics/getStatisticsExPUCount", method = RequestMethod.POST)
    public Map<String, List<String>> getStatisticsExPUCount(@RequestBody ExamStatisticsDto examDTO) {
//      时间参数
        String date_pre = examDTO.getQueryDateYy();
        /**
         *  查询考试统计 情况
         *
         *  活动状态：
         * 0:待启动、1已启动、2活动开始、6开始考试、3考试结束待阅卷、4完成、5:取消 (0,5)
         *
         */
        List<Integer> examStatusList = new ArrayList<>();
        examStatusList.add(1);
        examStatusList.add(2);
        examStatusList.add(3);
        examStatusList.add(4);
        examStatusList.add(6);
        examDTO.setExamStatusList(examStatusList);
        List<Map<String, Object>> examMapList = this.examService.getStatisticsExamCountByTypeAndStatus(examDTO);
        // TODO 转小写
//        examMapList = CollectionsCustomer.builder().build().listMapToLowerCase(examMapList);


        List<Map<String, Object>> paperMapList = this.examService.getStatisticsExPaperCountByTypeAndStatus(examDTO);
//        paperMapList = CollectionsCustomer.builder().build().listMapToLowerCase(paperMapList);


        // 增加 考生的状态
        List<Integer> userStatusList = new ArrayList<>();
        userStatusList.add(1);
        userStatusList.add(3);
        userStatusList.add(4);
        userStatusList.add(6);
        examDTO.setUserStatusList(userStatusList);
        List<Map<String, Object>> userMapList = this.examService.getStatisticsExUserCountByTypeAndStatus(examDTO);
//        userMapList = CollectionsCustomer.builder().build().listMapToLowerCase(userMapList);
//      保证有序
        Map<String, List<String>> resultMap = new LinkedHashMap<>();
        List<String> legend = new ArrayList<>();
        List<String> xaxis = new ArrayList<>();
        List<String> examData = new ArrayList<>();
        List<String> paperData = new ArrayList<>();
        List<String> peopleData = new ArrayList<>();
//      ‘在线考核数量’，‘使用试卷数量’，‘参加考试人数’
        legend.add("在线考核数量");
        legend.add("使用试卷数量");
        legend.add("参加考试人数");
        resultMap.put("legend", legend);
        /**
         *  补全 12各月份的 统计数据
         */
        for (int i = 1; i <= 12; i++) {
            String date_key = date_pre + "-" + (i < 10 ? "0" + i : i);
            String xaxis_key = i + "月";
            xaxis.add(xaxis_key);
//          考试 总量
            Optional<Map<String, Object>> examObj = examMapList.stream().parallel().filter(map -> {
                String date = map.get("date").toString();
                return date.equals(date_key);
            }).findFirst();
            if (examObj.isPresent()) {
                examData.add(examObj.get().get("count") + "");
            } else {
                examData.add(0 + "");
            }
//          试卷总量
            Optional<Map<String, Object>> paperObj = paperMapList.stream().filter(map -> {
                String date = map.get("date").toString();
                return date.equals(date_key);
            }).findFirst();
            if (paperObj.isPresent()) {
                paperData.add(paperObj.get().get("count") + "");
            } else {
                paperData.add(0 + "");
            }
//          人员总量
            Optional<Map<String, Object>> userObj = userMapList.stream().filter(map -> {
                String date = map.get("date").toString();
                return date.equals(date_key);
            }).findFirst();
            if (userObj.isPresent()) {
                peopleData.add(userObj.get().get("count") + "");
            } else {
                peopleData.add(0 + "");
            }
        }
        resultMap.put("xaxis", xaxis);
        resultMap.put("examData", examData);
        resultMap.put("paperData", paperData);
        resultMap.put("peopleData", peopleData);
        return resultMap;
    }


    /**
     * 以年为单位，统计每个月在线考核活动数量、使用试卷数量（被活动关联的）、参考人数")
     * 活动类型:
     * 0(在线考核)、1(竞答)、2(训练)、3(自测)
     *
     * @param examDTO
     * @return
     */
    @ApiOperation(value = "以年为单位，列出所有竞答活动，每次的参与人数、使用试题数量和活动时间（开始时间）,[名称：全年竞答活动] ")
    @RequestMapping(value = "/statistics/getStatisticsExPQCount", method = RequestMethod.POST)
    public List<Map<String, Object>> getStatisticsExPQCount(@RequestBody ExamStatisticsDto examDTO) {
        /**
         *  名称：全年竞答活动
         *   考生状态(0 未登录 1 已登录 3 考试结束待判卷  4 已完成 5 考试取消 6 考试开始)
         *
         */
        List<Integer> userStatusList = new ArrayList<>();
        userStatusList.add(1);
        userStatusList.add(3);
        userStatusList.add(4);
        userStatusList.add(6);
        examDTO.setUserStatusList(userStatusList);
        List<Map<String, Object>> userMapList = this.examService.getStatisticsExUserCountGroupExId(examDTO);
//        TODO 转小写
//        userMapList = CollectionsCustomer.builder().build().listMapToLowerCase(userMapList);

//        如果是JD活动的话
        List<Map<String, Object>> questionSMapList = this.examService.getStatisticsExPQCountGroupExId(examDTO);
        // TODO 转小写
//        List<Map<String, Object>> questionSMapListLower = CollectionsCustomer.builder().build().listMapToLowerCase(questionSMapList);
        /**
         *   考试的维度统计
         */
        userMapList.stream().forEach(userMap -> {
            Integer id = Integer.valueOf(userMap.get("id").toString());
            Optional<Map<String, Object>> optionQuestion = questionSMapList.stream().filter(questionMap -> {
                Integer questionExId = Integer.valueOf(questionMap.get("id").toString());
                return id.equals(questionExId);
            }).findFirst();
            if (optionQuestion.isPresent()) {
                userMap.put("questionCount", optionQuestion.get().get("count"));
            } else {
                userMap.put("questionCount", 0);
            }
        });
        return userMapList;
    }

    /**
     * 统计用户每月训练时间和排名信息
     */
    @GetMapping("getUserTrainTimeAndRank")
    public Map<String,Object> getUserTrainTimeAndRank(){
        HashMap<Long,Double> map = new HashMap();
        HashMap<String,Object> map1 = new HashMap();
        int rank = 0 ;
        Double time1 = 0.0;
        try {
            String firstDayOfMonth = DateConvertUtils.getFirstDayOfMonth(true);
            if(ObjectUtil.isNotNull(Tools.getDeptCache("USER_TRAIN_RANK"))){
                map = (HashMap<Long,Double>)Tools.getDeptCache("USER_TRAIN_RANK");
            }else {
                List<DrawResult> list =  this.examService.getUserTrainTimeAndRank(firstDayOfMonth);
                Map<Long, List<DrawResult>> collect = list.stream().collect(Collectors.groupingBy(dr -> dr.getUserId()));
                Set<Map.Entry<Long, List<DrawResult>>> entries = collect.entrySet();
                for (Map.Entry<Long,List<DrawResult>> entry:entries) {
                    Long time = 0L ;
                    List<DrawResult> value = entry.getValue();
                    for (DrawResult dr:value) {
                        if(ObjectUtil.isNull(dr.getLoginDate()) || ObjectUtil.isNull(dr.getCostTime())){
                            continue;
                        }else{
                            //处理训练时长
                            time += transferTime(dr.getCostTime());
                        }
                    }

                    //将训练时长秒转换成小时
                    double hours = time/60/60.0;
                    DecimalFormat df = new DecimalFormat("##.#");
                    String format = df.format(hours);
                    map.put(entry.getKey(),Double.valueOf(format));
                }
                map.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));
                Tools.putDeptCache("USER_TRAIN_RANK",map);
            }

            for (Map.Entry<Long,Double> entry:map.entrySet()) {
                rank++;
                if(AppUserUtil.getLoginAppUser().getId().equals(entry.getKey())){
                    time1 = entry.getValue();
                    break;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取当前用户训练时间和排名失败");
        }
        map1.put("time",time1);
        map1.put("rank",rank);
        return map1 ;

    }

    public Long transferTime(String costTime){
        //18天18 小时45    分 39秒
        Long millis = 0L;
        if(costTime.contains("分")){
            String[] s1 = costTime.split("分");
            if(s1[0].contains("小时")){
                String[] s2 = s1[0].split("小时");
                millis+=Integer.valueOf(s2[1])*60;

                if(s2[0].contains("天")){
                    String[] s3 = s2[0].split("天");
                    millis+=Integer.valueOf(s3[0])*24*60*60;
                    millis+=Integer.valueOf(s3[1])*60*60;
                }else {
                    millis+=Integer.valueOf(s2[0])*60*60;
                }
            }else {
                millis+=Integer.valueOf(s1[0])*60;
            }
        }

        return millis;
    }

}
