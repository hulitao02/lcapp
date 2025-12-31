package com.cloud.model.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog implements Serializable {
    private static final long serialVersionUID = -5398795297842979376L;
    private String name;
    //记录的内容（用于展示和恢复）
    private String content;
    //创建的时间信息
    private Date createTime;
    /** 是否执行成功 */
    private Boolean flag;
}
