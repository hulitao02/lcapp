package com.cloud.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.exam.model.exam.DrawResult;
import com.cloud.exam.model.record.Record;
import com.cloud.exam.service.DrawResultService;
import com.cloud.exam.service.RecordService;
import com.cloud.exception.ResultMesCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Objects;


@RestController
@RequestMapping("/record")
@Api(value = "录屏controller类")
public class RecordController {


    @Autowired
    private RecordService recordService;

    @Autowired
    private DrawResultService drawResultService;

    @ApiOperation("提交录屏信息")
    @RequestMapping(value = "/saveRecordInfos", method = RequestMethod.POST)
    public ApiResult saveRecordInfos(@RequestBody Record record) {

        boolean saveResult = false;
        try {
            String identityCard = record.getIdentityCard();
            QueryWrapper wrapper = new QueryWrapper();
            wrapper.eq("identity_card",identityCard);
            DrawResult drawResult = this.drawResultService.getOne(wrapper);
            if(Objects.nonNull(drawResult)){
                record.setAcId(drawResult.getAcId());
                record.setCreateTime(new Date());
                record.setPaperId(drawResult.getPaperId());
                saveResult = this.recordService.saveOrUpdate(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String resultStr = saveResult ? "录屏文件提交成功" : "保存异常";
        return ApiResultHandler.buildApiResult(ResultMesCode.SUCCESS.getResultCode(), resultStr, saveResult);
    }





}
