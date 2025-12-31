package com.cloud.model.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.feign.knowledge.KnowledgeFeign;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.model.bean.vo.KnowledgePointVo;
import com.cloud.model.bean.vo.ModelKpVo;
import com.cloud.model.model.ModelKp;
import com.cloud.model.service.ModelKpService;
import com.cloud.utils.CollectionsCustomer;
import com.cloud.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/modelKp")
@ApiModel(value = "模板与知识关系管理")
@Slf4j
public class ModelKpController {

    @Autowired
    private ModelKpService modelKpService;

    @Value("${file.local.urlPrefix}")
    private String fileUrlPrefix;

    @Autowired
    ManageBackendFeign manageBackendFeign;


    @ApiOperation("知识点添加模板")
    @PostMapping("/add")
    public ApiResult add(String kpId, Long modelId) {
        log.info("ModelKpController#add-->kpId={}, modelId={}", kpId, modelId);
        if (modelKpService.add(kpId, modelId)) {
            return ApiResultHandler.buildApiResult(200, "添加成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "添加失败", null);
    }

    @ApiOperation("知识点批量添加模板")
    @PostMapping("/addList/{kpId}")
    public ApiResult addList(@PathVariable(name = "kpId") String kpId,
                             @RequestBody List<Long> modelIds) {
        log.info("ModelKpController#addList-->kpId={}, modelIds={}", kpId, modelIds);
        if (modelKpService.addList(kpId, modelIds)) {
            return ApiResultHandler.buildApiResult(200, "添加成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "添加失败", null);
    }

    @ApiOperation("修改知识点模板名称")
    @PostMapping("/updateName")
    public ApiResult updateName(Long modelKpId, String name) {
        log.info("ModelKpController#updateName-->modelKpId={}, name={}", modelKpId, name);
        if (modelKpService.updateName(modelKpId, name)) {
            return ApiResultHandler.buildApiResult(200, "修改成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "修改失败", null);
    }

    @ApiOperation("修改知识点模板图标和导航方式")
    @PostMapping("/updateLinkIco")
    public ApiResult updateLinkIco(@RequestBody ModelKp modelKp) {
        try {
            ModelKp bean = modelKpService.getById(modelKp.getId());
            bean.setName(modelKp.getName());
            if (modelKp.getIcoPath() != null) {
                bean.setIcoPath(modelKp.getIcoPath().replace(fileUrlPrefix, ""));
            }
            modelKpService.updateById(bean);
            return ApiResultHandler.buildApiResult(200, "修改成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(400, "修改失败", null);
        }
    }

    @ApiOperation("根据知识点模板id查询信息")
    @GetMapping("/getById/{modelKpId}")
    public ApiResult<ModelKp> getById(@PathVariable(name = "modelKpId") Long modelKpId) {
        ModelKp modelKp = modelKpService.queryById(modelKpId);
        if (StringUtils.isNotBlank(modelKp.getIcoPath())) {
            modelKp.setIcoPath(fileUrlPrefix + modelKp.getIcoPath());
        }
        return ApiResultHandler.success(modelKp);
    }

    @ApiOperation("知识点删除模板")
    @PostMapping("/delete/{modelKpId}")
    public ApiResult deleteById(@PathVariable(name = "modelKpId") Long modelKpId) {
        if (modelKpService.deleteById(modelKpId)) {
            return ApiResultHandler.buildApiResult(200, "删除成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "删除失败", null);
    }

    @ApiOperation("获取知识点模板集合(分页)")
    @GetMapping("/getPageByKpId")
    public IPage<ModelKpVo> getPageByKpId(@RequestParam("kpId") String kpId,
                                          @RequestParam("pageNum") Integer pageNum,
                                          @RequestParam("pageSize") Integer pageSize) {
        IPage<ModelKpVo> modelKpPage = modelKpService.getPageByKpId(kpId, pageNum, pageSize);
        buildFileUrl(modelKpPage.getRecords());
        return modelKpPage;
    }

    private void buildFileUrl(List<ModelKpVo> modelKpVos) {
        if (!CollectionUtils.isEmpty(modelKpVos)) {
            modelKpVos.stream().forEach(m -> {
                if (StringUtils.isNotBlank(m.getIcoPath())) {
                    m.setIcoPath(fileUrlPrefix + m.getIcoPath().replace("\\", "/"));
                }
                if (StringUtils.isNotBlank(m.getPicPath())) {
                    m.setPicPath(fileUrlPrefix + m.getPicPath().replace("\\", "/"));
                }
            });
        }
    }

    @ApiOperation("获取知识点模板集合")
    @GetMapping("/getListByKpId")
    public ApiResult<List<ModelKpVo>> getListByKpId(@RequestParam("kpId") String kpId) {
        List<ModelKpVo> modelKpList = modelKpService.getVoListByKpId(kpId);
        buildFileUrl(modelKpList);
        return ApiResultHandler.success(modelKpList);
    }

    @Autowired
    KnowledgeFeign knowledgeFeign;

    @ApiOperation("根据父级id获取知识点列表")
    @GetMapping("/getKpList/{parentId}")
    public ApiResult getKpVoListByParentId(@PathVariable(name = "parentId") Long parentId,
                                           @RequestParam(defaultValue = "false") Boolean isAll) {
        try {
            // 查询根节点
            Map rootPoint = knowledgeFeign.getRootPoint();
            if (rootPoint.isEmpty()){
                return ApiResultHandler.buildApiResult(101,"根节点为空，请设置根节点",null);
            }
            List<KnowledgePointVo> voList = modelKpService.getKpVoListByParentId(rootPoint.get("id").toString(), isAll);
            return ApiResultHandler.success(voList);
        }catch (Exception e){
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500,"操作异常",e.toString());
        }

    }

    @ApiOperation("根据知识点模板id获取模板信息")
    @GetMapping("/getInfoById/{modelKpId}")
    public ApiResult<ModelKpVo> getInfoById(@PathVariable(name = "modelKpId") Long modelKpId) {
        ModelKpVo vo = modelKpService.getModelKpVoById(modelKpId);
        return ApiResultHandler.success(vo);
    }

    @ApiOperation("修改知识点模板id排序")
    @PostMapping("/updateSort")
    public ApiResult updateSort(@RequestBody List<Long> modelKpIds) {
        modelKpService.updateSort(modelKpIds);
        return ApiResultHandler.success();
    }

    @ApiOperation("修改知识点模板id数据对接状态")
    @PostMapping("/updateStatus/{modelKpId}")
    public ApiResult updateStatus(@PathVariable(name = "modelKpId") Long modelKpId, Integer status) {
        if (modelKpService.updateStatus(modelKpId, status)) {
            return ApiResultHandler.buildApiResult(200, "修改成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "修改失败", null);
    }
}
