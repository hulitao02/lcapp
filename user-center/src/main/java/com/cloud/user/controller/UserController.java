package com.cloud.user.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ExamConstants;
import com.cloud.feign.exam.ExamClient;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.model.common.Dict;
import com.cloud.model.common.Page;
import com.cloud.model.log.LogAnnotation;
import com.cloud.model.log.constants.LogModule;
import com.cloud.model.user.*;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.user.dao.AppUserDao;
import com.cloud.user.dao.LcTaskStateDao;
import com.cloud.user.dao.TaskUserDao;
import com.cloud.user.dao.UserRoleDao;
import com.cloud.user.model.LcTaskUser;
import com.cloud.user.service.AppUserService;
import com.cloud.user.service.SysDepartmentService;
import com.cloud.user.service.SysRoleService;
import com.cloud.user.service.UserPositionService;
import com.cloud.user.utils.FileUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.util.*;

@Slf4j
@RestController
public class UserController {

    @Autowired
    private AppUserService appUserService;
    @Resource
    private UserRoleDao userRoleDao;
    @Autowired
    private SysDepartmentService sysDepartmentService;
    @Autowired
    private UserPositionService userPositionService;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    AppUserDao appUserDao;
    @Resource
    private ManageBackendFeign manageBackendFeign;
    // 学员导入模板地址
    @Value(value = "${user-template}")
    private String userTemplate;
    @Autowired
    private ExamClient examClient;
    @Value(value = "${dj_localUrlPrefix}")
    private String djLocalUrlPrefix;

    @PostMapping(value = "/users/exportUsers")
    public ApiResult exportUsers() {
        try {
            QueryWrapper<AppUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 1);
            List<AppUser> appUsers = appUserDao.selectList(queryWrapper);
            List<Map> list = new ArrayList<>();
            for (AppUser appUser : appUsers) {
                Map map = new HashMap();
                map.put("id", appUser.getId());
                map.put("username", appUser.getUsername());
                map.put("password", appUser.getPassword());
                map.put("nickname", appUser.getNickname());
                list.add(map);
            }
            // 将数据写到指定的json文件中
            String basePath = "/dataManage/checkout/movetoFile/";
            // 文件夹名称
            String filePoder = UUID.randomUUID().toString().replaceAll("-", "");
            String filePath = basePath + filePoder + "_user.json";
            File jsonFile = new File(filePath);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(jsonFile, list);
            return ApiResultHandler.buildApiResult(200, "操作成功", djLocalUrlPrefix + filePoder + "_user.json");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * 当前登录用户 LoginAppUser
     */
    @GetMapping("/users/current")
    public LoginAppUser getLoginAppUser() {
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        SysDepartment sysDepartment = sysDepartmentService.findById(loginAppUser.getDepartmentId());
        if (sysDepartment != null) {
            loginAppUser.setDepartmentName(sysDepartment.getDname());
        }
        return loginAppUser;
    }

    @GetMapping(value = "/users-anon/internal", params = "username")
    public LoginAppUser findByUsername(String username) {

        LoginAppUser byUsername = appUserService.findByUsername(username);


        return byUsername;
    }

    /**
     * 用户查询
     *
     * @param params
     */
    //@PreAuthorize("hasAuthority('back:user:query')")
    @GetMapping("/users")
    public Page<AppUser> findUsers(@RequestParam Map<String, Object> params) {
        return appUserService.findUsers(params);
    }


    //@PreAuthorize("hasAuthority('back:user:query')")

    /**
     * @param id 用户ID
     * @return 查询用户的信息
     */
    @GetMapping("/users/{id}")
    public AppUser findUserById(@PathVariable Long id) {
        AppUser appUser = appUserService.findById(id);
        if (appUser != null) {
            if (ObjectUtil.isNotNull(appUser.getLevel())) {
                //根据字典来设置内容
                Map<String, Object> mapParam = new HashMap<>();
                mapParam.put("dictType", "user_level");
                mapParam.put("dictValue", String.valueOf(appUser.getLevel()));
                List<Dict> dictList = manageBackendFeign.findDict(mapParam);
                if (dictList.size() > 0) {
                    appUser.setLevelName(dictList.get(0).getDictName());
                } else {
                    appUser.setLevelName("");
                }
            } else {
                appUser.setLevelName("");
            }
        }
        return appUser;
    }

    /**
     * 添加用户,根据用户名注册
     *
     * @param appUser
     */
    @PostMapping("/users-anon/register")
    public AppUser register(@RequestBody AppUser appUser) {
        // 用户名等信息的判断逻辑挪到service了
        appUserService.addAppUser(appUser);

        return appUser;
    }

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * @author:胡立涛
     * @description: TODO 测试登录密码是否与数据库中存在的密码相同
     * @date: 2025/2/18
     * @param: [appUser]
     * @return: boolean
     */
    @PostMapping("/users-anon/jiemi")
    public boolean jiemi(@RequestBody AppUser appUser) {
        try {
            return passwordEncoder.matches(appUser.getPassword(), "$2a$10$w8mVWPHIRFJ0Gjp4z1.vsuE006MbV7Js94xq./9otAvgv/eoPX7Fe");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 修改自己的个人信息
     *
     * @param appUser
     */
    //@OperationLogAnnotation
    @LogAnnotation(module = LogModule.UPDATE_ME)
    @PutMapping("/users/me")
    public AppUser updateMe(@RequestBody AppUser appUser) {
        AppUser user = AppUserUtil.getLoginAppUser();
        appUser.setId(user.getId());
        appUserService.updateAppUser(appUser);
        return appUser;
    }

    /**
     * 修改密码
     */
    @LogAnnotation(module = LogModule.UPDATE_PASSWORD)
    @PutMapping(value = "/users/password")
    public void updatePassword(@RequestBody Map<String, String> map) {
        String oldPassword = map.get("oldPassword");
        String newPassword = map.get("newPassword");
        if (StringUtils.isBlank(oldPassword)) {
            throw new IllegalArgumentException("旧密码不能为空");
        }
        if (StringUtils.isBlank(newPassword)) {
            throw new IllegalArgumentException("新密码不能为空");
        }

        AppUser user = AppUserUtil.getLoginAppUser();
        appUserService.updatePassword(user.getId(), oldPassword, newPassword);
    }

    /**
     * 管理后台，给用户重置密码
     *
     * @param id          用户id
     * @param newPassword 新密码
     */
    @LogAnnotation(module = LogModule.RESET_PASSWORD)
    //@PreAuthorize("hasAuthority('back:user:password')")
    @PutMapping(value = "/users/{id}/password", params = {"newPassword"})
    public void resetPassword(@PathVariable Long id, String newPassword) {
        appUserService.updatePassword(id, null, newPassword);
    }

    /**
     * 管理后台修改用户
     *
     * @param appUser
     */
    @LogAnnotation(module = LogModule.UPDATE_USER)
    //@PreAuthorize("hasAuthority('back:user:update')")
    @PutMapping("/users")
    public void updateAppUser(@RequestBody AppUser appUser) {
        appUserService.updateAppUser(appUser);
    }

    /**
     * 管理后台给用户分配角色
     *
     * @param id      用户id
     * @param roleIds 角色ids
     */
    @LogAnnotation(module = LogModule.SET_ROLE)
    //@PreAuthorize("hasAuthority('back:user:role:set')")
    @PostMapping("/users/{id}/roles")
    public void setRoleToUser(@PathVariable Long id, @RequestBody Set<Long> roleIds) {
        appUserService.setRoleToUser(id, roleIds);
    }

    @GetMapping({"/getSysPermissionByRoleId"})
    public Set<SysPermission> getSysPermissionByRoleId(Long roleId) {
        return appUserService.findPermissionsByRoleId(roleId);
    }

    @GetMapping({"/getPermissonByUserId"})
    public Set<SysPermission> getPermissonByUserId(Long userid) {
        Set<SysPermission> ss = new HashSet();
        Set<SysRole> sysRoles = appUserService.findRolesByUserId(userid);
        Iterator var4 = sysRoles.iterator();

        while (var4.hasNext()) {
            SysRole sr = (SysRole) var4.next();
            Set<SysPermission> sps = appUserService.findPermissionsByRoleId(sr.getId());
            sps.stream().forEach((s) -> {
                ss.add(s);
            });
        }
        return ss;
    }

    @GetMapping({"/findAppUserById"})
    public AppUser findAppUserById(Long userid) {
        //AppUserVO vo = new AppUserVO();
        AppUser vo = appUserService.getById(userid);
        SysDepartment depart = sysDepartmentService.findById(vo.getDepartmentId());
        UserPosition byId1 = userPositionService.findById(vo.getPositionId());
        vo.setDepartmentName(null == depart ? "" : depart.getDname());
        vo.setPositionName(null == byId1 ? "" : byId1.getName());
        return vo;
    }

    /**
     * @author:胡立涛
     * @description: TODO 获取用户的知识点数据权限
     * @date: 2022/9/13
     * @param: [userid]
     * @return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */
    @GetMapping({"/getUserKpIds"})
    public List<Map<String, Object>> getUserKpIds(Long[] userIds) {
        List<Map<String, Object>> userKpIds = userRoleDao.getUserKpIds(userIds);
        return userKpIds;
    }

    @GetMapping({"/getAllUsers"})
    public List<AppUser> getAllUsers() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("status", 1);
        List<AppUser> list1 = appUserService.list(queryWrapper);
        Iterator var3 = list1.iterator();
        while (var3.hasNext()) {
            AppUser ap = (AppUser) var3.next();
            SysDepartment depart = sysDepartmentService.findById(ap.getDepartmentId());
            UserPosition byId1 = userPositionService.findById(ap.getPositionId());
            ap.setDepartmentName(null == depart ? "" : depart.getDname());
            ap.setPositionName(null == byId1 ? "" : byId1.getName());
        }

        return list1;
    }

    @GetMapping({"/getUserByIdCard"})
    public AppUser getUserByIdCard(String idCard) {
        QueryWrapper<AppUser> qw = new QueryWrapper();
        qw.eq("rank_num", idCard);
        AppUser one = appUserService.getOne(qw);
        return one;
    }

    /**
     * 获取用户的角色
     *
     * @param id 用户id
     */
    // @PreAuthorize("hasAnyAuthority('back:user:role:set','user:role:byuid')")
    @GetMapping("/users/{id}/roles")
    public Set<SysRole> findRolesByUserId(@PathVariable Long id) {
        return appUserService.findRolesByUserId(id);
    }

    @PostMapping("/users/RunManagerInterface")
    public Boolean RunManagerInterface(@RequestParam Map<String, String> param) {
        return appUserService.RunManagerInterface(param);
    }

    /**
     * 获取服务器时间
     *
     * @return
     */
    @GetMapping("/getServerTime")
    public long getServerTime() {
        Date date = new Date();
        return date.getTime();
    }

    @Autowired
    TaskUserDao taskUserDao;


    /**
     * 删除用户
     *
     * @param id
     */
    @DeleteMapping("/users/{id}")
    public ApiResult deleteUserById(@PathVariable Long id) {
        Set<SysRole> sysRoles = userRoleDao.findRolesByUserId(id);
        for (SysRole sysRole : sysRoles) {
            if (ExamConstants.SYSTEM_SUPER_ADMIN.equals(sysRole.getCode())) {
                return ApiResultHandler.buildApiResult(500, "管理员不可删除", null);
            }
        }
        QueryWrapper<LcTaskUser> qw = new QueryWrapper<>();
        qw.eq("user_id", id);
        long count = taskUserDao.selectCount(qw);
        if (count>0){
            return ApiResultHandler.buildApiResult(500, "用户已被任务引用，不可删除", null);
        }
        appUserService.removeById(id);
        return ApiResultHandler.success();
    }

    //zhangsheng add 2021-7-6
    @GetMapping("/getDeptUserIds/{deptId}")
    public List<AppUser> getDeptUserIds(@PathVariable Long deptId) {
        return appUserService.getDeptUserIds(deptId);
    }

    @GetMapping("/getDeptUserNum")
    public int getDeptUserNum() {
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        List<AppUser> list = appUserService.getDeptUserIds(loginAppUser.getDepartmentId());
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }

    /**
     * 返回用户信息
     *
     * @param userId
     * @return
     */
    //根据用户id获取用户详细信息
    @RequestMapping(value = "/getUserDetailsById", method = RequestMethod.GET)
    public LoginAppUser getUserDetailsById(Long userId) {
        LoginAppUser loginAppUser = new LoginAppUser();
        AppUser byId = appUserService.findById(userId);
        BeanUtils.copyProperties(byId, loginAppUser);
        Set<SysRole> rolesByUserId = appUserService.findRolesByUserId(userId);
        loginAppUser.setSysRoles(rolesByUserId);
        return loginAppUser;
    }

    /**
     * 根据登录用户名获取用户
     */
    @GetMapping("/getAppUsersByLoginName")
    public List<AppUser> getAppUsersByloginname(@RequestParam(value = "loginName", required = false) String loginName) {
        QueryWrapper<AppUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginName);
        List<AppUser> list = appUserService.list(queryWrapper);
        return list;
    }


    /**
     * @author:胡立涛
     * @description: TODO 获取阅卷组、监考组人员
     * @date: 2021/12/7
     * @param: [roleCode]
     * @return: java.util.List<com.cloud.model.user.AppUser>
     */
    @GetMapping("getAppUserByRole")
    public List<AppUser> getAppUserByRole(@RequestParam String roleCode) {
        return appUserService.getAppUserByRole(roleCode);
    }


    @ApiOperation("通过用户ID查询用户的信息集合")
    @RequestMapping(value = "/users/getAppUserListByIds", method = RequestMethod.POST)
    public List<AppUser> getAppUserListByUserIdList(@RequestParam("userIdArray") Long[] userIdArray) {
        List<Long> longs = Arrays.asList(userIdArray);
        return this.appUserService.getAppUserListByUserIdList(longs);
    }


    /**
     * @author: 胡立涛
     * @description: TODO 获取学员导入模板
     * @date: 2022/6/1
     * @param: [response, request]
     * @return: void
     */
    @GetMapping(value = "/user/getTemplate")
    public void getTemplate(HttpServletResponse response, HttpServletRequest request) {
        try {
            String path = userTemplate;
            String downloadName = StringUtils.substringAfterLast(path, "/");
            response.setCharacterEncoding("utf-8");
            response.setContentType("multipart/form-data");
            response.setHeader("Content-Disposition",
                    "attachment;fileName=" + FileUtils.setFileDownloadHeader(request, downloadName));
            FileUtils.writeBytes(path, response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: 胡立涛
     * @description: TODO 学员批量导入
     * @date: 2022/6/2
     * @param: [file, deptId]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "user/uploadInfo")
    public ApiResult uploadInfo(@RequestParam(value = "file", required = false) MultipartFile file,
                                @RequestParam(value = "deptId", required = false) Long deptId) {
        try {
            if (file == null) {
                return ApiResultHandler.buildApiResult(100, "参数为空。", null);
            }
            String fileName = file.getOriginalFilename();
            Workbook workbook = null;
            InputStream is = file.getInputStream();
            if (fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(is);
            } else if (fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(is);
            } else {
                log.error("批量导入用户异常,不支持此类型的文件：{}", fileName);
                return ApiResultHandler.buildApiResult(500, "批量导入用户异常,不支持此类型的文件:" + fileName, null);
            }
            Sheet sheet = workbook.getSheetAt(0);
            int rows = sheet.getPhysicalNumberOfRows();
            if (rows > 200) {
                return ApiResultHandler.buildApiResult(100, "超过最大行数（200）限制。", null);
            }
            List<Map<String, Object>> maps = appUserService.uploadInfo(sheet, deptId);
            return ApiResultHandler.buildApiResult(0, "操作成功。", maps);
        } catch (Exception e) {
            log.error("批量导入用户异常", e);
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }

    @RequestMapping(value = "/getKpIdsbyUserId", method = RequestMethod.GET)
    public Set<String> getKpIdsbyUserId(Long userId) {
        HashSet<Long> set = new HashSet<>();
        HashSet<String> kpIds = new HashSet<>();
        Set<SysRole> rolesByUserId = appUserService.findRolesByUserId(userId);
        rolesByUserId.stream().forEach(e -> set.add(e.getId()));
        for (Long roleId : set) {
            List<String> kpIdsByRoleId = sysRoleService.getKpIdsByRoleId(roleId);
            kpIdsByRoleId.stream().forEach(k -> kpIds.add(k));
        }
        return kpIds;
    }
}
