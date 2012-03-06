/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-7-29
 */
package com.zz91.mission.huzhu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.zz91.task.common.ZZTask;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;

/**
 * 分析用户成功发布和回复的贴子数量
 * @author mays (mays@zz91.com)
 *
 * created on 2011-7-29
 */
public class AnalysisPostAndReplyNumberTask implements ZZTask {

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		analysisPostNum();
		analysisReplyNum();
		return true;
	}
	
	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	private void analysisPostNum(){
//		select count(*), account from bbs_post  where company_id!=0 and account!='' and is_del=0 and (check_status=1 or check_status=2) group by account limit 100;
		String sql="select count(*) as a, account from bbs_post  where company_id!=0 and account!='' and is_del=0 and (check_status=1 or check_status=2) group by account";
		final Map<String, Integer> map=new HashMap<String, Integer>();
		
		DBUtils.select("ast", sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					map.put(rs.getString(2), rs.getInt(1));
				}
			}
		});
		
		for(String account: map.keySet()){
			updateNum("post_number", account, map.get(account));
		}
	}
	
	private void analysisReplyNum(){
//		select count(*), account from bbs_post_reply  where company_id!=0 and account!='' and is_del=0 and (check_status=1 or check_status=2)  group by account;
		
		String sql="select count(*), account from bbs_post_reply  where company_id!=0 and account!='' and is_del=0 and (check_status=1 or check_status=2)  group by account";
		final Map<String, Integer> map=new HashMap<String, Integer>();
		
		DBUtils.select("ast", sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					map.put(rs.getString(2), rs.getInt(1));
				}
			}
		});
		
		for(String account: map.keySet()){
			updateNum("reply_number", account, map.get(account));
		}
	}
	
	private void updateNum(String column, String account, Integer num){
		String sql="update bbs_user_profiler set "+column+"="+num+" where account='"+account+"'";
		DBUtils.insertUpdate("ast", sql);
	}
	
	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		AnalysisPostAndReplyNumberTask task=new AnalysisPostAndReplyNumberTask();
		try {
			task.exec(new Date());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("analysis completed...");
	}

}
