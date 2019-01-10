package com.nieyue.javacv.recorder;

import com.nieyue.util.SingletonHashMap;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.Frame;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

/**
 * 录制任务工作线程
 * @author eguid
 *
 */
public class RecordThread extends Thread {
	
	protected FFmpegFrameGrabberPlus grabber =null;
	protected FFmpegFrameRecorderPlus record =null;
	String  src;
	String  out;

	protected HashMap<String,Object> shm=SingletonHashMap.getInstance();
	/**
	 * 运行状态，0-初始状态，1-运行，2-停止,，3-异常停止（重启）
	 */
	protected volatile int status=0;
	protected volatile int pause=0;//是否暂停，1-暂停
	protected int err_stop_num=3;//默认错误数量达到三次终止录制
	protected int model=1;//1编码解码，2封装
	protected long timeout=2*1000;//超时，默认2秒


	public RecordThread(String name,FFmpegFrameGrabberPlus grabber, FFmpegFrameRecorderPlus record,Integer err_stop_num) {
		super(name);
		this.grabber = grabber;
		this.record = record;
		if(err_stop_num!=null) {
			this.err_stop_num=err_stop_num;
		}
	}
	/**
	 * 运行过一次后必须进行重置参数和运行状态
	 */
	public void reset(FFmpegFrameGrabberPlus grabber, FFmpegFrameRecorderPlus record) {
		this.grabber = grabber;
		this.record = record;
		this.status=0;
	}
	
	public int getErr_stop_num() {
		return err_stop_num;
	}
	
	public void setErr_stop_num(int err_stop_num) {
		this.err_stop_num = err_stop_num;
	}
	
	public FFmpegFrameGrabberPlus getGrabber() {
		return grabber;
	}

	public void setGrabber(FFmpegFrameGrabberPlus grabber) {
		this.grabber = grabber;
	}

	public FFmpegFrameRecorderPlus getRecord() {
		return record;
	}

	public void setRecord(FFmpegFrameRecorderPlus record) {
		this.record = record;
	}

	public int getStatus() {
		return status;
	}

	@Override
	public void run() {
	//	jmxthread(this);
		while(true) {
				if(status==2||status==3) {
					try {
						Thread.sleep(25);
					}catch(InterruptedException e) {
					}
					break;
				}
				//核心任务循环
				if(model==1){
					codecLoop();
				}else{
					forwardLoop();
				}

		}
	}
	/**
	 * 监控线程
	 */
	private void jmxthread(RecordThread _this){
		//做唤醒线程,放入名称和时间
		//shm.put("notify"+this.getName(),new Date().getTime());
		 boolean[] isstop = {false};
		Thread thread = new Thread() {
			@Override
			public void run() {
				while (!isstop[0]) {
					try {
						this.sleep(1000);
					} catch (InterruptedException e) {
						//System.out.println(2222);
					}
					Object notifyDateo = shm.get("notify" + _this.getName());
					if (notifyDateo != null) {
						Long notifyDate = (Long) notifyDateo;
						//System.out.println(notifyDate);
						//System.out.println(new Date().getTime());
						//System.out.println(new Date().getTime() - timeout);
						if (notifyDate <= new Date().getTime() - timeout) {
							isstop[0] = true;
							shm.remove("notify" + _this.getName());
							_this.reover();//异常停止（重启）
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
	}
	/**
	 * 转码循环
	 */
	private void codecLoop() {
		long startime=System.currentTimeMillis();
		long err_index = 0;//采集或推流失败次数
		long frame_index=0;
		int pause_num=0;//暂停次数
		if(status==0) {//正在运行
			status=1;
		}
		try {
			for(;status==1;frame_index++) {
				shm.put("notify"+this.getName(),new Date().getTime());
				Frame pkt=grabber.grabFrame();
				if(pause==1) {//暂停状态r
					this.sleep(1000);//不按秒钟算，会溢出
					pause_num++;
					continue;
				}
				if(pkt==null) {//采集空包结束
					if(err_index>err_stop_num) {//超过三次则终止录制
						break;
					}
					err_index++;
					continue;
				}
				try {
					//System.err.println(this.isAlive());
					//System.out.println(this.isInterrupted());
					this.sleep(10);//频率，数字越大cpu越小，视频越卡
				} catch (InterruptedException e) {
					System.out.println(11122233);
				}
				record.record(pkt);
			}

		}catch (Exception e) {//推流失败
			status=3;
			System.err.println("转码异常导致停止录像，详情："+e.getMessage());
		}finally {
			System.err.println("转码录像已停止，持续时长："+(System.currentTimeMillis()-startime)/1000+"秒，共录制："+frame_index+"帧，遇到的错误数："+err_index+",录制期间共暂停次数："+pause_num);
			if(status!=2&&record!=null){
				//不是正常停止需要重启
				try {
					this.sleep(err_index*10000);
				} catch (InterruptedException e) {

				}
				JavaCVRecord jcv =new JavaCVRecord(src,out,record.getImageWidth(),record.getImageHeight(),model);
				try {
					jcv.stream();
				} catch (IOException e) {
				}
				jcv.start();

				return;
			}
			stopRecord();

		}
	}
	/**
	 * 转封装循环
	 */
	private void forwardLoop() {
		long startime=System.currentTimeMillis();
		long err_index = 0;//采集或推流失败次数
		long frame_index=0;
		int pause_num=0;//暂停次数
		if(status==0) {//正在运行
			status=1;
		}
		try {
			for(;status==1;frame_index++) {
				shm.put("notify"+this.getName(),new Date().getTime());
				avcodec.AVPacket  pkt = grabber.grabPacket();
				if (pkt == null || pkt.size() <= 0 || pkt.data() == null) {// 空包结束
					break;
				}
				if(pause==1) {//暂停状态
					this.sleep(1000);//不按秒钟算，会溢出
					pause_num++;
					continue;
				}
				if(pkt==null) {//采集空包结束
					if(err_index>err_stop_num) {//超过三次则终止录制
						break;
					}
					err_index++;
					continue;
				}
				try{
					this.sleep(10);//频率，数字越大cpu越小，视频越卡
				}catch (InterruptedException ie ){
					System.out.println(11122233);
				}

				record.recordPacket(pkt);
			}
		}catch (Exception e) {//推流失败
			status=3;
			System.err.println("转封装异常导致停止录像，详情："+e.getMessage());
		}finally {
			System.err.println("转码录像已停止，持续时长："+(System.currentTimeMillis()-startime)/1000+"秒，共录制："+frame_index+"帧，遇到的错误数："+err_index+",录制期间共暂停次数："+pause_num);
			if(status!=2 &&record!=null){
				try {
					this.sleep(err_index*10000);
				} catch (InterruptedException e) {

				}
				//不是正常停止需要重启
				JavaCVRecord jcv =new JavaCVRecord(src,out,record.getImageWidth(),record.getImageHeight(),model);
				try {
					jcv.stream();
				} catch (IOException e) {
				}
				jcv.start();
				return;
			}
			stopRecord();
		}
	}

	/**
	 * 停止录制
	 */
	private void stopRecord() {
		try {
			if(grabber!=null) {
				grabber.close();
			}
			if(record!=null) {
				record.stop();
			}
		} catch (IOException e) {
		}
	}
	
	/**
	 * 暂停
	 */
	public void pause() {
		pause=1;
	}
	
	/**
	 * 继续（从暂停中恢复）
	 */
	public void carryon() {
		pause=0;
		status=1;
	}
	
	/**
	 * 结束
	 */
	public void over() {
		status=2;
	}
	/**
	 * 异常停止（重启）
	 */
	public void reover() {
		status=3;
	}

}