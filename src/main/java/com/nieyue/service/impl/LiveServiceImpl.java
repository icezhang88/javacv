package com.nieyue.service.impl;

import com.nieyue.bean.Live;
import com.nieyue.exception.CommonRollbackException;
import com.nieyue.service.LiveService;
import com.nieyue.util.CVUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class LiveServiceImpl extends BaseServiceImpl<Live,Long> implements LiveService {
    volatile ConcurrentHashMap chm=new ConcurrentHashMap();

    @Transactional(propagation= Propagation.REQUIRED)
    @Override
    public  boolean  add(Live live) {
        if(chm.get("addlive")!=null){
            throw new CommonRollbackException("请不要频繁操作");
        }else{
            chm.put("addlive","1");
        }
        boolean b=false;
        try{
            live.setStatus(1);//直播中
            b=CVUtil.frameRecord(live,2);
            if(!b){
                throw new CommonRollbackException("添加失败");
            }
            b = super.add(live);
            if(!b){
                throw new CommonRollbackException("添加失败");
            }
        }finally{
            chm.remove("addlive");
        }

        return b;
    }
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public  boolean update(Live live) {
        if(chm.get("updatelive")!=null){
            throw new CommonRollbackException("请不要频繁操作");
        }else{
            chm.put("updatelive","1");
        }
        boolean b=false;
        try{
            live.setStatus(1);//直播中
            b=CVUtil.stopThread(live.getLiveId());
            if(!b){
                throw new CommonRollbackException("修改失败");
            }
             b=CVUtil.frameRecord(live,2);
            if(!b){
                throw new CommonRollbackException("修改失败");
            }
            b = super.update(live);
            if(!b){
                throw new CommonRollbackException("修改失败");
            }
        }finally{
            chm.remove("updatelive");
        }
        return b;
    }
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public  boolean delete(Long liveId) {
        if(chm.get("deletelive")!=null){
            throw new CommonRollbackException("请不要频繁操作");
        }else{
            chm.put("deletelive","1");
        }
        boolean b=false;
        try{
            Live olive = super.load(liveId);
            b=CVUtil.stopThread(olive.getLiveId());
            if(!b){
                throw new CommonRollbackException("删除失败");
            }
            b = super.delete(liveId);
            if(!b){
                throw new CommonRollbackException("删除失败");
            }
        }finally{
            chm.remove("deletelive");
        }
        return b;
    }
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public  boolean changeStatus(Long liveId, Integer status) {
        if(status!=2&&status!=1){
            throw new CommonRollbackException("状态切换错误");
        }
        if(chm.get("changeStatuslive")!=null){
            throw new CommonRollbackException("请不要频繁操作");
        }else{
            chm.put("changeStatuslive","1");
        }
        boolean b=false;
        try{
            Live live = super.load(liveId);
            if(live==null){
                throw new CommonRollbackException("直播不存在");
            }

            if(status==1){
                if(live.getStatus()==1){
                    throw new CommonRollbackException("已经在直播");
                }
                b= CVUtil.frameRecord(live,2);
                if(!b){
                    throw new CommonRollbackException("状态切换失败");
                }
            }else if(status==2){
                if(live.getStatus()==2){
                    throw new CommonRollbackException("已经停止");
                }
                b=CVUtil.stopThread(live.getLiveId());
                if(!b){
                    throw new CommonRollbackException("状态切换失败");
                }
            }
            live.setStatus(status);
            b = super.update(live);
            if(!b){
                throw new CommonRollbackException("状态切换失败");
            }
        }finally{
            chm.remove("changeStatuslive");
        }
        return b;
    }
}
