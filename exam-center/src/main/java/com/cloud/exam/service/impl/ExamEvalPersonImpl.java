package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.ExamEvalPersonDao;
import com.cloud.exam.model.eval.ExamEvalPerson;
import com.cloud.exam.service.ExamEvalPersonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author: 胡立涛
 * @description: TODO
 * @date: 2022/5/16
 * @param:
 * @return:
 */
@Service
@Transactional
public class ExamEvalPersonImpl extends ServiceImpl<ExamEvalPersonDao, ExamEvalPerson> implements ExamEvalPersonService {


}
