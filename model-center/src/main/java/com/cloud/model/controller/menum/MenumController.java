package com.cloud.model.controller.menum;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.model.user.LoginAppUser;
import com.cloud.model.user.SysRole;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.utils.CollectionsCustomer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author:胡立涛
 * @description: TODO 产品菜单结构
 * @date: 2022/3/15
 * @param:
 * @return:
 */

@RestController
@RequestMapping("/pcMenum")
@Slf4j
@RefreshScope
public class MenumController {

    @Autowired
    ManageBackendFeign manageBackendFeign;
    // 文件服务地址
    @Value(value = "${file_server}")
    private String fileServer;


    /**
     * @author:胡立涛
     * @description: TODO 菜单列表
     * @date: 2022/3/15
     * @param: []
     * @return: com.cloud.core.ApiResult
     */
    @GetMapping(value = "getMenumList")
    public ApiResult getMenumList() {
        try {
            String roleIds = "";
            LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
            Set<SysRole> sysRoles = loginAppUser.getSysRoles();
            if (sysRoles == null || sysRoles.size() == 0) {
                return ApiResultHandler.buildApiResult(100, "当前登录用户没有分配权限", null);
            }
            for (SysRole sysRole : sysRoles) {
                roleIds += sysRole.getId() + ",";
            }
            roleIds = roleIds.substring(0, roleIds.length() - 1);
            // 查询当前用户拥有的菜单权限
            List<Map> menuByRoleIds = manageBackendFeign.getMenuByRoleIds(roleIds);
            JSONArray result = listToTree(JSONArray.parseArray(JSON.toJSONString(menuByRoleIds)), "id", "parentid", "children");
            Map rMap=new HashMap();
            rMap.put("result",result);
            rMap.put("fileServer",fileServer);
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
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
}
