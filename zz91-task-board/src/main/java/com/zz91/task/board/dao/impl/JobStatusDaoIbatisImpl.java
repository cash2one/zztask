package com.zz91.task.board.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;
import org.springframework.stereotype.Repository;

import com.zz91.task.board.dao.JobStatusDao;
import com.zz91.task.board.domain.JobStatus;
import com.zz91.task.board.dto.Pager;

@Repository("jobStatusDao")
public class JobStatusDaoIbatisImpl extends SqlMapClientDaoSupport implements
		JobStatusDao {

	final static String SQL_PREFIX = "jobStatus.";

	@Override
	public Integer insertJobStatus(JobStatus jobStatus) {
		return (Integer) getSqlMapClientTemplate().insert(
				SQL_PREFIX + "insertJobStatus", jobStatus);
	}

	@Override
	public JobStatus queryJobStatusByBaseTime(Date baseTime, String jobName) {
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("gmtBasetime", baseTime);
		root.put("jobName", jobName);

		return (JobStatus) getSqlMapClientTemplate().queryForObject(
				SQL_PREFIX + "queryJobStatusByBaseTime", root);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JobStatus> queryJobStatusByJobName(String jobName,
			Pager<JobStatus> page) {
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("jobName", jobName);
		root.put("page", page);

		return getSqlMapClientTemplate().queryForList(
				SQL_PREFIX + "queryJobStatusByJobName", root);
	}

	@Override
	public Integer queryJobStatusByJobNameCount(String jobName) {
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("jobName", jobName);
		return (Integer) getSqlMapClientTemplate().queryForObject(
				SQL_PREFIX + "queryJobStatusByJobNameCount", root);
	}

	@Override
	public Integer updateJobStatus(JobStatus jobStatus) {
		return getSqlMapClientTemplate().update(SQL_PREFIX + "updateJobStatus",
				jobStatus);
	}

	@Override
	public Integer deleteByJobname(String jobName) {
		
		return getSqlMapClientTemplate().delete(SQL_PREFIX + "deleteByJobname", jobName);
	}

}
