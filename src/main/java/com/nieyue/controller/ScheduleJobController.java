package com.nieyue.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.nieyue.bean.ScheduleJob;
import com.nieyue.exception.CommonRollbackException;
import com.nieyue.exception.NotAnymoreException;
import com.nieyue.exception.NotIsNotExistException;
import com.nieyue.schedule.QuartzEventService;
import com.nieyue.service.ScheduleJobService;
import com.nieyue.util.MyDom4jUtil;
import com.nieyue.util.ResultUtil;
import com.nieyue.util.StateResultList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;


/**
 * 计划任务控制类
 * @author yy
 *
 */
@Api(tags={"scheduleJob"},value="计划任务",description="计划任务管理")
@RestController
@RequestMapping("/scheduleJob")
public class ScheduleJobController extends BaseController<ScheduleJob,Long> {
	@Resource
	private ScheduleJobService scheduleJobService;
	@Autowired
	QuartzEventService quartzEventService;
	/**
	 * 计划任务分页浏览
	 * @param orderName 商品排序数据库字段
	 * @param orderWay 商品排序方法 asc升序 desc降序
	 * @return
	 */
	@ApiOperation(value = "计划任务列表", notes = "计划任务分页浏览")
	@ApiImplicitParams({
			@ApiImplicitParam(name="jobId",value="业务id",dataType="long", paramType = "query"),
			@ApiImplicitParam(name="type",value="类型，1自动直播换源",dataType="int", paramType = "query"),
	  		@ApiImplicitParam(name="pageNum",value="页头数位",dataType="int", paramType = "query",defaultValue="1"),
	  		@ApiImplicitParam(name="pageSize",value="每页数目",dataType="int", paramType = "query",defaultValue="10"),
	  		@ApiImplicitParam(name="orderName",value="排序字段",dataType="string", paramType = "query",defaultValue="updateDate"),
	  		@ApiImplicitParam(name="orderWay",value="排序方式",dataType="string", paramType = "query",defaultValue="desc")
	  })
	@RequestMapping(value = "/list", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<ScheduleJob>> list(
			@RequestParam(value="jobId",required=false)Long jobId,
			@RequestParam(value="type",required=false)Integer type,
			@RequestParam(value="pageNum",defaultValue="1",required=false)int pageNum,
			@RequestParam(value="pageSize",defaultValue="10",required=false) int pageSize,
			@RequestParam(value="orderName",required=false,defaultValue="updateDate") String orderName,
			@RequestParam(value="orderWay",required=false,defaultValue="desc") String orderWay)  {
			StateResultList<List<ScheduleJob>> rl = super.list(pageNum, pageSize, orderName, orderWay,null);
			Wrapper<ScheduleJob> wrapper=new EntityWrapper<>();
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("job_id", jobId);
			map.put("type", type);
			wrapper.allEq(MyDom4jUtil.getNoNullMap(map));
			List<ScheduleJob> list = scheduleJobService.list(pageNum,pageSize,orderName,orderWay,wrapper);
			if(list!=null&&list.size()>0){
				return ResultUtil.getSlefSRSuccessList(list);
			}else{
				throw new NotAnymoreException();//没有更多
			}
	}
	/**
	 * 计划任务修改
	 * @return
	 */
	@ApiOperation(value = "计划任务修改", notes = "计划任务修改")
	@RequestMapping(value = "/update", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<ScheduleJob>> update(@ModelAttribute ScheduleJob scheduleJob,HttpSession session)  {
		scheduleJob.setUpdateDate(new Date());
		boolean um = scheduleJobService.update(scheduleJob);
		if(um){
			List<ScheduleJob> list = new ArrayList<>();
			list.add(scheduleJob);
			return ResultUtil.getSlefSRSuccessList(list);
		}
		throw new CommonRollbackException("修改失败");
	}
	/**
	 * 计划任务增加
	 * @return 
	 */
	@ApiOperation(value = "计划任务增加", notes = "计划任务增加")
	@RequestMapping(value = "/add", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<ScheduleJob>> add(@ModelAttribute ScheduleJob scheduleJob, HttpSession session) {
		scheduleJob.setCreateDate(new Date());
		scheduleJob.setUpdateDate(new Date());
		boolean am = scheduleJobService.add(scheduleJob);
		if(am){
			List<ScheduleJob> list = new ArrayList<>();
			list.add(scheduleJob);
			return ResultUtil.getSlefSRSuccessList(list);
		}
		throw new CommonRollbackException("增加失败");
	}
	/**
	 * 计划任务删除
	 * @return
	 */
	@ApiOperation(value = "计划任务删除", notes = "计划任务删除")
	@ApiImplicitParams({
		  @ApiImplicitParam(name="scheduleJobId",value="计划任务ID",dataType="long", paramType = "query",required=true)
		  })
	@RequestMapping(value = "/delete", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<ScheduleJob>> delete(@RequestParam("scheduleJobId") Long scheduleJobId,HttpSession session)  {
		boolean dm = scheduleJobService.delete(scheduleJobId);
		if(dm){
			List<ScheduleJob> list = new ArrayList<>();
			list.add(new ScheduleJob());
			ScheduleJob scheduleJob = scheduleJobService.load(scheduleJobId);
			return ResultUtil.getSlefSRSuccessList(list);
		}
		throw new CommonRollbackException("删除失败");
	}
	/**
	 * 计划任务浏览数量
	 * @return
	 */
	@ApiOperation(value = "计划任务数量", notes = "计划任务数量查询")
	@ApiImplicitParams({
			@ApiImplicitParam(name="jobId",value="业务id",dataType="long", paramType = "query"),
			@ApiImplicitParam(name="type",value="类型，1自动直播换源",dataType="int", paramType = "query"),
	})
	@RequestMapping(value = "/count", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Integer>> count(
			@RequestParam(value="jobId",required=false) Long jobId,
			@RequestParam(value="type",required=false) Integer type,
			HttpSession session)  {
		Wrapper<ScheduleJob> wrapper=new EntityWrapper<>();
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("job_id", jobId);
		map.put("type", type);
		wrapper.allEq(MyDom4jUtil.getNoNullMap(map));
		int count = scheduleJobService.count(wrapper);
		if(count>=0){
			List<Integer> list = new ArrayList<Integer>();
			list.add(count);
			return ResultUtil.getSlefSRSuccessList(list);
		}else{
			throw new NotIsNotExistException("");//不存在
		}
	}
	/**
	 * 计划任务单个加载
	 * @return
	 */
	@ApiOperation(value = "计划任务单个加载", notes = "计划任务单个加载")
	@ApiImplicitParams({
		  @ApiImplicitParam(name="scheduleJobId",value="计划任务ID",dataType="long", paramType = "query",required=true)
		  })
	@RequestMapping(value = "/load", method = {RequestMethod.GET,RequestMethod.POST})
	public  StateResultList<List<ScheduleJob>> loadScheduleJob(@RequestParam("scheduleJobId") Long scheduleJobId,HttpSession session)  {
		ScheduleJob scheduleJob = scheduleJobService.load(scheduleJobId);
		if(scheduleJob!=null &&!scheduleJob.equals("")){
			List<ScheduleJob> list = new ArrayList<>();
			list.add(scheduleJob);
			return ResultUtil.getSlefSRSuccessList(list);
		}else{
			throw new NotIsNotExistException("");//不存在
		}
	}

	/**
	 *暂停
	 * @return
	 * @param scheduleJobId 不存在则全部转状态
	 * @throws SchedulerException
	 */
	@ApiOperation(value = "工作计划列表", notes = "工作计划分页浏览")
	@ApiImplicitParams({
			@ApiImplicitParam(name="scheduleJobId",value="工作计划ID",dataType="long", paramType = "query"),
			@ApiImplicitParam(name="jobStatus",value="状态",dataType="string", paramType = "query"),
	})
	@RequestMapping(value={"/turn"})
	public StateResultList<List<ScheduleJob>> turnScheduleJob(
			HttpSession session,
			@RequestParam(value="scheduleJobId",required=false) Long scheduleJobId,
			@RequestParam("jobStatus") String jobStatus,
			HttpServletResponse response
	) throws SchedulerException {
		if(scheduleJobId==null||scheduleJobId.equals("")){
			List<ScheduleJob> l =scheduleJobService.simplelist(null);
			l.forEach((sj)->{
				sj.setJobStatus(jobStatus);
				scheduleJobService.update(sj);
			});
			return ResultUtil.getSlefSRSuccessList(l);
		}
		ScheduleJob sj = scheduleJobService.load(scheduleJobId);
		sj.setJobStatus(jobStatus);
		boolean um = scheduleJobService.update(sj);
		if(um){
			List<ScheduleJob> list = new ArrayList<>();
			list.add(sj);
			return ResultUtil.getSlefSRSuccessList(list);
		}
		throw new CommonRollbackException("修改失败");

	}
	
}
