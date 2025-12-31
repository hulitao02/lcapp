package com.cloud.model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.model.dao.CollectKnowledgeDao;
import com.cloud.model.model.CollectKnowledgeBean;
import com.cloud.model.service.CollectKnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 收藏知识 Service 类
 */
@Service
@Slf4j
public class CollectKnowledgeServiceImpl extends ServiceImpl<CollectKnowledgeDao, CollectKnowledgeBean>
        implements CollectKnowledgeService {


    @Autowired
    private CollectKnowledgeDao collectKnowledgeDao;

    @Override
    public IPage<CollectKnowledgeBean> pageUserCollectKnowledge(int pageNo, int pageSize, CollectKnowledgeBean collectKnowledgeBean) {
        QueryWrapper<CollectKnowledgeBean> wrapper = new QueryWrapper<>();
        if (Objects.nonNull(collectKnowledgeBean.getUpdateUserId())) {
            wrapper.eq("user_id", collectKnowledgeBean.getUserId());
        }
        if (Objects.nonNull(collectKnowledgeBean.getSensesName())) {
            wrapper.like("senses_name", collectKnowledgeBean.getSensesName());
        }
        if (Objects.nonNull(collectKnowledgeBean.getCollectStatus())) {
            wrapper.eq("collect_status", collectKnowledgeBean.getCollectStatus());
        }
        wrapper.select().orderByDesc("update_time");
        Page<CollectKnowledgeBean> page = new Page();
        page.setCurrent(pageNo);
        page.setSize(pageSize);
        IPage<CollectKnowledgeBean> collectKnowledgeBeanIPage = this.collectKnowledgeDao.selectPage(page, wrapper);
        return collectKnowledgeBeanIPage;
    }


    /**
     * 知识学习统计
     *
     * @param paramMap
     * @return
     */
    @Override
    public List<Map<String, Object>> statisticsStudyCount(Map<String, Object> paramMap) {
        return this.collectKnowledgeDao.statisticsStudyCount(paramMap);
    }

    @Override
    public List<Map<String, Object>> knStudyUserCountGroup(String paramDate) {
        return this.collectKnowledgeDao.knStudyUserCountGroup(paramDate);
    }

    @Override
    public List<Map<String, Object>> getKnCountGroupbyKnId(String paramDate) {
        return this.collectKnowledgeDao.getKnCountGroupbyKnId(paramDate);
    }


}
