/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-3-31
 */
package com.zz91.task.board.thread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zz91.task.common.ZZSchedulerTask;

/**
 * @author mays (mays@zz91.com)
 *
 * created on 2011-3-31
 */
@Deprecated
public class RunningSimpleTask {

	static Map<String, ZZSchedulerTask> RUNNING_TASK = new ConcurrentHashMap<String, ZZSchedulerTask>();
	
	public static void putTask(String key, ZZSchedulerTask value){
		RUNNING_TASK.put(key, value);
	}
	
	public static ZZSchedulerTask holderTask(String key){
		return RUNNING_TASK.get(key);
	}
}
