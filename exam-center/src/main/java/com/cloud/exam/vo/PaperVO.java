package com.cloud.exam.vo;

import com.cloud.exam.model.exam.Paper;
import lombok.Data;

/**
 * Created by dyl on 2021/5/27.
 */
@Data
public class PaperVO  extends Paper{

    private Long paperId;
    //试卷关联的单位和活动
    private Long departId;
    private String examName;
    private Long examId;
}
