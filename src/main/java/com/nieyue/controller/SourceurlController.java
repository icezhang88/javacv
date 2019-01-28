package com.nieyue.controller;

import com.nieyue.bean.Sourceurl;
import com.nieyue.exception.CommonRollbackException;
import com.nieyue.service.SourceurlService;
import com.nieyue.util.ResultUtil;
import com.nieyue.util.StateResultList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 来源url控制类
 * @author yy
 *
 */
@Api(tags={"sourceurl"},value="来源url",description="来源url管理")
@RestController
@RequestMapping("/sourceurl")
public class SourceurlController extends BaseController<Sourceurl,Long> {
	@Resource
	private SourceurlService sourceurlService;
	
	/**
	 * 来源url分页浏览
	 * @param orderName 商品排序数据库字段
	 * @param orderWay 商品排序方法 asc升序 desc降序
	 * @return
	 */
	@ApiOperation(value = "来源url列表", notes = "角色分页浏览")
	@ApiImplicitParams({
	  @ApiImplicitParam(name="pageNum",value="页头数位",dataType="int", paramType = "query",defaultValue="1"),
	  @ApiImplicitParam(name="pageSize",value="每页数目",dataType="int", paramType = "query",defaultValue="10"),
	  @ApiImplicitParam(name="orderName",value="排序字段",dataType="string", paramType = "query",defaultValue="updateDate"),
	  @ApiImplicitParam(name="orderWay",value="排序方式",dataType="string", paramType = "query",defaultValue="desc")
	  })
	@RequestMapping(value = "/list", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Sourceurl>> list(
			@RequestParam(value="pageNum",defaultValue="1",required=false)int pageNum,
			@RequestParam(value="pageSize",defaultValue="10",required=false) int pageSize,
			@RequestParam(value="orderName",required=false,defaultValue="updateDate") String orderName,
			@RequestParam(value="orderWay",required=false,defaultValue="desc") String orderWay)  {
			StateResultList<List<Sourceurl>> rl = super.list(pageNum, pageSize, orderName, orderWay,null);
			return rl;
	}
	/**
	 * 来源url修改
	 * @return
	 */
	@ApiOperation(value = "来源url修改", notes = "来源url修改")
	@RequestMapping(value = "/update", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Sourceurl>> update(@ModelAttribute Sourceurl sourceurl,HttpSession session)  {
		sourceurl.setUpdateDate(new Date());
		StateResultList<List<Sourceurl>> u = super.update(sourceurl);
		return u;
	}
	/**
	 * 来源url增加
	 * @return 
	 */
	@ApiOperation(value = "来源url增加", notes = "来源url增加")
	@RequestMapping(value = "/add", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Sourceurl>> add(@ModelAttribute Sourceurl sourceurl, HttpSession session) {
		sourceurl.setCreateDate(new Date());
		sourceurl.setUpdateDate(new Date());
		StateResultList<List<Sourceurl>> a = super.add(sourceurl);
		return a;
	}
	/**
	 * 来源url删除
	 * @return
	 */
	@ApiOperation(value = "来源url删除", notes = "来源url删除")
	@ApiImplicitParams({
		  @ApiImplicitParam(name="sourceurlId",value="来源urlID",dataType="long", paramType = "query",required=true)
		  })
	@RequestMapping(value = "/delete", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Sourceurl>> delete(@RequestParam("sourceurlId") Long sourceurlId,HttpSession session)  {
		StateResultList<List<Sourceurl>> d = super.delete(sourceurlId);
		return d;
	}
	/**
	 * 来源url浏览数量
	 * @return
	 */
	@ApiOperation(value = "来源url数量", notes = "来源url数量查询")
	@RequestMapping(value = "/count", method = {RequestMethod.GET,RequestMethod.POST})
	public @ResponseBody StateResultList<List<Integer>> count(HttpSession session)  {
		StateResultList<List<Integer>> c = super.count(null);
		return c;
	}
	/**
	 * 来源url单个加载
	 * @return
	 */
	@ApiOperation(value = "来源url单个加载", notes = "来源url单个加载")
	@ApiImplicitParams({
		  @ApiImplicitParam(name="sourceurlId",value="来源urlID",dataType="long", paramType = "query",required=true)
		  })
	@RequestMapping(value = "/load", method = {RequestMethod.GET,RequestMethod.POST})
	public  StateResultList<List<Sourceurl>> loadSourceurl(@RequestParam("sourceurlId") Long sourceurlId,HttpSession session)  {
		 StateResultList<List<Sourceurl>> l = super.load(sourceurlId);
		 return l;
	}
	/**
	 * 来源url批量上传txt
	 * @return
	 */
	@ApiOperation(value = "来源url批量上传txt", notes = "来源url批量上传txt")
	@ApiImplicitParams({
		  @ApiImplicitParam(name="editorUpload",value="上传参数",dataType="file", paramType = "query",required=true)
		  })
	@RequestMapping(value = "/uploader", method = {RequestMethod.GET,RequestMethod.POST})
	public  StateResultList<List<Sourceurl>> uploader(
			@RequestParam("editorUpload") MultipartFile file,
			HttpSession session)   {
		List<Sourceurl> sl = new ArrayList<>();
		try {
			InputStream fis = file.getInputStream();

			InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				if(StringUtils.isEmpty(line)){
					continue;
				}
				String split="";
				if(line.indexOf(",")>-1){
					split=",";
				}else if(line.indexOf("，")>-1){
					split="，";
				}else{
					throw new CommonRollbackException();
				}
				String[] linesplit = line.split(split);
				Sourceurl sourceurl = new Sourceurl();
				if (!StringUtils.isEmpty(linesplit[0])) {
					sourceurl.setName(linesplit[0]);
				}
				if (!StringUtils.isEmpty(linesplit[1])) {
					sourceurl.setUrl(linesplit[1]);
				}
				sourceurl.setCreateDate(new Date());
				sourceurl.setUpdateDate(new Date());
				sl.add(sourceurl);
				//buf=new byte[1024];//重新生成，避免和上次读取的数据重复
			}
			isr.close();
			fis.close();
		}catch (Exception e){
			throw new CommonRollbackException("上传错误，请检查");
		}
		boolean b=false;
		for (int i = 0; i <sl.size() ; i++) {
			b = sourceurlService.add(sl.get(i));
		}
		if(b){
			return ResultUtil.getSlefSRSuccessList(sl);
		}
		throw new CommonRollbackException("增加失败");
	}

}
