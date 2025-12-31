package com.cloud.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.backend.service.DictService;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.model.common.Dict;
import com.cloud.model.common.Page;
import com.cloud.model.log.LogAnnotation;
import com.cloud.model.log.constants.LogModule;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class DictController {

    public final static Logger logger = LoggerFactory.getLogger(DictController.class);
    @Autowired
    DictService dictService;

    @Value("${file_server}")
    private String fileServer ;
    /**
     * 管理后台添加字典
     *
     * @param dict
     * @return
     */
    //@LogAnnotation(module = LogModule.ADD_PERMISSION)
    @PreAuthorize("hasAuthority('back:dict:save')")
    @PostMapping("/dict")
    public Dict save(@RequestBody Dict dict) {
        if (StringUtils.isBlank(dict.getDictName())) {
            throw new IllegalArgumentException("字典名称不能为空");
        }
        if (StringUtils.isBlank(dict.getDictValue())) {
            throw new IllegalArgumentException("字典值不能为空");
        }
        if (StringUtils.isBlank(dict.getDictType())) {
            throw new IllegalArgumentException("字典类型不能为空");
        }
        dictService.saveDict(dict);
        return dict;
    }

    /**
     * 管理后台修改字典
     *
     * @param dict
     */
    @LogAnnotation(module = LogModule.UPDATE_PERMISSION)
    @PreAuthorize("hasAuthority('back:dict:update')")
    @PutMapping("/dict")
    public Dict update(@RequestBody Dict dict) {
        if (StringUtils.isBlank(dict.getDictName())) {
            throw new IllegalArgumentException("字典名称不能为空");
        }
        if (StringUtils.isBlank(dict.getDictValue())) {
            throw new IllegalArgumentException("字典值不能为空");
        }
        if (StringUtils.isBlank(dict.getDictType())) {
            throw new IllegalArgumentException("字典类型不能为空");
        }

        dictService.update(dict);

        return dict;
    }

    /**
     * 删除权限标识
     *
     * @param id
     */
    //@LogAnnotation(module = LogModule.DELETE_PERMISSION)
    @PreAuthorize("hasAuthority('back:dict:delete')")
    @DeleteMapping("/dict/{id}")
    public void delete(@PathVariable Long id) {
        dictService.delete(id);
    }

    /**
     * 查询字典信息
     */
    @PreAuthorize("hasAuthority('back:dict:query')")
    @GetMapping("/dict")
    public Page<Dict> findPermissions(@RequestParam Map<String, Object> params) {
        return dictService.findPermissions(params);
    }

    /**
     * 查询字典信息(不分页)
     */
    /*@PreAuthorize("hasAuthority('back:dict:query')")*/
    @GetMapping("/findDict")
    public List<Dict> findDict(@RequestParam Map<String, Object> params) {
        return dictService.findDict(params);
    }

    /**
     * 获取系统主题配色
     */
    @GetMapping("getThemeType")
    public String getThemeType(){
        try {
           return dictService.getThemeType() ;
        }catch (Exception e){
            return "default" ;
        }
    }
    /**
     * 设置系统主题配色
     */
    @GetMapping("setThemeType")
    public ApiResult setThemeType(Long id){
        try {
            dictService.setThemeType(id) ;
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        }catch (Exception e){
            logger.error("设置系统主题颜色异常，id="+id+e.getMessage());
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }

    }
    /**
     * 获取系统主题配色
     */
    @GetMapping("getThemeTypeList")
    public ApiResult getThemeTypeList(@RequestParam Map<String, Object> params){
        try {
            params.put("dictType","theme_type");
            List<Dict> list = dictService.getThemeTypeList(params) ;
            return ApiResultHandler.buildApiResult(200, "操作成功", list);
        }catch (Exception e){
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }

    }

    /**
     * 获取系统logo配置
     */
    @GetMapping("getLogoTypeList")
    public Map<String,String> getLogoTypeList(){
        HashMap map = new HashMap() ;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("dictType","logo_type");
            List<Dict> list = dictService.getThemeTypeList(params) ;
            if(CollectionUtils.isNotEmpty(list)){
                list.stream().forEach(e->{
                    map.put(e.getDictName(),e.getDictValue());
                });
            }
            map.put("fileServer",fileServer) ;
        }catch (Exception e){
            logger.error("获取系统logo配置错误,异常信息：",e);
        }
        return map;
    }

    /**
     * 修改系统logo配置
     */
    @PostMapping("updateSystemLogo")
    public ApiResult updateSystemLogo(@RequestBody Map<String,String> map){
        try {
            Set<Map.Entry<String, String>> entries = map.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("dictname", entry.getKey());
                Dict dict = dictService.getLogoDict("logo_type", entry.getKey());
                dict.setDictValue(entry.getValue());
                dictService.update(dict);
            }
            return ApiResultHandler.buildApiResult(200, "系统logo配置修改成功", null);
        } catch (Exception e) {
            logger.error("修改系统logo配置错误,异常信息：", e);
            return ApiResultHandler.buildApiResult(500, "系统logo配置修改失败", null);
        }
    }

    @GetMapping("getNameValueMap")
    public Map<String, String> getNameValueMap(@RequestParam String type) {
        List<Dict> list = dictService.lambdaQuery().eq(Dict::getDictType, type).list();
        Map<String, String> map = list.stream().collect(Collectors.toMap(Dict::getDictName, Dict::getDictValue));
        return map;
    }
}
