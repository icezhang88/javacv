package com.nieyue.business;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.nieyue.bean.Live;
import com.nieyue.service.LiveService;
import com.nieyue.util.HttpClientUtil;
import com.nieyue.util.MyDESutil;
import com.nieyue.util.MyDom4jUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 推送直播
 */
@Configuration
public class SendLiveBusiness {
    @Autowired
    SendLiveBusiness sendLiveBusiness;
    @Autowired
    LiveService liveService;
    @Value("${myPugin.isSendLive}")
    Integer isSendLive;

    /**
     *发送
     */
    public boolean send(){
        //发送直播,只发送自动
        if(isSendLive==1){
            Wrapper<Live> wrapper2=new EntityWrapper<>();
            Map<String,Object> map2=new HashMap<String,Object>();
            map2.put("status", 1);
            map2.put("type", 2);
            wrapper2.allEq(MyDom4jUtil.getNoNullMap(map2));
            List<Live> ll2 = liveService.simplelist(wrapper2);
            boolean b = updateLiveChannel(ll2);
            return b;
        }else{
            //不需要发送，也算成功
            return true;
        }
    }

    /**
     * 批量更新频道播放地址
     */
    public boolean updateLiveChannel(List<Live> liveList){
      String updateLiveChannelUrl ="https://wintv.ottcom.cn/api/updateLiveChannel.aspx";
        Map<String,String> map=new HashMap<>();
        String timestamp = getTimestamp();
        if(StringUtils.isEmpty(timestamp)){
            return false;
        }
        map.put("timestamp",timestamp);
        map.put("token", MyDESutil.getOriginMD5(timestamp+"apptoken"));

        JSONArray jsonArray=new JSONArray();
        for (int i = 0; i < liveList.size(); i++) {
            Live live = liveList.get(i);
            JSONObject jo=new JSONObject();
            jo.put("channel_id",live.getLiveId());
            jo.put("channel_name",live.getName());
            JSONArray ja=new JSONArray();
            if(!StringUtils.isEmpty(live.getPlayUrl())){
                ja.add(live.getPlayUrl());
            }
            if(!StringUtils.isEmpty(live.getPlayUrl2())){
                ja.add(live.getPlayUrl2());
            }
            if(!StringUtils.isEmpty(live.getPlayUrl3())){
                ja.add(live.getPlayUrl3());
            }
            jo.put("url_list",ja);
            jsonArray.add(jo);
        }
        //jsonObject.put("update_info",jsonArray);
        map.put("update_info",String.valueOf(jsonArray));
        String result = HttpClientUtil.doPostForm(updateLiveChannelUrl, map);;
        JSONObject json = JSONObject.fromObject(result);
        if( String.valueOf(json.get("code")).equals("0")){
           // JSONArray resultJSONArray= json.getJSONArray("not_exists_channel_id_list");
           // System.out.println(resultJSONArray.toString());
            return true;
        }
        return false;

    }

    /**
     * 获取时间戳
     */
    public String getTimestamp(){
      String getTimestampUrl ="https://wintv.ottcom.cn/api/getTimestamp.aspx";
        String timestamp = "";
        try {
            String  result = HttpClientUtil.doGet(getTimestampUrl);
            JSONObject json = JSONObject.fromObject(result);
            if( String.valueOf(json.get("code")).equals("0")){
                timestamp= String.valueOf(json.getJSONObject("data").get("timestamp"));
            }
        } catch (Exception e) {
            //throw new RuntimeException("获取时间戳失败");
        }
        return timestamp;
    }

    public static void main(String[] args) {
        List<Live> liveList=new ArrayList<>();
        Live live=new Live();
        live.setName("球彩台");
        live.setTargetUrl("http://xy1-hls.live.huajiao.com/live/a3c33a20b20a41b5a21d98083483a8eb.m3u8");
        liveList.add(live);
        new SendLiveBusiness().updateLiveChannel(liveList);
    }
}
