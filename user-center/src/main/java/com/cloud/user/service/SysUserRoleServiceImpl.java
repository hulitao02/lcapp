package com.cloud.user.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.model.user.SysRoleUser;
import com.cloud.user.dao.UserRoleDao;
import org.springframework.stereotype.Service;

/**
 * <p>
 *
 * </p>
 *
 * @author tongkesong
 * @since 2023-08-22
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<UserRoleDao, SysRoleUser> implements SysUserRoleService {
}
