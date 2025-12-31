package com.cloud.model.bean.vo.es;

import lombok.Data;

import java.util.List;

@Data
public class EsKnowledgeRecord {

    private String area;
//    模板知识点名称
    private String bkClassLabel;
//    知识点
    private String classId;


//    知识ID
    private String sensesId;
//    这是名称
    private String sensesName;

    private String photo;
    // 封面图
    private String photoUrl;

    private Integer publishLabelId;

    private String publishLabelText;

    private List<KnowLedgeSensesCageProperty> sensesCageProperty;

    private Integer status;

    private String statusLabel;
    /**
     *  1:已关注 ，其他都是未关注的或者取消的
     */
    private Integer collectStatus = 0;
    /**
     *  1: 是否已学习
     */
    private Integer studyStatus = 0;
    /**
     *  数据库ID 标识
     */
    private Integer id ;

}
