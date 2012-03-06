package com.zz91.mission.ep;

import org.junit.Test;

import com.zz91.mission.BaseMissionTest;
import com.zz91.util.datetime.DateUtil;

public class CrmCompanyTaskTest extends BaseMissionTest {

	@Test
	public void testExec() {
		CrmCompanyTask task = new CrmCompanyTask();
		try {
			boolean r = task.exec(DateUtil.getDate("2011-07-20 12:52:30", "yyyy-MM-dd HH:mm:ss"));
			System.out.println(r);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
