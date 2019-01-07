package com.nieyue.service.impl;

import com.nieyue.bean.Live;
import com.nieyue.exception.CommonRollbackException;
import com.nieyue.javacv.JavaCVRecord;
import com.nieyue.service.LiveService;
import com.nieyue.javacv.CVUtil;
import com.nieyue.util.SingletonHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
        //b= CVUtil.frameRecord(live,2);
        JavaCVRecord jcv;
        if(live.getWidth().equals("0")||live.getHeight().equals("0")){
            jcv = new JavaCVRecord(live.getSourceUrl(),live.getTargetUrl());
        }else{
            jcv = new JavaCVRecord(live.getSourceUrl(),live.getTargetUrl(),Integer.valueOf(live.getWidth()),Integer.valueOf(live.getHeight()));
        }
        try {
            jcv.stream();
        } catch (IOException e) {
            throw new CommonRollbackException("添加失败");
        }
        jcv.start();
        //成功就放入
        shm.put("JavaCVRecord"+live.getLiveId(),jcv);
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
       // b= CVUtil.stopThread(live.getLiveId());
        Object jcvo = shm.get("JavaCVRecord" + live.getLiveId());
        JavaCVRecord jcv;
        if(jcvo!=null){
            jcv=(JavaCVRecord)jcvo;
            jcv.stop();
        }
        if(live.getWidth().equals("0")||live.getHeight().equals("0")){
            jcv = new JavaCVRecord(live.getSourceUrl(),live.getTargetUrl());
        }else{
            jcv = new JavaCVRecord(live.getSourceUrl(),live.getTargetUrl(),Integer.valueOf(live.getWidth()),Integer.valueOf(live.getHeight()));
        }
        try {
            jcv.stream();
        } catch (IOException e) {
            throw new CommonRollbackException("修改失败");
        }
        jcv.start();
        //成功就放入
        shm.put("JavaCVRecord"+live.getLiveId(),jcv);
        /*if(!b){
            throw new CommonRollbackException("修改失败");
        }
         b= CVUtil.frameRecord(live,2);
        if(!b){
            throw new CommonRollbackException("修改失败");
        }*/
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
        //b= CVUtil.stopThread(olive.getLiveId());
        Object jcvo = shm.get("JavaCVRecord" +liveId);
        JavaCVRecord jcv;
        if(jcvo!=null){
            jcv=(JavaCVRecord)jcvo;
            jcv.stop();
        }
        shm.remove("JavaCVRecord");
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
          /*  b= CVUtil.frameRecord(live,2);
            if(!b){
                throw new CommonRollbackException("状态切换失败");
            }*/
            Object jcvo = shm.get("JavaCVRecord" +liveId);
            JavaCVRecord jcv;
            if(jcvo!=null){
                jcv=(JavaCVRecord)jcvo;
                jcv.start();//启动
                shm.put("JavaCVRecord" +liveId,jcv);
            }

        }else if(status==2){
            if(live.getStatus()==2){
                throw new CommonRollbackException("已经停止");
            }
            /*b= CVUtil.stopThread(live.getLiveId());
            if(!b){
                throw new CommonRollbackException("状态切换失败");
            }*/
            Object jcvo = shm.get("JavaCVRecord" +liveId);
            JavaCVRecord jcv;
            if(jcvo!=null){
                jcv=(JavaCVRecord)jcvo;
                jcv.pause();//暂停
                shm.put("JavaCVRecord" +liveId,jcv);
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
