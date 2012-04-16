/**
 * 
 */
package com.zz91.task.board.service;

import java.util.Date;
import java.util.List;

import com.zz91.task.board.domain.JobDefinition;
import com.zz91.task.board.dto.Pager;

/**
 * @author yuyh
 * 
 */
public interface JobDefinitionService {
	
	public Pager<JobDefinition> pageJobDefinition(Boolean isuse, Pager<JobDefinition> page);
	
	public Integer deleteJobDefinition(Integer id);

	public Integer insertJobDefinition(JobDefinition jobDefinition);

	public JobDefinition queryJobDefinitionById(Integer id);

	public Integer updateJobDefinition(JobDefinition jobDefinition);

	public JobDefinition queryJobDefinitionByName(String jobName);

	public List<JobDefinition> queryAllJobDefinition(Boolean isUse);
	
	public Integer stopTask(Integer id);
	
	public Integer startTask(Integer id);
	
	public Integer updateStartDateById(Date startDate, Integer id);

}
