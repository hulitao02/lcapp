package com.cloud.model.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.feign.exam.ExamFeign;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.model.dao.*;
import com.cloud.model.service.KnowledgeViewService;
import com.cloud.utils.CollectionsCustomer;
import io.swagger.annotations.ApiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/delknowledge")
@ApiModel(value = "删除知识controller")
@Slf4j
@RefreshScope
public class DelKnowledgeController {

    @Autowired
    ManageBackendFeign manageBackendFeign;
    @Autowired
    ModelKpDao modelKpDao;
    @Autowired
    ModelDataDao modelDataDao;
    @Autowired
    ExamFeign examFeign;
    @Autowired
    CollectKnowledgeDao collectKnowledgeDao;
    @Autowired
    StudyKnowledgeDao studyKnowledgeDao;
    @Autowired
    KnowledgeViewDao knowledgeViewDao;
    @Autowired
    QuestionDao questionDao;
    @Autowired
    StudyNoteDao studyNoteDao;
    @Autowired
    KnowledgeViewService knowledgeViewService;


    /**
     * @author: 胡立涛
     * @description: TODO 验证知识点能否被删除
     * @date: 2022/5/20
     * @param: [kpCodeList]
     * @return: com.cloud.core.ApiResult 0：可以被删除 100：不可以删除 101：知识点已被使用，如果删除，与知识点相关数据均会被删除
     */
    @PostMapping(value = "checkKnowledgePoint")
    public ApiResult checkKnowledgePoint(@RequestBody List<String> kpCodeList) {
        try {
            List<Long> kpIdList = new ArrayList<>();

            // 步骤一
            //  验证知识点是否完成数据对接（model_kp)
            //  验证知识点是否绑定数据(model_data)
            //  验证知识点是否管理试题(question_kp_rl)

            // ture:可以进行删除 false:不可以进行删除
            boolean flg = true;
            for (String code : kpCodeList) {
                log.info("图谱传递知识点code："+code);
                // 根据kpcode查询kpid
                if (manageBackendFeign.getKnowledgePointsByCode(code)==null){
                    continue;
                }
                Map knowledgePointsByCode = manageBackendFeign.getKnowledgePointsByCode(code);
//                knowledgePointsByCode = CollectionsCustomer.builder().build().mapToLowerCase(knowledgePointsByCode);
                Long kpId = Long.valueOf(knowledgePointsByCode.get("id").toString());
                kpIdList.add(kpId);
                // 验证知识点是否完成数据对接（model_kp 表中的状态是否为2）
                QueryWrapper query = new QueryWrapper();
                query.eq("kp_id", kpId);
                query.eq("status", 2);
                List list = modelKpDao.selectList(query);
                if (!list.isEmpty() && list.size() > 0) {
                    flg = false;
                    break;
                }
                // 验证知识点是否已绑定数据（model_data表)
                query = new QueryWrapper();
                query.eq("kp_id", kpId);
                List dataList = modelDataDao.selectList(query);
                if (!dataList.isEmpty() && dataList.size() > 0) {
                    flg = false;
                    break;
                }
                // 验证知识点是否被试题使用（question_kp_rel)
                int checkKpId = examFeign.checkKpId(kpId);
                if (checkKpId == 1) {
                    flg = false;
                    break;
                }
            }
            if (flg == false) {
                return ApiResultHandler.buildApiResult(101, "知识点已被使用，不能删除", null);
            }

            // 步骤二
            //  校验入库出错试题是否使用该知识点(question_error)
            //  校验用户每次活动能力评估是否使用该知识点（exam_eval_person）
            //  校验用户历史活动能力评估是否使用该知识点（exam_eval_person_his）
            //  校验部门每次活动能力评估是否使用该知识点（exam_eval_dept）
            //  校验部门历史活动能力评估是否使用该知识点（exam_eval_dept_his）

            // ture:可以进行删除 false:不可以进行删除
            boolean flgTwo = true;
            for (Long kpId : kpIdList) {
                // 校验入库出错试题是否使用该知识点(question_error)
                int checkKpId = examFeign.checkErrorKpId(kpId);
                if (checkKpId == 1) {
                    flgTwo = false;
                    break;
                }
                // 校验用户每次活动能力评估是否使用该知识点（exam_eval_person）
                checkKpId = examFeign.checkExamEvalPerson(kpId);
                if (checkKpId == 1) {
                    flgTwo = false;
                    break;
                }
                // 校验用户历史活动能力评估是否使用该知识点（exam_eval_person_his）
                checkKpId = examFeign.checkExamEvalPersonHis(kpId);
                if (checkKpId == 1) {
                    flgTwo = false;
                    break;
                }
                // 校验部门每次活动能力评估是否使用该知识点（exam_eval_dept）
                checkKpId = examFeign.checkExamEvalDept(kpId);
                if (checkKpId == 1) {
                    flgTwo = false;
                    break;
                }
                // 校验部门历史活动能力评估是否使用该知识点（exam_eval_dept_his）
                checkKpId = examFeign.checkExamEvalDeptHis(kpId);
                if (checkKpId == 1) {
                    flgTwo = false;
                    break;
                }
            }
            if (flgTwo == false) {
                return ApiResultHandler.buildApiResult(102, "知识点已被使用，如果删除知识点，与之相关联数据均会被删除。", null);
            }
            return ApiResultHandler.buildApiResult(0, "知识点未被使用，可以删除。", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }


    /**
     * @author: 胡立涛
     * @description: TODO 删除知识点
     * @date: 2022/5/23
     * @param: [kpCodeList]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "/delKnowledgePoint")
    public ApiResult delKnowledgePoint(@RequestBody List<String> kpCodeList) {
        try {
            String kpCodeStr="";
            if (kpCodeList==null || kpCodeList.size()==0){
                return ApiResultHandler.buildApiResult(100, "参数为空", null);
            }
            for (int i=0;i<kpCodeList.size();i++){
                String kpCode=kpCodeList.get(i);
                kpCodeStr+=kpCode+",";
            }
            Map<String,Object> map=new HashMap();
            map.put("kpCodeStr",kpCodeStr);
            manageBackendFeign.delKnowledgePointManage(map);
            examFeign.delKnowledgePoint(map);
            return ApiResultHandler.buildApiResult(0, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }


    /**
     * @author: 胡立涛
     * @description: TODO 删除知识验证
     * @date: 2022/5/24
     * @param: [KnowledgeCodeList]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "/checDelKnowledge")
    public ApiResult checDelKnowledge(@RequestBody List<String> KnowledgeCodeList) {
        try {
            if (KnowledgeCodeList == null || KnowledgeCodeList.size() == 0) {
                return ApiResultHandler.buildApiResult(100, "KnowledgeCodeList参数为空", null);
            }
            // 删除标识 true：可以删除 false:不可以删除
            boolean flg = true;
            for (String knowledgeCode : KnowledgeCodeList) {
                //  校验该知识是否被收藏（collection_knowledge）
                QueryWrapper query = new QueryWrapper();
                query.eq("senses_id", knowledgeCode);
                List list = collectKnowledgeDao.selectList(query);
                if (!list.isEmpty() && list.size() > 0) {
                    flg = false;
                    break;
                }
                //  校验该知识是否被学习计划使用（study_knowledge）
                query = new QueryWrapper();
                query.eq("knowledge_id", knowledgeCode);
                list = studyKnowledgeDao.selectList(query);
                if (!list.isEmpty() && list.size() > 0) {
                    flg = false;
                    break;
                }
                //  校验该知识是否已被学习（knowledge_view）
                query = new QueryWrapper();
                query.eq("senses_id", knowledgeCode);
                list = knowledgeViewDao.selectList(query);
                if (!list.isEmpty() && list.size() > 0) {
                    flg = false;
                    break;
                }
                //  校验该知识是否已被提问（question）
                query = new QueryWrapper();
                query.eq("kn_id", knowledgeCode);
                list = questionDao.selectList(query);
                if (!list.isEmpty() && list.size() > 0) {
                    flg = false;
                    break;
                }
                //  校验该知识是否已被学习笔记使用（study_notes）
                query = new QueryWrapper();
                query.eq("kn_id", knowledgeCode);
                list = studyNoteDao.selectList(query);
                if (!list.isEmpty() && list.size() > 0) {
                    flg = false;
                    break;
                }
                //  校验该知识是否被课程使用（course_kp_rel）
                int result = examFeign.checkCourseKpRel(knowledgeCode);
                if (result == 1) {
                    flg = false;
                    break;
                }
            }

            if (flg == false) {
                return ApiResultHandler.buildApiResult(101, "知识已被使用，如果删除该知识，与之相关联数据均会被删除。", null);
            }
            return ApiResultHandler.buildApiResult(0, "知识未被使用，可以删除。", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }


    /**
     * @author: 胡立涛
     * @description: TODO 删除知识
     * @date: 2022/5/28
     * @param: [KnowledgeCodeList]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "delKnowledge")
    public ApiResult delKnowledge(@RequestBody List<String> knowledgeCodeList) {
        try {
            if (knowledgeCodeList == null || knowledgeCodeList.size() == 0) {
                return ApiResultHandler.buildApiResult(100, "参数为空。", null);
            }

            String kpIdsStr="";
            String[] kIds = new String[knowledgeCodeList.size()];
            for (int i = 0; i < knowledgeCodeList.size(); i++) {
                kIds[i] = "'" + knowledgeCodeList.get(i) + "'";
                kpIdsStr+=knowledgeCodeList.get(i)+",";
            }
            Map map=new HashMap<>();
            map.put("kpIdsStr",kpIdsStr);
            // 删除课程中的知识
            examFeign.delKnowledge(map);
            // 删除学习中的知识
            knowledgeViewService.delKnowledge(kIds);
            return ApiResultHandler.buildApiResult(0, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }


    /**
     * @author: 胡立涛
     * @description: TODO 知识管理中的知识点删除验证
     * @date: 2022/5/28
     * @param: [kpCodeList]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "checkKnowledgePointManage")
    public ApiResult checkKnowledgePointManage(@RequestBody List<Long> kpIds) {
        try {
            // 步骤一
            //  验证知识点是否管理试题(question_kp_rl)
            // ture:可以进行删除 false:不可以进行删除
            boolean flg = true;
            for (Long kpId : kpIds) {
                // 验证知识点是否被试题使用（question_kp_rel)
                int checkKpId = examFeign.checkKpId(kpId);
                if (checkKpId == 1) {
                    flg = false;
                    break;
                }
            }
            if (flg == false) {
                return ApiResultHandler.buildApiResult(101, "知识点已被使用，不能删除", null);
            }
            // 步骤二
            //  校验入库出错试题是否使用该知识点(question_error)
            //  校验用户每次活动能力评估是否使用该知识点（exam_eval_person）
            //  校验用户历史活动能力评估是否使用该知识点（exam_eval_person_his）
            //  校验部门每次活动能力评估是否使用该知识点（exam_eval_dept）
            //  校验部门历史活动能力评估是否使用该知识点（exam_eval_dept_his）

            // ture:可以进行删除 false:不可以进行删除
            boolean flgTwo = true;
            for (Long kpId : kpIds) {
                // 校验入库出错试题是否使用该知识点(question_error)
                int checkKpId = examFeign.checkErrorKpId(kpId);
                if (checkKpId == 1) {
                    flgTwo = false;
                    break;
                }
                // 校验用户每次活动能力评估是否使用该知识点（exam_eval_person）
                checkKpId = examFeign.checkExamEvalPerson(kpId);
                if (checkKpId == 1) {
                    flgTwo = false;
                    break;
                }
                // 校验用户历史活动能力评估是否使用该知识点（exam_eval_person_his）
                checkKpId = examFeign.checkExamEvalPersonHis(kpId);
                if (checkKpId == 1) {
                    flgTwo = false;
                    break;
                }
                // 校验部门每次活动能力评估是否使用该知识点（exam_eval_dept）
                checkKpId = examFeign.checkExamEvalDept(kpId);
                if (checkKpId == 1) {
                    flgTwo = false;
                    break;
                }
                // 校验部门历史活动能力评估是否使用该知识点（exam_eval_dept_his）
                checkKpId = examFeign.checkExamEvalDeptHis(kpId);
                if (checkKpId == 1) {
                    flgTwo = false;
                    break;
                }
            }
            if (flgTwo == false) {
                return ApiResultHandler.buildApiResult(102, "知识点已被使用，如果删除知识点，与之相关联数据均会被删除。", null);
            }
            return ApiResultHandler.buildApiResult(0, "知识点未被使用，可以删除。", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }


    /**
     * @author: 胡立涛
     * @description: TODO 删除知识管理中的知识点
     * @date: 2022/5/28
     * @param: [kpIds]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "/delKnowledgePointManage")
    public ApiResult delKnowledgePointManage(@RequestBody List<Long> kpIds) {
        try {
            if (kpIds == null || kpIds.size() == 0) {
                return ApiResultHandler.buildApiResult(100, "参数为空", null);
            }
            Long[] kpIdArr = new Long[kpIds.size()];
            for (int i = 0; i < kpIds.size(); i++) {
                kpIdArr[i] = kpIds.get(i);
            }
            Map map=new HashMap();
            map.put("kpIdArr",kpIdArr);
            examFeign.delKnowledgePoint(map);
            manageBackendFeign.delKnowledgePointManage(map);
            return ApiResultHandler.buildApiResult(0, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }
}
