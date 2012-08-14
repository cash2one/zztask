/**
 * 
 */
package com.zz91.task.board.thread.idx;

import java.util.Date;

import org.apache.log4j.Logger;

import com.zz91.task.board.domain.JobStatus;
import com.zz91.task.board.service.JobStatusService;
import com.zz91.task.board.thread.TaskControlThread;
import com.zz91.task.board.util.StacktraceUtil;
import com.zz91.task.common.AbstractIdxTask;

/**
 * @author mays
 *
 */
public class OptimizeThread extends Thread {

	String jobname;
	JobStatusService jobStatusService;
	
	public OptimizeThread(){
		
	}
	
	public OptimizeThread(String jobname, JobStatusService jobStatusService){
		this.jobname = jobname;
		this.jobStatusService = jobStatusService;
	}
	
	Logger LOG = Logger.getLogger("com.zz91.task");
	
	@Override
	public void run() {
		Date start = new Date();
		
		LOG.debug("taskbasetime:"+start.getTime()+" 准备。。。。");
		
		JobStatus status = new JobStatus();
		
		AbstractIdxTask task = TaskControlThread.BUILD_TASK_MAP.get(jobname);
		
		status.setJobName(jobname);
		status.setGmtBasetime(start);
		status.setGmtTrigger(start);
		status.setResult("运行中...");
		status.setId(jobStatusService.insertJobStatus(status));
		
		try {
			LOG.debug("taskbasetime:"+start.getTime()+" 开始。。。。");
			task.optimize();
			LOG.debug("taskbasetime:"+start.getTime()+" 结束。。。。");
			status.setResult("索引优化成功");
			
		} catch (Exception e) {
			status.setResult(e.getMessage());
			status.setErrorMsg(StacktraceUtil.getStackTrace(e));
			LOG.debug("发生错误:"+start.getTime()+" Exception："+e.getMessage()+"  "+StacktraceUtil.getStackTrace(e));
		}
		
		Date end = new Date();
		status.setRuntime(end.getTime() - start.getTime());
		LOG.debug("taskbasetime:"+start.getTime()+" 任务执行结束。。。。");
		jobStatusService.updateJobStatusById(status);
		
	}
}
