/**
 * 
 */
package com.zz91.task.board.service;

import java.util.Date;

import com.zz91.task.board.domain.JobStatus;
import com.zz91.task.board.dto.Pager;

/**
 * @author yuyh
 *
 */
public interface JobStatusService {
	
	
	public Pager<JobStatus> pageJobStatusByJobName(String jobName, Pager<JobStatus> page);
	
	public JobStatus queryJobStatusByBaseTime(Date baseTime, String jobName);
	
	public Integer updateJobStatusById(JobStatus jobStatus);
	
	public Integer insertJobStatus(JobStatus jobStatus);
	
	public Integer clear(String jobName);
	
}
