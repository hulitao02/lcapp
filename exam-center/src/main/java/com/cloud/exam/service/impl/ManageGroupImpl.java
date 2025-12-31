package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.ManageGroupDao;
import com.cloud.exam.model.exam.ManageGroup;
import com.cloud.exam.service.ManageGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class ManageGroupImpl extends ServiceImpl<ManageGroupDao,ManageGroup> implements ManageGroupService {

    @Autowired
    private ManageGroupDao manageGroupDao;

}
