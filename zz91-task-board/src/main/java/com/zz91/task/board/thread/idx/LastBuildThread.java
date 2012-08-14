/**
 * 
 */
package com.zz91.task.board.thread.idx;

import com.zz91.task.board.service.JobDefinitionService;
import com.zz91.task.board.thread.TaskControlThread;

/**
 * 定期保存最后更新时间
 * @author mays
 *
 */
public class LastBuildThread extends Thread {

	JobDefinitionService jobDefinitionService;
	
	final static long INTERVAL = 300000;
	public static boolean runSwitch = true; 
	
	@Override
	public void run(){
		
		while(runSwitch){
			
			for(String jn:TaskControlThread.LAST_BUILD_TIME_MAP.keySet()){
				jobDefinitionService.updateEndTime(jn, TaskControlThread.LAST_BUILD_TIME_MAP.get(jn));
			}
			
			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
			}
		}
		
	}
	
	public void setJobDefinitionService(JobDefinitionService jobDefinitionService){
		this.jobDefinitionService = jobDefinitionService;
	}
}
