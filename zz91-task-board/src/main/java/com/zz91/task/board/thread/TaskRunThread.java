/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-3-18
 */
package com.zz91.task.board.thread;

import java.net.MalformedURLException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.zz91.task.board.domain.JobDefinition;
import com.zz91.task.board.domain.JobStatus;
import com.zz91.task.board.service.JobDefinitionService;
import com.zz91.task.board.service.JobStatusService;
import com.zz91.task.board.util.ClassHelper;
import com.zz91.task.board.util.StacktraceUtil;
import com.zz91.task.common.ZZTask;

/**
 * @author mays (mays@zz91.com)
 * 
 *         created on 2011-3-18
 */
public class TaskRunThread extends Thread {

	JobDefinition definition;

	JobDefinitionService jobDefinitionService;
	JobStatusService jobStatusService;
	
	Date targetDate;
	
	Logger LOG = Logger.getLogger("com.zz91.task");

	public TaskRunThread() {

	}

	public TaskRunThread(JobDefinition job,
			JobDefinitionService jobDefinitionService,
			JobStatusService jobStatusService) {
		this.definition = job;
		this.jobDefinitionService = jobDefinitionService;
		this.jobStatusService = jobStatusService;
	}

	@Override
	public void run() {
		//LOG.debug("taskbasetime:"+targetDate.getTime()+" 任务开始。。。。");
		JobStatus status = new JobStatus();
		Date start = new Date();
		if(targetDate==null){
			targetDate = start;
		}
		try {
			
//			jobDefinitionService.updateStartDateById(start, definition.getId());
			
			status.setJobName(definition.getJobName());
			status.setGmtBasetime(targetDate);
			status.setGmtTrigger(start);
			status.setResult("运行中...");
			status.setId(jobStatusService.insertJobStatus(status));
			LOG.debug("taskbasetime:"+targetDate.getTime()+" 准备。。。。");
			
			ZZTask jobInstance = (ZZTask) ClassHelper.load(
					definition.getJobClasspath(), definition.getJobClassName())
					.newInstance();
			LOG.debug("taskbasetime:"+targetDate.getTime()+" 实例化任务体。。。。");
			LOG.debug(jobInstance);
			jobInstance.clear(targetDate); //清理任务执行前的数据
			if (jobInstance.exec(targetDate)) {
				status.setResult("执行成功");
				LOG.debug("taskbasetime:"+targetDate.getTime()+" 任务执行成功。。。。");
			} else {
				status.setResult("执行失败");
				LOG.debug("taskbasetime:"+targetDate.getTime()+" 任务执行失败。。。。");
			}
			
		} catch (MalformedURLException e) {
			status.setResult(e.getMessage());
			status.setErrorMsg(StacktraceUtil.getStackTrace(e));
		} catch (ClassNotFoundException e) {
			status.setResult(e.getMessage());
			status.setErrorMsg(StacktraceUtil.getStackTrace(e));
		} catch (InstantiationException e) {
			status.setResult(e.getMessage());
			status.setErrorMsg(StacktraceUtil.getStackTrace(e));
		} catch (IllegalAccessException e) {
			status.setResult(e.getMessage());
			status.setErrorMsg(StacktraceUtil.getStackTrace(e));
		} catch (Exception e) {
			status.setResult(e.getMessage());
			status.setErrorMsg(StacktraceUtil.getStackTrace(e));
		}
		
		Date end = new Date();
		status.setRuntime(end.getTime() - start.getTime());
		LOG.debug("taskbasetime:"+targetDate.getTime()+" 任务执行结束。。。。");
		jobStatusService.updateJobStatusById(status);
	}

	public void setJobDefinitionService(JobDefinitionService service) {
		this.jobDefinitionService = service;
	}

	public void setJobStatusService(JobStatusService service) {
		this.jobStatusService = service;
	}
	
	public void setTargetDate(Date targetDate){
		this.targetDate = targetDate;
	}
	
}
