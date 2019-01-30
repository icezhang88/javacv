package com.nieyue.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.nieyue.bean.Config;
import com.nieyue.bean.Live;
import com.nieyue.business.LiveBusiness;
import com.nieyue.exception.CommonRollbackException;
import com.nieyue.service.ConfigService;
import com.nieyue.service.LiveService;
import com.nieyue.util.MyDom4jUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigServiceImpl extends BaseServiceImpl<Config,Long> implements ConfigService {
    @Autowired
    LiveService liveService;
    @Autowired
    LiveBusiness liveBusiness;

    @Override
    public boolean update(Config config) {
        config.setUpdateDate(new Date());
        boolean b = super.update(config);
        if(!b){
            throw new CommonRollbackException("修改失败");
        }
        Wrapper<Live> wrapper=new EntityWrapper<>();
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("type",2);//1手动，2自动
        wrapper.allEq(MyDom4jUtil.getNoNullMap(map));
        List<Live> livelist = liveService.simplelist(wrapper);
        livelist= liveBusiness.updateBatchLive2(livelist);
        for (int i = 0; i < livelist.size(); i++) {
            Live live = livelist.get(i);
            b=liveService.update(live);
            if(b){
                liveBusiness.restartLive(live);
            }
        }
           if(!b){
               throw new CommonRollbackException("修改失败");
           }
        return b;
    }
}
