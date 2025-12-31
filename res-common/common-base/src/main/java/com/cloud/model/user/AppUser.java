package com.cloud.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Data
@TableName("app_user")
public class AppUser implements Serializable {

	private static final long serialVersionUID = 611197991672067628L;
	@TableId(type = IdType.AUTO)
	private Long id;
	private String username;
	private String password;
	private String nickname;
	private String headImgUrl;
	private String phone;
	private Integer sex;

	/**
	 *  level Kingbase关键字 。
	 */
	private Integer level;
	/**
	 * 状态 TODO DM 7 不支持boolean ,enabled 改为 status字段
	 */
	private Integer status = 1;
//	private boolean enabled;

	private String type;
	private Date createTime;
	private Date updateTime;
	private Long departmentId;
	private Long positionId;
	private String rankNum;
	private transient String departmentName;
	private transient String positionName;
	private transient String levelName;
	private String armServices;

	// 昵称（跟部门名称一致）
	private transient String dname;


//	TODO 达梦数据库 。
	public boolean isEnabled() {
		Integer status = getStatus();
		return (Objects.nonNull(status) && status.intValue()==1)?true:false;
	}



}
