package com.nieyue.util;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.*;

import javax.swing.*;

public class CVUtil {
    static boolean  isStart = true;//该变量建议设置为全局控制变量，用于控制录制结束
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
     * @param inputFile-该地址可以是网络直播/录播地址，也可以是远程/本地文件路径
     * @param outputFile
     *            -该地址只能是文件地址，如果使用该方法推送流媒体服务器会报错，原因是没有设置编码格式
     * @throws FrameGrabber.Exception
     * @throws FrameRecorder.Exception
     * @throws org.bytedeco.javacv.FrameRecorder.Exception
     */
    public static void frameRecord(String inputFile, String outputFile, int audioChannel)
            throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {

        // 获取视频源
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
        grabber.setOption("rtsp_transport", "tcp");
        //-vcodec copy -acodec copy -absf aac_adtstoasc -f flv
       // grabber.setVideoCodecName("copy");
        //grabber.setAudioCodecName("copy");
        grabber.setImageWidth(960);
        grabber.setImageHeight(480);
        /**
         * FFmpegFrameRecorder(String filename, int imageWidth, int imageHeight,
         * int audioChannels) fileName可以是本地文件（会自动创建），也可以是RTMP路径（发布到流媒体服务器）
         * imageWidth = width （为捕获器设置宽） imageHeight = height （为捕获器设置高）
         * audioChannels = 2（立体声）；1（单声道）；0（无音频）
         */
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, grabber.getImageWidth(), grabber.getImageHeight(), audioChannel);
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
        recorder.setVideoBitrate(2000000);
        // h264编/解码器
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        // 封装格式flv
        recorder.setFormat("flv");
        // 视频帧率(保证视频质量的情况下最低25，低于25会出现闪屏)
        //recorder.setFrameRate(25);
         //关键帧间隔，一般与帧率相同或者是视频帧率的两倍
       // recorder.setGopSize(25 * 2);
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
        // 音频编/解码器
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        // 开始取视频源
        recordByFrame(grabber, recorder);
    }
    private static void recordByFrame(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder)
            throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {//建议在线程中使用该方法
                    grabber.start();
                    recorder.start();
                    Frame frame = null;
                    System.out.println("推流开始！");
                    while (isStart && (frame = grabber.grabFrame()) != null) {
                        if(!isStart){
                            break;
                        }
                        recorder.record(frame);
                    }
                    recorder.stop();
                    grabber.stop();

                    } catch (FrameRecorder.Exception e) {
                        e.printStackTrace();
                    } catch (FrameGrabber.Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (grabber != null) {
                            try {
                                grabber.stop();
                            } catch (FrameGrabber.Exception e) {
                                e.printStackTrace();
                            }
                            System.out.println("推流结束！");
                        }
                    }
                }
            }).start();

    }
    //测试熔断
    public static void test() throws Exception {
        String inputFile = "rtsp://120.205.37.100:554/live/ch15021120011905096369.sdp?vcdnid=001";
        String outputFile = "rtmp://bytedance.uplive.ks-cdn.com/live/channel20801993";
        frameRecord(inputFile, outputFile,1);
        Thread.sleep(1000*10);
        isStart=false;
        System.out.println("停止");
        Thread.sleep(1000*2);
        System.out.println("准备开始");
        isStart=true;
        inputFile="rtsp://120.205.37.100:554/live/ch15021120011915466273.sdp?vcdnid=001";
        frameRecord(inputFile, outputFile,1);
    }
    public static void main(String[] args) throws Exception {
        //String inputFile = "rtsp://120.205.37.100:554/live/ch16070613003727442483.sdp?vcdnid=001";
        //String inputFile = "rtsp://120.205.37.100:554/live/ch15021120011905096369.sdp?vcdnid=001";
        String inputFile = "rtsp://120.205.37.100:554/live/ch16030115175852002239.sdp?vcdnid=001";
        // Decodes-encodes
       String outputFile = "rtmp://198.2.201.167:1935/app/test";
        //String outputFile = "rtmp://bytedance.uplive.ks-cdn.com/live/channel20801993";
        //String outputFile = "recorde.mp4";
        frameRecord(inputFile, outputFile,1);
       // test();


    }
}
