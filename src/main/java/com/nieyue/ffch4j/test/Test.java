package com.nieyue.ffch4j.test;

import com.nieyue.ffch4j.CommandManager;
import com.nieyue.ffch4j.CommandManagerImpl;
import com.nieyue.ffch4j.commandbuidler.CommandBuidlerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试
 * @author eguid
 * @since jdk1.7
 * @version 2017年10月13日
 */
public class Test {
	/**
	 * 命令组装器测试
	 * @throws InterruptedException
	 */
	public static void test1() throws InterruptedException{
		CommandManager manager = new CommandManagerImpl();
		Map<String,String> map = new HashMap<String,String>();
		map.put("appName", "channel20809665");
		map.put("input", "http://bytedance.hdllive.ks-cdn.com/live/channel20809260.flv");
		map.put("output", "rtmp://bytedance.uplive.ks-cdn.com/live/");
		//map.put("codec", "h264");
		map.put("codec", "copy");
		map.put("fmt", "flv");
		map.put("fps", "25");
		map.put("rs", "640x360");
		map.put("twoPart", "0");
		// 执行任务，id就是appName，如果执行失败返回为null
		String id = manager.start(map);
		System.out.println(id);
		// 通过id查询
		//CommandTasker info = manager.query(id);
		//System.out.println(info);
		// 查询全部
		//Collection<CommandTasker> infoList = manager.queryAll();
		//System.out.println(infoList);
		//Thread.sleep(30000);
		// 停止id对应的任务
		// manager.stop(id);
	}
	/**
	 * 默认方式，rtsp->rtmp转流单个命令测试
	 * @throws InterruptedException
	 */
	public static void test2() throws InterruptedException{
		CommandManager manager = new CommandManagerImpl();
		// -rtsp_transport tcp 
		//测试多个任何同时执行和停止情况
		//默认方式发布任务
		manager.start("tomcat", "ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -vcodec copy -acodec copy -f flv -y rtmp://106.14.182.20:1935/rtmp/tomcat");
		
		Thread.sleep(30000);
		// 停止全部任务
		manager.stopAll();
	}
	/**
	 * 完整ffmpeg路径测试
	 * @throws InterruptedException
	 */
	public static void test4() throws InterruptedException{
		CommandManager manager = new CommandManagerImpl();
		// -rtsp_transport tcp 
		//测试多个任何同时执行和停止情况
		//默认方式发布任务
		manager.start("tomcat", "D:/TestWorkspaces/FFmpegCommandHandler/src/cc/eguid/FFmpegCommandManager/ffmpeg/ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -vcodec copy -acodec copy -f flv -y rtmp://106.14.182.20:1935/rtmp/tomcat",true);
		
		Thread.sleep(30000);
		// 停止全部任务
		manager.stopAll();
	}
	
	/**
	 * rtsp-rtmp转流多任务测试
	 * @throws InterruptedException
	 */
	public static void test3() throws InterruptedException{
		CommandManager manager = new CommandManagerImpl();
		// -rtsp_transport tcp 
		//测试多个任何同时执行和停止情况
		//false表示使用配置文件中的ffmpeg路径，true表示本条命令已经包含ffmpeg所在的完整路径
		manager.start("tomcat", "ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -vcodec copy -acodec copy -f flv -y rtmp://106.14.182.20:1935/rtmp/tomcat",false);
		manager.start("tomcat1", "ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -vcodec copy -acodec copy -f flv -y rtmp://106.14.182.20:1935/rtmp/tomcat1",false);
		manager.start("tomcat2", "ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -vcodec copy -acodec copy -f flv -y rtmp://106.14.182.20:1935/rtmp/tomcat2",false);
		manager.start("tomcat3", "ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -vcodec copy -acodec copy -f flv -y rtmp://106.14.182.20:1935/rtmp/tomcat3",false);
		manager.start("tomcat4", "ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -vcodec copy -acodec copy -f flv -y rtmp://106.14.182.20:1935/rtmp/tomcat4",false);
		manager.start("tomcat5", "ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -vcodec copy -acodec copy -f flv -y rtmp://106.14.182.20:1935/rtmp/tomcat5",false);
		manager.start("tomcat6", "ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -vcodec copy -acodec copy -f flv -y rtmp://106.14.182.20:1935/rtmp/tomcat6",false);
		manager.start("tomcat7", "ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -vcodec copy -acodec copy -f flv -y rtmp://106.14.182.20:1935/rtmp/tomcat7",false);
		manager.start("tomcat8", "ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -vcodec copy -acodec copy -f flv -y rtmp://106.14.182.20:1935/rtmp/tomcat8",false);
		manager.start("tomcat9", "ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -vcodec copy -acodec copy -f flv -y rtmp://106.14.182.20:1935/rtmp/tomcat9",false);
		
		Thread.sleep(30000);
		// 停止全部任务
		manager.stopAll();
	}
	
	/**
	 * 测试流式命令行构建器
	 * @throws InterruptedException
	 */
	public static void testStreamCommandAssmbly() throws InterruptedException {
		CommandManager manager = new CommandManagerImpl();
		manager.start("test1", CommandBuidlerFactory.createBuidler()
				.add("ffmpeg").add("-i","rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov")
				.add("-rtsp_transport","tcp")
				.add("-vcodec","copy")
				.add("-acodec","copy")
				.add("-f","flv")
				.add("-y").add("rtmp://106.14.182.20:1935/rtmp/test1"));
		Thread.sleep(30000);
		// 停止全部任务
		manager.stopAll();
	}
	/**
	 * 测试任务中断自动重启任务
	 * @throws InterruptedException 
	 */
	public static void testBroken() throws InterruptedException {
		CommandManager manager = new CommandManagerImpl();
		/* manager.start("test1", CommandBuidlerFactory.createBuidler()
				//.add("ffmpeg")
				.add("ffmpeg-amd64")
				.add("-rtsp_transport", "tcp")
				 .add("-i", "rtsp://120.205.37.100:554/live/ch16070613003727442483.sdp?vcdnid=001")
				 //.add("-i", "rtmp://118.190.133.146:1936/app/test")
				 .add("-vcodec", "copy")
				.add("-acodec", "copy")
				.add("-f", "flv")
				.add("rtmp://xy1.live.huajiao.com/live/channel123456"));
		Thread.sleep(10000);
		manager.stop("test1");
		Thread.sleep(5000);*/
		manager.start("test1", CommandBuidlerFactory.createBuidler()
				//.add("ffmpeg")
				.add("ffmpeg-amd64")
				.add("-rtsp_transport", "tcp")
				.add("-i", "rtsp://120.205.37.100:554/live/ch16070613003727442483.sdp?vcdnid=001")
				//.add("-i", "http://cdnlz.tv189.com/live/laozuo-600k/playlist.m3u8")
				//.add("-i", "rtsp://118.122.175.90:554/PLTV/88888893/224/3221226889/10000100000000060000000000622347_0.smil")
				//.add("-i", "rtmp://118.190.133.146:1936/app/test")
				//.add("-vcodec", "h264")
				//.add("-acodec", "aac")
				.add("-vcodec", "copy")
				.add("-acodec", "copy")
				//.add("-c", "copy")
				//.add("-s", "300X200")
				//.add("-b:v", "100")
				.add("-b", "800k")
				.add("-bufsize", "800k")
				//速率  -r 60 -filter:v "setpts=2.0*PTS"  -filter:a "atempo=2.0"
				//.add("-r", "60")
				//.add("-filter:v", "setpts=1.5*PTS")
				//.add("-filter:a", "atempo=1.5")
				//-filter_complex "[0:v]setpts=0.5*PTS[v];[0:a]atempo=2.0[a]" -map "[v]" -map "[a]"
				//.add("-filter_complex", "[0:v]setpts=1.5*PTS[v];[0:a]atempo=1.5[a]")
				//.add("-map", "[v]")
				//.add("-map", "[a]")
				.add("-f", "flv")
				.add("-absf", "aac_adtstoasc")
				.add("rtmp://xy1.live.huajiao.com/live/channel123456"));
		// 停止全部任务
		//manager.stopAll();
		//manager.destory();

	}
	/**
	 * 批量测试任务中断自动重启任务
	 * @throws InterruptedException 
	 */
	public static void testBrokenMuti() throws InterruptedException {
		CommandManager manager = new CommandManagerImpl();
		manager.start("test1", CommandBuidlerFactory.createBuidler()
				.add("ffmpeg").add("-i","rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov")
				.add("-rtsp_transport","tcp")
				.add("-vcodec","copy")
				.add("-acodec","copy")
				.add("-f","flv")
				.add("-y").add("rtmp://106.14.182.20:1935/rtmp/test1"));
		manager.start("test2", CommandBuidlerFactory.createBuidler()
				.add("ffmpeg").add("-i","rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov")
				.add("-rtsp_transport","tcp")
				.add("-vcodec","copy")
				.add("-acodec","copy")
				.add("-f","flv")
				.add("-y").add("rtmp://106.14.182.20:1935/rtmp/test2"));
		manager.start("test3", CommandBuidlerFactory.createBuidler()
				.add("ffmpeg").add("-i","rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov")
				.add("-rtsp_transport","tcp")
				.add("-vcodec","copy")
				.add("-acodec","copy")
				.add("-f","flv")
				.add("-y").add("rtmp://106.14.182.20:1935/rtmp/test3"));
		manager.start("test4", CommandBuidlerFactory.createBuidler()
				.add("ffmpeg").add("-i","rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov")
				.add("-rtsp_transport","tcp")
				.add("-vcodec","copy")
				.add("-acodec","copy")
				.add("-f","flv")
				.add("-y").add("rtmp://106.14.182.20:1935/rtmp/test4"));
		manager.start("test5", CommandBuidlerFactory.createBuidler()
				.add("ffmpeg").add("-i","rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov")
//				.add("-rtsp_transport","tcp")
				.add("-vcodec","copy")
				.add("-acodec","copy")
				.add("-f","flv")
				.add("-y").add("rtmp://106.14.182.20:1935/rtmp/test5"));
		Thread.sleep(30000);
		// 停止全部任务
		manager.stopAll();
		manager.destory();
	}
	
	public static void main(String[] args) throws InterruptedException {
		testBroken();
		//test1();
		//test2();
		//test3();
		//test4();
		//testStreamCommandAssmbly();
		//testBrokenMuti();
	}
}
