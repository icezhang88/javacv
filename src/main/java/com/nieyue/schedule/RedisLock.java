package com.nieyue.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
/**
 *  使用redis作为任务调度中心，采用了redis的自动过期与分布式锁特性
 *	每个服务的ip加项目名作为每台服务的唯一别名
 *	通过redis中对应key值中的value来判定执行的是哪台服务: 如redis中key为 projectName+"quartz:"+jobGroup+jobName, value为192.168.1.187. 意为项目projectName+"quartz:"+jobGroup+jobName当前执行任务的节点为192.168.1.187这台机器上的服务
 *	每次执行任务之前判定下redis中projectName+"quartz:"+jobGroup+jobName是否为空，如果为空，则设置当前ip进去，设置一定时间的有效期，并执行定时任务；如果不为空，判断是否与本机ip相同，相同则执行定时任务，否则跳过
 *	设置有效期是为了某台机器发生故障时能进行故障转移
 * @author 聂跃
 * @date 2018年7月13日
 */
@Configuration
public class RedisLock {
	@Autowired
	StringRedisTemplate stringRedisTemplate;
	@Value("&{muPugin.projectName}")
	String projectName;
	/**
	 * 针对工作组与工作名的检测状态
	 * @return
	 */
	public boolean checkStatus(Integer type,Long jobId){
        String lockKey = projectName+"quartz:"+"type"+type+"jobId"+jobId;
        try {
        	//String ip = "8001";
        	//本地ip地址
            String ip = InetAddress.getLocalHost().getHostAddress();
            // 获取服务器上的工作ip
            BoundValueOperations<String, String> srt = stringRedisTemplate.boundValueOps(lockKey);
              Boolean b = srt.setIfAbsent(ip);//不存在就存储，存在则不存储
            // 如果为空的时候，设置进去
            if(b != null&&b){
            	srt.expire(1, TimeUnit.SECONDS);
                return true;
            }
            String currentIp = srt.get();
            // 就是当前机器，则返回true
            if(currentIp.equals(ip)){
                return true;
            }else{
                return false;
            }
        } catch (Exception e) {
            return false;
        } 
    }
	public static void main(String[] args) throws UnknownHostException {
		String ip = InetAddress.getLocalHost().getHostAddress();
		System.out.println(ip);
	}
	
}
