package com.cloud.exam.utils.exam;

import com.alibaba.druid.sql.dialect.hive.visitor.HiveASTVisitor;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.exam.model.exam.Question;
import com.cloud.exam.model.exam.QuestionKpRel;
import com.cloud.exam.model.exam.QuestionKpRelManage;
import com.cloud.exam.model.exam.QuestionManage;
import com.cloud.exam.service.QuestionKpRelManageService;
import com.cloud.exam.service.QuestionKpRelService;
import com.cloud.feign.knowledge.KnowledgeFeign;
import com.cloud.feign.managebackend.ManageBackendFeign;
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
public class QuestionUtils {

    @Autowired
    private QuestionKpRelService qk;
    private static QuestionKpRelService questionKpRelService;

    @Autowired
    private QuestionKpRelManageService qkm;
    private static QuestionKpRelManageService questionKpRelManageService;

    @Autowired
    private ManageBackendFeign mbf;
    private static ManageBackendFeign manageBackendFeign;
    @Autowired
    KnowledgeFeign kf;
    static  KnowledgeFeign knowledgeFeign;
    @PostConstruct
    public void init(){
        questionKpRelService = this.qk;
        manageBackendFeign = this.mbf;
        knowledgeFeign=this.kf;
        questionKpRelManageService=this.qkm;
    }

    /**
     * 根据试题获取试题对应知识点的中文名
     * @param question
     * @return
     */
    public static String getKpNamesByQuestion(Question question){
        QueryWrapper<QuestionKpRel> questionKpRelQueryWrapper  =  new QueryWrapper<>();
        questionKpRelQueryWrapper.eq("question_id",question.getId());
        List<QuestionKpRel> list = questionKpRelService.list(questionKpRelQueryWrapper);

        String str = "";
        if(list.size()>0){
            for (QuestionKpRel kid:list) {
                Map knowledgePointsById = knowledgeFeign.getKnowledgePointsById(kid.getKpId());
                if (knowledgePointsById!=null){
                    str += " "+knowledgePointsById.get("name").toString();
                }
            }
        }
        return str ;
    }


    /**
     *
     * @author:胡立涛
     * @description: TODO 试题管理知识点
     * @date: 2025/1/15
     * @param: [question]
     * @return: java.lang.String
     */
    public static String getKpNamesByQuestionManage(QuestionManage question){
        QueryWrapper<QuestionKpRelManage> questionKpRelQueryWrapper  =  new QueryWrapper<>();
        questionKpRelQueryWrapper.eq("question_id",question.getId());
        List<QuestionKpRelManage> list = questionKpRelManageService.list(questionKpRelQueryWrapper);

        String str = "";
        if(list.size()>0){
            for (QuestionKpRelManage kid:list) {
                Map knowledgePointsById = knowledgeFeign.getKnowledgePointsById(kid.getKpId());
                if (knowledgePointsById!=null){
                    str += " "+knowledgePointsById.get("name").toString();
                }
            }
        }
        return str ;
    }

    /**
     * 根据试题获取试题对应知识点的中文名和id
     * @param question
     * @return
     */
    public static List<HashMap<String,Object>> getKpDetailsByQuestion(Question question){
        QueryWrapper<QuestionKpRel> questionKpRelQueryWrapper  =  new QueryWrapper<>();
        questionKpRelQueryWrapper.eq("question_id",question.getId());
        List<QuestionKpRel> list = questionKpRelService.list(questionKpRelQueryWrapper);
        List<HashMap<String,Object>> list1 = new ArrayList<>();
        if(list.size()>0){
            for (QuestionKpRel kid:list) {
                HashMap<String,Object> map = new HashMap<>();
                if(null != manageBackendFeign.getKnowledgePointsById(kid.getKpId())){
                    map.put("id",kid.getKpId());
                    map.put("name",manageBackendFeign.getKnowledgePointsById(kid.getKpId()).getPointName());
                    map.put("score",0) ;
                }
                list1.add(map) ;
            }
        }
        return list1 ;
    }
}
