package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.QuestionKpRelManageDao;
import com.cloud.exam.model.exam.QuestionKpRelManage;
import com.cloud.exam.service.QuestionKpRelManageService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class QuestionKpRelManageServiceImpl extends ServiceImpl<QuestionKpRelManageDao, QuestionKpRelManage> implements QuestionKpRelManageService {


    @Override
    public List<QuestionKpRelManage> findByQuestionIdList(List<Long> questionIdList) {
        if (CollectionUtils.isEmpty(questionIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .in(QuestionKpRelManage::getQuestionId, questionIdList).list();
    }

    @Override
    public Map<Long, List<String>> getQuestionIdAndKpIdListMap(List<Long> idList) {
        List<QuestionKpRelManage> qkrList = findByQuestionIdList(idList);
        return qkrList.stream()
                .collect(Collectors.groupingBy(QuestionKpRelManage::getQuestionId,
                        Collectors.mapping(QuestionKpRelManage::getKpId, Collectors.toList())));
    }
}
