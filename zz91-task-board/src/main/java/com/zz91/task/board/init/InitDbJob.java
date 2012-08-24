package com.zz91.task.board.init;

import java.net.MalformedURLException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.quartz.CronExpression;

import com.zz91.task.board.domain.JobDefinition;
import com.zz91.task.board.service.JobDefinitionService;
import com.zz91.task.board.service.JobStatusService;
import com.zz91.task.board.thread.TaskControlThread;
import com.zz91.task.board.thread.idx.LastBuildThread;
import com.zz91.task.board.thread.idx.ListenIdxChangeThread;
import com.zz91.task.board.util.ClassHelper;
import com.zz91.task.common.AbstractIdxTask;

/**
 * 系统启动时加载数据库中的任务信息
 * 
 * @author mays (mays@zz91.com)
 * 
 *         created on 2011-3-16
 */
public class InitDbJob {

	final static Logger LOG = Logger.getLogger(InitDbJob.class);

	// @Resource
	// private SchedulerService schedulerServiceMem;

	@Resource
	JobDefinitionService jobDefinitionService;
	@Resource
	JobStatusService jobStatusService;

	public void initJob() {

		TaskControlThread.runSwitch = true;

		TaskControlThread taskThread = new TaskControlThread();
		taskThread.setName("TaskControlThread");
		taskThread.setJobDefinitionService(jobDefinitionService);
		taskThread.setJobStatusService(jobStatusService);
		taskThread.start();
		
		ListenIdxChangeThread listenIdxThread = new ListenIdxChangeThread();
		listenIdxThread.setName("indexChangeListenThread");
		listenIdxThread.setJobStatusService(jobStatusService);
		listenIdxThread.start();
		
		LastBuildThread lastBuildThread = new LastBuildThread();
		lastBuildThread.setName("holdLastBuildThread");
		lastBuildThread.setJobDefinitionService(jobDefinitionService);
		lastBuildThread.start();

		List<JobDefinition> jobList = jobDefinitionService
				.queryAllJobDefinition(true);

		for (JobDefinition def : jobList) {
			
			//普通任务
			if(JobDefinitionService.GROUP.equals(def.getJobGroup())){
				if (def.getCron() != null
						&& CronExpression.isValidExpression(def.getCron())) {
					TaskControlThread.addRunTask(def);
					continue;
				}
			}
			
			//搜索引擎索引任务
			if(JobDefinitionService.GROUP_IDX.equals(def.getJobGroup())){
				try {
					AbstractIdxTask jobInstance = (AbstractIdxTask) ClassHelper.load(
							def.getJobClasspath(), def.getJobClassName())
							.newInstance();
					jobInstance.setCron(def.getCron());
					
					TaskControlThread.LAST_BUILD_TIME_MAP.put(def.getJobName(), def.getEndTime().getTime());
					TaskControlThread.BUILD_TASK_MAP.put(def.getJobName(), jobInstance);
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			
		}

	}

}
