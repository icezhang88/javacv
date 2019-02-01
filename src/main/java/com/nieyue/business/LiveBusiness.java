package com.nieyue.business;

import com.nieyue.bean.Config;
import com.nieyue.bean.Live;
import com.nieyue.exception.CommonRollbackException;
import com.nieyue.ffch4j.CommandManager;
import com.nieyue.ffch4j.CommandManagerImpl;
import com.nieyue.ffch4j.commandbuidler.CommandBuidler;
import com.nieyue.ffch4j.commandbuidler.CommandBuidlerFactory;
import com.nieyue.ffch4j.data.CommandTasker;
import com.nieyue.service.ConfigService;
import com.nieyue.service.LiveService;
import com.nieyue.util.Arith;
import com.nieyue.util.SingletonHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Configuration
public class LiveBusiness {
    private CommandManager manager=new CommandManagerImpl();
    HashMap<String, Object> shm= SingletonHashMap.getInstance();
    @Autowired
    LiveService liveService;

    public CommandManager getManager() {
        return manager;
    }

    @Autowired
    ConfigService configService;
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
        //模式，1编码解码，2直接转流，3音频转acc
        if(live.getModel()==1){
            cbf.add("-vcodec", "h264");
            cbf.add("-acodec", "aac");
           // cbf.add("-acodec", "aac_adtstoasc");
           // cbf.add("-absf", "aac_adtstoasc");
            //cbf.add("-ar", "44100");

        }else if(live.getModel()==2){
            cbf.add("-vcodec", "copy");
            cbf.add("-acodec", "copy");
            //-absf aac_adtstoasc
            cbf.add("-absf", "aac_adtstoasc");
        }else if(live.getModel()==3){
            cbf.add("-vcodec", "copy");
            cbf.add("-acodec", "aac");
        }

        //大于0才有自定义的宽高
        if (live.getWidth() >0 && live.getHeight() > 0) {
            cbf.add("-s", live.getWidth()+"X"+live.getHeight());
        }

        if(!StringUtils.isEmpty(live.getVideoBitrate())&&!live.getVideoBitrate().equals("0")){
            Double vb= Arith.mul(Double.valueOf(live.getVideoBitrate()),8);
            cbf.add("-b", vb+"k");
            cbf.add("-bufsize", vb+"k");
        }
        /*if (hasRTMPFLV(live.getTargetUrl())||hasM3U8(live.getTargetUrl())||hasRTSP(live.getTargetUrl())) {
            cbf.add("-f", "flv");
        }else if(hasMP4(live.getTargetUrl())){//MP4
            cbf.add("-f", "mp4");
        }*/

        if(hasMP4(live.getTargetUrl())){
            cbf.add("-f", "mp4");
        }else{
            cbf.add("-f", "flv");
        }
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
        stopLive(live.getLiveId());
        startLive(live);
    }
    /**
     * 停止 单个
     * @param liveId
     * @return
     */
    public Boolean stopLive(Long liveId){
        boolean result = false;
        CommandTasker commandTasker = queryLive(liveId);
        if(!ObjectUtils.isEmpty(commandTasker)){
            while (!result){
                result = manager.stop("live" +liveId);
            }
        }
        //重启的删除
        Object rl = shm.get("restartlive");
        if (ObjectUtils.isEmpty(rl)) {
        } else {
            Map<String,Long> map = (HashMap<String,Long>) rl;
            map.remove("live"+liveId);
           /* Map<String,Long> nmap = new HashMap<String,Long>();
            Iterator<Map.Entry<String, Long>> iter = map.entrySet().iterator();
            while (iter.hasNext()){
                Map.Entry<String, Long> entry = iter.next();
                //不相等的保存
                if (!entry.getKey() .equals("live"+liveId.toString())) {
                    nmap.put(entry.getKey(),entry.getValue());
                }
            }
            shm.put("restartlive",nmap);*/
        }
        //制空当前的msg
        Object lmo = shm.get("liveMsg");
        Map<String,String> map2;
        if(!ObjectUtils.isEmpty(lmo)){
            map2 = (HashMap<String,String>) lmo;
            map2.put("live"+liveId,"");
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
        //制空当前的msg
        Object lmo = shm.get("liveMsg");
        Map<String,String> map2;
        if(!ObjectUtils.isEmpty(lmo)){
            map2 = new HashMap<>();
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
    /**
     * 获取时长
     * @returnC
     */
    public String getDuration(Long liveId){
            //System.out.println(id.substring(id.indexOf("live")+"live".length()));
        Object lmo = shm.get("liveMsg");
        Map<String,String> map2;
        if(ObjectUtils.isEmpty(lmo)){
            return "";
        }else{
            map2 = (HashMap<String,String>) lmo;
        }
        String msg=map2.get("live"+liveId);
        if(StringUtils.isEmpty(msg)){
            return "";
        }
        int timepositon = msg.indexOf("time=");
        int bitratepositon = msg.indexOf("bitrate=");
        String timevalue=msg.substring(timepositon+5,bitratepositon);
        //System.out.println("timevalue:"+timevalue);
        return timevalue;
    }
    /**
     * 获取码率
     * @returnC
     */
    public String getVideoBitrate(Long liveId){
        Object lmo = shm.get("liveMsg");
        Map<String,String> map2;
        if(ObjectUtils.isEmpty(lmo)){
            return "";
        }else{
            map2 = (HashMap<String,String>) lmo;
        }
        String msg=map2.get("live"+liveId);
        if(StringUtils.isEmpty(msg)){
            return "";
        }
        int bitratepositon = msg.indexOf("bitrate=");
       // int speedpositon = msg.indexOf("speed=");
        int kbitspositon = msg.indexOf("kbits/s");
        String bitrate=msg.substring(bitratepositon+8,kbitspositon);
        return bitrate;
        /*double bitratek = Arith.div(Double.valueOf(bitrate), 8);
        String bitratevalue="";
        if(bitratek<1024){
            bitratevalue=bitratek+" KB/s";
        }else{
            bitratevalue=Arith.div(bitratek, 1024)+" MB/s";
        }
        return bitratevalue;*/

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
    /**
     * 批量更新自动直播链接
     */
    public List<Live > updateBatchLive2(List<Live > list ){
        boolean b=false;
        if(list.size()<=0){
            list= new ArrayList<Live>();
            return list;
        }
        List<Config> configlist = configService.simplelist(null);
        if(configlist.size()<=0){
            list= new ArrayList<Live>();
            return list;
        }
        Config config = configlist.get(0);
        if(StringUtils.isEmpty(config.getTargetBaseUrl())
                ||StringUtils.isEmpty(config.getPlayBaseUrl())
                ||StringUtils.isEmpty(config.getPlayUrlSuffix())
        ){
            list= new ArrayList<Live>();
            return list;
        }
        for (int i = 0; i < list.size(); i++) {
            Live live = list.get(i);
            String uuid=UUID.randomUUID().toString().replace("-","");
            live.setTargetUrl(config.getTargetBaseUrl()+uuid);
            live.setPlayUrl(config.getPlayBaseUrl()+uuid+"."+config.getPlayUrlSuffix());
        }
        return list;
    }
    /**
     * 增加自动直播链接
     */
    public Live  addLive2(Live live){
        List<Config> configlist = configService.simplelist(null);
        if(configlist.size()<=0){
            return null;
        }
        Config config = configlist.get(0);
        if(StringUtils.isEmpty(config.getTargetBaseUrl())
                ||StringUtils.isEmpty(config.getPlayBaseUrl())
                ||StringUtils.isEmpty(config.getPlayUrlSuffix())
        ){
            return null;
        }
        String uuid=UUID.randomUUID().toString().replace("-","");
        live.setTargetUrl(config.getTargetBaseUrl()+uuid);
        live.setPlayUrl(config.getPlayBaseUrl()+uuid+"."+config.getPlayUrlSuffix());
        return live;
    }
    /*public static void main(String[] args) {
        String uuid=UUID.randomUUID().toString().replace("-","");
        System.out.println(uuid);
    }*/
}
