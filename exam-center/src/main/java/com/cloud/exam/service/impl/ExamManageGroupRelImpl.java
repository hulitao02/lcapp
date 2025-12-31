package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.ExamManageGroupRelDao;
import com.cloud.exam.model.exam.ExamManageGroupRel;
import com.cloud.exam.service.ExamManageGroupRelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class ExamManageGroupRelImpl extends ServiceImpl<ExamManageGroupRelDao,ExamManageGroupRel> implements ExamManageGroupRelService {

    @Autowired
    private ExamManageGroupRelDao examManageGroupRelDao;


}
