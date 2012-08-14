/**
 * 
 */
package com.zz91.task.board.service.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.zz91.task.board.dao.JobStatusDao;
import com.zz91.task.board.domain.JobStatus;
import com.zz91.task.board.dto.Pager;
import com.zz91.task.board.service.JobStatusService;
import com.zz91.util.Assert;
import com.zz91.util.lang.StringUtils;

/**
 * @author yuyh
 * 
 */
@Component("jobStatusService")
public class JobStatusServiceImpl implements JobStatusService {
	@Resource
	private JobStatusDao jobStatusDao;

	@Override
	public Integer insertJobStatus(JobStatus jobStatus) {
		Assert.notNull(jobStatus, "the jobStatus can not be null");
		return jobStatusDao.insertJobStatus(jobStatus);
	}

	@Override
	public Pager<JobStatus> pageJobStatusByJobName(String jobName,
			Pager<JobStatus> page) {
		if(StringUtils.isEmpty(page.getSort())){
			page.setSort("id");
		}
		page.setTotals(jobStatusDao.queryJobStatusByJobNameCount(jobName));
		page.setRecords(jobStatusDao.queryJobStatusByJobName(jobName, page));
		return page;
	}

	@Override
	public JobStatus queryJobStatusByBaseTime(Date baseTime, String jobName) {
		return jobStatusDao.queryJobStatusByBaseTime(baseTime, jobName);
	}

	@Override
	public Integer updateJobStatusById(JobStatus jobStatus) {
		
		if(jobStatus.getErrorMsg()!=null && jobStatus.getErrorMsg().length()>2000){
			jobStatus.setErrorMsg(jobStatus.getErrorMsg().substring(0, 2000));
		}
		
		return jobStatusDao.updateJobStatus(jobStatus);
	}

	@Override
	public Integer clear(String jobName) {
		if(StringUtils.isEmpty(jobName)){
			return null;
		}
		return jobStatusDao.deleteByJobname(jobName);
	}

}
