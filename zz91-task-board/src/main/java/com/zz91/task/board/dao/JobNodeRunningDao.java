package com.zz91.task.board.dao;

import java.util.List;

import com.zz91.task.board.domain.JobNodeRunning;

public interface JobNodeRunningDao {

	public List<String> queryByNode(String nodeKey);
	
	public Integer insertNodeRunning(JobNodeRunning running);
	
	public Integer deleteByJob(String jobName, String nodeKey);
}
