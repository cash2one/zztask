/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-3-16
 */
package com.zz91.mission.demo;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.zz91.util.db.pool.DBPoolFactory;

/**
 * @author mays (mays@zz91.com)
 *
 * created on 2011-3-16
 */
public class DemoTaskTest {

	/**
	 * Test method for {@link com.zz91.mission.demo.DemoTask#exec(java.util.Map)}.
	 */
	@Test
	public void testExec() {
		DBPoolFactory.getInstance().init("db-zztask.properties");
		
		DemoTask task = new DemoTask();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("test", "this is a test");
//		System.out.println(task.exec(map));
	}

}
