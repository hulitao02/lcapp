package com.cloud.exam.utils.exam;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.exam.model.exam.QuestionKpRelManage;
import com.cloud.exam.model.exam.QuestionManage;
import com.cloud.exam.service.QuestionKpRelManageService;
import com.cloud.feign.knowledge.KnowledgeFeign;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.model.common.KnowledgePoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dyl on 2022/01/17.
 */
@Component
public class QuestionManageUtils {

    @Autowired
    private QuestionKpRelManageService qk;
    private static QuestionKpRelManageService questionKpRelService;
    @Autowired
    private ManageBackendFeign mbf;
    private static ManageBackendFeign manageBackendFeign;

    @Autowired
    KnowledgeFeign kf;
    static KnowledgeFeign knowledgeFeign;

    @PostConstruct
    public void init() {
        questionKpRelService = this.qk;
        manageBackendFeign = this.mbf;
        knowledgeFeign = this.kf;
    }


    /**
     * 根据试题获取试题对应知识点的中文名
     *
     * @param question
     * @return
     */
    public static String getKpNamesByQuestion(QuestionManage question) {
        QueryWrapper<QuestionKpRelManage> questionKpRelQueryWrapper = new QueryWrapper<>();
        questionKpRelQueryWrapper.eq("question_id", question.getId());
        List<QuestionKpRelManage> list = questionKpRelService.list(questionKpRelQueryWrapper);
        String str = "";
        if (list.size() > 0) {
            for (QuestionKpRelManage kid : list) {
                Map knowledgePointsById = knowledgeFeign.getKnowledgePointsById(kid.getKpId());
                if (knowledgePointsById != null) {
                    str += " " + knowledgePointsById.get("name").toString();
                }
            }
        }
        return str;
    }

    /**
     * 根据试题获取试题对应知识点的中文名和id
     *
     * @param question
     * @return
     */
    public static List<HashMap<String, Object>> getKpDetailsByQuestion(QuestionManage question) {
        QueryWrapper<QuestionKpRelManage> questionKpRelQueryWrapper = new QueryWrapper<>();
        questionKpRelQueryWrapper.eq("question_id", question.getId());
        List<QuestionKpRelManage> list = questionKpRelService.list(questionKpRelQueryWrapper);
        List<HashMap<String, Object>> list1 = new ArrayList<>();
        if (list.size() > 0) {
            for (QuestionKpRelManage kid : list) {
                HashMap<String, Object> map = new HashMap<>();
                if (null != manageBackendFeign.getKnowledgePointsById(kid.getKpId())) {
                    map.put("id", kid.getKpId());
                    map.put("name", manageBackendFeign.getKnowledgePointsById(kid.getKpId()).getPointName());
                    map.put("score", 0);
                }
                list1.add(map);
            }
        }
        return list1;
    }
}
