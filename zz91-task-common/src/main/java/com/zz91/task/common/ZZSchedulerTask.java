package com.zz91.task.common;

@Deprecated
public interface ZZSchedulerTask {
	void startTask(Long interval);
	void stopTask();
}
