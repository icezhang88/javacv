package com.nieyue.javacv.recorder;

import com.nieyue.bean.Live;
import com.nieyue.exception.CommonRollbackException;
import com.nieyue.util.SingletonHashMap;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_YUV420P;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_FLTP;

public class JavaCVRecord implements Recorder {
	private static int threadInitNumber;
	private static synchronized int nextThreadNum() {
		return threadInitNumber++;
	}
	private final static String THREAD_NAME="录像工作线程";
	FFmpegFrameGrabberPlus grabber = null;
	FFmpegFrameRecorder record = null;
	private volatile Live live;
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

	public JavaCVRecord(Live live) {
		super();
		this.live=live;
		if(this.live.getStatus()==null){
			this.live.setStatus(0);
		}
		//1转码，2转流
		if(this.live.getModel()==null){
			this.live.setModel(1);
		}
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
		shm.put("grabberisstart"+live.getSourceUrl(),0);
		shm.put("grabberstarttime"+live.getSourceUrl(),new Date().getTime());
		boolean[] isstop={false};
		Thread thread = new Thread() {
			@Override
			public void run() {
				while (!isstop[0]) {
					try {
						this.sleep(1000);
					} catch (InterruptedException e) {
					}
					Object grabberisstarto = shm.get("grabberisstart" + live.getSourceUrl());
					Object grabberstarttimeo = shm.get("grabberstarttime" + live.getSourceUrl());
					if (grabberisstarto != null&& grabberstarttimeo != null) {
						Integer grabberisstart = (Integer) grabberisstarto;
						Long grabberstarttime = (Long) grabberstarttimeo;
						//System.out.println(notifyDate);
						//System.out.println(new Date().getTime());
						//System.out.println(new Date().getTime() - timeout);
						//成功了
						if(grabberisstart==1){
							isstop[0] = true;
							shm.remove("grabberisstart" + live.getSourceUrl());
							shm.remove("grabberstarttime" + live.getSourceUrl());
							break;
						}else if ((grabberstarttime <= new Date().getTime() - 2000)&&grabberisstart==0) {
							//超过两秒
							isstop[0] = true;
							shm.remove("grabberstart" + live.getSourceUrl());
							shm.remove("grabberstarttime" + live.getSourceUrl());
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
		shm.put("grabberisstart"+live.getSourceUrl(),1);
	}
	/**
	 * 视频源
	 * 
	 * @return
	 * @throws Exception
	 */
	public Recorder from() {
		if (live.getSourceUrl() == null) {
			throw new CommonRollbackException("源视频不能为空");
		}
		// 采集/抓取器
		grabber = new FFmpegFrameGrabberPlus(live.getSourceUrl());
		if (hasRTSP(live.getSourceUrl())) {
			grabber.setOption("rtsp_transport", "tcp");
		}
		//jmxthread();
		//重复启动,默认不能启动
		boolean canstart=false;
		while(!canstart ){
			canstart=true;
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
			}
			try{
				grabber.start();
			}catch (FrameGrabber.Exception e){
				canstart=false;
				if(live.getStatus()!=3){
					throw new CommonRollbackException("源视频启动失败");
				}
			}finally{
				if(canstart){
					break;
				}
			}
		}
		if (live.getWidth() <=0 || live.getHeight() <= 0) {
			live.setWidth(grabber.getImageWidth());
			live.setHeight(grabber.getImageHeight());
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
	 * @param framerate-帧率
	 * @param bitrate-比特率
	 * @return
	 */
	public Recorder videoParam( int framerate, int bitrate) {

		this.framerate = framerate;
		this.bitrate = bitrate;
		return this;
	}

	/**
	 * 输出视频到文件或者流服务
	 * @return
	 * @throws IOException
	 */
	public Recorder to() {
		if (live.getTargetUrl() == null) {
			throw new CommonRollbackException("输出视频不能为空");
		}
		// 录制/推流器
		record = new FFmpegFrameRecorder(live.getTargetUrl(), live.getWidth(), live.getHeight());
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
		if (hasRTMPFLV(live.getTargetUrl())||hasM3U8(live.getTargetUrl())||hasRTSP(live.getTargetUrl())) {
			// 封装格式flv，并使用h264和aac编码
			record.setFormat("flv");
			record.setVideoCodec(AV_CODEC_ID_H264);
			record.setAudioCodec(AV_CODEC_ID_AAC);
		}else if(hasMP4(live.getTargetUrl())){//MP4
			record.setFormat("mp4");
			record.setVideoCodec(AV_CODEC_ID_H264);
			record.setAudioCodec(AV_CODEC_ID_AAC);
		}
		System.out.println(grabber.getFormatContext());
		//只能转码
		if(hasM3U8(live.getSourceUrl())
				||grabber.getVideoCodec()!=AV_CODEC_ID_H264
				||grabber.getAudioCodec()!=AV_CODEC_ID_AAC
		){
			live.setModel(1);
		}
		if(live.getModel()==1){
			//重复启动,默认不能启动
			boolean canrecordstart=false;
			while(!canrecordstart ){
				canrecordstart=true;
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
				}
				try{
					record.start();
				}catch (FrameRecorder.Exception e){
					canrecordstart=false;
					if(live.getStatus()!=3){
						throw new CommonRollbackException("启动失败,接受端没开启");
					}
				}finally{
					if(canrecordstart){
						break;
					}
				}
			}
		}else{
			//重复启动,默认不能启动
			boolean canrecordstart=false;
			while(!canrecordstart ){
				canrecordstart=true;
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
				}
				try{
					record.start(grabber.getFormatContext());
				}catch (FrameRecorder.Exception e){
					canrecordstart=false;
					if(live.getStatus()!=3){
						throw new CommonRollbackException("启动失败,接受端没开启");
					}
				}finally{
					if(canrecordstart){
						break;
					}
				}
			}
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
	 * @return
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception
	 */
	public Recorder stream(){
		if (live.getSourceUrl() == null || live.getTargetUrl() == null) {
			throw new CommonRollbackException("源视频和输出为空");
		}
		// 采集/抓取器
		grabber = new FFmpegFrameGrabberPlus(live.getSourceUrl() );
		/*try {
			grabber = FFmpegFrameGrabberPlus.createDefault(live.getSourceUrl() );
		} catch (FrameGrabber.Exception e) {
			e.printStackTrace();
		}*/
		if (hasRTSP(live.getSourceUrl() )) {
			grabber.setOption("rtsp_transport", "tcp");
		}
		//重复启动,默认不能启动
		boolean canstart=false;
		while(!canstart ){
			canstart=true;
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
			}
			try{
				grabber.start();
			}catch (FrameGrabber.Exception e){
				canstart=false;
				if(live.getStatus()!=3){
					throw new CommonRollbackException("源视频启动失败");
				}
			}finally{
				if(canstart){
					break;
				}
			}
		}

		if (live.getWidth() <=0 || live.getHeight()  <= 0) {
			live.setWidth(grabber.getImageWidth());
			live.setHeight(grabber.getImageHeight());
		}
		record = new FFmpegFrameRecorder(live.getTargetUrl(), live.getWidth(), live.getHeight());

		//rtmp、flv、rtsp、m3u8
		if (hasRTMPFLV(live.getTargetUrl())||hasM3U8(live.getTargetUrl())||hasRTSP(live.getTargetUrl())) {
			// 封装格式flv，并使用h264和aac编码
			record.setFormat("flv");
			record.setVideoCodec(AV_CODEC_ID_H264);
			record.setAudioCodec(AV_CODEC_ID_AAC);
		}else if(hasMP4(live.getTargetUrl())){//MP4
			record.setFormat("mp4");
			record.setVideoCodec(AV_CODEC_ID_H264);
			record.setAudioCodec(AV_CODEC_ID_AAC);
		}
		record.setOption("fflags", "nobuffer");
		record.setAspectRatio(grabber.getAspectRatio());
		record.setInterleaved(true);
		// 视频帧率(保证视频质量的情况下最低25，低于25会出现闪屏)
		record.setFrameRate(25);
		//关键帧间隔，一般与帧率相同或者是视频帧率的两倍
		record.setGopSize(25 * 2);
		// 不可变(固定)音频比特率
		record.setAudioOption("crf", "0");
		// 最高质量
		record.setAudioQuality(0);
		// 音频比特率
		//record.setAudioBitrate(192000);
		// 音频采样率
		//record.setSampleRate(44100);
		// 双通道(立体声)
		record.setAudioChannels(2);
		System.err.println(grabber.getLengthInAudioFrames());
		//只能转码
		if(hasM3U8(live.getSourceUrl())
				||grabber.getVideoCodec()!=AV_CODEC_ID_H264
				||grabber.getAudioCodec()!=AV_CODEC_ID_AAC
		){
			live.setModel(1);
		}
		if(live.getModel()==1){
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
			//重复启动,默认不能启动
			boolean canrecordstart=false;
			while(!canrecordstart ){
				canrecordstart=true;
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
				}
				try{
					record.start();
				}catch (FrameRecorder.Exception e){
					canrecordstart=false;
					if(live.getStatus()!=3){
						throw new CommonRollbackException("启动失败,接受端没开启");
					}
				}finally{
					if(canrecordstart){
						break;
					}
				}
			}
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

			//System.out.println(oc.metadata());
			/*oc.video_codec_id(AV_CODEC_ID_H264);
			oc.audio_codec_id(AV_CODEC_ID_AAC);
			oc.start_time_realtime(grabber.getTimestamp());
			oc.start_time(grabber.getTimestamp());
			oc.use_wallclock_as_timestamps(1);
			oc.streams(grabber.getVideoStream());*/

			//record.start();
			//record.setAudioCodecName("LC");
			//record.setAudioCodecName("libfdk_aac");
			record.setAudioCodecName("aac_adtstoasc");

			avformat.AVFormatContext oc = grabber.getFormatContext();

			/*final AtomicBoolean interruptFlag = new AtomicBoolean(false);
			avformat.AVIOInterruptCB.Callback_Pointer cp = new avformat.AVIOInterruptCB.Callback_Pointer() {
				@Override
				public int call(Pointer pointer) {
					// 0 - continue, 1 - exit
					int interruptFlagInt = interruptFlag.get() ? 1 : 0;
					System.out.println("callback, interrupt flag == " + interruptFlagInt);
					return interruptFlagInt;
				}

			};
			avformat.avformat_alloc_context();
			avformat.AVIOInterruptCB cb = new avformat.AVIOInterruptCB();
			cb.callback(cp);
			oc.interrupt_callback(cb);*/
			//	JSONObject json = JSONObject.fromObject(oc.audio_codec());
			//重复启动,默认不能启动
			boolean canrecordstart=false;
			while(!canrecordstart ){
				canrecordstart=true;
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
				}
				try{
					record.start(oc);
				}catch (FrameRecorder.Exception e){
					canrecordstart=false;
					if(live.getStatus()!=3){
						throw new CommonRollbackException("启动失败,接受端没开启");
					}
				}finally{
					if(canrecordstart){
						break;
					}
				}
			}
		}
		return this;
	}

	/**
	 * 开始
	 *
	 * @return
	 */
	public JavaCVRecord start() {

		//保障能重启能运行
		if(live.getStatus()==3){
			live.setStatus(1);
		}
		if (cuThread == null) {
			String name=THREAD_NAME+nextThreadNum();
 			cuThread = new RecordThread(live,name,grabber, record, 1);
			cuThread.setDaemon(false);
			//System.out.println("null线程名"+cuThread.getName());
			cuThread.start();

		}else {
			//System.out.println("线程名"+cuThread.getName());
			cuThread.reset(live,grabber, record);// 重置
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
		return from().to().start();
	}

	/**
	 * 暂停
	 * 
	 * @return
	 */
	public JavaCVRecord pause() {
		//System.out.println("暂停"+cuThread.getName());
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
		//System.out.println("恢复"+cuThread.getName());
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
		//System.out.println("停止"+cuThread.getName());
		if (cuThread != null && cuThread.isAlive()) {
			cuThread.over();// 先结束线程，然后终止录制
		}
		return this;
	}

}