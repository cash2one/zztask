/**
 * 
 */
package com.zz91.task.board.service;


/**
 * @author yuyh
 *
 */
public interface JobNodeRunningService {
	
	public Integer insertNodeRunning(String jobName);
	
	public Integer removeRunning(String jobName);
}
