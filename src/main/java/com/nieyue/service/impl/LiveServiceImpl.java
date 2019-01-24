package com.nieyue.service.impl;

import com.nieyue.bean.Live;
import com.nieyue.business.LiveBusiness;
import com.nieyue.exception.CommonRollbackException;
import com.nieyue.service.LiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LiveServiceImpl extends BaseServiceImpl<Live,Long> implements LiveService {
    @Autowired
    LiveBusiness liveBusiness;
    @Transactional(propagation= Propagation.REQUIRED)
    @Override
    public  boolean  add(Live live) {
        boolean b=false;
        live.setStatus(1);//直播中
        /*JavaCVRecord jcv= new JavaCVRecord(live);
        jcv.stream();
        jcv.start();
        //成功就放入
        shm.put("JavaCVRecord"+live.getLiveId(),jcv);*/

        b = super.add(live);
        if(!b){
            throw new CommonRollbackException("添加失败");
        }
        liveBusiness.startLive(live);
        return b;
    }
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public  boolean update(Live live) {
        boolean b=false;
        live.setStatus(1);//直播中
        /*Object jcvo = shm.get("JavaCVRecord" + live.getLiveId());
        JavaCVRecord jcv;
        if(jcvo!=null){
            jcv=(JavaCVRecord)jcvo;
            jcv.stop();
        }
        jcv= new JavaCVRecord(live);
        jcv.stream();
        jcv.start();
        //成功就放入
        shm.put("JavaCVRecord"+live.getLiveId(),jcv);*/
        b = super.update(live);
        if(!b){
            throw new CommonRollbackException("修改失败");
        }
        liveBusiness.restartLive(live);

        return b;
    }
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public  boolean delete(Long liveId) {
        boolean b=false;
        Live olive = super.load(liveId);
       /* Object jcvo = shm.get("JavaCVRecord" +liveId);
        JavaCVRecord jcv;
        if(jcvo!=null){
            jcv=(JavaCVRecord)jcvo;
            jcv.stop();
        }
        shm.remove("JavaCVRecord"+liveId);*/
       liveBusiness.stopLive(liveId);
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
        boolean b=false;
        Live live = super.load(liveId);
        if(live==null){
            throw new CommonRollbackException("直播不存在");
        }

        if(status==1){
            if(live.getStatus()==1){
                //throw new CommonRollbackException("已经在直播");
            }else{
               /*Object jcvo = shm.get("JavaCVRecord" +liveId);
            JavaCVRecord jcv;
            if(jcvo!=null){
                jcv=(JavaCVRecord)jcvo;
                jcv.carryon();//恢复
            }else{
                jcv= new JavaCVRecord(live);
                jcv.stream();
                jcv.start();
            }
            shm.put("JavaCVRecord" +liveId,jcv);*/
                liveBusiness.startLive(live);
            }
        }else if(status==2){
            if(live.getStatus()==2){
                //throw new CommonRollbackException("已经停止");
            }else{
              /*Object jcvo = shm.get("JavaCVRecord" +liveId);
            JavaCVRecord jcv;
            if(jcvo!=null){
                jcv=(JavaCVRecord)jcvo;
                //jcv.pause();//暂停
                //shm.put("JavaCVRecord" +liveId,jcv);
                jcv.stop();
                shm.remove("JavaCVRecord" +liveId);
            }*/
                liveBusiness.stopLive(liveId);
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
