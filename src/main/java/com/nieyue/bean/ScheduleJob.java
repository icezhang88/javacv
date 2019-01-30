package com.nieyue.bean;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 计划任务信息
 * @author 聂跃
 * @date 2017年7月28日
 */
@Data
@ApiModel(value="计划任务",description="计划任务")
@TableName("schedule_job_tb")
public class ScheduleJob implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
    /**
     * 计划Id
     */
	@ApiModelProperty(value="计划任务id")
	@TableId("schedule_job_id")
    private Long scheduleJobId;  

    /** 
     * 任务名称 
     */
	@ApiModelProperty(value="任务名称")
    private String jobName;  
    /** 
     * 任务分组 
     */
	@ApiModelProperty(value="任务分组")
    private String jobGroup;  
    /** 
     * 任务状态 是否启动任务 
     */
	@ApiModelProperty(value="任务状态")
    private String jobStatus;  
    /** 
     * cron表达式 
     */
	@ApiModelProperty(value="cron表达式")
    private String cronExpression;  
    /** 
     * 描述 
     */
	@ApiModelProperty(value="描述")
    private String description;
    /** 
     * 任务Id (传参)
     */
	@ApiModelProperty(value="任务Id (传参)")
    private Long jobId;
    /** 
     * 任务类型 (传参)
     */
	@ApiModelProperty(value="任务类型 (传参)")
    private Integer type;
    /**
     * 任务调用的方法名 
     */
	@ApiModelProperty(value="任务调用的方法名")
    private String methodName;
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
