/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-6-3
 */
package com.zz91.mission;

import org.junit.BeforeClass;

import com.zz91.util.db.pool.DBPoolFactory;

/**
 * @author mays (mays@zz91.com)
 *
// * created on 2011-6-3
 */
public class BaseMissionTest {
	
	@BeforeClass
	public static void setUp(){
		System.out.println("set up is running");
		DBPoolFactory.getInstance().init("db-zztask-jdbc.properties");
	}
}
