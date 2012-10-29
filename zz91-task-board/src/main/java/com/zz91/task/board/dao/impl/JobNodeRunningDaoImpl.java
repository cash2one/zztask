/**
 * 
 */
package com.zz91.task.board.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;
import org.springframework.stereotype.Component;

import com.zz91.task.board.dao.JobNodeRunningDao;
import com.zz91.task.board.domain.JobNodeRunning;

/**
 * @author mays
 *
 */
@Component("jobNodeRunningDao")
public class JobNodeRunningDaoImpl extends SqlMapClientDaoSupport implements JobNodeRunningDao {

	
	final static String SQL_PREFIX = "jobNodeRunning.";
	
	@SuppressWarnings("unchecked")
	@Override
	public List<String> queryByNode(String nodeKey) {
		
		return getSqlMapClientTemplate().queryForList(SQL_PREFIX+"queryByNode", nodeKey);
	}

	@Override
	public Integer insertNodeRunning(JobNodeRunning running) {
		return (Integer) getSqlMapClientTemplate().insert(SQL_PREFIX+"insertNodeRunning", running);
	}

	@Override
	public Integer deleteByJob(String jobName, String nodeKey) {
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("jobId", jobName);
		root.put("nodeKey", nodeKey);
		return getSqlMapClientTemplate().delete(SQL_PREFIX+"deleteByJob", root);
	}

}
