package com.nieyue.javacv.recorder;

import com.nieyue.exception.CommonRollbackException;
import com.nieyue.javacv.grabber.FFmpegScreenshot;
import com.nieyue.util.IMGIndivisibleUtil;
import com.nieyue.util.SingletonHashMap;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.javacpp.avcodec.*;

public class JavaCVRecord implements Recorder {
	private static int threadInitNumber;
	private static synchronized int nextThreadNum() {
		return threadInitNumber++;
	}
	private final static String THREAD_NAME="录像工作线程";
	FFmpegFrameGrabberPlus grabber = null;
	FFmpegFrameRecorderPlus record = null;
	int model = 1;//1编码解码，2封装
	String src, out;
	int width = -1, height = -1;
	protected HashMap<String,Object> shm= SingletonHashMap.getInstance();
	// 视频参数
	protected int audiocodecid;
	protected int codecid;
	protected double framerate;// 帧率
	protected int bitrate;// 比特率

	// 音频参数
	// 想要录制音频，这三个参数必须有：audioChannels > 0 && audioBitrate > 0 && sampleRate > 0
	private int audioChannels;
	private int audioBitrate;
	private int sampleRate;

	protected RecordThread cuThread;// 当前线程

	public JavaCVRecord() {
		super();
	}

	public JavaCVRecord(String src, String out) {
		super();
		this.src = src;
		this.out = out;
	}
	public JavaCVRecord(String src, String out,int model) {
		super();
		this.src = src;
		this.out = out;
		this.model=model;
	}

	public JavaCVRecord(String src, String out, int width, int height) {
		super();
		this.src = src;
		this.out = out;
		this.width = width;
		this.height = height;
	}
	public JavaCVRecord(String src, String out, int width, int height,int model) {
		super();
		this.src = src;
		this.out = out;
		this.width = width;
		this.height = height;
		this.model=model;
	}

	public String getSrc() {
		return src;
	}

	public JavaCVRecord setSrc(String src) {
		this.src = src;
		return this;
	}

	public String getOut() {
		return out;
	}

	public JavaCVRecord setOut(String out) {
		this.out = out;
		return this;
	}

	public int getWidth() {
		return width;
	}

	public JavaCVRecord setWidth(int width) {
		this.width = width;
		return this;
	}

	public int getHeight() {
		return height;
	}

	public JavaCVRecord setHeight(int height) {
		this.height = height;
		return this;
	}

	public Recorder stream() throws IOException {
		return stream(src, out);
	}

	public int getAudioChannels() {
		return audioChannels;
	}

	public JavaCVRecord setAudioChannels(int audioChannels) {
		this.audioChannels = audioChannels;
		return this;
	}

	public int getAudioBitrate() {
		return audioBitrate;
	}

	public JavaCVRecord setAudioBitrate(int audioBitrate) {
		this.audioBitrate = audioBitrate;
		return this;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public JavaCVRecord setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
		return this;
	}
	/**
	 * 监控线程
	 */
	private void jmxthread( ){
		//防止超时,0是未开始，1是开始
		shm.put("grabberisstart"+src,0);
		shm.put("grabberstarttime"+src,new Date().getTime());
		boolean[] isstop={false};
		Thread thread = new Thread() {
			@Override
			public void run() {
				while (!isstop[0]) {
					try {
						this.sleep(1000);
					} catch (InterruptedException e) {
					}
					Object grabberisstarto = shm.get("grabberisstart" + src);
					Object grabberstarttimeo = shm.get("grabberstarttime" + src);
					if (grabberisstarto != null&& grabberstarttimeo != null) {
						Integer grabberisstart = (Integer) grabberisstarto;
						Long grabberstarttime = (Long) grabberstarttimeo;
						//System.out.println(notifyDate);
						//System.out.println(new Date().getTime());
						//System.out.println(new Date().getTime() - timeout);
						//成功了
						if(grabberisstart==1){
							isstop[0] = true;
							shm.remove("grabberisstart" + src);
							shm.remove("grabberstarttime" + src);
							break;
						}else if ((grabberstarttime <= new Date().getTime() - 2000)&&grabberisstart==0) {
							//超过两秒
							isstop[0] = true;
							shm.remove("grabberstart" + src);
							shm.remove("grabberstarttime" + src);
							//_this.reover();//异常停止（重启）
							//_this.stopRecord();
							break;
						}
					}else{
						break;
					}
				}
				System.out.println("over");
			}
		};
		thread.start();
		shm.put("grabberisstart"+src,1);
	}
	/**
	 * 视频源
	 * 
	 * @param src
	 * @return
	 * @throws Exception
	 */
	public Recorder from(String src) throws Exception {
		if (src == null) {
			throw new Exception("源视频不能为空");
		}
		this.src = src;
		// 采集/抓取器
		grabber = new FFmpegFrameGrabberPlus(src);
		if (hasRTSP(src)) {
			grabber.setOption("rtsp_transport", "tcp");
		}
		//jmxthread();
		grabber.start();// 开始之后ffmpeg会采集视频信息，之后就可以获取音视频信息
		if (width <= 0 || height <= 0) {
			width = grabber.getImageWidth();
			height = grabber.getImageHeight();
		}
		// 视频参数
		audiocodecid = grabber.getAudioCodec();
		codecid = grabber.getVideoCodec();
		framerate = grabber.getVideoFrameRate();// 帧率
		bitrate = grabber.getVideoBitrate();// 比特率
		// 音频参数
		// 想要录制音频，这三个参数必须有：audioChannels > 0 && audioBitrate > 0 && sampleRate > 0
		audioChannels = grabber.getAudioChannels();
		audioBitrate = grabber.getAudioBitrate();
		if (audioBitrate < 1) {
			audioBitrate = 128 * 1000;// 默认音频比特率
		}
		sampleRate = grabber.getSampleRate();
		return this;
	}

	/**
	 * 音频参数设置
	 * 
	 * @param audioChannels
	 * @param audioBitrate
	 * @param sampleRate
	 * @return
	 */
	public Recorder audioParam(int audioChannels, int audioBitrate, int sampleRate) {
		this.audioChannels = audioChannels;
		this.audioBitrate = audioBitrate;
		this.sampleRate = sampleRate;
		return this;
	}

	/**
	 * 视频参数设置
	 * 
	 * @param width
	 *            -可以为空，为空表示不改变源视频宽度
	 * @param height
	 *            -可以为空，为空表示不改变源视频高度
	 * @param framerate-帧率
	 * @param bitrate-比特率
	 * @return
	 */
	public Recorder videoParam(Integer width, Integer height, int framerate, int bitrate) {
		if (width != null) {
			this.width = width;
		}
		if (height != null) {
			this.height = height;
		}
		this.framerate = framerate;
		this.bitrate = bitrate;
		return this;
	}

	/**
	 * 输出视频到文件或者流服务
	 * 
	 * @param out
	 *            -输出位置（支持流服务和文件）
	 * @return
	 * @throws IOException
	 */
	public Recorder to(String out) throws IOException {
		if (out == null) {
			throw new CommonRollbackException("输出视频不能为空");
		}
		this.out = out;
		// 录制/推流器
		record = new FFmpegFrameRecorderPlus(out, width, height);
		// 视频帧率(保证视频质量的情况下最低25，低于25会出现闪屏)
		record.setFrameRate(25);
		//关键帧间隔，一般与帧率相同或者是视频帧率的两倍
		record.setGopSize(25 * 2);
		// 不可变(固定)音频比特率
		record.setAudioOption("crf", "0");
		// 最高质量
		record.setAudioQuality(0);
		// 音频比特率
		// recorder.setAudioBitrate(192000);
		// 音频采样率
		//recorder.setSampleRate(44100);
		// 双通道(立体声)
		record.setAudioChannels(2);
		record.setInterleaved(true);
		/**
		 * 该参数用于降低延迟 参考FFMPEG官方文档：https://trac.ffmpeg.org/wiki/StreamingGuide
		 * 官方原文参考：ffmpeg -f dshow -i video="Virtual-Camera" -vcodec libx264
		 * -tune zerolatency -b 900k -f mpegts udp://10.1.0.102:1234
		 */

		record.setVideoOption("tune", "zerolatency");
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
		record.setVideoOption("preset", "ultrafast");
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
		record.setVideoBitrate(2000*grabber.getImageWidth());

		//rtmp、flv、rtsp、m3u8
		if (hasRTMPFLV(out)||hasM3U8(out)||hasRTSP(out)) {
			// 封装格式flv，并使用h264和aac编码
			record.setFormat("flv");
			record.setVideoCodec(AV_CODEC_ID_H264);
			record.setAudioCodec(AV_CODEC_ID_AAC);
		}else if(hasMP4(out)){//MP4
			record.setFormat("mp4");
			record.setVideoCodec(AV_CODEC_ID_H264);
			record.setAudioCodec(AV_CODEC_ID_AAC);
		}
		System.out.println(grabber.getFormatContext());
		//只能转码
		if(hasM3U8(src)
				||grabber.getVideoCodec()!=AV_CODEC_ID_H264
				||grabber.getAudioCodec()!=AV_CODEC_ID_AAC
		){
			model=1;
		}
		if(model==1){
			record.start();
		}else{
			record.start(grabber.getFormatContext());
		}
		return this;
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
	 * 转发源视频到输出（复制）
	 * 
	 * @param src
	 *            -源视频
	 * @param out
	 *            -输出流媒体服务地址
	 * @return
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception
	 */
	public Recorder stream(String src, String out) throws IOException {
		if (src == null || out == null) {
			throw new CommonRollbackException("源视频和输出为空");
		}
		this.src = src;
		this.out = out;
		// 采集/抓取器
		grabber = new FFmpegFrameGrabberPlus(src);
		if (hasRTSP(src)) {
			grabber.setOption("rtsp_transport", "tcp");
		}
		try{

			grabber.start();
		}catch (FrameGrabber.Exception e){
			throw new CommonRollbackException("源视频启动失败");
		}
		if (width <=0 || height <= 0) {
			width = grabber.getImageWidth();
			height = grabber.getImageHeight();
		}
		record = new FFmpegFrameRecorderPlus(out, width, height);

		//rtmp、flv、rtsp、m3u8
		if (hasRTMPFLV(out)||hasM3U8(out)||hasRTSP(out)) {
			// 封装格式flv，并使用h264和aac编码
			record.setFormat("flv");
			record.setVideoCodec(AV_CODEC_ID_H264);
			record.setAudioCodec(AV_CODEC_ID_AAC);
		}else if(hasMP4(out)){//MP4
			record.setFormat("mp4");
			record.setVideoCodec(AV_CODEC_ID_H264);
			record.setAudioCodec(AV_CODEC_ID_AAC);
		}
		// 视频帧率(保证视频质量的情况下最低25，低于25会出现闪屏)
		record.setFrameRate(25);
		//关键帧间隔，一般与帧率相同或者是视频帧率的两倍
		record.setGopSize(25 * 2);

		record.setInterleaved(true);
		// 不可变(固定)音频比特率
		record.setAudioOption("crf", "0");
		// 最高质量
		record.setAudioQuality(0);
		// 音频比特率
		// recorder.setAudioBitrate(192000);
		// 音频采样率
		//recorder.setSampleRate(44100);
		// 双通道(立体声)
		record.setAudioChannels(2);

		//只能转码
		if(hasM3U8(src)
				||grabber.getVideoCodec()!=AV_CODEC_ID_H264
				||grabber.getAudioCodec()!=AV_CODEC_ID_AAC
		){
			model=1;
		}
		if(model==1){
			/**
			 * 该参数用于降低延迟 参考FFMPEG官方文档：https://trac.ffmpeg.org/wiki/StreamingGuide
			 * 官方原文参考：ffmpeg -f dshow -i video="Virtual-Camera" -vcodec libx264
			 * -tune zerolatency -b 900k -f mpegts udp://10.1.0.102:1234
			 */

			record.setVideoOption("tune", "zerolatency");
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
			record.setVideoOption("preset", "ultrafast");
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
			record.setVideoBitrate(2000*grabber.getImageWidth());
			record.start();
		}else{
			/*record.setPixelFormat(grabber.getPixelFormat());
			record.setAspectRatio(grabber.getAspectRatio());
			record.setSampleRate(grabber.getSampleRate());
			record.setAspectRatio(grabber.getAspectRatio());
			record.setFrameRate(grabber.getFrameRate());
			record.setAudioChannels(2);
			record.setAudioCodec(grabber.getAudioCodec());
			record.setOption("vtag","avc");*/
			//record.setVideoOption("crf","0");
			//record.setOptions(grabber.getOptions());
			avformat.AVFormatContext oc = grabber.getFormatContext();
			//System.out.println(oc.metadata());
			/*oc.video_codec_id(AV_CODEC_ID_H264);
			oc.audio_codec_id(AV_CODEC_ID_AAC);
			oc.start_time_realtime(grabber.getTimestamp());
			oc.start_time(grabber.getTimestamp());
			oc.use_wallclock_as_timestamps(1);
			oc.streams(grabber.getVideoStream());*/

			//record.start();
			record.start(oc);
		}
		return this;
	}

	/**
	 * 转封装
	 * 
	 * @throws IOException
	 */
	public void forward()  {
		long starttime = System.currentTimeMillis();
		System.out.println("开始循环读取时间：" + starttime);
		long err_index = 0;// 采集或推流失败次数
		for (long i = 0; err_index < Long.MAX_VALUE;) {
			avcodec.AVPacket pkt = null;
			try {
				pkt = grabber.grabPacket();
				System.err.println("采集到的");
				if (pkt == null || pkt.size() <= 0 || pkt.data() == null) {// 空包结束
					break;
				}
				System.err.println("准备推:" + pkt.stream_index());
				if (record.recordPacket(pkt)) {
					System.err.println("推送成功：" + i++);
				}
				av_free_packet(pkt);
			} catch (Exception e) {// 推流失败
				err_index++;
				System.out.println("采集失败:" + err_index);
				System.out.println("采集失败:" + e.getMessage());
				continue;
			}  finally {
				//
			}
			System.out.println("时间：" + (System.currentTimeMillis() - starttime));
		}
	}

	/**
	 * 转码
	 * 
	 * @throws IOException
	 */
	public void codec() throws IOException {
		long starttime = System.currentTimeMillis();
		System.out.println("开始循环读取时间：" + starttime);
		long err_index = 0;// 采集或推流失败次数
		for (; err_index < Long.MAX_VALUE;) {
			try {
				Frame pkt = grabber.grabFrame();
				if (pkt == null) {// 空包结束
					record.stop();
					break;
				}
				record.record(pkt);
			} catch (Exception e) {// 推流失败
				record.stop();
				err_index++;
				System.out.println("采集失败:" + err_index);
				throw e;
			}
			// System.out.println("推流后时间："+(System.currentTimeMillis()-starttime));
		}
	}

	/**
	 * 延迟录制
	 * 
	 * @param starttime
	 *            -开始录制的时间
	 * @param duration
	 *            -持续时长
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void record(long starttime, long duration) throws IOException, InterruptedException {
		long err_index = 0;// 采集或推流失败次数
		long now = System.currentTimeMillis();
		long delay = starttime - now;
		if (starttime > 0 && delay > 0) {
			System.out.println("进入休眠，等待开始时间，需要等待 " + delay / 1000 + " 秒");
			// 休眠
			Thread.sleep(delay);
		}

		for (; (now - starttime) <= duration; now = System.currentTimeMillis()) {
			try {
				Frame pkt = grabber.grabFrame();
				if (pkt == null) {// 采集空包结束
					if (err_index > 3) {// 超过三次则终止录制
						break;
					}
					err_index++;
					continue;
				}
				record.record(pkt);
			} catch (Exception e) {// 采集失败
				record.stop();
				throw e;
			}
		}
		record.stop();
	}

	/**
	 * 立即录制
	 * 
	 * @param duration
	 *            -持续时长
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void record(long duration) throws IOException, InterruptedException {
		long err_index = 0;// 采集或推流失败次数
		long now = System.currentTimeMillis();
		long starttime = now;
		for (; (now - starttime) <= duration; now = System.currentTimeMillis()) {
			System.out.println("持续录制" + now);
			try {
				Frame pkt = grabber.grabFrame();
				if (pkt == null) {// 采集空包结束
					if (err_index > 3) {// 超过三次则终止录制
						break;
					}
					err_index++;
					continue;
				}
				record.record(pkt);
			} catch (Exception e) {// 推流失败
				record.stop();
				throw e;
			}
		}
		record.stop();
	}

	/**
	 * 开始
	 *
	 * @return
	 */
	public JavaCVRecord start() {

		if (cuThread == null) {
			String name=THREAD_NAME+nextThreadNum();
			cuThread = new RecordThread(name,grabber, record, 1);
			cuThread.setDaemon(false);
			System.out.println("null线程名"+cuThread.getName());
			cuThread.model=this.model;
			cuThread.src=this.src;
			cuThread.out=this.out;
			cuThread.start();
		}else {
			System.out.println("线程名"+cuThread.getName());
			cuThread.model=this.model;
			cuThread.src=this.src;
			cuThread.out=this.out;
			cuThread.reset(grabber, record);// 重置
			cuThread.carryon();
		}

		return this;
	}

	/**
	 * 重新开始，实际链式调用了：from(src).to(out).start()
	 * 
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	public Recorder restart() throws Exception, IOException {
		return from(src).to(out).start();
	}

	/**
	 * 暂停
	 * 
	 * @return
	 */
	public JavaCVRecord pause() {
		System.out.println("暂停"+cuThread.getName());
		if (cuThread != null && cuThread.isAlive()) {
			cuThread.pause();
		}
		return this;
	}

	/**
	 * 从暂停中恢复
	 * 
	 * @return
	 */
	public JavaCVRecord carryon() {
		System.out.println("恢复"+cuThread.getName());
		if (cuThread != null && cuThread.isAlive()) {
			cuThread.carryon();
		}
		return this;
	}

	/**
	 * 停止录制线程和录制器
	 * 
	 * @return
	 */
	public JavaCVRecord stop() {
		System.out.println("停止"+cuThread.getName());
		if (cuThread != null && cuThread.isAlive()) {
			cuThread.over();// 先结束线程，然后终止录制
		}
		return this;
	}

}