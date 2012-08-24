package com.zz91.mission.huanbao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;

public class TradeSupplyTask implements ZZTask{
	
	final static String DB="ep";
	final static int LIMIT=1000;

	@Override
	public boolean init() throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		
		deal(DateUtil.toString(baseDate, "yyyy-MM-dd HH:mm:ss"));
		
//		deal("2012-07-12 00:00:00");
//		deal("2012-07-13 00:00:00");
//		deal("2012-07-17 00:00:00");
//		deal("2012-07-18 00:00:00");
//		deal("2012-07-19 00:00:00");
//		deal("2012-07-23 00:00:00");
//		deal("2012-07-24 00:00:00");
//		deal("2012-07-27 00:00:00");
//		deal("2012-07-28 00:00:00");
		
		return false;
	}
	
	private void deal(String dt){
		int docsize=0;
		do {
			List<Integer> ids=queryIds(dt);
			if(ids.size()<=0){
				break;
			}
			docsize=docsize+ids.size();
			updateGmtModified(dt, ids);
		} while (true);
		
		System.out.println(dt+":>>>>>>>>>"+docsize);
	}
	
	private List<Integer> queryIds(String d){
		final List<Integer> ids=new ArrayList<Integer>();
		
		DBUtils.select(DB, "select id from trade_supply where gmt_modified='"+d+"' limit "+LIMIT, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					ids.add(rs.getInt(1));
				}
			}
		});
		
		return ids;
	}
	
	private void updateGmtModified(String d, List<Integer> ids){
		
		Date dm = null;
		try {
			dm = DateUtil.getDate(d, "yyyy-MM-dd HH:mm:ss");
		} catch (ParseException e) {
			return ;
		}
		
		
		Long nd=dm.getTime();
		String sql="";
		for(Integer id:ids){
			nd=nd+1000;
			sql="update trade_supply set gmt_modified='"+DateUtil.toString(new Date(nd),"yyyy-MM-dd HH:mm:ss")+"' where id="+id;
			
//			System.out.println(sql);
			
			DBUtils.insertUpdate(DB, sql);
		}
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}
	
	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		TradeSupplyTask task=new TradeSupplyTask();
		task.exec(new Date());
	}
}
