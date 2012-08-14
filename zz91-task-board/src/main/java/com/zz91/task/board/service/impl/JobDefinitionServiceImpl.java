/**
 * 
 */
package com.zz91.task.board.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.zz91.task.board.dao.JobDefinitionDao;
import com.zz91.task.board.domain.JobDefinition;
import com.zz91.task.board.dto.Pager;
import com.zz91.task.board.service.JobDefinitionService;
import com.zz91.util.Assert;
import com.zz91.util.lang.StringUtils;

/**
 * @author yuyh
 *
 */
@Component("jobDefinitionService")
public class JobDefinitionServiceImpl implements JobDefinitionService{
	
	@Resource
	private JobDefinitionDao jobDefinitionDao;
	
	@Override
	public Pager<JobDefinition> pageJobDefinition(Boolean isuse, String jobGroup,
			Pager<JobDefinition> page) {
		String isinuse=null;
		if(isuse!=null && isuse){
			isinuse = JobDefinitionDao.ISUSE_TRUE;
		}
		if(isuse!=null && !isuse){
			isinuse = JobDefinitionDao.ISUSE_FALSE;
		}
		
		if(StringUtils.isEmpty(jobGroup)){
			jobGroup = JobDefinitionService.GROUP;
		}
		
		page.setTotals(jobDefinitionDao.queryJobDefinitionCount(isinuse, jobGroup));
		page.setRecords(jobDefinitionDao.queryJobDefinition(isinuse, jobGroup, page));
		return page;
	}

	@Override
	public Integer deleteJobDefinition(Integer id) {
		Assert.notNull(id, "id can not be null");
		return jobDefinitionDao.deleteJobDefinition(id);
	}

	@Override
	public Integer insertJobDefinition(JobDefinition jobDefinition) {
		Assert.notNull(jobDefinition, "the jobdefinition can not be null");
		
		return jobDefinitionDao.insertJobDefinition(jobDefinition);
	}

	@Override
	public JobDefinition queryJobDefinitionById(Integer id) {
		Assert.notNull(id, "id can not be null");
		return jobDefinitionDao.queryJobDefinitionById(id);
	}

	@Override
	public JobDefinition queryJobDefinitionByName(String jobName) {
		Assert.notNull(jobName, "the jobName can not be null");
		return jobDefinitionDao.queryJobDefinitionByName(jobName);
	}

	@Override
	public Integer updateJobDefinition(JobDefinition jobDefinition) {
		Assert.notNull(jobDefinition, "the jobdefinition can not be null");
		Assert.notNull(jobDefinition.getId(), "the jobdefinition.id can not be null");
		return jobDefinitionDao.updateJobDefinition(jobDefinition);
	}

	@Override
	public List<JobDefinition> queryAllJobDefinition(Boolean isUse) {
		String flag = null;
		if(isUse!=null && isUse){
			flag=JobDefinitionDao.ISUSE_TRUE;
		}
		
		if(isUse!=null && !isUse){
			flag=JobDefinitionDao.ISUSE_FALSE;
		}
		
		return jobDefinitionDao.queryAllJobDefinition(flag);
	}

	@Override
	public Integer startTask(Integer id) {
		Assert.notNull(id, "id can not be null");
		return jobDefinitionDao.updateIsInUseById(id, JobDefinitionDao.ISUSE_TRUE);
	}

	@Override
	public Integer stopTask(Integer id) {
		Assert.notNull(id, "id can not be null");
		return jobDefinitionDao.updateIsInUseById(id, JobDefinitionDao.ISUSE_FALSE);
	}

	@Override
	public Integer updateStartDateById(Date startDate, Integer id) {
		
		return jobDefinitionDao.updateStartDateById(startDate, id);
	}

	@Override
	public Integer updateEndTime(String jobName, Long endTime) {
		
		return jobDefinitionDao.updateEndTime(jobName, new Date(endTime));
	}
}
