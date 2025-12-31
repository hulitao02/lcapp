package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by dyl on 2021/03/24.
 */
@Data
@ApiModel(value = "examPlace",description = "考试场地实体类")
public class ExamPlace implements Serializable {

    @TableId(type = IdType.AUTO)
    private  Long id;
    @NotBlank(message = "场地名不能为空")
    private  String placeName;
    private  String placeArea;
    @NotBlank(message = "场地地址不能为空")
    private  String placeAddress;
    @NotNull(message = "座位数不能为空")
    @Min(value = 1,message = "请输入大于1的数字")
    private  Integer seatCount;
    private  Long placeareaId;
}
