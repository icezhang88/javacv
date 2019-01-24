package com.nieyue.ffch4j.handler;

import com.nieyue.bean.Live;
import com.nieyue.util.SingletonHashMap;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 默认任务消息输出处理
 * @author eguid
 * @since jdk1.7
 * @version 2017年10月13日
 */
public class DefaultOutHandlerMethod implements OutHandlerMethod{

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

		}

	}

	@Override
	public boolean isbroken() {
		return isb;
	}
	
}
