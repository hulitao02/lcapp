package com.cloud.model.controller;

import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.model.model.ModelGroup;
import com.cloud.model.service.ModelGroupService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Api("模板分组")
@RequestMapping("/modelGroup")
public class ModelGroupController {

    @Autowired
    private ModelGroupService modelGroupService;

    /**
     * 添加模板分组
     * @param name
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ApiResult add(String name){

        if (modelGroupService.add(name)){
            return ApiResultHandler.buildApiResult(200, "创建成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "创建失败", null);


    }




    /**
     * 修改模板分组
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody ModelGroup modelGroup){
        if (modelGroupService.updateGroup(modelGroup)){
            return ApiResultHandler.buildApiResult(200, "修改成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "修改失败", null);
      }

    /**
     * 删除模板分组
     */
    @PostMapping("/delete/{id}")
    public ApiResult deleteById(@PathVariable(name = "id") Long id) {
        try {
            modelGroupService.deleteGroup(id);
            return ApiResultHandler.buildApiResult(200, "删除成功", null);
        }catch (Exception e){
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(400, "删除失败", null);
        }
    }


    /**
     * 模板列表
     */
    @GetMapping("/queryAll")
    public ApiResult<List<ModelGroup>> queryAll() {
        List<ModelGroup> modelGroups = modelGroupService.queryAll();
        return ApiResultHandler.success(modelGroups);
    }

    /**
     * 模板信息
     */
    @GetMapping("/getInfo/{id}")
    public ApiResult<ModelGroup> getInfoById(@PathVariable(name = "id") Long groupId){
        ModelGroup modelGroup = modelGroupService.queryById(groupId);
        return ApiResultHandler.success(modelGroup);
    }
}
