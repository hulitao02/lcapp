package com.cloud.user.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.model.common.Page;
import com.cloud.model.user.AppUser;
import com.cloud.model.user.SysDepartment;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.user.dao.SysDepartmentDao;
import com.cloud.user.service.AppUserService;
import com.cloud.user.service.SysDepartmentService;
import com.cloud.utils.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysDepartmentServiceImpl extends ServiceImpl<SysDepartmentDao, SysDepartment> implements SysDepartmentService {

    @Autowired
    private SysDepartmentDao sysDepartmentDao;
    @Autowired
    private AppUserService appUserService;

    @Override
    public int add(SysDepartment sysDepartment) {
        if (sysDepartment.getParentId() == null) {
            sysDepartment.setParentId(0L);
        }
        sysDepartment.setCreator(AppUserUtil.getLoginAppUser().getId());
        sysDepartment.setCreateTime(new Date());
        sysDepartment.setUpdateTime(new Date());
        return sysDepartmentDao.insert(sysDepartment);
    }

    @Override
    public int update(SysDepartment sysDepartment) {
        return sysDepartmentDao.updateById(sysDepartment);
    }

    @Override
    public int delete(Long id) {
        List<Long> parentIdList = sysDepartmentDao.parentIdList();
        if (parentIdList.contains(id)) {
            throw new IllegalArgumentException("存在子节点，无法删除！");
        }
        //删除前判断是否存在用户绑定
        boolean existUserInDept = appUserService.existUserInDept(id);
        if (existUserInDept) {
            throw new IllegalArgumentException("该部门下存在用户，无法删除！");
        }
        return sysDepartmentDao.deleteById(id);
    }

    @Override
    public SysDepartment findById(Long id) {
        return sysDepartmentDao.selectById(id);
    }

    @Override
    public SysDepartment findByName(String name) {
        return sysDepartmentDao.findByName(name);
    }

    @Override
    public Page<SysDepartment> findByPage(Map<String, Object> params) {
        int count = sysDepartmentDao.count(params);
        List<SysDepartment> list = Collections.emptyList();
        if (count >0 ){
            PageUtil.pageParamConver(params,true);
            list = sysDepartmentDao.findData(params);

        }
        return new Page<>(count,list);
    }

    @Override
    public List<SysDepartment> findAll() {
        List<SysDepartment> sysDepartments = lambdaQuery().orderByAsc(SysDepartment::getSort, SysDepartment::getId).list();
        if (!CollectionUtils.isEmpty(sysDepartments)) {
            Set<Long> creatorSet = sysDepartments.stream().map(SysDepartment::getCreator).collect(Collectors.toSet());
            List<AppUser> appUserList = appUserService.lambdaQuery().select(AppUser::getUsername, AppUser::getId)
                    .in(AppUser::getId, creatorSet).list();
            Map<Long, String> idNameMap = appUserList.stream().collect(Collectors.toMap(AppUser::getId, AppUser::getUsername));
            sysDepartments.forEach(e -> e.setCreatorName(idNameMap.get(e.getCreator())));
        }
        return sysDepartments;
    }
}
