package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.UserActivityMessageDao;
import com.cloud.exam.model.exam.UserActivityMessage;
import com.cloud.exam.service.UserActivityMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class UserActivityMessageServiceImpl extends ServiceImpl<UserActivityMessageDao,UserActivityMessage> implements UserActivityMessageService {

    @Autowired
    private UserActivityMessageDao userActivityMessageDao;

}
