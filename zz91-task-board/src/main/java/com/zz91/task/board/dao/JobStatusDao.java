package com.zz91.task.board.dao;

import java.util.Date;
import java.util.List;

import com.zz91.task.board.domain.JobStatus;
import com.zz91.task.board.dto.Pager;

public interface JobStatusDao {

	public Integer insertJobStatus(JobStatus jobStatus);

	public List<JobStatus> queryJobStatusByJobName(String jobName,
			Pager<JobStatus> page);

	public Integer queryJobStatusByJobNameCount(String jobName);

	public JobStatus queryJobStatusByBaseTime(Date baseTime, String jobName);

	public Integer updateJobStatus(JobStatus jobStatus);

	public Integer deleteByJobname(String jobName);
}
