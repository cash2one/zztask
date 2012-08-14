package com.zz91.task.board.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;
import org.springframework.stereotype.Component;

import com.zz91.task.board.dao.JobDefinitionDao;
import com.zz91.task.board.domain.JobDefinition;
import com.zz91.task.board.dto.Pager;

@Component("jobDefinitionDao")
public class JobDefinitionDaoIbatisImpl extends SqlMapClientDaoSupport
		implements JobDefinitionDao {

	final static String SQL_PREFIX = "jobDefinition.";

	@Override
	public Integer deleteJobDefinition(Integer id) {
		return getSqlMapClientTemplate().delete(
				SQL_PREFIX + "deleteJobDefinitionById", id);
	}

	@Override
	public Integer insertJobDefinition(JobDefinition jobDefinition) {
		return (Integer) getSqlMapClientTemplate().insert(
				SQL_PREFIX + "insertJobDefinition", jobDefinition);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JobDefinition> queryAllJobDefinition(String isinuse) {
		return getSqlMapClientTemplate().queryForList(
				SQL_PREFIX + "queryAllJobDefinition", isinuse);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JobDefinition> queryJobDefinition(String isinuse, String jobGroup,
			Pager<JobDefinition> page) {
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("isInUse", isinuse);
		root.put("jobGroup", jobGroup);
		root.put("page", page);

		return getSqlMapClientTemplate().queryForList(
				SQL_PREFIX + "queryJobDefinition", root);
	}

	@Override
	public Integer queryJobDefinitionCount(String isinuse, String jobGroup) {
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("isInUse", isinuse);
		root.put("jobGroup", jobGroup);
		return (Integer) getSqlMapClientTemplate().queryForObject(
				SQL_PREFIX + "queryJobDefinitionCount", root);
	}

	public JobDefinition queryJobDefinitionById(Integer id) {
		return (JobDefinition) this.getSqlMapClientTemplate().queryForObject(
				SQL_PREFIX + "queryJobDefinitionById", id);
	}

	public JobDefinition queryJobDefinitionByName(String paramName) {
		return (JobDefinition) this.getSqlMapClientTemplate().queryForObject(
				SQL_PREFIX + "queryJobDefinitionByName", paramName);
	}

	public Integer updateJobDefinition(JobDefinition jobDefinition) {
		return getSqlMapClientTemplate().update(
				SQL_PREFIX+"updateJobDefinition", jobDefinition);
	}

	@Override
	public Integer updateIsInUseById(Integer id, String isinuse) {
		
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("id", id);
		root.put("isInUse", isinuse);
		
		return getSqlMapClientTemplate().update(SQL_PREFIX+"updateIsInUseById", root);
	}

	@Override
	public Integer updateStartDateById(Date startDate, Integer id) {

		Map<String, Object> root = new HashMap<String, Object>();
		root.put("startTime", startDate);
		root.put("id", id);
		return getSqlMapClientTemplate().update(SQL_PREFIX+"updateStartDateById", root);
	}

	@Override
	public Integer updateEndTime(String jobName, Date endTime) {
		Map<String, Object> root =  new HashMap<String, Object>();
		root.put("jobName", jobName);
		root.put("endTime", endTime);
		return getSqlMapClientTemplate().update(SQL_PREFIX+"updateEndTime", root);
	}

}
