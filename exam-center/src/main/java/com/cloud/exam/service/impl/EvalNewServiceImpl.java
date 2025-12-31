package com.cloud.exam.service.impl;

import com.aspose.words.PageInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.exam.dao.*;
import com.cloud.exam.model.eval.ExamEvalDept;
import com.cloud.exam.model.eval.ExamEvalDeptHis;
import com.cloud.exam.model.eval.ExamEvalPerson;
import com.cloud.exam.model.eval.ExamEvalPersonHis;
import com.cloud.exam.model.exam.Paper;
import com.cloud.exam.model.exam.PgInfo;
import com.cloud.exam.service.EvalNewService;
import com.cloud.exam.service.PaperService;
import com.cloud.exam.utils.Tools;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.common.KnowledgePoints;
import com.cloud.model.user.AppUser;
import com.cloud.model.user.SysDepartment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EvalNewServiceImpl implements EvalNewService {


    @Resource
    ManageBackendFeign manageBackendFeign;
    @Resource
    SysDepartmentFeign sysDepartmentFeign;
    @Autowired
    EvalPersonDao evalPersonDao;
    @Autowired
    EvalDeptDao evalDeptDao;
    @Autowired
    EvalPersonHisDao evalPersonHisDao;
    @Autowired
    EvalDeptHisDao evalDeptHisDao;
    @Autowired
    PaperService paperService;
    @Autowired
    PgInfoDao pgInfoDao;


    @Override
    @Transactional
    public void makeEvaluation(long examId) throws Exception {
        System.out.println("----------------评估值计算方法执行================================" + examId);
        PgInfo pgInfo = new PgInfo();
        pgInfo.setCreateTime(new Timestamp(System.currentTimeMillis()));
        Map map = new HashMap();
        map.put("examId", examId);
        //查询基本信息
        List<Map> list = pgInfoDao.baseInfo(map);
        if (list != null && list.size() > 0) {
            for (Map base : list) {
                Long studentId = Long.valueOf(base.get("student_id").toString());
                String pdType = base.get("pd_type").toString();
                String kpId =base.get("kp_id").toString();
                pgInfo.setStudentId(studentId);
                AppUser appUserById = sysDepartmentFeign.findAppUserById(studentId);
                pgInfo.setDeptId(appUserById.getDepartmentId());
                pgInfo.setDeptName(appUserById.getDepartmentName());
                pgInfo.setKpId(kpId);
                KnowledgePoints knowledgePointsById = manageBackendFeign.getKnowledgePointsById(kpId);
                pgInfo.setKpName(knowledgePointsById.getPointName());
                pgInfo.setStudentName(appUserById.getUsername());
                pgInfo.setPdType(pdType);
                //A部分计算结果
                map.put("kpId", kpId);
                map.put("pdType", pdType);
                map.put("studentId", studentId);
                Map baseA = pgInfoDao.getA(map);
                String rightQuestion = baseA.get("sum").toString();
                Integer totalQuestion = Integer.parseInt(baseA.get("count").toString());
                String questionResult = baseA.get("result").toString();
                pgInfo.setRightQuestion(rightQuestion);
                pgInfo.setTotalQuestion(totalQuestion);
                pgInfo.setQuestionResult(questionResult);
                pgInfo.setExamId(examId);
                //B部分计算结果
                Map baseB = pgInfoDao.getB(map);
                String rightScore = baseB.get("sum").toString();
                String totalScore = baseB.get("total_score").toString();
                String scoreResult = baseB.get("result").toString();
                pgInfo.setRightScore(rightScore);
                pgInfo.setTotalScore(totalScore);
                pgInfo.setScoreResult(scoreResult);
                double a = Double.valueOf(pgInfo.getQuestionResult());
                double b = Double.valueOf(pgInfo.getScoreResult());
                double result = (1 - (a - b)) * a * 10;
                pgInfo.setResult(String.valueOf(result));
                // 根据学生id，判读类型，知识点id，活动id查询是否已经存在
                QueryWrapper<PgInfo> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("student_id", studentId);
                queryWrapper.eq("pd_type", pdType);
                queryWrapper.eq("kp_id", kpId);
                queryWrapper.eq("exam_id", examId);
                PgInfo pgInfo1 = pgInfoDao.selectOne(queryWrapper);
                if (pgInfo1 != null) {
                    pgInfo.setId(pgInfo1.getId());
                    pgInfoDao.updateById(pgInfo);
                } else {
                    pgInfoDao.insert(pgInfo);
                }
            }
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 本次考试 各部门 各知识点能力计算 exam_eval_dept 表
     * @date: 2021/12/30
     * @param: [examId]
     * @return: void
     */
    public void getExamDeptKp(long examId) {
        Map<String, Object> map = new HashMap<>();
        map.put("acId", examId);
        List<Map<String, Object>> deptList = evalPersonDao.getDeptByAcId(map);
        if (deptList != null && deptList.size() > 0) {
            for (Map<String, Object> dept : deptList) {
                long deptId = Long.valueOf(dept.get("department_id").toString());
                map.put("deptId", deptId);
                List<Map<String, Object>> evalDeptScore = evalPersonDao.getEvalDeptScore(map);
                if (evalDeptScore != null && evalDeptScore.size() > 0) {
                    for (Map<String, Object> scoreBean : evalDeptScore) {
                        ExamEvalDept examEvalDept = new ExamEvalDept();
                        examEvalDept.setAcId(examId);
                        examEvalDept.setDepartmentId(deptId);
                        examEvalDept.setKpId(Long.valueOf(scoreBean.get("kp_id").toString()));
                        examEvalDept.setEvalScore(Long.valueOf(scoreBean.get("avg").toString()));
                        examEvalDept.setCreateTime(new Timestamp(System.currentTimeMillis()));
                        evalDeptDao.insert(examEvalDept);
                    }
                }
            }
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 本次考试 各部门综合能力计算  exam_eval_dept 表
     * @date: 2021/12/30
     * @param: [examId]
     * @return: void
     */
    public void getExamDept(long examId) {
        Map<String, Object> map = new HashMap<>();
        map.put("acId", examId);
        List<Map<String, Object>> examDeptScore = evalDeptDao.getExamDeptScore(map);
        if (examDeptScore != null && examDeptScore.size() > 0) {
            for (Map<String, Object> bean : examDeptScore) {
                ExamEvalDept examEvalDept = new ExamEvalDept();
                examEvalDept.setAcId(examId);
                examEvalDept.setDepartmentId(Long.valueOf(bean.get("department_id").toString()));
                examEvalDept.setKpId(0);
                examEvalDept.setEvalScore(Long.valueOf(bean.get("avg").toString()));
                examEvalDept.setCreateTime(new Timestamp(System.currentTimeMillis()));
                evalDeptDao.insert(examEvalDept);
            }
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 各人 各知识点 能力计算 exam_eval_person_his表
     * @date: 2021/12/30
     * @param: [examId, userKpCountMap]
     * @return: void
     */
    public void getPersonHisKp(long examId,
                               Map<Long, Map<Long, Integer>> userKpCountMap,
                               Map<Long, Map<Long, Integer>> userKpkhCountMap,
                               Map<Long, Map<Long, Integer>> userKpxlCountMap) {
        Map<String, Object> personMap = new HashMap<>();
        personMap.put("acId", examId);
        List<Map<String, Object>> evalPersonInfoList = evalPersonDao.getEvalPersonInfo(personMap);
        if (evalPersonInfoList != null && evalPersonInfoList.size() > 0) {
            for (Map<String, Object> bean : evalPersonInfoList) {
                int month = Integer.parseInt(bean.get("month").toString());
                long kpId = Long.valueOf(bean.get("kp_id").toString());
                long userId = Long.valueOf(bean.get("user_id").toString());
                // 根据user_id,kp_id,month查询表exam_eval_person_his
                personMap.put("userId", userId);
                personMap.put("kpId", kpId);
                personMap.put("month", month);
                Map<String, Object> evalPersonHisInfo = evalPersonHisDao.getEvalPersonHisInfo(personMap);
                // 已存在该数据：更新分数和试题数目
                if (evalPersonHisInfo != null) {
                    ExamEvalPersonHis examEvalPersonHis = evalPersonHisDao.selectById(Long.valueOf(evalPersonHisInfo.get("id").toString()));
                    double score = examEvalPersonHis.getEvalScore() * 0.8 + Long.valueOf(bean.get("eval_score") == null ? "0" : bean.get("eval_score").toString()) * 0.2;
                    examEvalPersonHis.setEvalScore(Long.valueOf((String.valueOf(score).substring(0, String.valueOf(score).indexOf(".")))));

                    Long totalNum = examEvalPersonHis.getTotalNum() == null ? 0 : examEvalPersonHis.getTotalNum();
                    examEvalPersonHis.setTotalNum(totalNum + getKpTotalNum(userId, kpId, userKpCountMap));
                    Long totalkhNum = examEvalPersonHis.getKhNum() == null ? 0 : examEvalPersonHis.getKhNum();
                    examEvalPersonHis.setKhNum(totalkhNum + getKpTotalNum(userId, kpId, userKpkhCountMap));
                    Long totalxlNum = examEvalPersonHis.getXlNum() == null ? 0 : examEvalPersonHis.getXlNum();
                    examEvalPersonHis.setXlNum(totalxlNum + getKpTotalNum(userId, kpId, userKpxlCountMap));

                    examEvalPersonHis.setAcId(examId);
                    examEvalPersonHis.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                    evalPersonHisDao.updateById(examEvalPersonHis);
                }
                // 不存在该数据：
                // 1>根据user_id,kp_id,cur_flg=1 查询试题数目
                personMap.put("curFlg", 1);
                Map<String, Object> infoMap = evalPersonHisDao.getEvalPersonHisInfo(personMap);
                long lastTotalNum = infoMap == null ? 0 : Long.valueOf(infoMap.get("total_num").toString());
                long lastkhNum = infoMap == null ? 0 : Long.valueOf(infoMap.get("kh_num").toString());
                long lastxlNum = infoMap == null ? 0 : Long.valueOf(infoMap.get("xl_num").toString());
                // 2>根据user_id,kp_id更新cur_flg=0
                evalPersonHisDao.updateInfo(personMap);
                // 3>添加数据，cur_flg=1
                ExamEvalPersonHis personHis = new ExamEvalPersonHis();
                personHis.setUserId(userId);
                personHis.setDepartmentId(Long.valueOf(bean.get("department_id").toString()));
                personHis.setKpId(kpId);
                personHis.setMonth(month);
                personHis.setCurFlg(1);
                personHis.setAcId(examId);
                personHis.setTotalNum(lastTotalNum + getKpTotalNum(userId, kpId, userKpCountMap));
                personHis.setXlNum(lastxlNum + getKpTotalNum(userId, kpId, userKpxlCountMap));
                personHis.setKhNum(lastkhNum + getKpTotalNum(userId, kpId, userKpkhCountMap));
                personHis.setEvalScore(Long.valueOf(bean.get("eval_score").toString()));
                personHis.setCreateTime(new Timestamp(System.currentTimeMillis()));
                evalPersonHisDao.insert(personHis);
            }
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 各人 综合能力计算 exam_eval_person_his表
     * @date: 2021/12/30
     * @param: [examId]
     * @return: void
     */
    public void getPersonHis(long examId,
                             Map<Long, Integer> userQuestionCountMap,
                             Map<Long, Integer> userQuestionxlCountMap,
                             Map<Long, Integer> userQuestionkhCountMap) {
        Map<String, Object> map = new HashMap<>();
        map.put("acId", examId);
        List<Map<String, Object>> personHisScore = evalPersonHisDao.getPersonHisScore(map);
        if (personHisScore != null && personHisScore.size() > 0) {
            for (Map<String, Object> hisMap : personHisScore) {
                long userId = Long.valueOf(hisMap.get("user_id").toString());
                int month = Integer.valueOf(hisMap.get("month").toString());
                Long score = Long.valueOf(hisMap.get("avg").toString());
                // 根据 user_id,month,kp_id=0 查询exam_eval_person_his
                map = new HashMap<>();
                map.put("userId", userId);
                map.put("month", month);
                map.put("kpId", 0);
                Map<String, Object> personHisPar = evalPersonHisDao.getPersonHisPar(map);
                // 存在该数据：更新eval_score,total_num
                if (personHisPar != null) {
                    map.put("id", Long.valueOf(personHisPar.get("id").toString()));
                    map.put("evalScore", hisMap.get("avg"));
                    map.put("totalNum", Long.parseLong(personHisPar.get("total_num").toString()) + userQuestionCountMap.get(userId));
                    map.put("xlNum", Long.parseLong(personHisPar.get("xl_num").toString()) + (userQuestionxlCountMap.get(userId) == null ? 0 : userQuestionxlCountMap.get(userId)));
                    map.put("khNum", Long.parseLong(personHisPar.get("kh_num").toString()) + (userQuestionkhCountMap.get(userId) == null ? 0 : userQuestionkhCountMap.get(userId)));
                    evalPersonHisDao.updateHisZonghe(map);
                } else {
                    // 不存在该数据：1>根据user_id,kp_id=0,cur_flg=1查询total_num
                    map = new HashMap<>();
                    map.put("userId", userId);
                    map.put("kpId", 0);
                    map.put("curFlg", 1);
                    personHisPar = evalPersonHisDao.getPersonHisPar(map);
                    long totalnum = personHisPar == null ? 0 : Long.valueOf(personHisPar.get("total_num").toString());
                    long totalxlnum = personHisPar == null ? 0 : Long.valueOf(personHisPar.get("xl_num").toString());
                    long totalkhnum = personHisPar == null ? 0 : Long.valueOf(personHisPar.get("kh_num").toString());
                    if (personHisPar != null) {
                        // 2>根据id 更新 cur_flg=0
                        map.put("id", Long.valueOf(personHisPar.get("id").toString()));
                        evalPersonHisDao.updateFlgById(map);
                    }
                    // 3> 新增数据 cur_flg=1,total_num=1>.total_num+本次的数量
                    ExamEvalPersonHis bean = new ExamEvalPersonHis();
                    bean.setAcId(examId);
                    bean.setCurFlg(1);
                    bean.setMonth(month);
                    bean.setUserId(userId);
                    bean.setEvalScore(Long.valueOf(hisMap.get("avg").toString()));
                    bean.setCreateTime(new Timestamp(System.currentTimeMillis()));
                    bean.setTotalNum(totalnum + userQuestionCountMap.get(userId));
                    bean.setKhNum(totalkhnum + (userQuestionkhCountMap.get(userId) == null ? 0 : userQuestionkhCountMap.get(userId)));
                    bean.setXlNum(totalxlnum + (userQuestionxlCountMap.get(userId) == null ? 0 : userQuestionxlCountMap.get(userId)));
                    bean.setDepartmentId(Long.valueOf(hisMap.get("department_id").toString()));
                    evalPersonHisDao.insert(bean);
                }
            }
        }
    }


    public void getDeptHis(long examId) {
        Map<String, Object> map = new HashMap<>();
        map.put("acId", examId);
        List<Map<String, Object>> deptHisList = evalPersonHisDao.getDeptHis(map);
        if (deptHisList != null && deptHisList.size() > 0) {
            for (Map<String, Object> m : deptHisList) {
                long deptId = Long.valueOf(m.get("department_id").toString());
                long kpId = Long.valueOf(m.get("kp_id").toString());
                long avg = Long.valueOf(m.get("avg").toString());
                // 根据kp_id,department_id删除数据
                map.put("deptId", deptId);
                evalDeptHisDao.delDeptHis(map);
                // 将当前能力值进行数据插入
                ExamEvalDeptHis examEvalDeptHis = new ExamEvalDeptHis();
                examEvalDeptHis.setCreateTime(new Timestamp(System.currentTimeMillis()));
                examEvalDeptHis.setDepartmentId(deptId);
                examEvalDeptHis.setEvalScore(avg);
                examEvalDeptHis.setKpId(kpId);
                evalDeptHisDao.insert(examEvalDeptHis);
            }
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 获取各知识点题目数量
     * @date: 2021/12/30
     * @param: [userId, kpId, userKpCountMap]
     * @return: java.lang.Long
     */
    public Long getKpTotalNum(long userId, long kpId, Map<Long, Map<Long, Integer>> userKpCountMap) {
        Integer result = 0;
        Map<Long, Integer> map = userKpCountMap.get(userId);
        if (map != null) {
            result = map.get(kpId) == null ? 0 : map.get(kpId);
        }
        return Long.valueOf(result);
    }


    public static void main(String[] args) {
        long a = 1l;
        double b = 1.23;
        double c = a + b;
        int total = 2;
        double d = c / total;
        System.out.println(d);

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
            kpName = manageBackendFeign.getKnowledgePointsById(kpId).getPointName();
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
        String userName = Tools.getUserCacheValue(userId);
        if (userName == null) {
            AppUser user = sysDepartmentFeign.findUserById(userId);
            userName = user.getUsername();
            Tools.putUserCacheValue(userId, userName);
        }
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
}
