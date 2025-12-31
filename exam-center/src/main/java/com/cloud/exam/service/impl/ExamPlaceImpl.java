package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.ExamPlaceDao;
import com.cloud.exam.model.exam.ExamPlace;
import com.cloud.exam.service.ExamPlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class ExamPlaceImpl extends ServiceImpl<ExamPlaceDao,ExamPlace> implements ExamPlaceService {

    @Autowired
    private ExamPlaceDao examPlaceDao;


}
