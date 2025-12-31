package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by dyl on 2021/05/07.
 */
@Data
@ApiModel(value="examPlacearea",description = "考试区域实体")
public class ExamPlacearea {
    @TableId(type = IdType.AUTO)
    private Long  id;
    private String areaName;
    private Long departId;
}
