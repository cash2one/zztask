package com.zz91.task.board.init;

import java.net.MalformedURLException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.quartz.CronExpression;

import com.zz91.task.board.domain.JobDefinition;
import com.zz91.task.board.service.JobDefinitionService;
import com.zz91.task.board.service.JobStatusService;
import com.zz91.task.board.thread.RunningSimpleTask;
import com.zz91.task.board.thread.TaskThread;
import com.zz91.task.board.util.ClassHelper;
import com.zz91.task.common.ZZSchedulerTask;
import com.zz91.util.lang.StringUtils;

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

		TaskThread.runSwitch = true;

		TaskThread taskThread = new TaskThread();
		taskThread.setJobDefinitionService(jobDefinitionService);
		taskThread.setJobStatusService(jobStatusService);
		taskThread.start();

		List<JobDefinition> jobList = jobDefinitionService
				.queryAllJobDefinition(true);

		for (JobDefinition def : jobList) {
			if (def.getCron() != null
					&& CronExpression.isValidExpression(def.getCron())) {
				TaskThread.addRunTask(def);
				continue;
			}
			
			if(StringUtils.isNumber(def.getCron())){
				try {
					ZZSchedulerTask task=(ZZSchedulerTask) ClassHelper.load(def.getJobClasspath(), def.getJobClassName()).newInstance();
					task.startTask(Long.valueOf(def.getCron()));
					RunningSimpleTask.putTask(def.getJobName(), task);
					continue;
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}finally{
					
				}
			}
		}

	}
}
