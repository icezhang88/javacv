package com.nieyue.task;

import com.nieyue.business.LiveBusiness;
import com.nieyue.service.LiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Configuration
public class LiveTask {
    @Autowired
    LiveService liveService;
    @Autowired
    LiveBusiness liveBusiness;
    final String scron="0/3 * * * * ?";


    //每3秒执行一次
   @Scheduled(cron=scron)
    public void perSeconds(){
       System.out.println(System.currentTimeMillis());


    }
}
