package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * Created by dyl on 2021/03/25.
 * 抽签结果
 */
@Data
@TableName(value = "drawresult")
public class DrawResult {
    @TableId(type= IdType.AUTO)
    private Long id;
    private Long acId;
    private Long departId;
    private Long paperId;
    private Long userId;
    private Integer placeId;
    //考试得分
    private Double score;
    //试卷类型
    private Integer paperType;
    //判卷人
    private Long judgePerson;
    //准考证号
    private String identityCard;
    //用户考试状态
    private Integer userStatus;
    //考试座位号
    private String placeNum;
    private String costTime;
    private Date loginDate;
    @TableField(exist = false)
    private String loginDateStr;
    private Integer abilityLevel;
    //pdf无答案的两个有无水印  无水印url#有水印url
    private String pdfNoAnswerUrl;
    //pdf有答案的两个有无水印  无水印url#有水印url
    private String pdfAnswerUrl;
    //活动类型
    private Integer examType;
}
