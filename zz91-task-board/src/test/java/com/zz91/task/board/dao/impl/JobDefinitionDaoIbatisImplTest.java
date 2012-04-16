package com.zz91.task.board.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.zz91.task.board.dao.JobDefinitionDao;
import com.zz91.task.board.domain.JobDefinition;

public class JobDefinitionDaoIbatisImplTest extends AbstractDependencyInjectionSpringContextTests {
	@Resource
	JobDefinitionDao jobDefinitionDao;

	static List<JobDefinition> testData = new ArrayList<JobDefinition>();

	protected String[] getConfigLocations() {
		return new String [] {"applicationContext.xml"};
	}

	public void testInsertAndDeleteJobDefinition() {
		JobDefinition definition = testData.get(0);
		jobDefinitionDao.insertJobDefinition(definition);
		definition = jobDefinitionDao.queryJobDefinitionByName(definition.getJobName());
		if (definition.getId() != null) {
			System.out.println("insert success." + definition.getId());
			jobDefinitionDao.deleteJobDefinition(definition.getId());
			definition = jobDefinitionDao.queryJobDefinitionByName(definition.getJobName());
			if (definition == null) {
				System.out.println("delete success ");
			}
		}
	}

//	public void testUpdateJobDefinition() {
//		JobDefinition definition = testData.get(0);
//		jobDefinitionDao.insertJobDefinition(definition);
//		definition = jobDefinitionDao.queryJobDefinitionByName(definition.getJobName());
//		System.out.println(definition.getGmtCreated());
//		definition.setName("newjoname");
//		definition.setClassName("newclassname");
//		definition.setDescription("sdkaljf;fkddkfkklsakfkjaskj djsajf");
//		jobDefinitionDao.updateJobDefinition(definition);
//		definition = jobDefinitionDao.queryJobDefinitionByName("newjoname");
//		System.out.println(definition.getClassName());
//		System.out.println(definition.getDescription());
//		if(definition.getClassName().equals("newclassname"))
//			System.out.println("update infor success.");
//		jobDefinitionDao.deleteJobDefinition(definition.getId());
//	}

//	public void testQueryAllJobDefinitionCount() {
//		for (JobDefinition definition : testData) {
//			jobDefinitionDao.insertJobDefinition(definition);
//		}
//		Integer count = jobDefinitionDao.queryAllJobDefinitionCount("1");
//		System.out.println("job definition count:" + count);
//		for (JobDefinition definition : testData) {
//			jobDefinitionDao.deleteJobDefinition(definition.getId());
//		}
//	}

//	public void testQueryJobDefinition() {
//		for (JobDefinition definition : testData) {
//			jobDefinitionDao.insertJobDefinition(definition);
//		}
//		List<JobDefinition> joblist=jobDefinitionDao.queryJobDefinition(0, 5);
//		for(JobDefinition job:joblist){
//			System.out.println("job definition name:" + job.getJobName());
//		}		
//		for (JobDefinition definition : testData) {
//			jobDefinitionDao.deleteJobDefinition(definition.getId());
//		}		
//	}

	public void testQueryJobDefinitionById() {
		JobDefinition definition = testData.get(0);
		jobDefinitionDao.insertJobDefinition(definition);
		definition = jobDefinitionDao.queryJobDefinitionByName(definition.getJobName());
		if (definition.getId() != null) {
			JobDefinition definition2 = jobDefinitionDao.queryJobDefinitionById(definition.getId());
			System.out.println("query job name:"+definition2.getJobName());
			jobDefinitionDao.deleteJobDefinition(definition.getId());
		}
	}

	public void testQueryJobDefinitionByName() {
		JobDefinition definition = testData.get(0);
		jobDefinitionDao.insertJobDefinition(definition);
		definition = jobDefinitionDao.queryJobDefinitionByName(definition.getJobName());
		if (definition.getId() != null) {
			System.out.println("query job name:"+definition.getJobName());
			jobDefinitionDao.deleteJobDefinition(definition.getId());
		}
	}

//	public void testQueryJobDefinitionByNames() {
//		System.out.println(" testQueryJobDefinitionByNames ");
//		List<String> names=new ArrayList<String>();
//		for (JobDefinition definition : testData) {
//			jobDefinitionDao.insertJobDefinition(definition);
//			names.add(definition.getJobName());
//		}
//		List<JobDefinition> joblist=jobDefinitionDao.queryJobDefinitionByNames(names, 0, 5);
//		System.out.println(joblist.size());
//		for(JobDefinition job:joblist){
//			System.out.println(" job definition name:" + job.getJobName());
//		}		
//		for (JobDefinition definition : testData) {
//			jobDefinitionDao.deleteJobDefinition(definition.getId());
//		}
//	}

	static {
		for (int i = 0; i < 10; i++) {
			testData.add(createObject("" + i));
		}
	}

	private static JobDefinition createObject(String add) {
		JobDefinition definition = new JobDefinition();
//		definition.setName("jobName" + add);
//		definition.setClasspath("jobClassPath" + add);
//		definition.setClassName("jobclassname" + add);
//		definition.setGroup("jobGroup" + add);
		definition.setCron("corn" + add);
		definition.setDescription("description" + add);
		definition.setEndTime(new Date());
//		definition.setIsInUse(true);
		definition.setStartTime(new Date());
		return definition;
	}
}
