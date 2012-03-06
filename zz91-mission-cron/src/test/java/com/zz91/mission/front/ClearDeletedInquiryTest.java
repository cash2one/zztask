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

public class ClearDeletedInquiryTest extends BaseMissionTest{

	@Test
	public void testInit() {

	}

	@Test
	public void testExec() {
		clean();
		String sql="insert into `inquiry` (`title`,`sender_id`,`receiver_id`,`batch_send_type`,`is_sender_del`," +
				"`gmt_created`,`gmt_modified`,`is_receiver_del`) values";
		DBUtils.insertUpdate("ast",sql+"('titletest01',1,1,'0',1,now(),now(),1)");
		DBUtils.insertUpdate("ast",sql+"('titletest02',1,1,'0',1,now(),now(),1)");
		DBUtils.insertUpdate("ast",sql+"('titletest03',0,0,'0',0,now(),now(),0)");
		DBUtils.insertUpdate("ast",sql+"('titletest04',0,0,'0',0,now(),now(),0)");
		ClearDeletedInquiry inquiry=new ClearDeletedInquiry();
		Date baseDate=new Date();
		try {
			inquiry.exec(baseDate);
			Map<String,Integer> map=queryInquiry();
			assertEquals(2,map.get("count"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testClear() {

	}
	public void clean(){
		DBUtils.insertUpdate("ast", "delete from inquiry");
	}
	public Map<String, Integer> queryInquiry()
	{
		final Map<String, Integer> map=new HashMap<String, Integer>();
		DBUtils.select("ast","select count(*) from inquiry", new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				if(rs.next()){
					map.put("count",rs.getInt(1));
				}
			}
		});
		return map;
	}
}
