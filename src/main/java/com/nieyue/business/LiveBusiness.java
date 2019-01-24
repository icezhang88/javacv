package com.nieyue.business;

import com.nieyue.bean.Live;
import com.nieyue.exception.CommonRollbackException;
import com.nieyue.ffch4j.CommandManager;
import com.nieyue.ffch4j.CommandManagerImpl;
import com.nieyue.ffch4j.commandbuidler.CommandBuidler;
import com.nieyue.ffch4j.commandbuidler.CommandBuidlerFactory;
import com.nieyue.ffch4j.data.CommandTasker;
import com.nieyue.util.SingletonHashMap;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Configuration
public class LiveBusiness {
    CommandManager manager=new CommandManagerImpl();
    HashMap<String, Object> shm= SingletonHashMap.getInstance();
    /**
     * 开始
     * @param live
     * @return
     */
    public String startLive(Live live){
        //已经启动
        CommandTasker commandTasker = queryLive(live.getLiveId());
        if(!ObjectUtils.isEmpty(commandTasker)){
            return commandTasker.getId();
        }
        if (StringUtils.isEmpty(live.getSourceUrl())
                ||StringUtils.isEmpty(live.getTargetUrl())) {
            throw new CommonRollbackException("源视频不能为空");
        }
        CommandBuidler cbf = CommandBuidlerFactory.createBuidler();
        cbf.add("ffmpeg-amd64");
        if (hasRTSP(live.getSourceUrl())) {
            cbf.add("-rtsp_transport", "tcp");
        }
        cbf.add("-i",live.getSourceUrl());
        //模式，1编码解码，2直接转流
        if(live.getModel()==1){
            cbf.add("-vcodec", "h264");
            cbf.add("-acodec", "copy");
        }else if(live.getModel()==2){
            cbf.add("-vcodec", "copy");
            cbf.add("-acodec", "copy");
        }
        //-absf aac_adtstoasc
        cbf.add("-absf", "aac_adtstoasc");

        //大于0才有自定义的宽高
        if (live.getWidth() >0 && live.getHeight() > 0) {
            cbf.add("-s", live.getWidth()+"X"+live.getHeight());
        }

        /*if (hasRTMPFLV(live.getTargetUrl())||hasM3U8(live.getTargetUrl())||hasRTSP(live.getTargetUrl())) {
            cbf.add("-f", "flv");
        }else if(hasMP4(live.getTargetUrl())){//MP4
            cbf.add("-f", "mp4");
        }*/
        cbf.add("-f", "flv");
        cbf.add(live.getTargetUrl());

        String result = manager.start("live" + live.getLiveId(), cbf);
        if(result==null){
            throw new CommonRollbackException("直播启动错误，请检查参数！");
        }
        return result;
    }

    /**
     * 重启
     * @param live
     * @return
     */
    public void restartLive(Live live){
        boolean result = false;
        CommandTasker commandTasker = queryLive(live.getLiveId());
        if(!ObjectUtils.isEmpty(commandTasker)){
            while (!result){
                result = manager.stop("live" + live.getLiveId());
            }
        }
        startLive(live);
    }
    /**
     * 停止 单个
     * @param liveId
     * @return
     */
    public Boolean stopLive(Long liveId){
        boolean result = false;
        while (!result){
            result = manager.stop("live" +liveId);
        }
        //重启的删除
        Object rl = shm.get("restartlive");
        if (ObjectUtils.isEmpty(rl)) {
        } else {
            Map<String,Long> map = (HashMap<String,Long>) rl;
            Map<String,Long> nmap = new HashMap<String,Long>();
            Iterator<Map.Entry<String, Long>> iter = map.entrySet().iterator();
            while (iter.hasNext()){
                Map.Entry<String, Long> entry = iter.next();
                //不相等的保存
                if (!entry.getKey() .equals("live"+liveId.toString())) {
                    nmap.put(entry.getKey(),entry.getValue());
                }
            }
            shm.put("restartlive",nmap);
        }
        return result;
    }
    /**
     * 停止 全部
     * @return number 停止了number个
     */
    public int stopAllLive(){
        int number = manager.stopAll();
        //重启的清空
        Object rl = shm.get("restartlive");
        if (ObjectUtils.isEmpty(rl)) {
        } else {
            Map<String,Long> nmap = new HashMap<String,Long>();
            shm.put("restartlive",nmap);
        }
        return number;
    }
    /**
     * 销毁一些后台资源和保活线程
     * @return number 停止了number个
     */
    public void destoryAllLive(){
        manager.destory();
    }
    /**
     * 查询 单个
     * @param liveId
     * @return
     */
    public CommandTasker queryLive(Long liveId){
        return manager.query("live" + liveId);
    }
    /**
     * 查询 全部
     * @returnC
     */
    public Collection<CommandTasker> queryAllLive(){
        Collection<CommandTasker> infoList = manager.queryAll();
        return infoList;
    }

    /*
     * 是否包含rtmp或flv
     */
    private boolean hasRTMPFLV(String str) {
        return str.indexOf("rtmp") >-1|| str.indexOf("flv")>-1;
    }
    /*
     * 是否包含M3U8
     */
    private boolean hasM3U8(String str) {
        return str.indexOf(".m3u8") > -1;
    }

    /*
     * 是否包含mp4
     */
    private boolean hasMP4(String str) {
        return str.indexOf("mp4") >0;
    }
    /*
     * 是否包含rtsp
     */
    private boolean hasRTSP(String str) {
        return str.indexOf("rtsp") >-1;
    }
}
