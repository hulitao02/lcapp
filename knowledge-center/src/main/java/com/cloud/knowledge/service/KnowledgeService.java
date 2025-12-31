package com.cloud.knowledge.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.knowledge.model.PicImport;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface KnowledgeService extends IService<PicImport> {

    List<PicImport> importWord(MultipartFile multipartFile) throws Exception;
}
