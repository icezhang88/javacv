package com.nieyue.javacv;

import com.nieyue.bean.Live;
import com.nieyue.comments.MyThread;
import com.nieyue.javacv.GrabberTmplate;
import com.nieyue.util.SingletonHashMap;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.*;

import javax.swing.*;
import java.util.HashMap;

public class CVUtil2 {
    /**
     * 调用本地摄像头窗口视频
     *
     */
    public static void get() throws FrameGrabber.Exception, InterruptedException {

        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();   //开始获取摄像头数据
        CanvasFrame canvas = new CanvasFrame("摄像头");//新建一个窗口
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas.setAlwaysOnTop(true);

        while(true)
        {
            if(!canvas.isDisplayable())
            {//窗口是否关闭
                grabber.stop();//停止抓取
                System.exit(2);//退出
            }
            canvas.showImage(grabber.grab());//获取摄像头图像并放到窗口上显示， 这里的Frame frame=grabber.grab(); frame是一帧视频图像

            Thread.sleep(50);//50毫秒刷新一次图像
        }

    }
    /**
     * 按帧录制视频
     *
     * @param live-直播类
     * @throws FrameGrabber.Exception
     * @throws FrameRecorder.Exception
     * @throws FrameRecorder.Exception
     */
    public  static  boolean frameRecord(Live live, int audioChannel) {
        boolean isSuccess=true;

        // 开始取视频源
        try {
            recordByFrame(live,audioChannel);
        } catch (Exception e) {
            isSuccess=false;
            return isSuccess;
        }
        return isSuccess;
    }
    private static void recordByFrame(Live live,int audioChannel)
            throws Exception, FrameRecorder.Exception {
        HashMap<String, Object> shm = SingletonHashMap.getInstance();
        // 获取视频源
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(live.getSourceUrl());
        MyThread thread =
                new MyThread() {
                    @Override
                    public void run() {

                        FFmpegFrameRecorder recorder = null;
                        try {
                            if(live.getSourceUrl().indexOf("rtsp")>=0) {
                                grabber.setOption("rtsp_transport","tcp");
                            }
                            if(shm.get("grabber"+live.getLiveId())!=null){
                                return;
                            }
                            shm.put("grabber"+live.getLiveId(),grabber);
                            grabber.start();
                            //-vcodec copy -acodec copy -absf aac_adtstoasc -f flv
                            // grabber.setVideoCodecName("copy");
                            //grabber.setAudioCodecName("copy");
                            // live.setWidth(live.getWidth()==null?"960":live.getWidth());
                            // live.setHeight(live.getHeight()==null?"480":live.getHeight());

                            /**
                             * FFmpegFrameRecorder(String filename, int imageWidth, int imageHeight,
                             * int audioChannels) fileName可以是本地文件（会自动创建），也可以是RTMP路径（发布到流媒体服务器）
                             * imageWidth = width （为捕获器设置宽） imageHeight = height （为捕获器设置高）
                             * audioChannels = 2（立体声）；1（单声道）；0（无音频）
                             */
                            if(Integer.parseInt(live.getWidth())==0||Integer.parseInt(live.getHeight())==0){
                                // 源码，不编码解码
                            } else {
                                live.setWidth(live.getWidth() == null ? "960" : live.getWidth());
                                live.setHeight(live.getHeight() == null ? "480" : live.getHeight());
                                grabber.setImageWidth(Integer.parseInt(live.getWidth()));
                                grabber.setImageHeight(Integer.parseInt(live.getHeight()));
                            }
                            recorder = new FFmpegFrameRecorder(live.getTargetUrl(), grabber.getImageWidth(), grabber.getImageHeight(), audioChannel);
                            // 视频帧率(保证视频质量的情况下最低25，低于25会出现闪屏)
                            recorder.setFrameRate(25);
                            //关键帧间隔，一般与帧率相同或者是视频帧率的两倍
                            recorder.setGopSize(25 * 2);
                            // 不可变(固定)音频比特率
                            recorder.setAudioOption("crf", "0");
                            // 最高质量
                            recorder.setAudioQuality(0);
                            // 音频比特率
                            // recorder.setAudioBitrate(192000);
                            // 音频采样率
                            //recorder.setSampleRate(44100);
                            // 双通道(立体声)
                            recorder.setAudioChannels(2);
                            recorder.setInterleaved(true);
                            /**
                             * 该参数用于降低延迟 参考FFMPEG官方文档：https://trac.ffmpeg.org/wiki/StreamingGuide
                             * 官方原文参考：ffmpeg -f dshow -i video="Virtual-Camera" -vcodec libx264
                             * -tune zerolatency -b 900k -f mpegts udp://10.1.0.102:1234
                             */

                            recorder.setVideoOption("tune", "zerolatency");
                            /**
                             * 权衡quality(视频质量)和encode speed(编码速度) values(值)：
                             * ultrafast(终极快),superfast(超级快), veryfast(非常快), faster(很快), fast(快),
                             * medium(中等), slow(慢), slower(很慢), veryslow(非常慢)
                             * ultrafast(终极快)提供最少的压缩（低编码器CPU）和最大的视频流大小；而veryslow(非常慢)提供最佳的压缩（高编码器CPU）的同时降低视频流的大小
                             * 参考：https://trac.ffmpeg.org/wiki/Encode/H.264 官方原文参考：-preset ultrafast
                             * as the name implies provides for the fastest possible encoding. If
                             * some tradeoff between quality and encode speed, go for the speed.
                             * This might be needed if you are going to be transcoding multiple
                             * streams on one machine.
                             */
                            recorder.setVideoOption("preset", "ultrafast");
                            /**
                             * 参考转流命令: ffmpeg
                             * -i'udp://localhost:5000?fifo_size=1000000&overrun_nonfatal=1' -crf 30
                             * -preset ultrafast -acodec aac -strict experimental -ar 44100 -ac
                             * 2-b:a 96k -vcodec libx264 -r 25 -b:v 500k -f flv 'rtmp://<wowza
                             * serverIP>/live/cam0' -crf 30
                             * -设置内容速率因子,这是一个x264的动态比特率参数，它能够在复杂场景下(使用不同比特率，即可变比特率)保持视频质量；
                             * 可以设置更低的质量(quality)和比特率(bit rate),参考Encode/H.264 -preset ultrafast
                             * -参考上面preset参数，与视频压缩率(视频大小)和速度有关,需要根据情况平衡两大点：压缩率(视频大小)，编/解码速度 -acodec
                             * aac -设置音频编/解码器 (内部AAC编码) -strict experimental
                             * -允许使用一些实验的编解码器(比如上面的内部AAC属于实验编解码器) -ar 44100 设置音频采样率(audio sample
                             * rate) -ac 2 指定双通道音频(即立体声) -b:a 96k 设置音频比特率(bit rate) -vcodec libx264
                             * 设置视频编解码器(codec) -r 25 -设置帧率(frame rate) -b:v 500k -设置视频比特率(bit
                             * rate),比特率越高视频越清晰,视频体积也会变大,需要根据实际选择合理范围 -f flv
                             * -提供输出流封装格式(rtmp协议只支持flv封装格式) 'rtmp://<FMS server
                             * IP>/live/cam0'-流媒体服务器地址
                             */
                            // 2000 kb/s, 720P视频的合理比特率范围
                            // recorder.setVideoBitrate(2000000);
                            recorder.setVideoBitrate(2000*grabber.getImageWidth());
                            // h264编/解码器
                            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                            // 音频编/解码器
                            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                            // 封装格式flv
                            recorder.setFormat("flv");
                            // 建议在线程中使用该方法
                            shm.put("recorder"+live.getLiveId(),recorder);
                            recorder.start();
                            Frame frame = null;
                            //System.out.println("推流开始！");
                            // 编码解码
                            while (!this.exit &&((frame = grabber.grabFrame()) != null)) {
                                this.sleep(10);
                                recorder.record(frame);
                            }
                        } catch (Exception e) {

                        }  finally {
                            //人为控制才能停止
                            while (true) {
                                try {
                                    recorder.stop();
                                    grabber.stop();
                                    break;
                                } catch (Exception e) {
                                }
                            }
                            //     System.out.println("推流结束！");
                            // 如果不是人为控制停止的，需要重新启动
                            if (!this.exit) {
                                while (true) {
                                    try {
                                        // 1分钟重启一次
                                        this.sleep(60000);
                                        boolean b = stopThread(live.getLiveId());
                                        if(b){
                                            b=frameRecord(live,2);
                                            if(b){
                                                break;
                                            }
                                        }
                                    } catch (InterruptedException ee) {

                                    }
                                }
                            }
                        }
                    }
                };
        thread.start();
        //加入live
        shm.put("liveId"+live.getLiveId(),thread);

    }
    //停止
    public static  boolean stopThread(Long liveId){
        boolean b=false;
        HashMap<String, Object> shm = SingletonHashMap.getInstance();
        Object livethreadobject = shm.get("liveId" + liveId);
        //已经停止了
        if(livethreadobject==null
        ){
            b=true;
            return b;
        }
        Object grabbero = shm.get("grabber" + liveId);
        Object recordero = shm.get("recorder" + liveId);
        //阻塞的
        if(grabbero!=null){
            //阻塞的
            FFmpegFrameGrabber grabber = (FFmpegFrameGrabber) grabbero;
            /*try {
                grabber.stop();
            } catch (FrameGrabber.Exception e) {
            }*/
            grabber=null;
        }
        if(recordero!=null){
            //阻塞的
            FFmpegFrameRecorder recorder = (FFmpegFrameRecorder) recordero;
            /*try {
                recorder.stop();
            } catch (FrameRecorder.Exception e) {
            }*/
            recorder=null;
        }
        //非阻塞
        MyThread thread = (MyThread) livethreadobject;
        if(!thread.exit){
            thread.exit=true;
            thread.stop();
        }
        shm.remove("liveId"+liveId);
        shm.remove("grabber"+liveId);
        shm.remove("recorder"+liveId);
        b=true;
        return b;
    }

    public static void test() throws InterruptedException {
        //String inputFile = "rtsp://120.205.37.100:554/live/ch16070613003727442483.sdp?vcdnid=001";
        //String inputFile = "rtsp://120.205.37.100:554/live/ch15021120011905096369.sdp?vcdnid=001";
        // String inputFile = "rtsp://120.205.37.100:554/live/ch16030115175852002239.sdp?vcdnid=001";
        //String inputFile = "rtsp://183.58.12.204/PLTV/88888905/224/3221227287/10000100000000060000000001066432_0.smil";
        // Decodes-encodes
        //String outputFile = "rtmp://bytedance.uplive.ks-cdn.com/live/channel20801993";
        //String outputFile = "recorde.mp4";
        Live live=new Live();
        live.setLiveId(100000l);
        //live.setSourceUrl("https://acfun.iqiyi-kuyun.com/ppvod/1151F4A53CC48AD2A45E6A33AA40D303.m3u8");
        live.setSourceUrl("rtsp://120.205.37.100:554/live/ch15021120011915466273.sdp?vcdnid=001");
        //live.setTargetUrl("rtmp://118.190.133.146:1936/app/test");
        live.setTargetUrl("rtmp://bytedance.uplive.ks-cdn.com/live/channel20809665");
        //live.setWidth("960");
        //live.setHeight("480");
        live.setWidth("0");
        live.setHeight("0");
        frameRecord(live,2);
        Thread.sleep(1000*10);
        System.out.println("停止");
        System.out.println((Thread) SingletonHashMap.getInstance().get("liveId" + 100000));
        stopThread(live.getLiveId());
        Thread.sleep(1000*10);
        System.out.println("准备开始");
        live.setSourceUrl("rtsp://120.205.37.100:554/live/ch15021120011915466273.sdp?vcdnid=001");
        frameRecord(live,2);
    }
    public static void test2() throws Exception {
        String inputstr="rtsp://120.205.37.100:554/live/ch15021120011915466273.sdp?vcdnid=001";
        //String inputstr="rtsp://120.205.37.100:554/live/ch15021120011915466273.sdp?vcdnid=001";
        // String inputstr="http://dbiptv.sn.chinamobile.com/PLTV/88888888/224/3221225775/index.m3u8";
        //String inputstr="http://hwltc.tv.cdn.zj.chinamobile.com/PLTV/88888888/224/3221228306/42329183.smil/index.m3u8?fmt=ts2hls";
        String outputstr="rtmp://bytedance.uplive.ks-cdn.com/live/channel20809990";
        GrabberTmplate gt = new GrabberTmplate();
        gt.openInput(inputstr);
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputstr);
        if(inputstr.indexOf("rtsp")>=0) {
            grabber.setOption("rtsp_transport","tcp");
        }
        //grabber.setVideoCodecName("copy");
        //grabber.setAudioCodecName("copy");
        grabber.start();
        System.out.println("grabber开始");
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputstr, grabber.getImageWidth(), grabber.getImageHeight(),2);

        // 2000 kb/s, 720P视频的合理比特率范围
        // recorder.setVideoBitrate(2000000);
        recorder.setVideoBitrate(2000*grabber.getImageWidth());

        // 视频帧率(保证视频质量的情况下最低25，低于25会出现闪屏)
        recorder.setFrameRate(25);
        //关键帧间隔，一般与帧率相同或者是视频帧率的两倍
        recorder.setGopSize(25 * 2);
    recorder.setVideoOption("crf","25");
        // 不可变(固定)音频比特率
        recorder.setAudioOption("crf", "18");
        // 最高质量
        recorder.setAudioQuality(0);
        // 音频比特率
        // recorder.setAudioBitrate(192000);
        // 音频采样率
        //recorder.setSampleRate(44100);
        // 双通道(立体声)
        recorder.setAudioChannels(2);
        // h264编/解码器rawvideo
        System.out.println(grabber.getAudioCodec());
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        // 音频编/解码器
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        // 封装格式flv
        recorder.setFormat("flv");
        //avformat.AVFormatContext fc = grabber.getFormatContext();
       // recorder.start(fc);
        recorder.start();
        System.out.println("recorder开始");
        Frame frame;
        try{
           while ((frame = grabber.grab()) != null) {
                Thread.sleep(1);
                recorder.record(frame);
            }
          /* while (true){
               avcodec.AVPacket gp = grabber.grabPacket();
               if(gp==null||gp.data()==null||gp.size()<=0){
                   continue;
               }
               recorder.recordPacket(gp);

           }*/
        }catch (Exception e){
            while (true) {
                try {
                    // 5秒重启一次
                    grabber.restart();
                    recorder.stop();
                    recorder.start();
                    while ((frame = grabber.grab()) != null) {
                        recorder.record(frame);
                    }
                } catch (Exception ee) {
                }
            }
        }
        // System.out.println("recorder结束");
    }
    private volatile boolean isActive = false;
    public static void main(String[] args) throws Exception {

        //test();
         //test2();
        String inputstr="rtsp://120.205.37.100:554/live/ch15021120011915466273.sdp?vcdnid=001";
        //String inputstr="rtsp://120.205.37.100:554/live/ch15021120011915466273.sdp?vcdnid=001";
        // String inputstr="http://dbiptv.sn.chinamobile.com/PLTV/88888888/224/3221225775/index.m3u8";
        //String inputstr="http://hwltc.tv.cdn.zj.chinamobile.com/PLTV/88888888/224/3221228306/42329183.smil/index.m3u8?fmt=ts2hls";
        String outputstr="rtmp://bytedance.uplive.ks-cdn.com/live/channel20809990";
        JavaCVRecord jcv =null; //new JavaCVRecord(inputstr,outputstr,800,500);
        jcv.stop();
        //jcv.from(inputstr).to(outputstr);
        //jcv.stream();
        //jcv.forward();
        //jcv.codec();
        jcv.start();
        Thread.sleep(10000);
        System.out.println("暂停");
        Thread.sleep(3000);
        jcv.pause();
        Thread.sleep(5000);
        System.out.println("开始");
        jcv.setWidth(300);
        jcv.setHeight(200);
        jcv.start();
        Thread.sleep(5000);
        System.out.println("停止");
        jcv.stop();
        Thread.sleep(3000);
        System.out.println("开始");

        jcv.restart();
    }
}
