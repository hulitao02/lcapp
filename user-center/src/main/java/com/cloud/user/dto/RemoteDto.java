package com.cloud.user.dto;

import lombok.Data;

import java.io.Serializable;


@Data
public class RemoteDto implements Serializable {

    private static final long serialVersionUID = -6816635307922091062L;
//    操作类型，字符串
    private String operateFlag;
    private String jsonData;


}
