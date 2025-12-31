package com.cloud.exam.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * Created by dyl on 2021/08/17.
 */
@ApiModel("条件封装实体")
@Data
public class ConditionBeanVO {

    public Integer page;
    public Integer size;
    public Long userId;
    //活动状态（1未开始 6考试中 4已结束）
    public List<Integer> examStatus;
    //试卷类型（0理论 1清晰  实操）
    public List<Integer> types;

}
