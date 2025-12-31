package com.cloud.exam.model.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud.exam.utils.ExcelUtil.ExcelCell;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author md
 */
@Data
@TableName("question_manage")
public class QuestionManage implements Serializable {

    private static final long serialVersionUID = -4985822594623105994L;


    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @ExcelCell(index = 1)
    @NotBlank(message = "试题名不能为空")
    private String question;
    @ExcelCell(index = 2)
    private String answer;
    @ExcelCell(index = 6)
    private String analysis;
    //@ExcelCell(index = 3)
    private String kpId;
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
    private Integer status;
    // 是否有卷帘效果 1：有 0或者空为无
    private Integer jlFlg;
    //用途：0：考试；1：训练
    private Integer use;
    private Integer version;
    private String code;
    private String keywords;
    private String modelUrl;
    private String imageAnnotion;
    private Long modelId;
    private Integer orderNum;
    private String batch;
    private String pdType;


    private transient String modelName;
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

    //创建课程时需要根据选择的知识点查询试题
    private transient String kps;

    //搜索类型(true 搜索全部题型试题,false 搜索单选多选判断,默认搜索全部)
    private transient boolean flag = true;
    //试题导入失败的原因
    private transient String errorText;
    //试题导入 重复试题的信息
    private transient String repeatData;
    //失败类型
    private transient Long errorType;
    //知识类别
    @ExcelCell(index = 3)
    @TableField(exist = false)
    private List<String> kpIds = new ArrayList<>();

    // 文件地址
    private transient String fileAddr;
    // 影像缩略图地址
    private transient String localUrlPrefix;


    @TableField(exist = false)
    private String stuAnswer;

    // 收藏记录id
    @TableField(exist = false)
    private Long collectionId = null;

    // 活动id
    @TableField(exist = false)
    private Long examId;
    // 判读类型 多个用逗号进行分割
    @TableField(exist = false)
    private String pdTypeStr;

}
