package com.cloud.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.model.user.UserCredential;
import com.cloud.user.dao.UserCredentialsDao;
import com.cloud.user.service.UserCredentialsService;
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
public class UserCredentialsServiceImpl extends ServiceImpl<UserCredentialsDao, UserCredential> implements UserCredentialsService {
}
