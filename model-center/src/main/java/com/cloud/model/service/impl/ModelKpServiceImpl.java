package com.cloud.model.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.feign.knowledge.KnowledgeFeign;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.bean.vo.KnowledgePointVo;
import com.cloud.model.bean.vo.ModelKpVo;
import com.cloud.model.common.KnowledgePoints;
import com.cloud.model.common.KnowledgePointsBean;
import com.cloud.model.dao.ModelKpDao;
import com.cloud.model.enums.ModelKpStatus;
import com.cloud.model.model.ModelKp;
import com.cloud.model.model.PageModel;
import com.cloud.model.service.ModelKpService;
import com.cloud.model.service.PageModelService;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.model.utils.FileUtil;
import com.cloud.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.SocketUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ModelKpServiceImpl extends ServiceImpl<ModelKpDao, ModelKp> implements ModelKpService {

    @Autowired
    private PageModelService pageModelService;

    @Autowired
    private ManageBackendFeign manageBackendFeign;

    @Autowired
    private SysDepartmentFeign sysDepartmentFeign;

    @Value("${file.local.path}")
    private String filePath;

    @Value("${file.local.urlPrefix}")
    private String fileUrlPrefix;

    @Autowired
    private ModelKpDao modelKpDao;
    @Autowired
    KnowledgeFeign knowledgeFeign;

    @Override
    public List<KnowledgePointVo> getKpVoListByParentId(String parentId, Boolean isAll) {
        List<KnowledgePointVo> voList = new ArrayList<>();
        List<KnowledgePointsBean> knowledgePoints = new ArrayList<>();
        //根据用户知识点权限获取知识模板
        Set<String> kpIdsbyUserId = sysDepartmentFeign.getKpIdsbyUserId(AppUserUtil.getLoginAppUser().getId());
        if (kpIdsbyUserId == null) {
            return voList;
        }
        for (String kid : kpIdsbyUserId) {
            Map knowledgePointsById = knowledgeFeign.getKnowledgePointsById(kid);
            if (knowledgePointsById != null) {
                KnowledgePointsBean bean = new KnowledgePointsBean();
                bean.setId(knowledgePointsById.get("id").toString());
                bean.setPointName(knowledgePointsById.get("name").toString());
                bean.setParentId(knowledgePointsById.get("super_class_id").toString());
                knowledgePoints.add(bean);
            }
        }
        Map<String, KnowledgePointVo> voMap = knowledgePoints.stream()
                .filter(kp -> (isAll ? true : Objects.equals(kp.getParentId(), parentId)))
                .collect(Collectors.toMap(KnowledgePointsBean::getId, kp -> bulidKnowledgePointVo(kp), (k1, k2) -> k1));

        if (voMap.size() == 0) {
            return voList;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("kpIds", voMap.keySet());
        List<KnowledgePointVo> kpCountList = baseMapper.getModelCount(paramMap);
        Map<String, KnowledgePointVo> kpCountMap = kpCountList.stream().collect(Collectors.toMap(KnowledgePointVo::getKpId, kp -> kp, (k1, k2) -> k1));
        voMap.forEach((kpId, vo) -> {
            if (Objects.equals(vo.getKpParentId(), parentId)) {
                voList.add(vo);
            }
            KnowledgePointVo kpCount = kpCountMap.get(kpId);
            if (kpCount != null) {
                vo.setModelCount(kpCount.getModelCount());
                vo.setModelDataCount(kpCount.getModelDataCount());
            }
            if (isAll) {
                KnowledgePointVo parentVo = voMap.get(vo.getKpParentId());
                if (parentVo != null) {
                    List<KnowledgePointVo> childList = parentVo.getChildList();
                    if (childList == null) {
                        childList = new ArrayList<>();
                        parentVo.setChildList(childList);
                    }
                    childList.add(vo);
                }
            }
        });
        return voList;
    }

    private KnowledgePointVo bulidKnowledgePointVo(KnowledgePointsBean kp) {
        KnowledgePointVo vo = new KnowledgePointVo();
        vo.setKpId(kp.getId());
        vo.setKpName(kp.getPointName());
        vo.setKpParentId(kp.getParentId() != null ? kp.getParentId() : null);
        return vo;
    }

    @Override
    public List<ModelKp> getByModelId(Long pageModelId) {
        return baseMapper.selectList(new QueryWrapper<ModelKp>().lambda().eq(ModelKp::getModelId, pageModelId).gt(ModelKp::getStatus, ModelKpStatus.UNUSABLE.getValue()));
    }

    @Override
    public ModelKp queryById(Long id) {
        return baseMapper.selectOne(new QueryWrapper<ModelKp>().lambda().eq(ModelKp::getId, id).gt(ModelKp::getStatus, ModelKpStatus.UNUSABLE.getValue()));
    }

    @Override
    public Boolean add(String kpId, Long modelId) {
        //获取模板信息
        if (kpId == null || modelId == null) {
            throw new IllegalArgumentException("知识点或模板不能为空");
        }
        PageModel pageModel = pageModelService.getById(modelId);
        if (pageModel == null) {
            throw new IllegalArgumentException("模板信息不存在");
        }
        ModelKp modelKp = new ModelKp();
        modelKp.setKpId(kpId);
        modelKp.setModelId(modelId);
        modelKp.setName(pageModel.getName());
        modelKp.setStatus(ModelKpStatus.USABLE.getValue());
        return save(modelKp);
    }

    @Override
    @Transactional
    public Boolean addList(String kpId, List<Long> modelIds) {
        //获取模板信息
        if (kpId == null || CollectionUtils.isEmpty(modelIds)) {
            throw new IllegalArgumentException("知识点或模板不能为空");
        }
        List<PageModel> pageModels = pageModelService.query().in("id", modelIds).list();
        if (CollectionUtils.isEmpty(pageModels) || pageModels.size() != modelIds.size()) {
            throw new IllegalArgumentException("有无效模板，请先确认模板信息");
        }
        List<ModelKp> mkList = new ArrayList<>();
        ModelKp modelKp = null;
        for (PageModel pm : pageModels) {
            modelKp = new ModelKp();
            modelKp.setKpId(kpId);
            modelKp.setModelId(pm.getId());
            modelKp.setName(pm.getName());
            modelKp.setStatus(ModelKpStatus.USABLE.getValue());
            mkList.add(modelKp);
        }
        return saveBatch(mkList);
    }

    @Override
    public Boolean updateName(Long modelKpId, String name) {
        if (modelKpId == null) {
            throw new IllegalArgumentException("知识点模板id不能为空");
        }
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("名称不能为空");
        }
        ModelKp modelKp = new ModelKp();
        modelKp.setId(modelKpId);
        modelKp.setName(name);
        return updateById(modelKp);
    }

    @Override
    public Boolean updateLinkIco(Long modelKpId, Integer linkType, MultipartFile icoFile) {
        if (modelKpId == null) {
            throw new IllegalArgumentException("知识点模板id不能为空");
        }
        if (linkType == null || icoFile == null) {
            throw new IllegalArgumentException("导航方式或图标不能为空");
        }
        ModelKp oldModelKp = baseMapper.selectById(modelKpId);
        if (oldModelKp == null) {
            throw new IllegalArgumentException("知识点模板信息不存在");
        }
        String fileSeparator = File.separator;
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", ""); //上传文件到ftp上的名称
        String icoFileName = icoFile.getOriginalFilename();
        String fileExtension = icoFileName.substring(icoFileName.lastIndexOf("."));
        String icoPath = fileSeparator + "icoFile" + fileSeparator + DateUtil.format(new Date(), "yyyyMMdd") + fileSeparator + uuid + fileExtension;
        FileUtil.saveFile(icoFile, filePath + icoPath);

        ModelKp modelKp = new ModelKp();
        modelKp.setId(modelKpId);
        modelKp.setLinkType(linkType);
        modelKp.setIcoPath(icoPath);
        Boolean updateFlag = updateById(modelKp);
        log.info("ModelKpServiceImpl#updateLinkIco-->updateFlag={}, modelKp={}, oldModelKp={}", updateFlag, JSON.toJSONString(modelKp), JSON.toJSONString(oldModelKp));
        if (updateFlag && StringUtils.isNotBlank(oldModelKp.getIcoPath())) {
            if (FileUtil.deleteFile(filePath + oldModelKp.getIcoPath())) {
                log.info("ModelKpServiceImpl#updateLinkIco-->oldIcoFilePath={}, 原图标删除成功", filePath + oldModelKp.getIcoPath());
            }
        }
        return updateFlag;
    }

    @Override
    public Boolean deleteById(Long modelKpId) {
        ModelKp modelKp = new ModelKp();
        modelKp.setId(modelKpId);
        modelKp.setStatus(ModelKpStatus.UNUSABLE.getValue());
        return updateById(modelKp);
    }

    @Override
    public IPage<ModelKpVo> getPageByKpId(String kpId, Integer pageNum, Integer pageSize) {
        Page page = new Page(pageNum, pageSize);
        return baseMapper.getVolListByKpId(page, kpId);
    }

    @Override
    public List<ModelKp> getListByKpId(String kpId) {
        return baseMapper.selectList(new QueryWrapper<ModelKp>().lambda().eq(ModelKp::getKpId, kpId).gt(ModelKp::getStatus, ModelKpStatus.UNUSABLE.getValue()).orderByAsc(ModelKp::getSort));
    }

    @Override
    public List<ModelKpVo> getVoListByKpId(String kpId) {
        return baseMapper.getVolListByKpId(kpId);
    }

    @Override
    public ModelKpVo getModelKpVoById(Long modelKpId) {
        return baseMapper.getModelKpVoById(modelKpId);
    }

    @Override
    @Transactional
    public Boolean updateStatus(Long modelKpId, Integer status) {
        ModelKp modelKp = new ModelKp();
        modelKp.setId(modelKpId);
        modelKp.setStatus(status);
        return updateById(modelKp);
    }

    @Override
    @Transactional
    public void updateSort(List<Long> modelKpIds) {
        if (CollectionUtils.isEmpty(modelKpIds)) {
            throw new IllegalArgumentException("知识点模板为空");
        }
        List<ModelKp> modelKps = new ArrayList<>();
        ModelKp modelKp = null;
        for (int i = 0; i < modelKpIds.size(); i++) {
            modelKp = new ModelKp();
            modelKp.setId(modelKpIds.get(i));
            modelKp.setSort(i + 1);
            modelKps.add(modelKp);
        }
        updateBatchById(modelKps);
    }

    @Override
    public List<ModelKp> getModelKpList(List<String> kpIdList) {
        return this.modelKpDao.selectBatchIds(kpIdList);
    }
}
