package com.nieyue.ffch4j.handler;

import com.nieyue.business.LiveBusiness;
import com.nieyue.util.SingletonHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认任务消息输出处理
 * @version 2017年10月13日
 */
public class DefaultOutHandlerMethod implements OutHandlerMethod{
	@Autowired
	LiveBusiness liveBusiness;

	/**
	 * 任务是否异常中断，如果
	 */
	public boolean isb=false;
	Map<String,Object> shm=SingletonHashMap.getInstance();
	@Override
	public void parse(String id,String msg) {
		//过滤消息
		if (msg.indexOf("fail") != -1) {
			System.err.println(id + "任务可能发生故障：" + msg);
			System.err.println("失败，设置中断状态");
			isb=true;
		}else if(msg.indexOf("miss")!= -1) {
			System.err.println(id + "任务可能发生丢包：" + msg);
			System.err.println("失败，设置中断状态");
			isb=true;
		}else {
			isb=false;
			//System.err.println(id + "消息：" + msg);
			//重启数据
			Object rl = shm.get("restartlive");
			Map<String,Long> map;
			if(ObjectUtils.isEmpty(rl)){
				map = new HashMap<>();
			}else{
				map = (HashMap<String,Long>) rl;
			}
			map.put(id,new Date().getTime());
			shm.put("restartlive",map);

			//msg放入
			Object lmo = shm.get("liveMsg");
			Map<String,String> map2;
			if(ObjectUtils.isEmpty(lmo)){
				map2 = new HashMap<>();
			}else{
				map2 = (HashMap<String,String>) lmo;
			}
			if(msg.indexOf("bitrate=")<=-1){
				return;
			}
			map2.put(id,msg);
			shm.put("liveMsg",map2);
		}

	}

	@Override
	public boolean isbroken() {
		return isb;
	}
	
}
