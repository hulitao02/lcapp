package com.cloud.user.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.model.common.Dict;
import com.cloud.model.common.Page;
import com.cloud.model.user.*;
import com.cloud.model.user.constants.CredentialType;
import com.cloud.model.user.constants.UserType;
import com.cloud.user.dao.*;
import com.cloud.user.service.*;
import com.cloud.utils.PageUtil;
import com.cloud.utils.PhoneUtil;
import com.cloud.utils.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppUserServiceImpl extends ServiceImpl<AppUserDao, AppUser> implements AppUserService {

    @Resource
    private AppUserDao appUserDao;
    @Resource
    private BCryptPasswordEncoder passwordEncoder;
    @Resource
    private SysPermissionService sysPermissionService;
    @Resource
    private UserRoleDao userRoleDao;
    @Resource
    private UserCredentialsDao userCredentialsDao;
    @Resource
    private SysRoleService sysRoleService;
    @Resource
    private SysPermissionDao sysPermissionDao;
    @Resource
    private SysRoleDao sysRoleDao;
    @Resource
    private SysDepartmentDao sysDepartmentDao;
    @Resource
    private UserPositionDao userPositionDao;
    @Resource
    private ManageBackendFeign manageBackendFeign;
    @Transactional
    @Override
    public void addAppUser(AppUser appUser) {
        String username = appUser.getUsername();
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        if (PhoneUtil.checkPhone(username)) {// 防止用手机号直接当用户名，手机号要发短信验证
            throw new IllegalArgumentException("用户名要包含英文字符");
        }

        if (username.contains("@")) {// 防止用邮箱直接当用户名，邮箱也要发送验证（暂未开发）
            throw new IllegalArgumentException("用户名不能包含@");
        }

        if (username.contains("|")) {
            throw new IllegalArgumentException("用户名不能包含|字符");
        }

        if (StringUtils.isBlank(appUser.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }

        if (StringUtils.isBlank(appUser.getNickname())) {
            appUser.setNickname(username);
        }

        if (StringUtils.isBlank(appUser.getType())) {
            appUser.setType(UserType.APP.name());
        }

        QueryWrapper<AppUser> appUserQueryWrapper = new QueryWrapper<>();
        appUserQueryWrapper.eq("username", username);
        long count = appUserDao.selectCount(appUserQueryWrapper);
        if (count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        if (!Validator.isNull(appUser.getRankNum())) {
            QueryWrapper<AppUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("rank_num", appUser.getRankNum());
            List<AppUser> appUsers = appUserDao.selectList(queryWrapper);
            if (appUsers.size() > 0) {
                throw new IllegalArgumentException("身份证号已存在");
            }
        }

        appUser.setPassword(passwordEncoder.encode(appUser.getPassword())); // 加密密码

//       DM Status
        appUser.setStatus(1);

        appUser.setCreateTime(new Date());
        appUser.setUpdateTime(appUser.getCreateTime());

        appUserDao.save(appUser);
        //userCredentialsDao.save(new UserCredential(appUser.getUsername(), CredentialType.USERNAME.name(), appUser.getId()));
        log.info("添加用户：{}", appUser);
    }

    @Transactional
    @Override
    public void updateAppUser(AppUser appUser) {
        appUser.setUpdateTime(new Date());
        appUserDao.update(appUser);
        log.info("修改用户：{}", appUser);
    }

    @Transactional
    @Override
    public LoginAppUser findByUsername(String username) {
        AppUser appUser = appUserDao.selectOne(new QueryWrapper<AppUser>().eq("username", username));
        if (appUser != null) {
            LoginAppUser loginAppUser = new LoginAppUser();
            BeanUtils.copyProperties(appUser, loginAppUser);

            Set<SysRole> sysRoles = userRoleDao.findRolesByUserId(appUser.getId());
            loginAppUser.setSysRoles(sysRoles);// 设置角色

            if (!CollectionUtils.isEmpty(sysRoles)) {
                Set<Long> roleIds = sysRoles.parallelStream().map(SysRole::getId).collect(Collectors.toSet());
                Set<SysPermission> sysPermissions = sysPermissionService.findByRoleIds(roleIds);
                if (!CollectionUtils.isEmpty(sysPermissions)) {
                    Set<String> permissions = sysPermissions.parallelStream().map(SysPermission::getPermission)
                            .collect(Collectors.toSet());

                    loginAppUser.setPermissions(permissions);// 设置权限集合
                }

            }

            return loginAppUser;
        }

        return null;
    }

    @Override
    public AppUser findById(Long id) {
        return appUserDao.findById(id);
    }

    /**
     * 给用户设置角色<br>
     * 这里采用先删除老角色，再插入新角色
     */
    @Transactional
    @Override
    public void setRoleToUser(Long id, Set<Long> roleIds) {
        AppUser appUser = appUserDao.findById(id);
        if (appUser == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        userRoleDao.deleteUserRole(id, null);
        if (!CollectionUtils.isEmpty(roleIds)) {
            roleIds.forEach(roleId -> {
                userRoleDao.saveUserRoles(id, roleId);
            });
        }

        log.info("修改用户：{}的角色，{}", appUser.getUsername(), roleIds);
    }

    /**
     * 修改密码
     *
     * @param id
     * @param oldPassword
     * @param newPassword
     */
    @Transactional
    @Override
    public void updatePassword(Long id, String oldPassword, String newPassword) {
        AppUser appUser = appUserDao.findById(id);
        if (StringUtils.isNoneBlank(oldPassword)) {
            if (!passwordEncoder.matches(oldPassword, appUser.getPassword())) { // 旧密码校验
                throw new IllegalArgumentException("旧密码错误");
            }
        }
        AppUser user = new AppUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(newPassword)); // 加密密码

        updateAppUser(user);
        log.info("修改密码：{}", user);
    }

    @Override
    public Page<AppUser> findUsers(Map<String, Object> params) {
        if (ObjectUtil.isEmpty(params.get("enabled"))) {
            params.remove("enabled");
        }
        if (ObjectUtil.isNotEmpty(params.get("enabled"))) {
            if ("true".equals(params.get("enabled"))) {
                params.put("enabled", "1");
            } else {
                params.put("enabled", "0");
            }
        }
        Object departmentIdObject = params.get("departmentId");
        if (ObjectUtil.isNotEmpty(departmentIdObject)) {
            List<Long> childIdList = sysDepartmentDao.findChildIdList(Long.valueOf((String) departmentIdObject));
            params.put("departmentIdList", childIdList);
        }
        int total = appUserDao.count(params);
        List<AppUser> list = Collections.emptyList();
        List<AppUser> reList = new ArrayList<>();
        if (total > 0) {
            PageUtil.pageParamConver(params, true);
            list = appUserDao.findData(params);
        }
        for (AppUser appUser : list) {
            SysDepartment byId1 = sysDepartmentDao.selectById(appUser.getDepartmentId());
            appUser.setDepartmentName(byId1 == null ? "" : byId1.getDname());
            UserPosition byId = userPositionDao.findById(appUser.getPositionId());
            appUser.setPositionName(byId==null?"":byId.getName());

            if(ObjectUtil.isNotNull(appUser.getLevel())){
                //根据字典来设置内容
                Map<String, Object> mapParam = new HashMap<>();
                mapParam.put("dictType", "user_level");
                mapParam.put("dictValue", String.valueOf(appUser.getLevel()));
                List<Dict> dictList = manageBackendFeign.findDict(mapParam);
                if(dictList.size()>0){
                    appUser.setLevelName(dictList.get(0).getDictName());
                }else {
                    appUser.setLevelName("");
                }
            }else {
                appUser.setLevelName("");
            }


            reList.add(appUser);
        }
        return new Page<>(total, reList);
    }

    @Override
    public Set<SysRole> findRolesByUserId(Long userId) {
        return userRoleDao.findRolesByUserId(userId);
    }

    /**
     * 绑定手机号
     */
    @Transactional
    @Override
    public void bindingPhone(Long userId, String phone) {
        UserCredential userCredential = userCredentialsDao.findByUsername(phone);
        if (userCredential != null) {
            throw new IllegalArgumentException("手机号已被绑定");
        }
        AppUser appUser = appUserDao.findById(userId);
        appUser.setPhone(phone);

        updateAppUser(appUser);
        log.info("绑定手机号成功,username:{}，phone:{}", appUser.getUsername(), phone);

        // 绑定成功后，将手机号存到用户凭证表，后续可通过手机号+密码或者手机号+短信验证码登陆
        userCredentialsDao.save(new UserCredential(phone, CredentialType.PHONE.name(), userId));
    }

    @Override
    public Boolean RunManagerInterface(Map<String, String> param) {
        Boolean res = false;
        try {
            AppUser appUser = new AppUser();
            appUser.setSex(1);
            appUser.setUsername(param.get("username"));
            appUser.setPassword(param.get("password"));

            LoginAppUser byUsername = findByUsername(appUser.getUsername());
            //判断用户是否存在如果不存在则创建
            if (byUsername == null) {
                addAppUser(appUser);
                byUsername = findByUsername(appUser.getUsername());
            } else {
                //判断密码是否正确，不正确则为修改

            }
            //获取到用户的角色不存在则创建
            Set<SysRole> rolesByUserId = findRolesByUserId(byUsername.getId());
//        SysRole[] objects = new SysRole[0];
//        objects = rolesByUserId.toArray(objects);
            SysRole sysRole = new SysRole();
            if (rolesByUserId == null || rolesByUserId.size() == 0) {
                //角色不存在则创建
                String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                uuid.substring(uuid.length() - 29, uuid.length() - 1);
                sysRole.setCode(uuid);
                sysRole.setName(uuid);
                sysRoleService.save(sysRole);
                SysRole byCode = sysRoleDao.findByCode(uuid);
                rolesByUserId.add(byCode);
            }
            Set<Long> idsRole = new HashSet<>();
            for (SysRole sp : rolesByUserId) {
                idsRole.add(sp.getId());
            }
            setRoleToUser(byUsername.getId(), idsRole);
            //获取所有权限
            Map<String, Object> pa = new HashMap<>();
            List<SysPermission> data = sysPermissionDao.findData(pa);
            //将权限分配给角色
            for (SysRole sr : rolesByUserId) {

                Set<Long> pIds = new HashSet<>();
                for (SysPermission sp : data) {
                    String permission = sp.getPermission();
                    if (permission.indexOf("Front") == 0) {
                        for (String key : param.keySet()) {
                            if (key.equals("1") && permission.equals("Front:RawData")) {
                                pIds.add(sp.getId());
                                break;
                            }
                            if (key.equals("2") && permission.equals("Front:ProductData")) {
                                pIds.add(sp.getId());
                                break;
                            }
                            if (key.equals("3") && permission.equals("Front:PlaceFile")) {
                                pIds.add(sp.getId());
                                break;
                            }
                            if (key.equals("4") && permission.equals("Front:ShareFile")) {
                                pIds.add(sp.getId());
                                break;
                            }
                        }
                    } else {
                        pIds.add(sp.getId());
                    }
                }
                sysRoleService.setPermissionToRole(sr.getId(), pIds);
            }
            res = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return res;
        }
    }

    @Override
    public Set<SysPermission> findPermissionsByRoleId(Long roleId) {
        return userRoleDao.findPermissionsByRoleId(roleId);
    }

    //zhangsheng add 2021-7-6
    @Override
    public List<AppUser> getDeptUserIds(Long deptId) {
        LambdaQueryWrapper<AppUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(AppUser::getId, AppUser::getUsername, AppUser::getNickname);
        lambdaQueryWrapper.eq(AppUser::getDepartmentId, deptId);
        lambdaQueryWrapper.eq(AppUser::getStatus, 1 );
        List<AppUser> list = appUserDao.selectList(lambdaQueryWrapper);
        return list;
    }


    @Override
    public List<AppUser> getAppUserByRole(String roleCode) {
        return appUserDao.getAppUserByRole(roleCode);
    }

    /**
     * ID集合查询
     *
     * @param userIdList
     * @return
     */
    @Override
    public List<AppUser> getAppUserListByUserIdList(List<Long> userIdList) {
        return this.appUserDao.selectBatchIds(userIdList);
    }

    @Transactional
    public boolean removeById(Serializable id) {
        return SqlHelper.retBool(this.baseMapper.deleteById(id));
    }

    // 学员导入模板地址
    @Value(value = "${user-role}")
    private Long userRole;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> uploadInfo(Sheet sheet, Long deptId) {
        List<Map<String, Object>> msgList = new ArrayList<>();
        int rows = sheet.getPhysicalNumberOfRows();
        List<AppUser> appUserList = new ArrayList<>();
        Date createTime = new Date();

        for (int i = 1; i < rows; i++) {
            Map map = new HashMap();
            Row row = sheet.getRow(i);
            if (null == row) {
                continue;
            }
            // 登录名称
            String loginName = row.getCell(1) == null ? null : row.getCell(1).getStringCellValue();
            // 身份证号
            String carNo = row.getCell(5) == null ? null : row.getCell(5).getStringCellValue();
            if (loginName == null) {
                map = new HashMap();
                map.put("lineNo", i + 1);
                map.put("msg", "登录名称为空");
                msgList.add(map);
                continue;
            }
            if (carNo == null) {
                map = new HashMap();
                map.put("lineNo", row.getRowNum());
                map.put("msg", "身份证号为空");
                msgList.add(map);
                continue;
            }

            Map parMap = new HashMap();
            // 查询该登录名称是否存在
            parMap.put("userName", loginName);
            List<Map> users = appUserDao.getUsers(parMap);
            if (users != null && users.size() > 0) {
                map = new HashMap();
                map.put("lineNo", i + 1);
                map.put("msg", "登录名称已存在");
                msgList.add(map);
                continue;
            }
            // 查看身份证号在该部门是否存在
            parMap = new HashMap();
            parMap.put("rankNum", carNo);
            parMap.put("deptId", deptId);
            List list = appUserDao.getUsers(parMap);
            if (list != null && list.size() > 0) {
                map = new HashMap();
                map.put("lineNo", i + 1);
                map.put("msg", "身份证号已存在");
                msgList.add(map);
                continue;
            }
            // 姓名
            String nickName = row.getCell(2) == null ? null : row.getCell(2).getStringCellValue();
            // 性别
            String sex = row.getCell(3) == null ? null : row.getCell(3).getStringCellValue();
            // 手机号
            String phone = row.getCell(4) == null ? null : row.getCell(4).getStringCellValue();
            // 添加用户信息
            int intSex = -1;
            if (sex != null && sex.equals("男")) {
                intSex = 1;
            }
            if (sex != null && sex.equals("女")) {
                intSex = 0;
            }
            AppUser appUser = new AppUser();
            appUser.setUsername(loginName);
            appUser.setNickname(nickName);
            appUser.setPassword(passwordEncoder.encode("88888888"));
            appUser.setPhone(phone);
            appUser.setSex(intSex);
            appUser.setCreateTime(createTime);
            appUser.setDepartmentId(deptId);
            appUser.setUpdateTime(createTime);
            appUser.setRankNum(carNo);
            appUser.setType(UserType.BACKEND.name());
            appUserList.add(appUser);
//            Map userMap = new HashMap();
//            userMap.put("userName", loginName);
//            userMap.put("password", passwordEncoder.encode("88888888"));
//            userMap.put("phone", phone);
//            userMap.put("sex", intSex);
//            userMap.put("createTime", new Timestamp(System.currentTimeMillis()));
//            userMap.put("deptId", deptId);
//            userMap.put("updateTime", new Timestamp(System.currentTimeMillis()));
//            userMap.put("rankNum", carNo);
//            appUserDao.saveInfo(userMap);
            // 根据身份证号和部门查询数据
//            list = appUserDao.getUsers(parMap);
//            Map uMap = (Map) list.get(0);
//            Long uid = Long.valueOf(uMap.get("id").toString());
//            // 保存用户角色信息
//            userRoleDao.saveUserRoles(uid, userRole);
        }
//        if (!CollectionUtils.isEmpty(appUserList)) {
//            this.saveBatch(appUserList);
//            List<UserCredential> userCredentialList = new ArrayList<>(appUserList.size());
//            List<SysRoleUser> sysRoleUserList = appUserList.stream().map(user -> {
//                userCredentialList.add(new UserCredential(user.getUsername(), CredentialType.USERNAME.name(), user.getId()));
//                return new SysRoleUser(user.getId(), userRole());
//            }).collect(Collectors.toList());
//            sysUserRoleService.saveBatch(sysRoleUserList);
//            userCredentialsService.saveBatch(userCredentialList);
//        }
        return msgList;
    }

    @Override
    public boolean existUserInDept(Long deptId) {
        Integer count = lambdaQuery().eq(AppUser::getDepartmentId, deptId).count();
        return count > 0;
    }
}
