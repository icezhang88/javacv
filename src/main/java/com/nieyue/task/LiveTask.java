package com.nieyue.task;

import com.nieyue.ffch4j.handler.KeepAliveHandler;
import com.nieyue.util.SingletonHashMap;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Configuration
@EnableScheduling
public class LiveTask {
    HashMap<String, Object> shm= SingletonHashMap.getInstance();


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
                   //把中断的任务交给保活处理器进行进一步处理
                   KeepAliveHandler.add(entry.getKey());
               }
           }

       }
   }

}
