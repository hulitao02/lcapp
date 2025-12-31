package com.cloud.model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.model.dao.*;
import com.cloud.model.model.KnowledgeViewBean;
import com.cloud.model.service.KnowledgeViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * @author:胡立涛
 * @description: TODO 浏览记录
 * @date: 2022/1/13
 * @param:
 * @return:
 */
@Service
@Slf4j
public class KnowledgeViewServiceImpl extends ServiceImpl<KnowledgeViewDao, KnowledgeViewBean>
        implements KnowledgeViewService {

    @Autowired
    KnowledgeViewDao knowledgeViewDao;
    @Autowired
    CollectKnowledgeDao collectKnowledgeDao;
    @Autowired
    StudyKnowledgeDao studyKnowledgeDao;
    @Autowired
    QuestionDao questionDao;
    @Autowired
    StudyNoteDao studyNoteDao;

    @Override
    public IPage<KnowledgeViewBean> pageUserKnowledgeView(int pageNo, int pageSize, KnowledgeViewBean knowledgeViewBean) {
        QueryWrapper<KnowledgeViewBean> wrapper = new QueryWrapper<>();
        if (Objects.nonNull(knowledgeViewBean.getUserId())) {
            wrapper.eq("user_id", knowledgeViewBean.getUserId());
        }
        if (Objects.nonNull(knowledgeViewBean.getSensesName())) {
            wrapper.like("senses_name", knowledgeViewBean.getSensesName());
        }
        wrapper.select().orderByDesc("study_date");
        Page<KnowledgeViewBean> page = new Page();
        page.setCurrent(pageNo);
        page.setSize(pageSize);
        return this.knowledgeViewDao.selectPage(page, wrapper);
    }

    @Override
    public List<Map<String, Object>> statisticsStudyCount(Map<String, Object> paramMap) {
        return this.knowledgeViewDao.statisticsStudyCount(paramMap);
    }


    @Override
    @Transactional
    public void delKnowledge(String[] kIds) {
        // 删除收藏与该知识相关数据（collection_knoledge）
        collectKnowledgeDao.delCollectionKnowledge(kIds);
        Map pmap=new HashMap();
        pmap.put("kIds",kIds);
        studyKnowledgeDao.delStudyKnowledge(pmap);
        // 浏览记录表与该知识相关数据进行删除（knowledge_view）
        knowledgeViewDao.delKnowledgeView(kIds);
        // 根据知识code查询question信息
        List<Map> question = questionDao.getQuestion(kIds);
        if (question != null && question.size() > 0) {
            Long[] qIds = new Long[question.size()];
            for (int i = 0; i < question.size(); i++) {
                Map map = question.get(i);
                qIds[i] = Long.valueOf(map.get("id").toString());
            }
            // 问题信息表与知识相关数据进行删除（question）
            questionDao.delQuestion(kIds);
            // 专家答疑表与知识相关数据进行删除（answer）
            questionDao.delAnswer(qIds);
        }
        // 学习笔记表与该知识相关数据进行删除（study_notes）
        studyNoteDao.delStudyNode(kIds);
    }
}
