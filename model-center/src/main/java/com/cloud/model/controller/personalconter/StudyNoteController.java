package com.cloud.model.controller.personalconter;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.model.bean.dto.StudyNotesDto;
import com.cloud.model.model.StudyNotes;
import com.cloud.model.service.StudyNoteService;
import com.cloud.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.INTERNAL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ApiModel(value = "笔记controller")
@RestController
@RequestMapping("/studyNote")
@Slf4j
@RefreshScope
public class StudyNoteController {

    @Autowired
    private StudyNoteService studyNoteService;


    @ApiOperation("[保存|更新]学习笔记")
    @PostMapping("/saveOrUpdateStudyNote")
    public ApiResult saveStudyNote(@RequestBody StudyNotes studyNotes) {
        log.info("studyNote/saveOrUpdateStudyNote#  --> params:studyNote {}", JSON.toJSONString(studyNotes));
        studyNotes.setUpdateTime(new Date());
        if (Objects.isNull(studyNotes.getId())) {
            studyNotes.setCreateTime(new Date());
        }
        try {
            this.studyNoteService.saveOrUpdate(studyNotes);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("studyNote/saveOrUpdateStudyNote#  --> exception:{} ", e.getMessage());
            return ApiResultHandler.buildApiResult(500, "笔记操作异常:{} ", e.getMessage());
        }
        return ApiResultHandler.buildApiResult(200, "笔记操作创建成功", null);

    }


//    @ApiOperation("查询笔记分页")
//    @PostMapping("/getPageStudyNoteList")
//    public ApiResult getPageStudyNoteList(@RequestBody StudyNotesDto studyNoteDto) {
//        log.info("studyNote/getPageStudyNoteList#  --> params:studyNoteDto {}", JSON.toJSONString(studyNoteDto));
//        try {
//            String queryParams = studyNoteDto.getQueryParams();
//            if(StringUtils.isNotBlank(queryParams)){
//                studyNoteDto.setNotesInfo(queryParams);
//            }
//
//            IPage<StudyNotesDto> studyNoteListPage = this.studyNoteService.getStudyNoteListPage(studyNoteDto);
//            return ApiResultHandler.buildApiResult(200, "查询笔记列表成功", studyNoteListPage);
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("studyNote/getPageStudyNoteList#  --> exception:{} ", e.getMessage());
//            return ApiResultHandler.buildApiResult(500, "查询笔记异常:{} ", e.getMessage());
//        }
//    }

    // 文件服务器地址
    @Value(value = "${file_server}")
    private String fileServer;


    /**
     * @author:胡立涛
     * @description: TODO 根据知识id，用户id查询笔记详细信息
     * @date: 2025/1/15
     * @param: [studyNoteDto]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/getNoteDetail")
    public ApiResult getNoteDetail(@RequestBody StudyNotes studyNotes) {
        try {
            QueryWrapper<StudyNotes> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("kn_id", studyNotes.getKnId());
            queryWrapper.eq("user_id", studyNotes.getUserId());
            StudyNotes one = studyNoteService.getOne(queryWrapper);
            Map rMap = new HashMap();
            rMap.put("fileServer", fileServer);
            rMap.put("bean", one);
            return ApiResultHandler.buildApiResult(200, "操作成功 ", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "查询笔记异常:{} ", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 个人中心查询笔记列表
     * @date: 2025/1/15
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/getPageStudyNoteList")
    public ApiResult getPageStudyNoteList(@RequestBody Map map) {
        try {
            int page = Integer.parseInt(map.get("page").toString());
            int size = Integer.parseInt(map.get("size").toString());
            Page<StudyNotes> pg = new Page<>(page, size);
            QueryWrapper<StudyNotes> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", Integer.parseInt(map.get("userId").toString()));
            String keyWord = map.get("keyWord") == null ? "" : map.get("keyWord").toString();
            if (keyWord != "") {
                queryWrapper.like("notes_info", keyWord);
            }
            queryWrapper.orderByDesc("update_time");
            IPage<StudyNotes> list = studyNoteService.page(pg, queryWrapper);
            Map rMap = new HashMap();
            rMap.put("fileServer", fileServer);
            rMap.put("list", list);
            return ApiResultHandler.buildApiResult(200, "操作成功 ", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "查询笔记异常:{} ", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据id删除笔记
     * @date: 2025/1/15
     * @param: [studyNotes]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/delInfoById")
    public ApiResult delInfoById(@RequestBody StudyNotes studyNotes) {
        try {
            studyNoteService.removeById(studyNotes.getId());
            return ApiResultHandler.buildApiResult(200, "操作成功 ", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "查询笔记异常:{} ", e.toString());
        }
    }

}
