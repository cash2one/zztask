package com.zz91.task.board.service;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Date;

import org.quartz.SchedulerException;

import com.zz91.task.board.domain.JobDefinition;

public interface SchedulerService {
	/**
	 * 停用任务
	 * 
	 * @param jobName
	 */
	public void unschedulerJobDefinition(String jobName);

	/**
	 * 暂停任务
	 * 
	 * @param jobName
	 */
	public void pauseJob(String jobName, String jobGroup);

	/**
	 * 重新开始任务
	 * 
	 * @param jobName
	 */
	public void resumeJob(String jobName);

	/**
	 * 获取调度中的任务列表
	 * 
	 * @param jobName
	 */
	// public Pager queryAllScheduledJob(Pager pager);

	/**
	 * 调度器中是否存在指定任务
	 * 
	 * @param jobName
	 * @return
	 */
	public Boolean isExistScheduledJob(String jobName);

	/**
	 * 启动指定的schedulerName的scheduler
	 */
	public void startup(String schedulerName);

	public void standby(String schedulerName);

	/**
	 * 暂停指定的schedulerName的scheduler
	 */
	public void pause(String schedulerName);

	/**
	 * 停止指定的schedulerName的scheduler
	 */
	public void stop(String schedulerName);

	/**
	 * 等待指定的schedulerName的正在运行的JOB结束后停止此scheduler
	 */
	public void waitAndStopScheduler(String schedulerName);

	public void resumeAll(String schedulerName);

	/**
	 * 在startTime时执行调试，endTime结束执行调度，重复执行repeatCount次，每隔repeatInterval秒执行一次
	 * 
	 * @param name
	 *            Quartz SimpleTrigger 名称
	 * @param startTime
	 *            调度开始时间
	 * @param endTime
	 *            调度结束时间
	 * @param repeatCount
	 *            重复执行次数
	 * @param repeatInterval
	 *            执行时间隔间
	 */
	public void schedule(String name, Date startTime, Date endTime,
			int repeatCount, long repeatInterval);

	public void createAndScheduled(JobDefinition definition)
			throws MalformedURLException, ClassNotFoundException,
			SchedulerException, ParseException;

	public boolean schedulerIsShutdown();

	public boolean schedulerIsInStandbyMode();

	public boolean schedulerIsStarted();

	public void stopJob(String jobName, String jobGroup);

	public boolean doSimpleJob(JobDefinition definition, Date basetime)
			throws MalformedURLException, ClassNotFoundException,
			SchedulerException;
}
