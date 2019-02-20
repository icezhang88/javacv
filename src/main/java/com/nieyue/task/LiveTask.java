package com.nieyue.task;

import com.nieyue.business.LiveBusiness;
import com.nieyue.ffch4j.handler.KeepAliveHandler;
import com.nieyue.ffch4j.util.ExecUtil;
import com.nieyue.util.SingletonHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Configuration
@EnableScheduling
public class LiveTask {
    @Autowired
    LiveBusiness liveBusiness;
    @Value("${myPugin.projectName}")
    String projectName;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    //是否锁死
    boolean isLock=false;
    HashMap<String, Object> shm= SingletonHashMap.getInstance();
    //初始化
    @PostConstruct
    void init(){
        BoundValueOperations<String, String> bvo = stringRedisTemplate.boundValueOps(projectName + "die");
        bvo.set(String.valueOf(new Date().getTime()));
    }
    //锁死服务器,1小时监测一次
    @Scheduled(fixedDelay = 3600000)
    public void lock(){
        if(isLock){
            BoundValueOperations<String, String> bvo = stringRedisTemplate.boundValueOps(projectName + "die");
            String value = bvo.get();
            if(!StringUtils.isEmpty(value)){
                //测试时间，一个月=3600000*24*30=2592000000
                if(new Date().getTime()-Long.valueOf(value)>2592000000l){
                    //System.out.println("到时了");
                    System.exit(0);
                }else{
                    //System.out.println("没到时");
                }
            }
        }
    }
    //每3秒执行一次
   @Scheduled(fixedDelay = 3000)
    public void perSeconds(){
       Object rl = shm.get("restartlive");
       if (ObjectUtils.isEmpty(rl)) {
       } else {
           Map<String,Long> map = (HashMap<String,Long>) rl;
           Iterator<Map.Entry<String, Long>> iter = map.entrySet().iterator();
           while (iter.hasNext()){
               Map.Entry<String, Long> entry = iter.next();
               //大于5秒
               if (entry.getValue() <= new Date().getTime() - 5000) {
                   //System.out.println(entry.getKey()+"准备开始重启！");
                   //把中断的任务交给保活处理器进行进一步处理
                   KeepAliveHandler.add(entry.getKey());
                   //如果在运行，停止后再重启
                   iter.remove();
               }
           }

       }
   }

}
