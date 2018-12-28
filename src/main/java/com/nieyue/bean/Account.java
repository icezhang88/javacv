package com.nieyue.bean;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
/**
 * 账户
 * @author 聂跃
 * @date 2017年4月12日
 */
@Data
@ApiModel(value="账户",description="账户")
@TableName("account_tb")
public class Account implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 账户id
	 */
	@ApiModelProperty(value="账户id")
	@TableId("account_id")
	private Long accountId;
	/**
	 * 注册手机号
	 */
	@ApiModelProperty(value="注册手机号")
	private String phone;
	/**
	 * 密码
	 */
	@ApiModelProperty(value="密码")
	private String password;
	/**
	 * 昵称
	 */
	@ApiModelProperty(value="昵称")
	private String nickname;
	/**
	 * 图像
	 */
	@ApiModelProperty(value="图像")
	private String icon;
	/**
	 * 性别,默认为0未知，为1男性，为2女性
	 */
	@ApiModelProperty(value="性别,默认为0未知，为1男性，为2女性")
	private Integer sex;
	/**
	 * 真实姓名
	 */
	@ApiModelProperty(value="真实姓名")
	private String realname;
	/**
	 * email
	 */
	@ApiModelProperty(value="email")
	private String email;
	/**
	 * 国家
	 */
	@ApiModelProperty(value="国家")
	private String country;
	/**
	 * 邀请码
	 */
	@ApiModelProperty(value="邀请码")
	private String inviteCode;
	/**
	 * 收货地址
	 */
	@ApiModelProperty(value="收货地址")
	private String address;

	/**
	 * 认证，0没认证，1审核中，2已认证
	 */
	@ApiModelProperty(value="认证，0没认证，1审核中，2已认证")
	private Integer auth;
	/**
	 * 身份证正面
	 */
	@ApiModelProperty(value="身份证正面")
	private String identityCardsFrontImg;
	/**
	 * 身份证反面
	 */
	@ApiModelProperty(value="身份证反面")
	private String identityCardsBackImg;
	/**
	 * 驾照正面
	 */
	@ApiModelProperty(value="驾照正面")
	private String drivingLicenseFrontImg;
	/**
	 * 驾照反面
	 */
	@ApiModelProperty(value="驾照反面")
	private String drivingLicenseBackImg;
	/**
	 * 创建时间
	 */
	@ApiModelProperty(value="创建时间")
	private Date createDate;
	/**
	 * 登陆时间
	 */
	@ApiModelProperty(value="登陆时间")
	private Date loginDate;
	/**
	 * 状态 0正常，1封禁，2异常
	 */
	@ApiModelProperty(value="状态 0正常，1封禁，2异常")
	private Integer status;
	/**
	 * 上级id外键
	 */
	@ApiModelProperty(value="上级id外键")
	private Long masterId;
	/**
	 * 角色id外键
	 */
	@ApiModelProperty(value="角色id外键")
	private Long roleId;
	/**
	 * 角色
	 */
	@ApiModelProperty(value="角色")
	@TableField(exist=false)
	private  Role role;

}
