package com.cloud.model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.model.dao.AnswerDao;
import com.cloud.model.dao.KnowledgeTifDao;
import com.cloud.model.dao.ModelKpDao;
import com.cloud.model.model.Answer;
import com.cloud.model.model.KnowledgeTif;
import com.cloud.model.model.ModelKp;
import com.cloud.model.service.AnswerService;
import com.cloud.model.service.KnowledgeTifService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KnowledgeTifServiceImpl extends ServiceImpl<KnowledgeTifDao, KnowledgeTif>
        implements KnowledgeTifService {


    @Autowired
    KnowledgeTifDao knowledgeTifDao;
    @Autowired
    ModelKpDao modelKpDao;

    /**
     * @author:胡立涛
     * @description: TODO 影像标注模板：完成数据对接
     * @date: 2022/10/20
     * @param: [knowledgeTif]
     * @return: void
     */
    @Override
    @Transactional
    public void finishData(KnowledgeTif knowledgeTif) {
        // 根据知识点id和知识code查询是否存在该数据
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("kp_id", knowledgeTif.getKpId());
        queryWrapper.eq("knowledge_code", knowledgeTif.getKnowledgeCode());
        List list = knowledgeTifDao.selectList(queryWrapper);
        // 不存在该数据，进行保存操作
        if (list.isEmpty() || list.size() == 0) {
            knowledgeTifDao.insert(knowledgeTif);
        }
        // 根据主键id更新model_kp表的状态 status 为2
        ModelKp modelKp = modelKpDao.selectById(knowledgeTif.getModelKpId());
        modelKp.setStatus(2);
        modelKpDao.updateById(modelKp);
    }
}
