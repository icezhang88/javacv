package com.nieyue.controller;

import com.nieyue.bean.Integral;
import com.nieyue.bean.RolePermission;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import com.nieyue.service.IntegralService;
import com.nieyue.util.MyDom4jUtil;
import com.nieyue.util.StateResultList;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 积分控制类
 * @author yy
 *
 */
@Api(tags={"integral"},value="积分",description="积分管理")
@RestController
@RequestMapping("/integral")
public class IntegralController extends BaseController<Integral,Long> {
	@Resource
	private IntegralService integralService;
	
	/**
	 * 积分分页浏览
	 * @param orderName 商品排序数据库字段
	 * @param orderWay 商品排序方法 asc升序 desc降序
	 * @return
	 */
	@ApiOperation(value = "积分列表", notes = "积分分页浏览")
	@ApiImplicitParams({
	  @ApiImplicitParam(name="accountId",value="账户id",dataType="long", paramType = "query"),
	  @ApiImplicitParam(name="pageNum",value="页头数位",dataType="int", paramType = "query",defaultValue="1"),
	  @ApiImplicitParam(name="pageSize",value="每页数目",dataType="int", paramType = "query",defaultValue="10"),
	  @ApiImplicitParam(name="orderName",value="排序字段",dataType="string", paramType = "query",defaultValue="updateDate"),
	  @ApiImplicitParam(name="orderWay",value="排序方式",dataType="string", paramType = "query",defaultValue="desc")
	  })
	@RequestMapping(value = "/list", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Integral>> list(
			@RequestParam(value="accountId",required=false)Long accountId,
			@RequestParam(value="pageNum",defaultValue="1",required=false)int pageNum,
			@RequestParam(value="pageSize",defaultValue="10",required=false) int pageSize,
			@RequestParam(value="orderName",required=false,defaultValue="updateDate") String orderName,
			@RequestParam(value="orderWay",required=false,defaultValue="desc") String orderWay)  {
		Wrapper<Integral> wrapper=new EntityWrapper<>();
		Map<String,Object> map=new HashMap<>();
		map.put("account_id", accountId);
		wrapper.allEq(MyDom4jUtil.getNoNullMap(map));
		StateResultList<List<Integral>> rl = super.list(pageNum, pageSize, orderName, orderWay,wrapper);
			return rl;
	}
	/**
	 * 积分修改
	 * @return
	 */
	@ApiOperation(value = "积分修改", notes = "积分修改")
	@RequestMapping(value = "/update", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Integral>> update(@ModelAttribute Integral integral,HttpSession session)  {
		integral.setUpdateDate(new Date());
		StateResultList<List<Integral>> u = super.update(integral);
		return u;
	}
	/**
	 * 积分增加
	 * @return 
	 */
	@ApiOperation(value = "积分增加", notes = "积分增加")
	@RequestMapping(value = "/add", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Integral>> add(@ModelAttribute Integral integral, HttpSession session) {
		integral.setUpdateDate(new Date());
		StateResultList<List<Integral>> a = super.add(integral);
		return a;
	}
	/**
	 * 积分删除
	 * @return
	 */
	@ApiOperation(value = "积分删除", notes = "积分删除")
	@ApiImplicitParams({
		  @ApiImplicitParam(name="integralId",value="积分ID",dataType="long", paramType = "query",required=true)
		  })
	@RequestMapping(value = "/delete", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Integral>> delete(@RequestParam("integralId") Long integralId,HttpSession session)  {
		StateResultList<List<Integral>> d = super.delete(integralId);
		return d;
	}
	/**
	 * 积分浏览数量
	 * @return
	 */
	@ApiOperation(value = "积分数量", notes = "积分数量查询")
	@ApiImplicitParams({
			@ApiImplicitParam(name="accountId",value="账户id",dataType="long", paramType = "query"),
	})
	@RequestMapping(value = "/count", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Integer>> count(
			@RequestParam(value="accountId",required=false)Long accountId,
			HttpSession session)  {
		Wrapper<Integral> wrapper=new EntityWrapper<>();
		Map<String,Object> map=new HashMap<>();
		map.put("account_id", accountId);
		wrapper.allEq(MyDom4jUtil.getNoNullMap(map));
		StateResultList<List<Integer>> c = super.count(wrapper);
		return c;
	}
	/**
	 * 积分单个加载
	 * @return
	 */
	@ApiOperation(value = "积分单个加载", notes = "积分单个加载")
	@ApiImplicitParams({
		  @ApiImplicitParam(name="integralId",value="积分ID",dataType="long", paramType = "query",required=true)
		  })
	@RequestMapping(value = "/load", method = {RequestMethod.GET,RequestMethod.POST})
	public  StateResultList<List<Integral>> loadIntegral(@RequestParam("integralId") Long integralId,HttpSession session)  {
		 StateResultList<List<Integral>> l = super.load(integralId);
		 return l;
	}
	
}
