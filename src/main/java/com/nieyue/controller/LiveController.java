package com.nieyue.controller;

import com.nieyue.bean.Live;
import com.nieyue.exception.CommonRollbackException;
import com.nieyue.service.LiveService;
import com.nieyue.util.CVUtil;
import com.nieyue.util.ResultUtil;
import com.nieyue.util.StateResultList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 直播控制类
 * @author yy
 *
 */
@Api(tags={"live"},value="直播",description="直播管理")
@RestController
@RequestMapping("/live")
public class LiveController extends BaseController<Live,Long> {
	@Resource
	private LiveService liveService;
	
	/**
	 * 直播分页浏览
	 * @param orderName 商品排序数据库字段
	 * @param orderWay 商品排序方法 asc升序 desc降序
	 * @return
	 */
	@ApiOperation(value = "直播列表", notes = "直播分页浏览")
	@ApiImplicitParams({
	  @ApiImplicitParam(name="pageNum",value="页头数位",dataType="int", paramType = "query",defaultValue="1"),
	  @ApiImplicitParam(name="pageSize",value="每页数目",dataType="int", paramType = "query",defaultValue="10"),
	  @ApiImplicitParam(name="orderName",value="排序字段",dataType="string", paramType = "query",defaultValue="updateDate"),
	  @ApiImplicitParam(name="orderWay",value="排序方式",dataType="string", paramType = "query",defaultValue="desc")
	  })
	@RequestMapping(value = "/list", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Live>> list(
			@RequestParam(value="pageNum",defaultValue="1",required=false)int pageNum,
			@RequestParam(value="pageSize",defaultValue="10",required=false) int pageSize,
			@RequestParam(value="orderName",required=false,defaultValue="updateDate") String orderName,
			@RequestParam(value="orderWay",required=false,defaultValue="desc") String orderWay)  {
			StateResultList<List<Live>> rl = super.list(pageNum, pageSize, orderName, orderWay,null);
			return rl;
	}
	/**
	 * 直播修改
	 * @return
	 */
	@ApiOperation(value = "直播修改", notes = "直播修改")
	@RequestMapping(value = "/update", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Live>> update(@ModelAttribute Live live,HttpSession session)  {
		live.setUpdateDate(new Date());
		StateResultList<List<Live>> u = super.update(live);
		return u;
	}
	/**
	 * 直播增加
	 * @return 
	 */
	@ApiOperation(value = "直播增加", notes = "直播增加")
	@RequestMapping(value = "/add", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Live>> add(@ModelAttribute Live live, HttpSession session) {
		live.setUpdateDate(new Date());
		List<Live> list = new ArrayList<Live>();
		boolean am = liveService.add(live);
		if(am){
			list.add(live);
			return ResultUtil.getSlefSRSuccessList(list);
		}
		throw new CommonRollbackException("增加失败");
	}
	/**
	 * 直播删除
	 * @return
	 */
	@ApiOperation(value = "直播删除", notes = "直播删除")
	@ApiImplicitParams({
		  @ApiImplicitParam(name="liveId",value="直播ID",dataType="long", paramType = "query",required=true)
		  })
	@RequestMapping(value = "/delete", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Live>> delete(@RequestParam("liveId") Long liveId,HttpSession session)  {
		StateResultList<List<Live>> d = super.delete(liveId);
		return d;
	}
	/**
	 * 直播浏览数量
	 * @return
	 */
	@ApiOperation(value = "直播数量", notes = "直播数量查询")
	@RequestMapping(value = "/count", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Integer>> count(HttpSession session)  {
		StateResultList<List<Integer>> c = super.count(null);
		return c;
	}
	/**
	 * 直播单个加载
	 * @return
	 */
	@ApiOperation(value = "直播单个加载", notes = "直播单个加载")
	@ApiImplicitParams({
		  @ApiImplicitParam(name="liveId",value="直播ID",dataType="long", paramType = "query",required=true)
		  })
	@RequestMapping(value = "/load", method = {RequestMethod.GET,RequestMethod.POST})
	public  StateResultList<List<Live>> loadLive(@RequestParam("liveId") Long liveId,HttpSession session)  {
		 StateResultList<List<Live>> l = super.load(liveId);
		 return l;
	}

}
