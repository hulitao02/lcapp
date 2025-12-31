package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.DrawResultDao;
import com.cloud.exam.dao.EvalDao;
import com.cloud.exam.model.exam.DrawResult;
import com.cloud.exam.model.exam.Eval;
import com.cloud.exam.service.DrawResultService;
import com.cloud.exam.service.EvalService;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.user.AppUser;
import com.cloud.utils.Validator;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class EvalServiceImpl extends ServiceImpl<EvalDao, Eval> implements EvalService {

    @Autowired
    private EvalDao evalDao;
    @Autowired
    private DrawResultDao drawResultDao;
    @Resource
    private SysDepartmentFeign sysDepartmentFeign;
    @Autowired
    private DrawResultService drawResultService;

    private List<Long> queryExamUserId(Long examId) {
        return evalDao.queryExamUserId(examId);
    }

    private List<Map<String, Object>> queryExamUserPaperId(Long examId, Long userId) {
        return evalDao.queryExamUserPaperId(examId, userId);
    }

    private List<Map<String, Object>> queryExamUserKpId(Long paperId, Long userId) {
        return evalDao.queryExamUserKpId(paperId, userId);
    }

    private float getDByN(int n) {
        if (n == 5) {
            return 1;
        } else if (n == 4){
            return 0.9f;
        }else if(n == 3) {
            return 0.8f;
        }else if(n == 2) {
            return 0.7f;
        }else{
    		return 0.6f;
        }
    }

    @Override
    public void makeEvaluation(long examId) {
        //查询所有考生
        List<Long> userIdList = queryExamUserId(examId);
        for(Long userId : userIdList) {
        	//获取有哪些考卷
        	List<Map<String, Object>> paperList = queryExamUserPaperId(examId, userId);
        	for(Map<String, Object> paperMap : paperList) {
        		Long paperId = MapUtils.getLong(paperMap, "paper_id");
        		String examDate = MapUtils.getString(paperMap, "login_date");
        		if(Validator.isEmpty(examDate)){
                    continue;
                }
        		int month = Integer.parseInt(examDate.substring(0, 4) + examDate.substring(5,7));
	        	List<Map<String, Object>> list = queryExamUserKpId(paperId, userId);
	        	Map<Long,Map<Integer, Double>> kpMap = new HashMap<>();
	        	for(Map<String, Object> map : list) {
	        		//各难度得分点数C=s/z
	        		long kpId = MapUtils.getLongValue(map, "kp_id");
	        		int n = MapUtils.getIntValue(map, "n");
	        		double z = MapUtils.getDoubleValue(map, "z");
	        		double s = MapUtils.getDoubleValue(map, "s");
	        		double c = s/z;
	        		if (kpMap.containsKey(kpId)) {
	            		Map<Integer, Double> nMap = kpMap.get(kpId);
	            		nMap.put(n, c);
	            	} else {
	            		Map<Integer, Double> nMap = new TreeMap<>(new Comparator<Integer>() {
	
							@Override
							public int compare(Integer key1, Integer key2) {
								//降序排列
								return key2 - key1;
							}
	            			
	            		});
	            		nMap.put(n, c);
	            		kpMap.put(kpId, nMap);
	            	}
	        	}
	        	for(Map.Entry<Long, Map<Integer, Double>> entry : kpMap.entrySet()) {
	        		long kpId = entry.getKey();
	            	Map<Integer, Double> nMap = entry.getValue();
	            	double total = 0;
	            	//知识点能力评估点(A)=(C1+C2+C3+C4+C5)/n*dn*t*100
	            	for(Map.Entry<Integer, Double> entry2 :nMap.entrySet()) {
	            		//int n = entry2.getKey();
	            		double c = entry2.getValue();
	            		total = total + c;
	            	}
	            	int n=nMap.keySet().stream().findFirst().get();
	            	float dn = getDByN(n); 
	            	int a = (int)Math.round(total/n*dn*100);
	            	Eval eval = new Eval();
	            	eval.setUserId(userId);
	            	AppUser user = sysDepartmentFeign.findUserById(userId);
	            	eval.setDepartmentId(user.getDepartmentId());
	            	eval.setKpId(kpId);
	            	eval.setAcId(examId);
	            	eval.setMonth(month);
	            	eval.setEvalScore(a);
	            	eval.setPaperId(paperId);
	            	eval.setCreateTime(new Date());
	            	eval.setUpdateTime(new Date());
	            	evalDao.insert(eval);
                    //将评估得分更新到抽签表
                    QueryWrapper<DrawResult> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("ac_id",examId);
                    queryWrapper.eq("user_id",userId);
                    DrawResult drawResult = drawResultDao.selectOne(queryWrapper);
                    drawResult.setAbilityLevel(a);
                    drawResultService.saveOrUpdate(drawResult);

	        	}
        	}
        }
    }

    @Override
    public int getPersonalScore(Long userId, Long kpId) {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String month = sdf.format(cal.getTime());
        if(day > 15) {//本月
            Map<String, Object> map = evalDao.getPersonalScore(userId, Integer.parseInt(month), kpId);
            int total = MapUtils.getInteger(map, "total");
            int num = MapUtils.getInteger(map, "num");
            return (int) Math.round(((double)total)/num);
        } else {//本月+上月
            //上月
            cal.add(Calendar.MONTH, -1);
            String lastMonth = sdf.format(cal.getTime());
            Map<String, Object> map = evalDao.getPersonalScore2(userId, Integer.parseInt(month), Integer.parseInt(lastMonth), kpId);
            int total = MapUtils.getInteger(map, "total");
            int num = MapUtils.getInteger(map, "num");
            return (int) Math.round(((double)total)/num);
        }
    }

    @Override
    public List<Map<String, Object>> getPersonalAbility(Long userId, Long[] kpIds) {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String month = sdf.format(cal.getTime());
        if(day > 15) {//本月
            List<Map<String, Object>> list = evalDao.getPersonalAbility(userId, Integer.parseInt(month), kpIds);
            return list;
        } else {//本月+上月
            //上月
            cal.add(Calendar.MONTH, -1);
            String lastMonth = sdf.format(cal.getTime());
            List<Map<String, Object>> list = evalDao.getPersonalAbility2(userId, Integer.parseInt(month), Integer.parseInt(lastMonth), kpIds);
            return list;
        }
    }
    
    @Override
    public Map<Long, Integer> getPersonalQuestions(Long userId, Long[] kpIds) {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String month = sdf.format(cal.getTime());
        List<Map<String, Object>> list = null;
        Map<Long, Integer> resultMap = new HashMap<>();
        if(day > 15) {//本月
            list = evalDao.getPersonalQuestions(userId, Integer.parseInt(month), kpIds);
        } else {//本月+上月
            //上月
            cal.add(Calendar.MONTH, -1);
            String lastMonth = sdf.format(cal.getTime());
            list = evalDao.getPersonalQuestions2(userId, Integer.parseInt(month), Integer.parseInt(lastMonth), kpIds);
        }
        for(Map<String, Object> map : list) {
        	long kpId = MapUtils.getLong(map, "kp_id");
            int num = MapUtils.getInteger(map, "num");
            resultMap.put(kpId, num);
        }
        return resultMap;
    }

    @Override
    public List<Map<String, Object>> getPersonalHisScore(Long userId, Long[] kpIds) {
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.MONTH, -11);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String fromMonth = sdf.format(cal.getTime());
        List<Map<String, Object>> list = evalDao.getPersonalHisScore(userId, Integer.parseInt(fromMonth), kpIds);
        return list;
    }

    @Override
    public List<Map<String, Object>> getPersonalDeptScore(Set<Long> userIds, Long kpId) {
    	Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String month = sdf.format(cal.getTime());
        if(day > 15) {//本月
            List<Map<String, Object>> list = evalDao.getPersonalDeptScore(userIds, Integer.parseInt(month), kpId);
            return list;
        } else {//本月+上月
            //上月
            cal.add(Calendar.MONTH, -1);
            String lastMonth = sdf.format(cal.getTime());
            List<Map<String, Object>> list = evalDao.getPersonalDeptScore2(userIds, Integer.parseInt(month), Integer.parseInt(lastMonth), kpId);
            return list;
        }
    }
    
    @Override
    public int getDeptScore(Long deptId, Long kpId) {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String month = sdf.format(cal.getTime());
        if(day > 15) {//本月
            int score = (int)Math.round(evalDao.getDeptScore(deptId, Integer.parseInt(month), kpId));
            return (int) score;
        } else {//本月+上月
            //上月
            cal.add(Calendar.MONTH, -1);
            String lastMonth = sdf.format(cal.getTime());
            int score = (int)Math.round(evalDao.getDeptScore2(deptId, Integer.parseInt(month), Integer.parseInt(lastMonth), kpId));
            return (int) score;
        }
    }
    
    @Override
    public List<Map<String, Object>> getDeptKpScore(Long deptId, Long[] kpIds) {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String month = sdf.format(cal.getTime());
        if(day > 15) {//本月
        	List<Map<String, Object>> list = evalDao.getDeptKpScore(deptId, Integer.parseInt(month), kpIds);
            return list;
        } else {//本月+上月
            //上月
            cal.add(Calendar.MONTH, -1);
            String lastMonth = sdf.format(cal.getTime());
            List<Map<String, Object>> list = evalDao.getDeptKpScore2(deptId, Integer.parseInt(month), Integer.parseInt(lastMonth), kpIds);
            return list;
        }
    }

	@Override
	public List<Map<String, Object>> getDeptAbility(Long deptId, Long[] kpIds) {
		Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String month = sdf.format(cal.getTime());
        if(day > 15) {//本月
        	List<Map<String, Object>> list = evalDao.getDeptAbility(deptId, Integer.parseInt(month), kpIds);
            return list;
        } else {//本月+上月
            //上月
            cal.add(Calendar.MONTH, -1);
            String lastMonth = sdf.format(cal.getTime());
            List<Map<String, Object>> list = evalDao.getDeptAbility2(deptId, Integer.parseInt(month), Integer.parseInt(lastMonth), kpIds);
            return list;
        }
	}

	@Override
	public List<Map<String, Object>> getDeptDistribution(Long deptId, Long kpId) {
		Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String month = sdf.format(cal.getTime());
        if(day > 15) {//本月
        	List<Map<String, Object>> list = evalDao.getDeptDistribution(deptId, Integer.parseInt(month), kpId);
            return list;
        } else {//本月+上月
            //上月
            cal.add(Calendar.MONTH, -1);
            String lastMonth = sdf.format(cal.getTime());
            List<Map<String, Object>> list = evalDao.getDeptDistribution2(deptId, Integer.parseInt(month), Integer.parseInt(lastMonth), kpId);
            return list;
        }
	}

	@Override
	public Map<Long, Integer> getDeptQuestions(Long deptId, Long kpId) {
		Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String month = sdf.format(cal.getTime());
        Map<Long, Integer> resultMap = new HashMap<>();
        List<Map<String, Object>> list = null;
        if(day > 15) {//本月
        	list = evalDao.getDeptQuestions(deptId, Integer.parseInt(month), kpId);
        } else {//本月+上月
            //上月
            cal.add(Calendar.MONTH, -1);
            String lastMonth = sdf.format(cal.getTime());
            list = evalDao.getDeptQuestions2(deptId, Integer.parseInt(month), Integer.parseInt(lastMonth), kpId);
        }
        for(Map<String, Object> map : list) {
        	long userId = MapUtils.getLong(map, "user_id");
            int num = MapUtils.getInteger(map, "num");
            resultMap.put(userId, num);
        }
        return resultMap;
	}

	@Override
	public List<Map<String, Object>> getExamDeptEval(Long examId, Long paperId) {
		return evalDao.getExamDeptEval(examId, paperId);
	}

	@Override
	public List<Map<String, Object>> getExamDeptDetail(Long examId, Long paperId, Long deptId) {
		return evalDao.getExamDeptDetail(examId, paperId, deptId);
	}

	@Override
	public Map<String, Object> getExamDeptScore(Long examId, Long paperId) {
		List<Map<String, Object>> list = evalDao.getExamDeptScore(examId, paperId);
		Map<String, Object> resultMap = new HashMap<>();
		for(Map<String, Object> map : list) {
        	long deptId = MapUtils.getLong(map, "depart_id");
            int num = MapUtils.getInteger(map, "num");
            double score = MapUtils.getDouble(map, "total_score");
            resultMap.put(deptId+"-1", num);
            resultMap.put(deptId+"-2", score);
        }
		return resultMap;
	}

	@Override
	public List<Map<String, Object>> getExamPersonalEval(Long examId, Long paperId, Long deptId) {
		return evalDao.getExamPersonalEval(examId, paperId, deptId);
	}

	@Override
	public Map<Long, Double> getExamPersonalScore(Long examId, Long paperId, Long deptId) {
		List<Map<String, Object>> list = evalDao.getExamPersonalScore(examId, paperId, deptId);
		Map<Long, Double> resultMap = new HashMap<>();
		for(Map<String, Object> map : list) {
        	long userId = MapUtils.getLong(map, "user_id");
            double score = MapUtils.getDouble(map, "exam_score");
            resultMap.put(userId, score);
        }
		return resultMap;
	}

	@Override
	public List<Map<String, Object>> getExamPersonalDetail(Long examId, Long paperId, Long userId) {
		return evalDao.getExamPersonalDetail(examId, paperId, userId);
	}
    
}
