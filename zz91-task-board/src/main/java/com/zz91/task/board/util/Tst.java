/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-7-21
 */
package com.zz91.task.board.util;

import java.net.MalformedURLException;

import com.zz91.task.common.ZZTask;

/**
 * @author mays (mays@zz91.com)
 *
 * created on 2011-7-21
 */
public class Tst {

	
	public static void main(String[] args) {
		try {
			ZZTask jobInstance = (ZZTask) ClassHelper.load(
					"/usr/data/task/zz91cron.jar", "com.zz91.mission.ads.AnalysisAdHitTask").newInstance();
			
			jobInstance.init();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
