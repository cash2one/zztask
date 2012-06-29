package com.zz91.mission.ep;

import org.junit.Test;

import com.zz91.mission.BaseMissionTest;
import com.zz91.util.datetime.DateUtil;

public class EpUpdateDetailsQueryTest extends BaseMissionTest {

	@Test
	public void testExec() {
		EpUpdateDetailsQueryTask task = new EpUpdateDetailsQueryTask();
		try {
			boolean r = task.exec(DateUtil.getDate("2012-01-12 13:42:30", "yyyy-MM-dd HH:mm:ss"));
			System.out.println(r);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
