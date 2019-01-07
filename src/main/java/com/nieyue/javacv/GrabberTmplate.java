package com.nieyue.javacv;

import com.nieyue.exception.CommonRollbackException;
import org.bytedeco.javacpp.*;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.swscale.*;

public  class GrabberTmplate {

 static {
  // Register all formats and codecs
  av_register_all();
  avformat_network_init();
 }
 //保留，暂不用
 protected int width;//宽度
 protected int height;//高度
 
 /**
  * 打开视频流
  * @param url -url
  * @return
  * @throws CommonRollbackException
  */
 protected avformat.AVFormatContext openInput(String url) throws CommonRollbackException {
  avformat.AVFormatContext pFormatCtx = new avformat.AVFormatContext(null);
  if(avformat_open_input(pFormatCtx, url, null, null)==0) {
   return pFormatCtx;
  }
  throw new CommonRollbackException("不能打开视频url");
 }
 
 /**
  * 检索流信息
  * @param pFormatCtx
  * @return
  */
 protected AVFormatContext findStreamInfo(avformat.AVFormatContext pFormatCtx) throws CommonRollbackException{
  if (avformat_find_stream_info(pFormatCtx, (PointerPointer<?>) null)>= 0) {
   return pFormatCtx;
  }
  throw new CommonRollbackException("没有流信息");
 }
 
 /**
  * 获取第一帧视频位置
  * @param pFormatCtx
  * @return
  */
 protected int findVideoStreamIndex(AVFormatContext pFormatCtx) {
  int i = 0, videoStream = -1;
  for (i = 0; i < pFormatCtx.nb_streams(); i++) {
   AVStream stream=pFormatCtx.streams(i);
   avcodec.AVCodecContext codec=stream.codec();
   if (codec.codec_type() == AVMEDIA_TYPE_VIDEO) {
    videoStream = i;
    break;
   }
  }
  return videoStream;
 }
 
 /**
  * 指定视频帧位置获取对应视频帧
  * @param pFormatCtx
  * @param videoStream
  * @return
  */
 protected avcodec.AVCodecContext findVideoStream(AVFormatContext pFormatCtx , int videoStream)throws CommonRollbackException {
  if(videoStream >=0) {
   // Get a pointer to the codec context for the video stream
   AVStream stream=pFormatCtx.streams(videoStream);
   avcodec.AVCodecContext pCodecCtx = stream.codec();
   return pCodecCtx;
  }
  throw new CommonRollbackException("不能打开视频文件");
 }
 
 /**
  * 查找并尝试打开解码器
  * @return
  */
 protected avcodec.AVCodecContext findAndOpenCodec(avcodec.AVCodecContext pCodecCtx) {
  // Find the decoder for the video stream
  avcodec.AVCodec pCodec = avcodec_find_decoder(pCodecCtx.codec_id());
  if (pCodec == null) {
   System.err.println("Codec not found");
   throw new CommonRollbackException("Codec not found");
  }
  avutil.AVDictionary optionsDict = null;
  // Open codec
  if (avcodec_open2(pCodecCtx, pCodec, optionsDict) < 0) {
   System.err.println("Could not open codec");
   throw new CommonRollbackException("Could not open codec"); // Could not open codec
  }
  return pCodecCtx;
 }
 

 /**
  * 抓取视频帧（默认跳过音频帧和空帧）
  * @param url -视频源（rtsp/rtmp/hls/文件等等）
  * @param fmt - 像素格式，比如AV_PIX_FMT_BGR24
  * @return
  * @throws IOException
  */
 public ByteBuffer grabVideoFrame(String url, int fmt) throws IOException {
  // Open video file
  AVFormatContext pFormatCtx=openInput(url);

  // Retrieve stream information
  pFormatCtx=findStreamInfo(pFormatCtx);

  // Dump information about file onto standard error
  //av_dump_format(pFormatCtx, 0, url, 0);

  //Find a video stream
  int videoStream=findVideoStreamIndex(pFormatCtx);
  avcodec.AVCodecContext pCodecCtx =findVideoStream(pFormatCtx,videoStream);
  
  // Find the decoder for the video stream
  pCodecCtx= findAndOpenCodec(pCodecCtx);

  // Allocate video frame
  avutil.AVFrame pFrame = av_frame_alloc();
  //Allocate an AVFrame structure
  avutil.AVFrame pFrameRGB = av_frame_alloc();

  width = pCodecCtx.width();
  height = pCodecCtx.height();
  pFrameRGB.width(width);
  pFrameRGB.height(height);
  pFrameRGB.format(fmt);

  // Determine required buffer size and allocate buffer
  int numBytes = avpicture_get_size(fmt, width, height);

  swscale.SwsContext sws_ctx = sws_getContext(width, height, pCodecCtx.pix_fmt(), width, height,fmt, SWS_BILINEAR, null, null, (DoublePointer) null);

  BytePointer buffer = new BytePointer(av_malloc(numBytes));
  // Assign appropriate parts of buffer to image planes in pFrameRGB
  // Note that pFrameRGB is an AVFrame, but AVFrame is a superset
  // of AVPicture
  avpicture_fill(new AVPicture(pFrameRGB), buffer, fmt, width, height);
  AVPacket packet = new AVPacket();
  int[] frameFinished = new int[1];
  try {
   // Read frames and save first five frames to disk
   while (av_read_frame(pFormatCtx, packet) >= 0) {
    // Is this a packet from the video stream?
    if (packet.stream_index() == videoStream) {
     // Decode video frame
     avcodec_decode_video2(pCodecCtx, pFrame, frameFinished, packet);
     // Did we get a video frame?
     if (frameFinished != null&&frameFinished[0] != 0) {
      // Convert the image from its native format to BGR
      sws_scale(sws_ctx, pFrame.data(), pFrame.linesize(), 0, height, pFrameRGB.data(),pFrameRGB.linesize());
      //Convert BGR to ByteBuffer
      return saveFrame(pFrameRGB, width, height);
     }
    }
    // Free the packet that was allocated by av_read_frame
    av_free_packet(packet);
   }
   return null;
  }finally {
   //Don't free buffer
//   av_free(buffer);
   av_free(pFrameRGB);// Free the RGB image
   av_free(pFrame);// Free the YUV frame
   sws_freeContext(sws_ctx);//Free SwsContext
   avcodec_close(pCodecCtx);// Close the codec
   avformat_close_input(pFormatCtx);// Close the video file
  }
 }
 
 /**
  * BGR图像帧转字节缓冲区（BGR结构）
  *
  * @param pFrameRGB
  * -bgr图像帧
  * @param width
  * -宽度
  * @param height
  * -高度
  * @return
  * @throws IOException
  */
  ByteBuffer saveFrame(AVFrame pFrameRGB, int width, int height){
      return null;
  };
 /**
    * 打开视频流或者视频文件，并解码视频帧为YUVJ420P数据
    *
    * @param url -视频源地址
    * @param out_file 截图文件保存地址
    * @author eguid
    * @throws IOException
    */
 private int openVideo(String url,String out_file) throws IOException {
  AVFormatContext pFormatCtx = new AVFormatContext(null);
  int i, videoStream;
  AVCodecContext pCodecCtx = null;
  AVCodec pCodec = null;
  AVFrame pFrame = null;
  AVPacket packet = new AVPacket();
  int[] frameFinished = new int[1];
  AVDictionary optionsDict = null;

  AVFrame pFrameRGB = null;
  int numBytes;
  BytePointer buffer = null;
  SwsContext sws_ctx = null;

  // Open video file

  if (avformat_open_input(pFormatCtx, url, null, null) != 0) {
   return -1; // Couldn't open file
  }

  // Retrieve stream information
  if (avformat_find_stream_info(pFormatCtx, (PointerPointer<Pointer>) null) < 0) {
   return -1;// Couldn't find stream information
  }

  av_dump_format(pFormatCtx, 0, url, 0);// Dump information about file onto standard error

  // Find the first video stream
  videoStream = -1;
  for (i = 0; i < pFormatCtx.nb_streams(); i++) {
   if (pFormatCtx.streams(i).codec().codec_type() == AVMEDIA_TYPE_VIDEO) {
    videoStream = i;
    break;
   }
  }
  if (videoStream == -1) {
   return -1; // Didn't find a video stream
  }

  // Get a pointer to the codec context for the video stream
  pCodecCtx = pFormatCtx.streams(videoStream).codec();

  // Find the decoder for the video stream
  pCodec = avcodec_find_decoder(pCodecCtx.codec_id());
  if (pCodec == null) {
   System.err.println("Unsupported codec!");
   return -1; // Codec not found
  }
  // Open codec
  if (avcodec_open2(pCodecCtx, pCodec, optionsDict) < 0) {
   return -1; // Could not open codec
  }

  pFrame = av_frame_alloc();// Allocate video frame

  // Allocate an AVFrame structure
  pFrameRGB = av_frame_alloc();
  if (pFrameRGB == null) {
   return -1;
  }
  int width = pCodecCtx.width(), height = pCodecCtx.height();
  pFrameRGB.width(width);
  pFrameRGB.height(height);
  pFrameRGB.format(AV_PIX_FMT_YUVJ420P);
  // Determine required buffer size and allocate buffer
  numBytes = avpicture_get_size(AV_PIX_FMT_YUVJ420P, width, height);

  buffer = new BytePointer(av_malloc(numBytes));

  sws_ctx = sws_getContext(pCodecCtx.width(), pCodecCtx.height(), pCodecCtx.pix_fmt(), pCodecCtx.width(),
              pCodecCtx.height(), AV_PIX_FMT_YUVJ420P, SWS_BICUBIC, null, null, (DoublePointer) null);

  // Assign appropriate parts of buffer to image planes in pFrameRGB
  // Note that pFrameRGB is an AVFrame, but AVFrame is a superset
  // of AVPicture
  avpicture_fill(new AVPicture(pFrameRGB), buffer, AV_PIX_FMT_YUVJ420P, pCodecCtx.width(), pCodecCtx.height());

  // Read frames and save first five frames to disk
  
  int ret=-1;
  while (av_read_frame(pFormatCtx, packet) >= 0) {
   if (packet.stream_index() == videoStream) {// Is this a packet from the video stream?
    avcodec_decode_video2(pCodecCtx, pFrame, frameFinished, packet);// Decode video frame

    // Did we get a video frame?
    if (frameFinished != null) {
     // 转换图像格式，将解压出来的YUV420P的图像转换为YUVJ420P的图像
     sws_scale(sws_ctx, pFrame.data(), pFrame.linesize(), 0, pCodecCtx.height(), pFrameRGB.data(),
                    pFrameRGB.linesize());
    }

    if (frameFinished[0] != 0 && !pFrame.isNull()) {
     // Convert the image from its native format to YUVJ420P
     sws_scale(sws_ctx, pFrame.data(), pFrame.linesize(), 0, pCodecCtx.height(), pFrameRGB.data(),pFrameRGB.linesize());
     if((ret=saveImg(pFrameRGB,out_file))>=0) {
      break;
     }
    }
   }

  }
  av_free_packet(packet);// Free the packet that was allocated by av_read_frame
  // Free the RGB image
  av_free(buffer);

  av_free(pFrameRGB);

  av_free(pFrame);// Free the YUV frame

  avcodec_close(pCodecCtx);// Close the codec

  avformat_close_input(pFormatCtx);// Close the video file

  return ret;
 }

 /**
   * 把YUVJ420P数据编码保存成jpg图片
   *
   * @param pFrame -图像帧
   * @param out_file -截图文件保存地址
   * @author eguid
   * @return
   */
         private int saveImg(AVFrame pFrame, String out_file) {

       av_log_set_level(AV_LOG_ERROR);//设置FFmpeg日志级别（默认是debug，设置成error可以屏蔽大多数不必要的控制台消息）
       AVPacket pkt = null;
       AVStream pAVStream = null;
       AVCodec codec = null;
       int ret = -1;

       int width = pFrame.width(), height = pFrame.height();
       // 分配AVFormatContext对象
       AVFormatContext pFormatCtx = avformat_alloc_context();
       // 设置输出文件格式
       pFormatCtx.oformat(av_guess_format("mjpeg", null, null));
       if (pFormatCtx.oformat() == null) {
        return -1;
       }
       try {
        // 创建并初始化一个和该url相关的AVIOContext
        AVIOContext pb = new AVIOContext();
        if (avio_open(pb, out_file, AVIO_FLAG_READ_WRITE) < 0) {// dont open file
         return -1;
        }
        pFormatCtx.pb(pb);
        // 构建一个新stream
        pAVStream = avformat_new_stream(pFormatCtx, codec);
        if (pAVStream == null) {
         return -1;
        }
        int codec_id = pFormatCtx.oformat().video_codec();
        // 设置该stream的信息
        // AVCodecContext pCodecCtx = pAVStream.codec();
        AVCodecContext pCodecCtx = pAVStream.codec();
        pCodecCtx.codec_id(codec_id);
        pCodecCtx.codec_type(AVMEDIA_TYPE_VIDEO);
        pCodecCtx.pix_fmt(AV_PIX_FMT_YUVJ420P);
        pCodecCtx.width(width);
        pCodecCtx.height(height);
        pCodecCtx.time_base().num(1);
        pCodecCtx.time_base().den(25);

        // Begin Output some information
        av_dump_format(pFormatCtx, 0, out_file, 1);
        // End Output some information

        // 查找解码器
        AVCodec pCodec = avcodec_find_encoder(codec_id);
        if (pCodec == null) {// codec not found
         return -1;
        }
        // 设置pCodecCtx的解码器为pCodec
        if (avcodec_open2(pCodecCtx, pCodec, (PointerPointer<Pointer>) null) < 0) {
         System.err.println("Could not open codec.");
         return -1;
        }

        // Write Header
        avformat_write_header(pFormatCtx, (PointerPointer<Pointer>) null);

        // 给AVPacket分配足够大的空间
        pkt = new AVPacket();
        if (av_new_packet(pkt, width * height * 3) < 0) {
         return -1;
        }
        int[] got_picture = { 0 };
        // encode
        if (avcodec_encode_video2(pCodecCtx, pkt, pFrame, got_picture) >= 0) {
         // flush
         if ((ret = av_write_frame(pFormatCtx, pkt)) >= 0) {
          // Write Trailer
          if (av_write_trailer(pFormatCtx) >= 0) {
           System.err.println("Encode Successful.");
          }
         }
        }
        return ret;
        // 结束时销毁
       } finally {
        if (pkt != null) {
         av_free_packet(pkt);
        }
        if (pAVStream != null) {
         avcodec_close(pAVStream.codec());
        }
        if (pFormatCtx != null) {
         avio_close(pFormatCtx.pb());
         avformat_free_context(pFormatCtx);
        }
       }
      }
    public static void main(String[] args) throws Exception {
        GrabberTmplate grabber = new GrabberTmplate();
        grabber.openVideo("rtsp://120.205.37.100:554/live/ch15021120011915466273.sdp?vcdnid=001","rtmp://bytedance.uplive.ks-cdn.com/live/channel20809990");
          // grabber.grabVideoFrame("rtsp://120.205.37.100:554/live/ch15021120011915466273.sdp?vcdnid=001",0);//AV_PIX_FMT_BGR24
    }
}