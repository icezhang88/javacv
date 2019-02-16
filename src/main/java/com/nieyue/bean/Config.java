package com.nieyue.bean;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 配置
 * @author yy
 *
 */
@Data
@ApiModel(value="配置",description="配置")
@TableName("config_tb")
public class Config implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 配置id
	 */
	@ApiModelProperty(value="配置id")
	@TableId("config_id")
	private Long configId;
	/**
	 * 平台名称
	 */
	@ApiModelProperty(value="平台名称")
	private String platformName;
	/**
	 * 平台联系电话
	 */
	@ApiModelProperty(value="平台联系电话")
	private String servicePhone;
	/**
	 * 平台联系qq
	 */
	@ApiModelProperty(value="平台联系qq")
	private String serviceQq;
	/**
	 * 目的基础url
	 */
	@ApiModelProperty(value="目的基础url")
	private String targetBaseUrl;
    /**
     * 目的url尾缀
     */
    @ApiModelProperty(value="目的url尾缀")
    private String targetUrlSuffix;
    /**
	 * 播放基础url
	 */
	@ApiModelProperty(value="播放基础url")
	private String playBaseUrl;
    /**
	 * 播放url尾缀
	 */
	@ApiModelProperty(value="播放url尾缀")
	private String playUrlSuffix;
    /**
	 * 播放基础url2
	 */
	@ApiModelProperty(value="播放基础url2")
	private String playBaseUrl2;
    /**
	 * 播放url尾缀2
	 */
	@ApiModelProperty(value="播放url尾缀2")
	private String playUrlSuffix2;
    /**
	 * 播放基础url3
	 */
	@ApiModelProperty(value="播放基础url3")
	private String playBaseUrl3;
    /**
	 * 播放url尾缀3
	 */
	@ApiModelProperty(value="播放url尾缀3")
	private String playUrlSuffix3;
	/**
	 * 创建时间
	 */
	@ApiModelProperty(value="创建时间",example="创建时间")
	private Date createDate;
	/**
	 * 更新时间
	 */
	@ApiModelProperty(value="更新时间",example="更新时间")
	private Date updateDate;
}
