package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.DrawResultDao;
import com.cloud.exam.model.exam.DrawResult;
import com.cloud.exam.service.DrawResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class DrawResultImpl extends ServiceImpl<DrawResultDao,DrawResult> implements DrawResultService {

    @Autowired
    private DrawResultDao drawResultDao;


}
