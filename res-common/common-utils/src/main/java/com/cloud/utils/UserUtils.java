package com.cloud.utils;

import com.cloud.core.ExamConstants;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.user.LoginAppUser;
import com.cloud.model.user.SysRole;
import com.cloud.model.utils.AppUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * Created by dyl on 2022/01/13.
 */
@Component
public class UserUtils {

    @Autowired
    private SysDepartmentFeign sd;
    private static SysDepartmentFeign sysDepartmentFeign;
    @PostConstruct
    public void init(){
        sysDepartmentFeign = this.sd;
    }
    public static boolean ifAdmin(){
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        Set<SysRole> rolesByUserId = sysDepartmentFeign.findRolesByUserId(loginAppUser.getId());
        boolean b = rolesByUserId.stream().anyMatch(e -> e.getCode().equals(ExamConstants.SYSTEM_SUPER_ADMIN) || e.getCode().equals(ExamConstants.SYS_SUPER_ADMIN));
        return b;
    }

}
