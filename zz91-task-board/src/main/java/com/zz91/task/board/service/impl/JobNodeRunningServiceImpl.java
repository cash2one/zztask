/**
 * 
 */
package com.zz91.task.board.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.zz91.task.board.dao.JobNodeRunningDao;
import com.zz91.task.board.domain.JobNodeRunning;
import com.zz91.task.board.service.JobNodeRunningService;
import com.zz91.task.board.util.TaskConst;

/**
 * @author mays
 *
 */
@Component("jobNodeRunningService")
public class JobNodeRunningServiceImpl implements JobNodeRunningService {

	@Resource
	private JobNodeRunningDao jobNodeRunningDao;
	
	@Override
	public Integer insertNodeRunning(String jobName) {
		JobNodeRunning running = new JobNodeRunning();
		running.setJobId(jobName);
		running.setNodeKey(TaskConst.NODE_KEY);
		return jobNodeRunningDao.insertNodeRunning(running);
	}

	@Override
	public Integer removeRunning(String jobName) {
		
		return jobNodeRunningDao.deleteByJob(jobName, TaskConst.NODE_KEY);
	}

}
