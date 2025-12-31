package com.cloud.backend.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.backend.dao.PcMenuDao;
import com.cloud.backend.model.PcMenu;
import com.cloud.backend.service.DictService;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.model.common.Dict;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.cloud.utils.CollectionsCustomer;

@RestController
@RequestMapping("/pcMenu")
@Slf4j
@RefreshScope
public class PcMenuController {

    @Autowired
    PcMenuDao pcMenuDao;
    // 文件服务地址
    @Value(value = "${file_server}")
    private String fileServer;

    /**
     * @author:胡立涛
     * @description: TODO 添加产品端菜单
     * @date: 2022/3/16
     * @param: [pcMenu]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "saveInfo")
    public ApiResult saveInfo(@RequestBody PcMenu pcMenu) {
        try {
            if (pcMenu.getParentId() == null) {
                pcMenu.setParentId(0L);
            }
            pcMenu.setCreateTime(new Timestamp(System.currentTimeMillis()));
            pcMenuDao.saveInfo(pcMenu);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 修改产品菜单
     * @date: 2022/3/16
     * @param: [pcMenu]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "updateInfo")
    public ApiResult updateInfo(@RequestBody PcMenu pcMenu) {
        try {
            if (pcMenu.getId() == null) {
                return ApiResultHandler.buildApiResult(100, "参数id为空", null);
            }
            if (pcMenu.getParentId() == null) {
                pcMenu.setParentId(0L);
            }
            pcMenu.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            pcMenuDao.updateInfo(pcMenu);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据ids删除产品菜单
     * @date: 2022/3/16
     * @param: [pcMenu]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "delInfo")
    public ApiResult delInfo(@RequestBody PcMenu pcMenu) {
        try {
            if (pcMenu.getId() == null) {
                return ApiResultHandler.buildApiResult(100, "参数id为空", null);
            }
            List<Map> menuList = pcMenuDao.getMenuList(pcMenu);
//            menuList = CollectionsCustomer.builder().build().listMapToLowerCase(menuList);
            if (!menuList.isEmpty() && menuList.size()>0){
                Long[] ids=new Long[menuList.size()];
                 for (int i=0;i<menuList.size();i++){
                     ids[i]=Long.valueOf(menuList.get(i).get("id").toString());
                 }
                Map map=new HashMap();
                map.put("ids",ids);
                pcMenuDao.delMenus(map);
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 产品菜单树形结构
     * @date: 2022/3/16
     * @param: []
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getInfoList")
    public ApiResult getInfoList() {
        try {
            List<Map> infoList = pcMenuDao.getInfoList();
//            infoList = CollectionsCustomer.builder().build().listMapToLowerCase(infoList);
            JSONArray result = listToTree(JSONArray.parseArray(JSON.toJSONString(infoList)), "id", "parentid", "children");
            return ApiResultHandler.buildApiResult(200, "操作成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 将list集合转换为树形结构
     * @date: 2022/3/14
     * @param: [arr, id, parentid, child]
     * @return: com.alibaba.fastjson.JSONArray
     */
    public static JSONArray listToTree(JSONArray arr, String id, String parentid, String child) {
        JSONArray r = new JSONArray();
        JSONObject hash = new JSONObject();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject json = (JSONObject) arr.get(i);
            hash.put(json.getString(id), json);
        }
        for (int j = 0; j < arr.size(); j++) {
            JSONObject aVal = (JSONObject) arr.get(j);
            JSONObject hashVp = (JSONObject) hash.get(aVal.get(parentid).toString());
            if (hashVp != null) {
                if (hashVp.get(child) != null) {
                    JSONArray ch = (JSONArray) hashVp.get(child);
                    ch.add(aVal);
                    hashVp.put(child, ch);
                } else {
                    JSONArray ch = new JSONArray();
                    ch.add(aVal);
                    hashVp.put(child, ch);
                }
            } else {
                r.add(aVal);
            }
        }
        return r;
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据id查询产品菜单详细信息
     * @date: 2022/3/16
     * @param: [pcMenu]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "getInfoDetail")
    public ApiResult getInfoDetail(@RequestBody PcMenu pcMenu) {
        try {
            if (pcMenu.getId() == null) {
                return ApiResultHandler.buildApiResult(100, "参数id为空", null);
            }
            PcMenu infoDetail = pcMenuDao.getInfoDetail(pcMenu);
            infoDetail.setFileServer(fileServer);
            return ApiResultHandler.buildApiResult(200, "操作成功", infoDetail);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", null);
        }
    }


}
