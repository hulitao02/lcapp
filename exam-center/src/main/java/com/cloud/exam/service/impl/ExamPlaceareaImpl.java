package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.ExamPlaceareaDao;
import com.cloud.exam.model.exam.ExamPlacearea;
import com.cloud.exam.service.ExamPlaceareaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class ExamPlaceareaImpl extends ServiceImpl<ExamPlaceareaDao,ExamPlacearea> implements ExamPlaceareaService {

    @Autowired
    private ExamPlaceareaDao examPlaceareaDao;


}
