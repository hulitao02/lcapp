package com.cloud.model.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.model.bean.vo.KnowledgeProVO;
import com.cloud.model.dao.KnowledgeProDao;
import com.cloud.model.model.KnowledgePro;
import com.cloud.model.service.KnowledgeProService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public class KnowledgeProServiceImpl extends ServiceImpl<KnowledgeProDao, KnowledgePro>
        implements KnowledgeProService {

    @Autowired
    private KnowledgeProDao knowledgeProDao;

    @Override
    public List<KnowledgeProVO> getKpProList(Map<String, Object> paramMap) {
        return this.knowledgeProDao.getKpProList(paramMap);
    }


}
