package com.nieyue.javacv.recorder;

import java.io.IOException;

/**
 * 录制器接口
 * 
 * @author eguid
 *
 */
public interface Recorder {

	/**
	 * 设置视频源
	 *
	 * @return
	 */
	Recorder from() throws Exception;

	/**
	 * 设置输出文件或推流到流媒体服务
	 *
	 * @return
	 * @throws IOException
	 */
	Recorder to() throws IOException;

	/**
	 * 转发源视频到输出（复制）
	 * 
	 * @return
	 * @throws IOException
	 */
	Recorder stream() throws IOException;

	/**
	 * 设置音频参数
	 * 
	 * @param audioChannels
	 * @param audioBitrate
	 * @param sampleRate
	 * @return
	 */
	Recorder audioParam(int audioChannels, int audioBitrate, int sampleRate);

	/**
	 * 设置视频参数
	 * 
	 * @param framerate
	 * @param bitrate
	 * @return
	 */
	Recorder videoParam( int framerate, int bitrate);

	/**
	 * 开始录制
	 * 
	 * @return
	 */
	Recorder start();

	/**
	 * 暂停录制
	 * 
	 * @return
	 */
	Recorder pause();

	/**
	 * 继续录制（从暂停中恢复）
	 * 
	 * @return
	 */
	Recorder carryon();

	/**
	 * 停止录制
	 * 
	 * @return
	 */
	Recorder stop();
}