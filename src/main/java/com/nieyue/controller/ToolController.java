package com.nieyue.controller;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;


/**
 * 工具控制类
 * @author yy
 *
 */
@RestController
@RequestMapping("/tool")
public class ToolController {

	/**
	 * 验证码
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/getVerificationCode", method = {RequestMethod.GET,RequestMethod.POST})
	public void getVerificationCode(
			HttpSession session, HttpServletRequest request, HttpServletResponse response) throws Exception{

		return ;
	}



}
