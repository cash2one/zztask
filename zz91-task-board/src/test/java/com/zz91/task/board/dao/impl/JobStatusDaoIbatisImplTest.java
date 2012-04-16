package com.zz91.task.board.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.zz91.task.board.dao.JobStatusDao;
import com.zz91.task.board.domain.JobStatus;


public class JobStatusDaoIbatisImplTest extends AbstractDependencyInjectionSpringContextTests {
	@Resource
	JobStatusDao jobStatusDao;

	static List<JobStatus> testData = new ArrayList<JobStatus>();

	protected String[] getConfigLocations() {
		return new String [] {"applicationContext.xml"};
	}

	static {
		for (int i = 0; i < 10; i++) {
			testData.add(createObject("" + i));
		}
	}

	private static JobStatus createObject(String add) {
		JobStatus status = new JobStatus();
//		status.setCron("cron"+add);
//		status.setErrorMsg("ErrorMsg"+add);
//		status.setFinishTime(new Date());
//		status.setJobName("jobName"+add);
//		status.setResult("success");
//		status.setStartExecuteTime(new Date());
		return status;
	}
	
 
//	public void testInsertAndDeleteJobStatus() {
//		JobStatus jobStatus=testData.get(0);
//		jobStatusDao.insertJobStatus(jobStatus);
//		jobStatus = jobStatusDao.queryJobStatusByJobName(jobStatus.getJobName());
//		if (jobStatus.getId() != null) {
//			System.out.println("job parameter insert success id:" + jobStatus.getId());
//			jobStatusDao.deleteJobStatusById(jobStatus.getId());
//			jobStatus = jobStatusDao.queryJobStatusByJobName(jobStatus.getJobName());
//			if(jobStatus==null){
//				System.out.println("delete success.");
//			}
//		}		
//	}
// 
//	public void TestQueryJobStatus() {
//		for(JobStatus jobStatus :testData)
//			jobStatusDao.insertJobStatus(jobStatus);
//		System.out.println("data count :"+jobStatusDao.queryAllJobStatusCount());
//		List<JobStatus> jsts=jobStatusDao.queryJobStatus(0, 5);
//		for(JobStatus jobStatus :jsts)
//			System.out.println("query status:"+jobStatus.getJobName()+":"+jobStatus.getId());
//		jsts=jobStatusDao.queryJobStatus(0, 20);
//		for(JobStatus jobStatus :jsts)
//			jobStatusDao.deleteJobStatusById(jobStatus.getId());
//	}
	
//	public void testQueryJobStatus(){
//		JobStatus jobStatus=testData.get(0);
//		jobStatusDao.insertJobStatus(jobStatus);
//		jobStatus = jobStatusDao.queryJobStatusByJobName(jobStatus.getJobName());
//		if (jobStatus.getId() != null) {
//			System.out.println("job status query by name success:" + jobStatus.getId());
//			jobStatus = jobStatusDao.queryJobStatusById(jobStatus.getId());
//			if (jobStatus!=null){
//				System.out.println("query success by id");
//			}
//			jobStatusDao.deleteJobStatusById(jobStatus.getId());
//		}		
//	}
 
//	public void testUpdateJobStatus(){
//		JobStatus jobStatus=testData.get(0);
//		jobStatusDao.insertJobStatus(jobStatus);
//		jobStatus = jobStatusDao.queryJobStatusByJobName(jobStatus.getJobName());
//		if (jobStatus.getId() != null) {
//			jobStatus.setJobName("newjobname");
//			jobStatusDao.updateJobStatus(jobStatus);
//			jobStatus = jobStatusDao.queryJobStatusById(jobStatus.getId());
//			if (jobStatus!=null){
//				System.out.println("update new jobName="+jobStatus.getJobName());
//				System.out.println("newjobname".equals(jobStatus.getJobName()));
//			}
//			jobStatusDao.deleteJobStatusById(jobStatus.getId());
//		}		
//	}
 
}
