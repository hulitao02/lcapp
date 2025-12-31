package com.cloud.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.core.ApiResult;
import com.cloud.exam.model.exam.PaperManageRel;
import com.cloud.exam.model.exam.Question;

import java.util.List;

public interface PaperManageRelService extends IService<PaperManageRel> {


    ApiResult updatePaperManageById(PaperManageRel paperManage);

    List<PaperManageRel> findByPaperId(Long paperId);

    List<Question> findPaperDetail(Long paperId);
}
