package com.zz91.mission.front;

import static org.junit.Assert.assertEquals;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.zz91.mission.BaseMissionTest;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;

public class ClearAbnormalAccountTest extends BaseMissionTest{

	@Test
	public void testInit() {
	}

	@Test
	public void testExec() {
		clean();
		String sql="insert into `auth_user` (`username`,`password`,`account`,`email`,`steping`," +
				"`gmt_created`,`gmt_modified`,`old_company_id`) values";
		DBUtils.insertUpdate("ast", sql+"('zhangsan@123.com','123456','小小的我','zhangsan@123.com',1,now(),now(),null)");
		DBUtils.insertUpdate("ast", sql+"('lisi@123.com','123456','小小的我','lisi@123.com',0,now(),now(),null)");
		DBUtils.insertUpdate("ast", sql+"('lisi1@123.com','123456','小小的我','lisi1@123.com',0,now(),now(),null)");
		DBUtils.insertUpdate("ast", sql+"('zhangsan1@123.com','123456','小小的我','zhangsan1@123.com',1,now(),now(),null)");
		ClearAbnormalAccount account=new ClearAbnormalAccount();
		Date baseDate=new Date();
		try {
			account.exec(baseDate);
			Map<String, Integer> map=queryAuthUser();
			assertEquals(2, map.get("count"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testClear() {
	}
	
	public void clean() {
		DBUtils.insertUpdate("ast","delete from auth_user");
	}
	
	public Map<String, Integer> queryAuthUser(){
		final Map<String, Integer> map=new HashMap<String, Integer>();
		DBUtils.select("ast","select count(*) from auth_user", new IReadDataHandler() {
			@Override
			public void handleRead(final ResultSet rs) throws SQLException {
				if(rs.next()){
					map.put("count", rs.getInt(1));
				}
			}
		});
		return map;
	}
}
