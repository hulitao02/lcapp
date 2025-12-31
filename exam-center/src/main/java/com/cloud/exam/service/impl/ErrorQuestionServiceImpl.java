package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.ErrorQuestionDao;
import com.cloud.exam.model.exam.ErrorQuestion;
import com.cloud.exam.service.ErrorQuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author:胡立涛
 * @description: TODO 错误试题
 * @date: 2022/8/19
 * @param:
 * @return:
 */

@Service
@Transactional
public class ErrorQuestionServiceImpl extends ServiceImpl<ErrorQuestionDao, ErrorQuestion> implements ErrorQuestionService {


}
