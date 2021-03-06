package com.nieyue.bean;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 直播
 * @author 聂跃
 * @date 2017年4月12日
 */
@Data
@ApiModel(value="直播",description="直播")
@TableName("live_tb")
public class Live implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 直播id
     */
    @ApiModelProperty(value="直播id")
    @TableId("live_id")
    private Long liveId;
    /**
     * 名称
     */
    @ApiModelProperty(value="名称")
    private String name;
    /**
     * 类型,1手动生成，2自动生成
     */
    @ApiModelProperty(value="类型,1手动生成，2自动生成")
    private Integer type;
    /**
     * 来源url
     */
    @ApiModelProperty(value="来源url")
    private String sourceUrl;
    /**
     * 目的url
     */
    @ApiModelProperty(value="目的url")
    private String targetUrl;
    /**
     * 播放url
     */
    @ApiModelProperty(value="播放url")
    private String playUrl;
    /**
     * 播放url2
     */
    @ApiModelProperty(value="播放url2")
    private String playUrl2;
    /**
     * 播放url3
     */
    @ApiModelProperty(value="播放url3")
    private String playUrl3;
    /**
     * 宽
     */
    @ApiModelProperty(value="宽")
    private Integer width;
    /**
     * 高
     */
    @ApiModelProperty(value="高")
    private Integer height;
    /**
     * 状态，默认1直播中，2停止，3异常停止
     */
    @ApiModelProperty(value="状态，默认1直播中，2停止，3异常停止")
    private Integer status;
    /**
     * 模式，1编码解码，2直接转流，3音频转acc
     */
    @ApiModelProperty(value="模式，1编码解码，2直接转流，3音频转acc")
    private Integer model;
    /**
     * 码率
     */
    @ApiModelProperty(value="码率")
    private String videoBitrate;
    /**
     * 时长
     */
    @ApiModelProperty(value="时长")
    private String duration;
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
