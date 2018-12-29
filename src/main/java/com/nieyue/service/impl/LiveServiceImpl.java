package com.nieyue.service.impl;

import com.nieyue.bean.Live;
import com.nieyue.exception.CommonRollbackException;
import com.nieyue.service.LiveService;
import com.nieyue.util.CVUtil;
import org.springframework.stereotype.Service;

@Service
public class LiveServiceImpl extends BaseServiceImpl<Live,Long> implements LiveService {
    @Override
    public boolean add(Live live) {
        boolean b = super.add(live);
        if(!b){
            throw new CommonRollbackException("添加失败");
        }
        try {
           b= CVUtil.frameRecord(live.getSourceUrl(),live.getTargetUrl(),live.getWidth(),live.getHeight(),2);
        } catch (Exception e) {
            throw new CommonRollbackException("直播参数错误");
        }
        return b;
    }
}
