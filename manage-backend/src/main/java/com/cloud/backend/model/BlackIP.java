package com.cloud.backend.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * IP黑名单
 * 
 * @author 数据管理
 *
 */
@Data
public class BlackIP implements Serializable {

	private static final long serialVersionUID = -2064187103535072261L;

	private String ip;
	private Date createTime;
}
