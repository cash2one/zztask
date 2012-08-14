package com.zz91.task.board.dao;

import java.util.Date;
import java.util.List;

import com.zz91.task.board.domain.JobDefinition;
import com.zz91.task.board.dto.Pager;

public interface JobDefinitionDao {

	final static String ISUSE_TRUE = "1";

	final static String ISUSE_FALSE = "0";

	public Integer queryJobDefinitionCount(String isinuse, String jobGroup);

	public List<JobDefinition> queryJobDefinition(String isinuse, String jobGroup,
			Pager<JobDefinition> page);
	
	public Integer deleteJobDefinition(Integer id);
	public Integer insertJobDefinition(JobDefinition jobDefinition);
	
	public List<JobDefinition> queryAllJobDefinition(String isinuse);

	public Integer updateJobDefinition(JobDefinition jobDefinition);
	public JobDefinition queryJobDefinitionByName(String paramName);
	public JobDefinition queryJobDefinitionById(Integer id);
	
	public Integer updateIsInUseById(Integer id, String isinuse);
	
	public Integer updateStartDateById(Date startDate, Integer id);
	public Integer updateEndTime(String jobName, Date endTime);
}
