/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-3-15
 */
package com.zz91.task.board.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.zz91.task.board.dto.ExtResult;
import com.zz91.task.board.thread.TaskControlThread;
import com.zz91.task.board.util.TaskConst;
import com.zz91.task.board.util.TimeHelper;
import com.zz91.util.auth.AuthMenu;
import com.zz91.util.auth.AuthUtils;
import com.zz91.util.auth.SessionUser;
import com.zz91.util.datetime.DateUtil;

/**
 * @author mays (mays@zz91.com)
 * 
 *         created on 2011-3-15
 */
@Controller
public class RootController extends BaseController {

	final static Logger LOG = Logger.getLogger(RootController.class);

	@RequestMapping
	public ModelAndView index(HttpServletRequest request,
			Map<String, Object> out) {
		out.put("staffName", AuthUtils.getInstance().queryStaffNameOfAccount(getCachedUser(request).getAccount()));
		return null;
	}

	@RequestMapping
	public ModelAndView login(HttpServletRequest request,
			Map<String, Object> out) {

		return null;
	}
	
	@RequestMapping
	public ModelAndView logout(Map<String, Object> out, HttpServletRequest request, HttpServletResponse response){
		AuthUtils.getInstance().logout(request, response, null);
		return new ModelAndView("redirect:login.htm");
	}

	@RequestMapping
	public ModelAndView authorize(HttpServletRequest request,HttpServletResponse response, 
			Map<String, Object> out, String username, String password) {
		SessionUser sessionUser = AuthUtils.getInstance().validateUser(response, username, password, TaskConst.PROJECT_CODE, TaskConst.PROJECT_PASSWORD);
		ExtResult result = new ExtResult();
		if(sessionUser!=null){
			setSessionUser(request, sessionUser);
			result.setSuccess(true);
		}else{
			result.setData("用户名或者密码写错了，检查下大小写是否都正确了，再试一次吧 :)");
		}
		return printJson(result, out);
	}

	@RequestMapping
	public ModelAndView welcome(HttpServletRequest request,
			Map<String, Object> out) {
		
		return null;
	}

	@RequestMapping
	public ModelAndView mymenu(String parentCode, Map<String, Object> out, HttpServletRequest request){
		if(parentCode==null){
			parentCode="";
		}
		SessionUser sessionUser = getCachedUser(request);
		List<AuthMenu> list = AuthUtils.getInstance().queryMenuByParent(parentCode, TaskConst.PROJECT_CODE, sessionUser.getAccount());
		return printJson(list, out);
	}

	@RequestMapping
	public ModelAndView monitor(HttpServletRequest request,
			Map<String, Object> out) {
		return printJson("{'numQueue':" + TaskControlThread.getNumQueue()
				+ ",'numTask':" + TaskControlThread.getNumTask() + ",'totalTime':'"
				+ TimeHelper.formatTime(TaskControlThread.getTotalTime() / 1000000)
				+ "','nowDate':'"
				+ DateUtil.toString(new Date(), "yyyy-MM-dd hh:mm:ss") + "'}",
				out);
	}
	
}
