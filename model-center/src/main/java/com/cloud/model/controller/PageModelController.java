package com.cloud.model.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.model.bean.vo.PageModelVo;
import com.cloud.model.dao.PageModelDao;
import com.cloud.model.model.PageModel;
import com.cloud.model.service.PageModelService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pageModel")
@ApiModel(value = "知识模板管理")
@Slf4j
public class PageModelController {

    @Autowired
    private PageModelService pageModelService;
    @Autowired
    PageModelDao pageModelDao;
    @Value("${file.local.urlPrefix}")
    private String urlPrefix;


    /**
     * 添加模板
     */
    @ApiOperation("添加模板")
    @PostMapping("/add")
    public ApiResult add(PageModel pageModel) {
        try {
            log.info("PageModelController#add-->pageModel={}", JSON.toJSONString(pageModel));

            // 查看该分组下是否有该类型的模版
            QueryWrapper<PageModel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("service_path", pageModel.getServicePath());
            queryWrapper.eq("group_id", pageModel.getGroupId());
            List<PageModel> pageModels = pageModelDao.selectList(queryWrapper);
            if (pageModels != null && pageModels.size() > 0) {
                return ApiResultHandler.buildApiResult(200, "当前分组下已存在该类型模版", null);
            }
            if (StringUtils.isBlank(pageModel.getName())) {
                throw new IllegalArgumentException("模板名称不能为空");
            }
            if (pageModel.getGroupId() == null) {
                throw new IllegalArgumentException("模板分组不能为空");
            }
            if (StringUtils.isBlank(pageModel.getServicePath())) {
                throw new IllegalArgumentException("模板类型不能为空");
            }
            pageModelService.save(pageModel);
            return ApiResultHandler.buildApiResult(200, "添加成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(400, "添加失败", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 查看该分组下是否有模版
     * @date: 2021/11/29
     * @param: [id]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/findModel/{id}")
    public ApiResult findModel(@PathVariable(name = "id") Long id) {
        try {
            QueryWrapper<PageModel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("group_id", id);
            List<PageModel> pageModels = pageModelDao.selectList(queryWrapper);
            if (pageModels != null && pageModels.size() > 0) {
                return ApiResultHandler.buildApiResult(200, "该分组下有模板", 1);
            }
            return ApiResultHandler.buildApiResult(200, "该分组下没有模板", 0);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(400, "删除失败", null);
        }
    }


    /**
     * 修改模板
     */
    @ApiOperation("修改模板")
    @PostMapping("/update")
    public ApiResult update(PageModel pageModel) {
        try {
            if (StringUtils.isBlank(pageModel.getName())) {
                throw new IllegalArgumentException("模板名称不能为空");
            }
            if (pageModel.getGroupId() == null) {
                throw new IllegalArgumentException("模板分组不能为空");
            }
            PageModel bean = pageModelService.getById(pageModel.getId());
            bean.setName(pageModel.getName());
            bean.setGroupId(pageModel.getGroupId());
            if (!StringUtils.isEmpty(pageModel.getPicPath())) {
                bean.setPicPath(pageModel.getPicPath().replace(urlPrefix,""));
            }
            pageModelService.updateById(bean);
            return ApiResultHandler.buildApiResult(200, "修改成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(400, "修改失败", null);
        }
    }

    /**
     * 根据模板分组id分页查询分组列表
     */
    @ApiOperation("根据模板分组id分页查询分组列表")
    @GetMapping("/listByPage")
    public IPage<PageModelVo> listByPage(@RequestParam("groupId") Long groupId,
                                         @RequestParam("pageNum") Integer pageNum,
                                         @RequestParam("pageSize") Integer pageSize) {
        if (pageNum == null || pageSize == null) {
            pageNum = 1;
            pageSize = 8;
        }
        return pageModelService.listByPage(groupId, pageNum, pageSize);
    }

    /**
     * 删除模板
     */
    @ApiOperation("删除模板")
    @PostMapping("/delete/{pageModelId}")
    public ApiResult deleteById(@PathVariable(name = "pageModelId") Long id) {
        if (pageModelService.deleteById(id)) {
            return ApiResultHandler.buildApiResult(200, "删除成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "删除失败", null);
    }
}
