package com.cloud.model.controller;

import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.core.ResultMesEnum;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.dao.KnowledgeTifDao;
import com.cloud.model.dao.StudyTimeDao;
import com.cloud.model.dao.StudyTimeStatisticsDao;
import com.cloud.model.model.KnowledgeTif;
import com.cloud.model.service.KnowledgeTifService;
import com.cloud.model.service.StudyTimeService;
import com.cloud.model.user.AppUser;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.model.utils.CommonDate;
import com.cloud.utils.CollectionsCustomer;
import io.swagger.annotations.ApiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/knowledgetif")
@ApiModel(value = "影像标注模板")
@Slf4j
@RefreshScope
public class KnowledgeTifController {

    @Autowired
    KnowledgeTifDao knowledgeTifDao;
    @Autowired
    KnowledgeTifService knowledgeTifService;

    /**
     * @author:胡立涛
     * @description: TODO 完成数据对接逻辑
     * @date: 2022/10/20
     * @param: [knowledgeTif]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "finishData")
    public ApiResult finishData(@RequestBody KnowledgeTif knowledgeTif) {
        try {
            if (knowledgeTif.getModelKpId() == null) {
                return ApiResultHandler.buildApiResult(100, "参数modelKpId为空", null);
            }
            if (knowledgeTif.getKnowledgeCode() == null) {
                return ApiResultHandler.buildApiResult(100, "参数knowledgeCode为空", null);
            }
            if (knowledgeTif.getKnowledgeName() == null) {
                return ApiResultHandler.buildApiResult(100, "参数knowledgeName为空", null);
            }
            if (knowledgeTif.getKpId() == null) {
                return ApiResultHandler.buildApiResult(100, "参数kpId为空", null);
            }
            knowledgeTifService.finishData(knowledgeTif);
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.getMessage());
        }
    }
}
