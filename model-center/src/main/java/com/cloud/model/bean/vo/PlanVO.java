package com.cloud.model.bean.vo;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 *
 *  select sk.id, sk.study_status as study_status,
 *  sp.id as plan_id , sp.create_time as start_time , sp.plan_start_time as end_time
 *
 */
@Data
public class PlanVO {


    @ApiModelProperty("学习计划关联的知识ID")
    private Integer id ;
    private Integer planId ;
    private Integer studyStatus;
    private Date startTime;
    private Date endTime;


    public Date getEndTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = dateFormat.format(endTime);
        String endTimeStr = format + " " + " 23:59:59";
        SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            endTime = defaultFormat.parse(endTimeStr);
        } catch (ParseException e) {
            e.printStackTrace();
            endTime = new Date();
        }
        return endTime;
    }

    public Date getDefaultEndTime() throws ParseException {
        return endTime;
    }


}
