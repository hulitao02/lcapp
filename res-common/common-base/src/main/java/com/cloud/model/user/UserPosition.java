package com.cloud.model.user;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author meidan
 */
@Data
public class UserPosition implements Serializable {

    private static final long serialVersionUID = 611197991672067628L;

    private Long id;
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd",timezone="GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd",timezone="GMT+8")
    private Date updateTime;
    private Long creator;
    private String positionDesc;

}
