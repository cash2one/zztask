package com.zz91.mission.ep;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.zz91.task.common.ZZTask;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;

public class EpUpdateDetailsQueryTask implements ZZTask {
	
	final static String DB = "ep";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String sql = "select id,details from trade_supply where details_query like concat('%','<','%')";
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					String tmp=Jsoup.clean(rs.getString(2), Whitelist.none());
					if (StringUtils.isNotEmpty(tmp) && tmp.length() > 800) {
						tmp = tmp.substring(0, 800);
					}
					updateDetails(rs.getInt(1),tmp);
				}
			}
		});
		return true;
	}
	
	public boolean updateDetails(Integer id,String details) {
		boolean isSuccess = DBUtils.insertUpdate(DB, "update trade_supply set gmt_modified=now(),details_query='"+details+"' where id="+id);
		return isSuccess;
	}
	
	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		EpUpdateDetailsQueryTask task = new EpUpdateDetailsQueryTask();
		try {
			task.exec(new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 
}
