package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Created by dyl on 2021/03/23.
 */
@Data
public class ManageGroup {

    @TableId(type = IdType.AUTO)
    private Integer id;
    @NotBlank(message = "小组名不能为空")
    private String name;
    private String describe;
}
