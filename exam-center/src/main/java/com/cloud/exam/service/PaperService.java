package com.cloud.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.exam.model.exam.Paper;
import com.cloud.exam.model.exam.QuestionManage;
import com.cloud.exam.model.exam.QuestionTransfer;
import com.cloud.exam.vo.PaperVO;
import com.cloud.exam.vo.RuleBeanVO;

import java.util.List;


public interface PaperService extends IService<Paper> {

    IPage<Paper> findList(Page page);

    Paper paperSave(RuleBeanVO rule, Paper paper);

    boolean deletePaperById(long paperId);

    IPage<Paper> findByPage(PaperVO paper);

    QuestionTransfer transferQuestion(List<QuestionManage> questionManageList);
}
