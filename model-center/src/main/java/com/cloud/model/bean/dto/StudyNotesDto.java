package com.cloud.model.bean.dto;

import com.cloud.model.model.StudyNotes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("前端传递到后端实体类")
@Data
public class StudyNotesDto extends StudyNotes {

    @ApiModelProperty("查询参数")
    private String queryParams;
    private Integer pageNum;
    private Integer pageSize;



}
