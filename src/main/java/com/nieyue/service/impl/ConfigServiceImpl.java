package com.nieyue.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.nieyue.bean.Config;
import com.nieyue.bean.Live;
import com.nieyue.business.LiveBusiness;
import com.nieyue.business.SendLiveBusiness;
import com.nieyue.exception.CommonRollbackException;
import com.nieyue.service.ConfigService;
import com.nieyue.service.LiveService;
import com.nieyue.util.MyDom4jUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class ConfigServiceImpl extends BaseServiceImpl<Config,Long> implements ConfigService {
    @Autowired
    LiveService liveService;
    @Autowired
    LiveBusiness liveBusiness;
    @Autowired
    SendLiveBusiness sendLiveBusiness;

    @Transactional(propagation= Propagation.REQUIRED)
    @Override
    public boolean update(Config config) {
        config.setUpdateDate(new Date());
        if(StringUtils.isEmpty(config.getTargetUrlSuffix())){
            config.setTargetUrlSuffix(" ");
        }
        if(StringUtils.isEmpty(config.getPlayBaseUrl())){
            config.setPlayBaseUrl(" ");
        }
        if(StringUtils.isEmpty(config.getPlayBaseUrl2())){
            config.setPlayBaseUrl2(" ");
        }
        if(StringUtils.isEmpty(config.getPlayBaseUrl3())){
            config.setPlayBaseUrl3(" ");
        }
        if(StringUtils.isEmpty(config.getPlayUrlSuffix())){
            config.setPlayUrlSuffix(" ");
        }
        if(StringUtils.isEmpty(config.getPlayUrlSuffix2())){
            config.setPlayUrlSuffix2(" ");
        }
        if(StringUtils.isEmpty(config.getPlayUrlSuffix3())){
            config.setPlayUrlSuffix3(" ");
        }
        boolean b = super.update(config);
        if(!b){
            throw new CommonRollbackException("修改失败");
        }
        Wrapper<Live> wrapper=new EntityWrapper<>();
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("type",2);//1手动，2自动
        wrapper.allEq(MyDom4jUtil.getNoNullMap(map));
        List<Live> livelist = liveService.simplelist(wrapper);
        List<Config> configList=new ArrayList<>();
        configList.add(config);
        livelist= liveBusiness.updateBatchLive2(livelist,configList);
        for (int i = 0; i < livelist.size(); i++) {
            Live live = livelist.get(i);
            b=liveService.update(live);
        }
           if(!b){
               throw new CommonRollbackException("修改失败");
           }
         //发送直播
        sendLiveBusiness.send();
        return b;
    }
}
