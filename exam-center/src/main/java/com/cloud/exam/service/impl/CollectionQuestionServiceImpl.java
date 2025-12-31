package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.CollectionQuestionDao;
import com.cloud.exam.dao.UserActivityMessageDao;
import com.cloud.exam.model.exam.CollectionQuestion;
import com.cloud.exam.model.exam.UserActivityMessage;
import com.cloud.exam.service.CollectionQuestionService;
import com.cloud.exam.service.UserActivityMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author:胡立涛
 * @description: TODO 试题收藏
 * @date: 2022/8/19
 * @param:
 * @return:
 */

@Service
@Transactional
public class CollectionQuestionServiceImpl extends ServiceImpl<CollectionQuestionDao, CollectionQuestion> implements CollectionQuestionService {


}
