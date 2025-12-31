package com.cloud.backend.controller;

import com.cloud.backend.dao.MenuDao;
import com.cloud.backend.dao.RoleMenuDao;
import com.cloud.backend.model.Menu;
import com.cloud.backend.service.MenuService;
import com.cloud.model.log.LogAnnotation;
import com.cloud.model.log.constants.LogModule;
import com.cloud.model.user.LoginAppUser;
import com.cloud.model.user.SysRole;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.utils.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;
    @Autowired
    private MenuDao menuDao;
    @Autowired
    RoleMenuDao roleMenuDao;

    /**
     * 当前登录用户的菜单
     *
     * @return
     */
    @GetMapping("/me")
    public List<Menu> findMyMenu() {
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        Set<SysRole> roles = loginAppUser.getSysRoles();
        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptyList();
        }
        Set<Menu> menus = new HashSet<>(menuService
                .findByRoles(roles.parallelStream().map(SysRole::getId).collect(Collectors.toSet())));
        List<Menu> arrayList = new ArrayList<>(menus);
        arrayList.sort(Comparator.comparingInt(Menu::getSort));
        List<Menu> firstLevelMenus = arrayList.stream().filter(m -> m.getParentId().equals(0L))
                .collect(Collectors.toList());
        firstLevelMenus.forEach(m -> {
            setChild(m, arrayList);
        });

        return firstLevelMenus;
    }

    private void setChild(Menu menu, List<Menu> menus) {
        List<Menu> child = menus.stream().filter(m -> m.getParentId().equals(menu.getId()))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(child)) {
            menu.setChild(child);
            // 2018.06.09递归设置子元素，多级菜单支持
            child.parallelStream().forEach(c -> {
                setChild(c, menus);
            });
        }
    }

    /**
     * 给角色分配菜单
     *
     * @param roleId  角色id
     * @param menuIds 菜单ids
     */
    @LogAnnotation(module = LogModule.SET_MENU_ROLE)
    @PreAuthorize("hasAuthority('back:menu:set2role')")
    @PostMapping("/toRole")
    public void setMenuToRole(Long roleId, @RequestBody Set<Long> menuIds) {
        menuService.setMenuToRole(roleId, menuIds);
    }

    /**
     * 菜单树ztree
     */
    @PreAuthorize("hasAnyAuthority('back:menu:set2role','back:menu:query')")
    @GetMapping("/tree")
    public List<Menu> findMenuTree() {
        List<Menu> all = menuService.findAll();
        List<Menu> list = new ArrayList<>();
        setMenuTree(0L, all, list);
        return list;
    }

    /**
     * 菜单树
     *
     * @param parentId
     * @param all
     * @param list
     */
    private void setMenuTree(Long parentId, List<Menu> all, List<Menu> list) {
        all.forEach(menu -> {
            if (parentId.equals(menu.getParentId())) {
                list.add(menu);

                List<Menu> child = new ArrayList<>();
                menu.setChild(child);
                setMenuTree(menu.getId(), all, child);
            }
        });
    }

    /**
     * 获取角色的菜单
     *
     * @param roleId
     */
    @PreAuthorize("hasAnyAuthority('back:menu:set2role','menu:byroleid')")
    @GetMapping(params = "roleId")
    public Set<Long> findMenuIdsByRoleId(Long roleId) {
        return menuService.findMenuIdsByRoleId(roleId);
    }


    /**
     * 添加菜单
     *
     * @param menu
     */
    @LogAnnotation(module = LogModule.ADD_MENU)
    @PreAuthorize("hasAuthority('back:menu:save')")
    @PostMapping
    public Menu save(@RequestBody Menu menu) {
        if (Validator.isEmpty(menu.getParentId())) {
            menu.setParentId(0L);
        }
        // 根据父节点id和菜单名称查询菜单信息
        Map<String, Object> map = new HashMap<>();
        map.put("parentId", menu.getParentId());
        map.put("name", menu.getName());
        List<Map<String, Object>> menuByParam = menuDao.getMenuByParam(map);
        if (menuByParam != null && menuByParam.size() > 0) {
            return null;
        }
        menuService.save(menu);

        return menu;
    }

    /**
     * 修改菜单
     *
     * @param menu
     */
    @LogAnnotation(module = LogModule.UPDATE_MENU)
    @PreAuthorize("hasAuthority('back:menu:update')")
    @PutMapping
    public Menu update(@RequestBody Menu menu) {
        if (Validator.isEmpty(menu.getParentId())) {
            menu.setParentId(0L);
        }
        menuService.update(menu);
        return menu;
    }

    /**
     * 删除菜单
     *
     * @param id
     */
    @LogAnnotation(module = LogModule.DELETE_MENU)
    @PreAuthorize("hasAuthority('back:menu:delete')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        menuService.delete(id);
    }

    /**
     * 查询所有菜单
     */
    @PreAuthorize("hasAuthority('back:menu:query')")
    @GetMapping("/all")
    public List<Menu> findAll() {
        List<Menu> all = menuService.findAll();
        List<Menu> list = new ArrayList<>();
        setSortTable(0L, all, list);

        return list;
    }

    /**
     * 菜单table
     *
     * @param parentId
     * @param all
     * @param list
     */
    private void setSortTable(Long parentId, List<Menu> all, List<Menu> list) {
        all.forEach(a -> {
            if (a.getParentId().equals(parentId)) {
                list.add(a);
                setSortTable(a.getId(), all, list);
            }
        });
    }

    @PreAuthorize("hasAuthority('back:menu:query')")
    @GetMapping("/{id}")
    public Menu findById(@PathVariable Long id) {
        return menuService.findById(id);
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据角色ids查询菜单列表
     * @date: 2022/3/15
     * @param: [roleIds]
     * @return: java.util.List<java.util.Map>
     */
    @GetMapping(value = "/getMenuByRoleIds/{roleIds}")
    public List<Map> getMenuByRoleIds(@PathVariable("roleIds") String roleIds) {
        String roleIdArr[] = roleIds.split(",");
        List<Long> roleIdList = new ArrayList<>();
        for (int i = 0; i < roleIdArr.length; i++) {
            roleIdList.add(Long.valueOf(roleIdArr[i]));
        }
        return roleMenuDao.getMenuByRoleIds(roleIdList);
    }

}
