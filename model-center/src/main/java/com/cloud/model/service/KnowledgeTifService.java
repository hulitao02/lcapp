package com.cloud.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.model.KnowledgeTif;

public interface KnowledgeTifService extends IService<KnowledgeTif> {

    /**
     *
     * @author:胡立涛
     * @description: TODO 影像标注模板：完成数据对接
     * @date: 2022/10/20
     * @param: [knowledgeTif]
     * @return: void
     */
    void finishData(KnowledgeTif knowledgeTif);
}
