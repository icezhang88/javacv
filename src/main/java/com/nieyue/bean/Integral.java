package com.nieyue.bean;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 积分
 * @author 聂跃
 * @date 2017年4月12日
 */
@Data
@ApiModel(value="积分",description="积分")
@TableName("integral_tb")
public class Integral implements Serializable {

	/**
     *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 积分id
	 */
	@ApiModelProperty(value="积分id")
	@TableId("integral_id")
	private Long integralId;
	/**
	 * 剩余积分
	 */
	@ApiModelProperty(value="剩余积分")
	private Double integral;
	/**
	 * 充值积分
	 */
	@ApiModelProperty(value="充值积分")
	private Double recharge;
	/**
	 * 消费积分
	 */
	@ApiModelProperty(value="消费积分")
	private Double consume;
	/**
	 * 赠送积分
	 */
	@ApiModelProperty(value="赠送积分")
	private Double baseProfit;
	/**
	 * 创建时间
	 */
	@ApiModelProperty(value="创建时间")
	private Date createDate;
	/**
	 * 更新时间
	 */
	@ApiModelProperty(value="更新时间")
	private Date updateDate;
	/**
	 * 账户id外键
	 */
	@ApiModelProperty(value="账户id外键")
	private Long accountId;

}