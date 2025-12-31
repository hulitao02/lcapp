package com.cloud.file.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *  TIF 和 文件服务器 关联实体
 */
@Data
public class TifFastdfsRelation implements Serializable {

    @TableId(type = IdType.AUTO)
    private int id ;
    private String fileMd5;
    private String tifServerId;
    private String fastdfsPath;
    private Date create_time;

}
