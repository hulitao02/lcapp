package com.cloud.model.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.model.bean.vo.KnowledgePointVo;
import com.cloud.model.bean.vo.ModelKpVo;
import com.cloud.model.model.ModelKp;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ModelKpService extends IService<ModelKp> {

    List<KnowledgePointVo> getKpVoListByParentId(String parentId, Boolean isAll);

    List<ModelKp> getByModelId(Long pageModelId);

    ModelKp queryById(Long id);

    Boolean add(String kpId, Long modelId);

    Boolean addList(String kpId, List<Long> modelIds);

    Boolean updateName(Long modelKpId, String name);

    Boolean updateLinkIco(Long modelKpId, Integer linkType, MultipartFile icoFile);

    Boolean deleteById(Long modelKpId);

    IPage<ModelKpVo> getPageByKpId(String kpId, Integer pageNum, Integer pageSize);

    List<ModelKp> getListByKpId(String kpId);

    List<ModelKpVo> getVoListByKpId(String kpId);

    ModelKpVo getModelKpVoById(Long modelKpId);

    Boolean updateStatus(Long modelKpId, Integer status);

    void updateSort(List<Long> modelKpIds);

    List<ModelKp> getModelKpList(List<String> kpIdList);
}
