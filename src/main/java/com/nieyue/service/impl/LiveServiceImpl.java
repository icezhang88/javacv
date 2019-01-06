package com.nieyue.service.impl;

import com.nieyue.bean.Live;
import com.nieyue.exception.CommonRollbackException;
import com.nieyue.service.LiveService;
import com.nieyue.util.CVUtil;
import com.nieyue.util.SingletonHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;

@Service
public class LiveServiceImpl extends BaseServiceImpl<Live,Long> implements LiveService {
    int timenumber=2;

    @Transactional(propagation= Propagation.REQUIRED)
    @Override
    public  boolean  add(Live live) {
        HashMap<String, Object> shm=SingletonHashMap.getInstance();
        Object ffmpegtime = shm.get("ffmpegtime");
        if(ffmpegtime!=null&&new Date(Long.valueOf(ffmpegtime.toString())).after(new Date(new Date().getTime()-1000*timenumber))){
            throw new CommonRollbackException("请求过快！");
        }else{
            shm.put("ffmpegtime",new Date().getTime());
        }
        boolean b=false;
        live.setStatus(1);//直播中
        b= CVUtil.frameRecord(live,2);
        if(!b){
            throw new CommonRollbackException("添加失败");
        }
        b = super.add(live);
        if(!b){
            throw new CommonRollbackException("添加失败");
        }
        return b;
    }
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public  boolean update(Live live) {
        HashMap<String, Object> shm = SingletonHashMap.getInstance();
        Object ffmpegtime = shm.get("ffmpegtime");
        if(ffmpegtime!=null&&new Date(Long.valueOf(ffmpegtime.toString())).after(new Date(new Date().getTime()-1000*timenumber))){
            throw new CommonRollbackException("请求过快！");
        }else{
            shm.put("ffmpegtime",new Date().getTime());
        }
        boolean b=false;
        live.setStatus(1);//直播中
        b= CVUtil.stopThread(live.getLiveId());
        if(!b){
            throw new CommonRollbackException("修改失败");
        }
         b= CVUtil.frameRecord(live,2);
        if(!b){
            throw new CommonRollbackException("修改失败");
        }
        b = super.update(live);
        if(!b){
            throw new CommonRollbackException("修改失败");
        }
        return b;
    }
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public  boolean delete(Long liveId) {
        HashMap<String, Object> shm = SingletonHashMap.getInstance();
        Object ffmpegtime = shm.get("ffmpegtime");
        if(ffmpegtime!=null&&new Date(Long.valueOf(ffmpegtime.toString())).after(new Date(new Date().getTime()-1000*timenumber))){
            throw new CommonRollbackException("请求过快！");
        }else{
            shm.put("ffmpegtime",new Date().getTime());
        }
        boolean b=false;
        Live olive = super.load(liveId);
        b= CVUtil.stopThread(olive.getLiveId());
        if(!b){
            throw new CommonRollbackException("删除失败");
        }
        b = super.delete(liveId);
        if(!b){
            throw new CommonRollbackException("删除失败");
        }
        return b;
    }
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public  boolean changeStatus(Long liveId, Integer status) {
        if(status!=2&&status!=1){
            throw new CommonRollbackException("状态切换错误");
        }
        HashMap<String, Object> shm = SingletonHashMap.getInstance();
        Object ffmpegtime = shm.get("ffmpegtime");
        if(ffmpegtime!=null&&new Date(Long.valueOf(ffmpegtime.toString())).after(new Date(new Date().getTime()-1000*timenumber))){
            throw new CommonRollbackException("请求过快！");
        }else{
            shm.put("ffmpegtime",new Date().getTime());
        }
        boolean b=false;
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
            b= CVUtil.stopThread(live.getLiveId());
            if(!b){
                throw new CommonRollbackException("状态切换失败");
            }
        }
        live.setStatus(status);
        b = super.update(live);
        if(!b){
            throw new CommonRollbackException("状态切换失败");
        }
        return b;
    }
}
