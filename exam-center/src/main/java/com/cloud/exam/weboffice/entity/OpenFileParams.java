package com.cloud.exam.weboffice.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("打开WebOffice文件的参数")
@Data
public class OpenFileParams {

    /**
     * 文档名（默认从filePath中截取）
     */
//    @ApiModelProperty("文档名（默认从filePath中截取）")
//    private String fileName;

    /**
     * 此路径是相对父路径（配置文件filePath）下的子路径
     */
    @ApiModelProperty("文档路径")
    private String filePath;

    /**
     * 文档id：传文档id
     */
    @ApiModelProperty("文档ID")
    private String fileId;

    /**
     * 用户头像的地址。（默认使用WebOffice内置的头像）
      */
//    @ApiModelProperty("用户头像的地址。（默认使用WebOffice内置的头像）")
//    private String userAvatar;


    /**
     * true：以只读模式打开文档
     * false：以编辑模式打开文档
     * 默认为false
     */
    @ApiModelProperty("是否只读模式（true：以只读模式打开文档，false：默认，以编辑模式打开文档）")
    private boolean readOnly;

    /**
     * true：自动保存文档
     * false：不自动保存文档
     * 默认为true
     */
    @ApiModelProperty("是否自动保存文档（true：默认，自动保存文档，false：不自动保存文档）")
    private boolean saveFlag;

    @ApiModelProperty("是否打开过文档（false：默认，未打开过，true：已打开过）")
    private boolean openFlag;

    @ApiModelProperty("考生准考证号")
    private String identityId;

    @ApiModelProperty("试题id")
    private Long questionId;



}
