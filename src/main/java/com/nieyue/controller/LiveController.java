package com.nieyue.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.nieyue.bean.Live;
import com.nieyue.business.LiveBusiness;
import com.nieyue.exception.CommonRollbackException;
import com.nieyue.exception.NotAnymoreException;
import com.nieyue.service.LiveService;
import com.nieyue.util.MyDom4jUtil;
import com.nieyue.util.NumberUtil;
import com.nieyue.util.ResultUtil;
import com.nieyue.util.StateResultList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;


/**
 * 直播控制类
 * @author yy
 *
 */
@Api(tags={"live"},value="直播",description="直播管理")
@RestController
@RequestMapping("/live")
public class LiveController extends BaseController<Live,Long> {
	@Autowired
	private LiveService liveService;
	@Autowired
	private LiveBusiness liveBusiness;

	/**
	 * 直播分页浏览
	 * @param orderName 商品排序数据库字段
	 * @param orderWay 商品排序方法 asc升序 desc降序
	 * @return
	 */
	@ApiOperation(value = "直播列表", notes = "直播分页浏览")
	@ApiImplicitParams({
	  @ApiImplicitParam(name="status",value="状态，默认1直播中，2停止，3异常停止",dataType="int", paramType = "query"),
	  @ApiImplicitParam(name="model",value="模式，1编码解码，2直接转流",dataType="int", paramType = "query"),
	  @ApiImplicitParam(name="accountId",value="直播Id",dataType="long", paramType = "query"),
	  @ApiImplicitParam(name="pageNum",value="页头数位",dataType="int", paramType = "query",defaultValue="1"),
	  @ApiImplicitParam(name="pageSize",value="每页数目",dataType="int", paramType = "query",defaultValue="10"),
	  @ApiImplicitParam(name="orderName",value="排序字段",dataType="string", paramType = "query",defaultValue="createDate"),
	  @ApiImplicitParam(name="orderWay",value="排序方式",dataType="string", paramType = "query",defaultValue="desc")
	  })
	@RequestMapping(value = "/list", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Live>> list(
			@RequestParam(value="status",required=false)Integer status,
			@RequestParam(value="model",required=false)Integer model,
			@RequestParam(value="accountId",required=false)Long accountId,
			@RequestParam(value="pageNum",defaultValue="1",required=false)int pageNum,
			@RequestParam(value="pageSize",defaultValue="10",required=false) int pageSize,
			@RequestParam(value="orderName",required=false,defaultValue="createDate") String orderName,
			@RequestParam(value="orderWay",required=false,defaultValue="desc") String orderWay)  {
			Wrapper<Live> wrapper=new EntityWrapper<>();
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("status", status);
			map.put("model", model);
			map.put("account_id", accountId);
			wrapper.allEq(MyDom4jUtil.getNoNullMap(map));
			List<Live> list = liveService.list(pageNum, pageSize, orderName, orderWay, wrapper);
		list.forEach(live->{
				live.setDuration(liveBusiness.getDuration(live.getLiveId()));
				live.setVideoBitrate(liveBusiness.getVideoBitrate(live.getLiveId()));
			});
			if(list!=null&&list.size()>0){
				return ResultUtil.getSlefSRSuccessList(list);
			}else{
				throw new NotAnymoreException();//没有更多
			}
	}
	/**
	 * 直播修改
	 * @return
	 */
	@ApiOperation(value = "直播修改", notes = "直播修改")
	@RequestMapping(value = "/update", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Live>> update(@ModelAttribute Live live,HttpSession session)  {
		live.setUpdateDate(new Date());
		List<Live> list = new ArrayList<Live>();
		boolean am = liveService.update(live);
		if(am){
			list.add(live);
			return ResultUtil.getSlefSRSuccessList(list);
		}
		throw new CommonRollbackException("修改失败");
	}
	/**
	 * 直播增加
	 * @return 
	 */
	@ApiOperation(value = "直播增加", notes = "直播增加")
	@RequestMapping(value = "/add", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Live>> add(@ModelAttribute Live live, HttpSession session) {
		live.setCreateDate(new Date());
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
		StateResultList<List<Live>> ll = load(liveId);
		boolean am = liveService.delete(liveId);
		if(am){
			return ll;
		}
		throw new CommonRollbackException("删除失败");
	}

	/**
	 * 直播浏览数量
	 * @return
	 */
	@ApiOperation(value = "直播数量", notes = "直播数量查询")
	@ApiImplicitParams({
			@ApiImplicitParam(name="status",value="状态，默认1直播中，2停止，3异常停止",dataType="int", paramType = "query"),
			@ApiImplicitParam(name="model",value="模式，1编码解码，2直接转流",dataType="int", paramType = "query"),
			@ApiImplicitParam(name="accountId",value="直播Id",dataType="long", paramType = "query"),
	})
	@RequestMapping(value = "/count", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Integer>> count(
			@RequestParam(value="status",required=false)Integer status,
			@RequestParam(value="model",required=false)Integer model,
			@RequestParam(value="accountId",required=false)Long accountId,
			HttpSession session)  {
		Wrapper<Live> wrapper=new EntityWrapper<>();
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("status", status);
		map.put("model", model);
		map.put("account_id", accountId);
		wrapper.allEq(MyDom4jUtil.getNoNullMap(map));
		StateResultList<List<Integer>> c = super.count(wrapper);
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
	/**
	 * 切换状态，直播中，停止
	 * @return
	 */
	@ApiOperation(value = "切换状态，直播中，停止", notes = "切换状态，直播中，停止")
	@ApiImplicitParams({
			@ApiImplicitParam(name="status",value="状态，默认1直播中，2停止",dataType="int", paramType = "query"),
		    @ApiImplicitParam(name="liveId",value="直播ID",dataType="long", paramType = "query",required=true)
		  })
	@RequestMapping(value = "/changeStatus", method = {RequestMethod.GET,RequestMethod.POST})
	public  StateResultList<List<Live>> changeStatus(
			@RequestParam("liveId") Long liveId,
			@RequestParam("status") Integer status,
			HttpSession session)  {
		StateResultList<List<Live>> ll = load(liveId);
		boolean b = liveService.changeStatus(liveId,status);
		if(b){
			return ll;
		}
		throw new CommonRollbackException("切换失败");
	}
	/**
	 * 直播批量切换
	 * @return
	 */
	@ApiOperation(value = "直播批量切换", notes = "直播批量切换")
	@ApiImplicitParams({
			@ApiImplicitParam(name="status",value="状态，默认1直播中，2停止",dataType="int", paramType = "query"),
			@ApiImplicitParam(name="type",value="类型,1手动生成，2自动生成",dataType="int", paramType = "query"),
			@ApiImplicitParam(name="liveIds",value="直播ID集合数组，\"22,33,44,53,3\"",dataType="string", paramType = "query",required=true)
	})
	@RequestMapping(value = "/changeStatusBatch", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Live>> changeStatusBatch(
			@RequestParam("liveIds") String liveIds,
			@RequestParam(value="type",required = false) Integer type,
			@RequestParam("status") Integer status,
			HttpSession session)  {
		boolean dm=false;
		//全部
		if(liveIds.equals("all")){
			Wrapper<Live> wrapper=new EntityWrapper<>();
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("type", type);
			map.put("status", status==1?2:1);
			wrapper.allEq(MyDom4jUtil.getNoNullMap(map));
			List<Live> ll = liveService.simplelist(wrapper);
			for (int i = 0; i < ll.size(); i++) {
				dm= liveService.changeStatus(ll.get(i).getLiveId(),status);
			}
		}else{
			String[] lds = liveIds.replace(" ","").split(",");
			for (int i = 0; i < lds.length; i++) {
				if(!NumberUtil.isNumeric(lds[i])){
					throw new CommonRollbackException("参数错误");
				}
			}
			for (int i = 0; i < lds.length; i++) {
				dm= liveService.changeStatus(new Long(lds[i]),status);
			}
		}
		if(dm){
			List<Live> list = new ArrayList<>();
			list.add(new Live());
			return ResultUtil.getSlefSRSuccessList(list);
		}
		return ResultUtil.getSlefSRFailList(null);
	}
}
