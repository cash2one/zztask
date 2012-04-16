/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-3-24
 */
package com.zz91.task.board.controller.job;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.zz91.task.board.controller.BaseController;
import com.zz91.task.board.domain.JobStatus;
import com.zz91.task.board.dto.ExtResult;
import com.zz91.task.board.dto.Pager;
import com.zz91.task.board.service.JobStatusService;

/**
 * @author mays (mays@zz91.com)
 *
 * created on 2011-3-24
 */
@Controller
public class StatusController extends BaseController {

	@Resource
	JobStatusService jobStatusService;
	
	@RequestMapping
	public ModelAndView index(HttpServletRequest request, Map<String, Object> out){
		return null;
	}
	
	@RequestMapping
	public ModelAndView queryAllStatus(HttpServletRequest request, Map<String, Object> out, Pager<JobStatus> page ){
		page.setLimit(25);
		page = jobStatusService.pageJobStatusByJobName(null, page);
		return printJson(page, out);
	}
	
	@RequestMapping
	public ModelAndView clearStatus(HttpServletRequest request, Map<String, Object> out, String jobName){
		Integer i=jobStatusService.clear(jobName);
		ExtResult result=new ExtResult();
		if(i!=null ){
			result.setSuccess(true);
			result.setData(i);
		}
		return printJson(result, out);
	}
	
}
