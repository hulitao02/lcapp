package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.exam.utils.ExcelUtil.ExcelCell;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author md
 */
@Data
@TableName("question_error")
public class QuestionError implements Serializable {

    private static final long serialVersionUID = -4985822594623105994L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    @ExcelCell(index = 1)
    private String question;
    @ExcelCell(index = 2)
    private String answer;
    @ExcelCell(index = 6)
    private String analysis;
    @ExcelCell(index = 3)
    private Long kpId;
    @ExcelCell(index = 7)
    private Long directId;
    @ExcelCell(index = 4)
    private String options;
    @ExcelCell(index = 0)
    private Double difficulty;
    @ExcelCell(index = 5)
    private Integer type;
    private Date createTime;
    private Date updateTime;
    private Long creator;
    private  Integer status;
    //用途：0：考试；1：训练
    private Integer use;
    private  Integer version;
    private String code;
    private String keywords;
    private String modelUrl;
    private transient Double score;
    private transient String kpName;
    private transient String typeName;
    private transient String directName;
    private transient Integer current;
    private transient Integer size;
    private transient Long relId;
    private transient String scoreBasis;
    //抽题换一批的时候去除重复试题
    private transient List questionList;

    //搜索类型(true 搜索全部题型试题,false 搜索单选多选判断,默认搜索全部)
    private transient boolean flag = true;
    //试题导入失败的原因
    private String errorText;
    //试题导入 重复试题的信息
    private String repeatData;
    //失败类型 失败类型 1、知识点为空2、知识点不存在3、试题重复（题干、知识点）4、答案不能为空
    private Long errorType;
    private transient List<Long> kpIds;
}
