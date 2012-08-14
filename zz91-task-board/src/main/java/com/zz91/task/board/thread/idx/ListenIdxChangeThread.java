/**
 * 
 */
package com.zz91.task.board.thread.idx;

import com.zz91.task.board.service.JobStatusService;
import com.zz91.task.board.thread.TaskControlThread;
import com.zz91.task.common.domain.IdxTaskDefine;

/**
 * 监听数据库变化
 * @author mays
 *
 */
public class ListenIdxChangeThread extends Thread {

	JobStatusService jobStatusService;
	
	static boolean runSwitch = true;
	
	@Override
	public void run(){
		
		while(runSwitch){
			if(TaskControlThread.getTaskSize()<=20){
				
				long planEnd=System.currentTimeMillis();
				
				String jobName=null;
				long min=System.currentTimeMillis();
				for(String t: TaskControlThread.LAST_BUILD_TIME_MAP.keySet()){
					if(min>TaskControlThread.LAST_BUILD_TIME_MAP.get(t).longValue()){
						min=TaskControlThread.LAST_BUILD_TIME_MAP.get(t).longValue();
						jobName=t;
					}
				}
				
				if(jobName==null){
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}
					continue;
				}
				
				TaskControlThread.LAST_BUILD_TIME_MAP.put(jobName, planEnd);
				try {
					if(TaskControlThread.BUILD_TASK_MAP.get(jobName).idxReq(min, planEnd)){
						IdxTaskDefine task=new IdxTaskDefine();
						task.setJobName(jobName);
						task.setStart(min);
						task.setEnd(planEnd);
						
						TaskControlThread.excute(new BuildIdxThread(task, jobStatusService));
					}
				} catch (Exception e) {
				}
			}
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}
		
	}
	
	public void setJobStatusService(JobStatusService service) {
		this.jobStatusService = service;
	}
	
}
