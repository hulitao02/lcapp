package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 活动创建
 *
 * @author meidan
 */
@Data
@ApiModel(value = "exam", description = "考试活动实体类")
public class Exam implements Serializable {
    private static final long serialVersionUID = -1118671419161945076L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    @NotBlank(message = "活动名不能为空")
    @Length(max = 100,message = "名字长度最大100")
    private String name;
    @Length(max = 200,message = "描述长度最大200")
    private String describe;
    private Integer type;
    private Integer unit;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date startTime;
    @DateTimeFormat
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date endTime;
    @DateTimeFormat
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date examTime;
    private Date createTime;
    private Date updateTime;
    private Long creator;
    private Integer examStatus;
    private String place;
    private Integer screenRecord;
    private Integer videoRecord;
    private Integer examBreakoff;
    private double scorePercent;
    private double initialScore;
    private Integer scoringMethod;
    private Long hostId;
    private String examRemark;
    private Long scorechartsId;
    private Integer isSign;
    private Integer isFix;
    @TableField(exist = false)
    private Long totalTime;
    @TableField(exist = false)
    private Integer paperFlg=0;
    @TableField(exist = false)
    private String createDepartName;
    @TableField(exist = false)
    private List<Exam> childList = new ArrayList<>();
    @TableField(exist = false)
    private String hostName;
    @TableField(exist = false)
    private String identityCard;
    @TableField(exist = false)
    private boolean isJudge = false;
    @TableField(exist = false)
    private boolean isMonitor = false;
}
