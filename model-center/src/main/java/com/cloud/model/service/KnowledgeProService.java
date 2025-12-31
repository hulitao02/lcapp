package com.cloud.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.bean.vo.KnowledgeProVO;
import com.cloud.model.model.KnowledgePro;

import java.util.List;
import java.util.Map;

public interface KnowledgeProService extends IService<KnowledgePro> {

    List<KnowledgeProVO> getKpProList(Map<String, Object> paramMap);


}
