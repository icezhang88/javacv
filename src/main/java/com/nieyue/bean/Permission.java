package com.nieyue.bean;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 权限
 * @author yy
 *
 */
@Data
@ApiModel(value="权限",description="权限")
@TableName(value="permission_tb")
public class Permission implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 权限id
     */
    @ApiModelProperty(value="权限id")
    @TableId(value="permission_id")
    private Long permissionId;

    /**
     * 权限类型，默认0开放，1鉴权
     */
    @ApiModelProperty(value="权限类型，默认0开放，1鉴权")
    private Integer type;
    /**
     * 权限管理名
     */
    @ApiModelProperty(value="权限管理名")
    private String managerName;
    /**
     * 权限名
     */
    @ApiModelProperty(value="权限名")
    private String name;
    /**
     * 权限路由
     */
    @ApiModelProperty(value="权限路由")
    private String route;
    /**
     * 权限更新时间
     */
    @ApiModelProperty(value="权限更新时间")
    private Date updateDate;


}
