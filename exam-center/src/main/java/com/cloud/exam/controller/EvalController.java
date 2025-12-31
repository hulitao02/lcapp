//package com.cloud.exam.controller;
//
//import com.cloud.core.ApiResult;
//import com.cloud.core.ApiResultHandler;
//import com.cloud.exam.model.eval.*;
//import com.cloud.exam.service.EvalService;
//import com.cloud.exam.utils.Tools;
//import com.cloud.feign.managebackend.ManageBackendFeign;
//import com.cloud.feign.usercenter.SysDepartmentFeign;
//import com.cloud.model.user.AppUser;
//import com.cloud.model.user.LoginAppUser;
//import com.cloud.model.user.SysDepartment;
//import com.cloud.model.utils.AppUserUtil;
////import com.cloud.utils.CollectionsCustomer;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiImplicitParam;
//import io.swagger.annotations.ApiImplicitParams;
//import io.swagger.annotations.ApiOperation;
//import org.apache.commons.collections4.MapUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.annotation.Resource;
//import java.util.*;
//import java.util.Map.Entry;
//
//@Api(value = "考试评估控制器类")
//@RestController
//@RequestMapping("/eval2")
//public class EvalController {
//
//    @Autowired
//    private EvalService evalService;
//
//    @Resource
//    private ManageBackendFeign manageBackendFeign;
//
//    @Resource
//    private SysDepartmentFeign sysDepartmentFeign;
//
//    @ApiOperation(value = "查询个人能力综合评分", notes = "查询个人能力综合评分")
//    @ApiImplicitParam(name = "kpId", value = "知识点ID", required = false, dataType = "Long")
//    @RequestMapping(value = "/personal/score", method = RequestMethod.GET)
//    public ApiResult<EvalDto> getPersonalScore(@RequestParam(value = "kpId", required = false) Long kpId) {
//        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
//        int score = evalService.getPersonalScore(loginAppUser.getId(), kpId);
//        EvalDto dto = new EvalDto();
//        dto.setScore(score);
//        dto.caculateLevel();
//        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", dto);
//    }
//
//    private String getKpName(String  kpId) {
//        String kpName = Tools.getKpCacheValue(kpId);
//        if(kpName == null) {
//            kpName = manageBackendFeign.getKnowledgePointsById(kpId).getPointName();
//            Tools.putKpCacheValue(kpId, kpName);
//        }
//        return kpName;
//    }
//
//    private String getUserName(long userId) {
//    	String userName = Tools.getUserCacheValue(userId);
//    	if(userName == null) {
//    		AppUser user = sysDepartmentFeign.findUserById(userId);
//    		userName = user.getUsername();
//    		Tools.putUserCacheValue(userId, userName);
//    	}
//    	return userName;
//    }
//
//    @ApiOperation(value = "查询个人能力分布", notes = "可通过知识点筛选")
//    @ApiImplicitParam(name = "kpIds", value = "知识点ID数组", required = false, dataType = "Long[]")
//    @RequestMapping(value = "/personal/ability", method = RequestMethod.GET)
//    public ApiResult<List<EvalKpDto>> getPersonalAbility(@RequestParam(value = "kpIds", required = false) Long[] kpIds) {
//        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
//        List<Map<String, Object>> list = evalService.getPersonalAbility(loginAppUser.getId(), kpIds);
////        list = CollectionsCustomer.builder().build().listMapToLowerCase(list);
//        Map<Long, Integer> numMap = evalService.getPersonalQuestions(loginAppUser.getId(), kpIds);
////        numMap = CollectionsCustomer.builder().build().mapToLowerCase(numMap);
//        List<EvalKpDto> abilityList = new ArrayList<>();
//        for(Map<String, Object> map : list) {
//            String kpId = MapUtils.getLong(map, "kp_id");
//            int score = MapUtils.getInteger(map, "score");
//            EvalKpDto dto = new EvalKpDto();
//            dto.setScore(score);
//            dto.caculateLevel();
//            String kpName = getKpName(kpId);
//            dto.setKpName(kpName);
//            dto.setKpId(kpId);
//            if(numMap!=null) {
//            	Integer num = MapUtils.getInteger(numMap, kpId);
//            	if(num!=null) {
//            		dto.setKhNum(num);
//            	}
//            }
//            abilityList.add(dto);
//        }
//        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", abilityList);
//    }
//
//    @ApiOperation(value = "查询个人能力趋势", notes = "12个月之内的评分")
//    @ApiImplicitParam(name = "kpIds", value = "知识点ID数组", required = false, dataType = "Long[]")
//    @RequestMapping(value = "/personal/history", method = RequestMethod.GET)
//    public ApiResult<Map<String, Object>> getPersonalHisScore(@RequestParam(value = "kpIds", required = false) Long[] kpIds) {
//        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
//        List<Map<String, Object>> list = evalService.getPersonalHisScore(loginAppUser.getId(), kpIds);
//        Map<Long,Map<Integer, Integer>> kpMap = new HashMap<>();
//        Set<Integer> monthSet = new TreeSet<>(new Comparator<Integer>() {
//
//			@Override
//			public int compare(Integer key1, Integer key2) {
//				//降序排列
//				return key1 - key2;
//			}
//
//		});
//        Set<Long> kpIdSet = new HashSet<>();
//        for(Map<String, Object> map : list) {
//            long kpId = MapUtils.getLong(map, "kp_id");
//            kpIdSet.add(kpId);
//            int month = MapUtils.getInteger(map, "month");
//            monthSet.add(month);
//            int score = MapUtils.getInteger(map, "score");
//            /*EvalHisDto dto = new EvalHisDto();
//            dto.setScore(score);
//            String kpName = getKpName(kpId);
//            dto.setKpName(kpName);
//            dto.setKpId(kpId);
//            dto.setMonth(month);
//            hisScoreList.add(dto);*/
//            if (kpMap.containsKey(kpId)) {
//        		Map<Integer, Integer> monthMap = kpMap.get(kpId);
//        		monthMap.put(month, score);
//        	} else {
//        		Map<Integer, Integer> monthMap = new HashMap<>();
//                monthMap.put(month, score);
//        		kpMap.put(kpId, monthMap);
//        	}
//        }
//        //legend
//        List<String> legendList = new ArrayList<>();
//        List<Map<String, Object>> seriesList = new ArrayList<>();
//        for(Long kpId : kpIdSet) {
//        	String kpName = getKpName(kpId);
//        	Map<String, Object> dataMap = new HashMap<>();
//        	dataMap.put("name", kpName);
//        	//dataMap.put("xAxis", monthSet);
//        	legendList.add(kpName);
//        	List<Integer> dataList = new ArrayList<>();
//        	Map<Integer, Integer> monthMap = kpMap.get(kpId);
//        	for(Integer month : monthSet) {
//            	Integer score = monthMap.get(month);
//            	dataList.add(score);
//        	}
//        	dataMap.put("data", dataList);
//        	seriesList.add(dataMap);
//        }
//        Map<String, Object> resultMap = new HashMap<>();
//        resultMap.put("legend", legendList);
//        resultMap.put("xAxis", monthSet);
//        resultMap.put("series", seriesList);
//        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", resultMap);
//    }
//
//    @ApiOperation(value = "查询个人能力我的排名", notes = "单位内的评分情况")
//    @ApiImplicitParam(name = "kpId", value = "知识点ID", required = false, dataType = "Long")
//    @RequestMapping(value = "/personal/dept", method = RequestMethod.GET)
//    public ApiResult<List<EvalDeptDto>> getPersonalDeptScore(@RequestParam(value = "kpId", required = false) Long kpId) {
//        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
//        if(kpId !=null && kpId!=0) {
//	        //先判断本人有没有数据，没有就不显示
//	        Long[] kpIds = new Long[] {kpId};
//	        List<Map<String, Object>> list = evalService.getPersonalAbility(loginAppUser.getId(), kpIds);
//	        if(list == null || list.size() == 0) {
//	        	return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "没有数据", null);
//	        }
//        }
//        //查询本单位所有用户
//        List<AppUser> userList = sysDepartmentFeign.getDeptUserIds(loginAppUser.getDepartmentId());
//        Set<Long> userIds = new HashSet<>();
//        for(AppUser user : userList) {
//        	userIds.add(user.getId());
//        }
//        List<Map<String, Object>> list = evalService.getPersonalDeptScore(userIds, kpId);
//        List<EvalDeptDto> deptScoreList = new ArrayList<>();
//        for(Map<String, Object> map : list) {
//            long userId = MapUtils.getLongValue(map, "user_id");
//            String userName = getUserName(userId);
//            int score = MapUtils.getInteger(map, "score");
//            EvalDeptDto dto = new EvalDeptDto();
//            dto.setScore(score);
//            dto.caculateLevel();
//            dto.setUserId(userId);
//            dto.setUserName(userName);
//            deptScoreList.add(dto);
//        }
//        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", deptScoreList);
//    }
//
//    @ApiOperation(value = "查询团体能力综合评分", notes = "查询团体能力综合评分")
//    @ApiImplicitParam(name = "kpId", value = "知识点ID", required = false, dataType = "Long")
//    @RequestMapping(value = "/dept/score", method = RequestMethod.GET)
//    public ApiResult<EvalKpDto> getDeptScore(@RequestParam(value = "kpId", required = false) Long kpId) {
//        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
//        int avgScore = evalService.getDeptScore(loginAppUser.getDepartmentId(), kpId);
//        EvalKpDto dto = new EvalKpDto();
//        dto.setScore(avgScore);
//        dto.caculateLevel();
//        if(kpId!=null) {
//	        dto.setKpId(kpId);
//	        String kpName = getKpName(kpId);
//	        dto.setKpName(kpName);
//        }
//        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", dto);
//    }
//
//    @ApiOperation(value = "查询团体综合能力", notes = "查询团体综合能力")
//    @ApiImplicitParam(name = "kpIds", value = "知识点ID", required = false, dataType = "Long[]")
//    @RequestMapping(value = "/dept/kpScore", method = RequestMethod.GET)
//    public ApiResult<List<EvalKpDto>> getDeptKpScore(@RequestParam(value = "kpIds", required = false) Long[] kpIds) {
//        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
//        List<EvalKpDto> kpList = new ArrayList<>();
//    	List<Map<String, Object>> list = evalService.getDeptKpScore(loginAppUser.getDepartmentId(), kpIds);
//    	for(Map<String, Object> map : list) {
//    		long kpId = MapUtils.getLong(map, "kp_id");
//            int avgScore = MapUtils.getInteger(map, "dept_score");
//        	EvalKpDto dto = new EvalKpDto();
//            dto.setScore(avgScore);
//            dto.setKpId(kpId);
//            String kpName = getKpName(kpId);
//            dto.setKpName(kpName);
//            kpList.add(dto);
//    	}
//        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", kpList);
//    }
//
//    @ApiOperation(value = "查询团体成员能力排名", notes = "查询团体成员能力排名")
//    @ApiImplicitParams({
//    	@ApiImplicitParam(name = "num", value = "显示人数", required = true, dataType = "Integer"),
//        @ApiImplicitParam(name = "kpIds", value = "知识点ID", required = false, dataType = "Long[]")
//    })
//    @RequestMapping(value = "/dept/deptAbility", method = RequestMethod.GET)
//    public ApiResult<Map<Long, List<EvalDeptDto>>> getDeptAbility(@RequestParam(value = "num") Integer num, @RequestParam(value = "kpIds", required = false) Long[] kpIds) {
//        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
//    	List<Map<String, Object>> list = evalService.getDeptAbility(loginAppUser.getDepartmentId(), kpIds);
//    	Map<Long, Map<Long, Integer>> kpMap = new HashMap<>();
//    	for(Map<String, Object> map : list) {
//    		long kpId = MapUtils.getLong(map, "kp_id");
//    		//String kpName = getKpName(kpId);
//    		long userId = MapUtils.getLong(map, "user_id");
//            int avgScore = MapUtils.getInteger(map, "score");
//            if (kpMap.containsKey(kpId)) {
//            	Map<Long, Integer> userMap = kpMap.get(kpId);
//            	userMap.put(userId, avgScore);
//            } else {
//            	Map<Long, Integer> userMap = new HashMap<>();
//            	userMap.put(userId, avgScore);
//        		kpMap.put(kpId, userMap);
//            }
//    	}
//    	Map<Long, List<EvalDeptDto>> sortedMap = new HashMap<>();
//    	for(Map.Entry<Long, Map<Long, Integer>> entry : kpMap.entrySet()) {
//    		long kpId = entry.getKey();
//        	Map<Long, Integer> userMap = entry.getValue();
//        	List<Entry<Long, Integer>> sortList = new ArrayList<>(userMap.entrySet());
//        	Collections.sort(sortList, new Comparator<Map.Entry<Long, Integer>>() {
//
//				@Override
//				public int compare(Entry<Long, Integer> o1, Entry<Long, Integer> o2) {
//					//降序排列
//					return o2.getValue().compareTo(o1.getValue());
//				}
//
//        	});
//        	List<EvalDeptDto> userList = new ArrayList<>();
//        	int n=0;
//        	for(Entry<Long, Integer> e : sortList) {
//        		n++;
//        		EvalDeptDto dto = new EvalDeptDto();
//                dto.setScore(e.getValue());
//                dto.caculateLevel();
//                Long userId = e.getKey();
//                dto.setUserId(userId);
//                String userName = getUserName(userId);
//                dto.setUserName(userName);
//                userList.add(dto);
//                if(n >= num) {
//                	break;
//                }
//        	}
//        	sortedMap.put(kpId, userList);
//    	}
//    	List<String> kpList = new ArrayList<>();
//    	List<List<EvalDeptDto>> seriesList = new ArrayList<>();
//    	for(int i=0; i<num; i++) {
//        	//Map<String, Object> dataMap = new HashMap<>();
//        	//dataMap.put("name", "第"+num+"名");
//        	List<EvalDeptDto> dataList = new ArrayList<>();
//            //dataList.add(userList.get(num));
//        	//dataMap.put("data", dataList);
//        	seriesList.add(dataList);
//        }
//    	for(Map.Entry<Long, List<EvalDeptDto>> entry : sortedMap.entrySet()) {
//    		long kpId = entry.getKey();
//    		String kpname = getKpName(kpId);
//    		kpList.add(kpname);
//    		List<EvalDeptDto> userList = entry.getValue();
//    		for(int i=0; i<num; i++) {
//            	//Map<String, Object> dataMap = new HashMap<>();
//            	//dataMap.put("name", "第"+num+"名");
//            	List<EvalDeptDto> dataList = seriesList.get(i);
//            	if(i < userList.size()) {
//            		dataList.add(userList.get(i));
//            	}
//            	//dataMap.put("data", dataList);
//            	//seriesList.add(dataMap);
//            }
//    	}
//    	Map<String, Object> resultMap = new HashMap<>();
//    	resultMap.put("xAxis", kpList);//知识点
//        resultMap.put("series", seriesList);
//        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", resultMap);
//    }
//
//    @ApiOperation(value = "查询团体成员能力分布", notes = "查询团体成员能力分布")
//    @ApiImplicitParam(name = "kpId", value = "知识点ID", required = false, dataType = "Long")
//    @RequestMapping(value = "/dept/distribution", method = RequestMethod.GET)
//    public ApiResult<List<EvalDeptDto>> getDeptDistribution(@RequestParam(value = "kpId", required = false) Long kpId) {
//        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
//        //查询本单位所有用户
//        /*List<AppUser> userList = sysDepartmentFeign.getDeptUserIds(loginAppUser.getDepartmentId());
//        Set<Long> userIds = new HashSet<>();
//        for(AppUser user : userList) {
//        	userIds.add(user.getId());
//        }*/
//        List<Map<String, Object>> list = evalService.getDeptDistribution(loginAppUser.getDepartmentId(), kpId);
////        list = CollectionsCustomer.builder().build().listMapToLowerCase(list);
//        Map<Long, Integer> numMap = evalService.getDeptQuestions(loginAppUser.getDepartmentId(), kpId);
////        numMap = CollectionsCustomer.builder().build().mapToLowerCase(numMap);
//        List<EvalDeptDto> deptScoreList = new ArrayList<>();
//        for(Map<String, Object> map : list) {
//            long userId = MapUtils.getLongValue(map, "user_id");
//            String userName = getUserName(userId);
//            int score = MapUtils.getInteger(map, "score");
//            EvalDeptDto dto = new EvalDeptDto();
//            dto.setScore(score);
//            dto.caculateLevel();
//            dto.setUserId(userId);
//            dto.setUserName(userName);
//            Integer num = numMap.get(userId);
//            if(num!=null) {
//            	dto.setKhNum(num);
//            }
//            deptScoreList.add(dto);
//        }
//        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", deptScoreList);
//    }
//
//    private String getDepartmentName(long deptId) {
//    	String deptName = Tools.getDeptCacheValue(deptId);
//    	if(deptName == null) {
//    		SysDepartment dept = sysDepartmentFeign.findSysDepartmentById(deptId);
//    		deptName = dept.getDname();
//    		Tools.putDeptCacheValue(deptId, deptName);
//    	}
//    	return deptName;
//    }
//
//    @ApiOperation(value = "查询考试综合评分", notes = "查询考试综合评分")
//    @ApiImplicitParam(name = "examId", value = "考试活动ID", required = true, dataType = "Long")
//    @RequestMapping(value = "/exam/dept/eavl", method = RequestMethod.GET)
//    public ApiResult<List<DeptEvalDto>> getExamDeptEval(@RequestParam(value = "examId") Long examId, @RequestParam(value = "paperId", required = false) Long paperId) {
//    	List<Map<String, Object>> list = evalService.getExamDeptEval(examId, paperId);
//    	Map<String, Object> numMap = evalService.getExamDeptScore(examId, paperId);
//    	List<DeptEvalDto> deptScoreList = new ArrayList<>();
//    	for(Map<String, Object> map : list) {
//    		long deptId = MapUtils.getLongValue(map, "department_id");
//    		int score = MapUtils.getInteger(map, "dept_score");
//    		DeptEvalDto dto = new DeptEvalDto();
//            dto.setScore(60);
//            dto.caculateLevel();
//            dto.setDeptId(deptId);
//            dto.setDeptName(getDepartmentName(deptId));
//            Integer num = MapUtils.getInteger(numMap, deptId+"-1");
//            Double totalScore = MapUtils.getDouble(numMap, deptId+"-2");
//            if(num != null) {
//            	dto.setNum(num);
//            }
//            if(totalScore !=null) {
//            	dto.setTotalScore(totalScore);
//            }
//            deptScoreList.add(dto);
//    	}
//        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", deptScoreList);
//    }
//
//    @ApiOperation(value = "查询考试知识点评分", notes = "查询考试知识点评分")
//    @ApiImplicitParam(name = "examId", value = "考试活动ID", required = false, dataType = "Long")
//    @RequestMapping(value = "/exam/dept/detail", method = RequestMethod.GET)
//    public ApiResult<List<EvalKpDto>> getExamDeptDetail(@RequestParam(value = "examId") Long examId,  @RequestParam(value = "paperId", required = false) Long paperId,  @RequestParam(value = "deptId") Long deptId) {
//    	List<Map<String, Object>> list = evalService.getExamDeptDetail(examId, paperId, deptId);
//    	List<EvalKpDto> kpScoreList = new ArrayList<>();
//    	for(Map<String, Object> map : list) {
//    		long kpId = MapUtils.getLong(map, "kp_id");
//    		int score = MapUtils.getInteger(map, "score");
//    		EvalKpDto dto = new EvalKpDto();
//            dto.setScore(score);
//            dto.caculateLevel();
//            dto.setKpId(kpId);
//            String kpName = getKpName(kpId);
//            dto.setKpName(kpName);
//            kpScoreList.add(dto);
//    	}
//        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", kpScoreList);
//    }
//
//    @ApiOperation(value = "查询考试个人综合评分", notes = "查询考试个人综合评分")
//    @ApiImplicitParam(name = "examId", value = "考试活动ID", required = true, dataType = "Long")
//    @RequestMapping(value = "/exam/personal/eval", method = RequestMethod.GET)
//    public ApiResult<List<UserEvalDto>> getExamPersonalEval(@RequestParam(value = "examId") Long examId, @RequestParam(value = "deptId") Long deptId, @RequestParam(value = "paperId", required = false) Long paperId) {
//    	List<Map<String, Object>> list = evalService.getExamPersonalEval(examId, paperId, deptId);
//    	Map<Long, Double> scoreMap = evalService.getExamPersonalScore(examId, paperId, deptId);
//    	List<UserEvalDto> userScoreList = new ArrayList<>();
//    	for(Map<String, Object> map : list) {
//    		long userId = MapUtils.getLongValue(map, "user_id");
//    		int score = MapUtils.getInteger(map, "user_score");
//    		UserEvalDto dto = new UserEvalDto();
//            dto.setScore(score);
//            dto.caculateLevel();
//            dto.setUserId(userId);
//            dto.setUserName(getUserName(userId));
//            Double examScore = MapUtils.getDouble(scoreMap, userId);
//            if(examScore!=null) {
//            	dto.setExamScore(examScore);
//            }
//            userScoreList.add(dto);
//    	}
//        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", userScoreList);
//    }
//
//    @ApiOperation(value = "查询考试个人知识点评分", notes = "查询考试个人知识点评分")
//    @ApiImplicitParam(name = "examId", value = "考试活动ID", required = false, dataType = "Long")
//    @RequestMapping(value = "/exam/personal/detail", method = RequestMethod.GET)
//    public ApiResult<List<EvalKpDto>> getExamPersonalDetail(@RequestParam(value = "examId") Long examId,  @RequestParam(value = "userId") Long userId,  @RequestParam(value = "paperId", required = false) Long paperId) {
//    	List<Map<String, Object>> list = evalService.getExamPersonalDetail(examId, paperId, userId);
//    	List<EvalKpDto> kpScoreList = new ArrayList<>();
//    	for(Map<String, Object> map : list) {
//    		long kpId = MapUtils.getLong(map, "kp_id");
//    		int score = MapUtils.getInteger(map, "score");
//    		EvalKpDto dto = new EvalKpDto();
//            dto.setScore(score);
//            dto.caculateLevel();
//            dto.setKpId(kpId);
//            String kpName = getKpName(kpId);
//            dto.setKpName(kpName);
//            kpScoreList.add(dto);
//    	}
//        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "查询成功", kpScoreList);
//    }
//
//}
