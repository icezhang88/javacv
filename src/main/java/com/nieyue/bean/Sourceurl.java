package com.nieyue.bean;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 来源url
 * @author yy
 *
 */
@Data
@ApiModel(value="来源url",description="来源url")
@TableName("sourceurl_tb")
public class Sourceurl implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 来源urlid
     */
    @ApiModelProperty(value="来源urlid")
    @TableId("sourceurl_id")
    private Long sourceurlId;

    /**
     * 名称
     */
    @ApiModelProperty(value="名称")
    private String name;
    /**
     * 来源url
     */
    @ApiModelProperty(value="来源url")
    private String url;
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


}



