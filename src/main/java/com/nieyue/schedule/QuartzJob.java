package com.nieyue.schedule;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.nieyue.bean.Live;
import com.nieyue.business.LiveBusiness;
import com.nieyue.dao.LiveDao;
import com.nieyue.service.BaseService;
import com.nieyue.service.LiveService;
import com.nieyue.service.impl.BaseServiceImpl;
import com.nieyue.util.MyDom4jUtil;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuartzJob implements Job{
	@Autowired
	RedisLock redisLock;
	@Autowired
	LiveService liveService;
	@Autowired
	LiveBusiness liveBusiness;


	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
			try {
				JobDataMap jdm = arg0.getJobDetail().getJobDataMap();
				int type=(int) jdm.get("type");
				Long jobId=(Long) jdm.get("jobId");
				//分布式锁
				if(redisLock.checkStatus(type,jobId)){
					//System.out.println("jobId"+jdm.get("jobId")+new Date().toLocaleString());
					if(type==1){
						List<Live> ll=new ArrayList<>();
						if(jobId.equals(0l)){
							System.out.println(jobId);
							//所有的
							Wrapper<Live> wrapper=new EntityWrapper<>();
							Map<String,Object> map=new HashMap<String,Object>();
							map.put("type", 2);//类型,1手动生成，2自动生成
							wrapper.allEq(MyDom4jUtil.getNoNullMap(map));
							ll = liveService.simplelist(wrapper);
						}else{
							Live live = liveService.load(jobId);
							if(!ObjectUtils.isEmpty(live)){
								ll.add(live);
							}
						}
						List<Live> nll = liveBusiness.updateBatchLive2(ll);
						boolean b=true;
						for (int i = 0; i < ll.size(); i++) {
							Live live = ll.get(i);
							b=liveService.update2(live);
							if(b&&live.getStatus().equals(1)){
								liveBusiness.restartLive(live);
							}
						}
					}
				}


			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
