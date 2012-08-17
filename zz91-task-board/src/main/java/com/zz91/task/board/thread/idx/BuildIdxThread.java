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
import com.zz91.task.common.domain.IdxTaskDefine;

/**
 * @author mays
 *
 */
public class BuildIdxThread extends Thread {

	IdxTaskDefine idxTaskDefine;
	JobStatusService jobStatusService;
	
	public BuildIdxThread(){
		
	}
	
	public BuildIdxThread(IdxTaskDefine idxTaskDefine, JobStatusService jobStatusService){
		this.idxTaskDefine = idxTaskDefine;
		this.jobStatusService = jobStatusService;
	}
	
	Logger LOG = Logger.getLogger("com.zz91.task");
	
	@Override
	public void run() {
		Date start = new Date();
		
		LOG.debug("taskbasetime:"+start.getTime()+" 准备。。。。");
		
		JobStatus status = new JobStatus();
		
		AbstractIdxTask task = TaskControlThread.BUILD_TASK_MAP.get(idxTaskDefine.getJobName());
		
		status.setJobName(idxTaskDefine.getJobName());
		status.setGmtBasetime(start);
		status.setGmtTrigger(start);
		status.setResult("运行中...");
		status.setId(jobStatusService.insertJobStatus(status));
		
		try {
			LOG.debug("taskbasetime:"+start.getTime()+" 开始。。。。");
			task.idxPost(idxTaskDefine.getStart(), idxTaskDefine.getEnd());  //调用任务方法，提交索引数据
			LOG.debug("taskbasetime:"+start.getTime()+" 结束。。。。");
			status.setResult("执行成功 ");
			status.setErrorMsg("from/to:"+idxTaskDefine.getStart()+"/"+idxTaskDefine.getEnd());
			
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
