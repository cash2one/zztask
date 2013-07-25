package com.zz91.mission.financial;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
/**
 * 
 * */
public class RepairDocument implements ZZTask {

	final static String DB = "financial";

	final static String DATE_FORMAT = "yyyy-MM-dd 00:00:00";
	
	final static String COA_PREFIX="";
	
	final static int PAGE_SIZE=50;

	@Override
	public boolean init() throws Exception {
		
		return false;
	}
	
	@Override
	public boolean exec(Date baseDate) throws Exception {

//		Date periodTo= DateUtil.getDate(baseDate, DATE_FORMAT);
//		Date periodFrom=DateUtil.getDateAfterDays(periodTo, -1);
		
//		Integer start=0;
//		Set<String> coaSet = new HashSet<String>();
//		do {
//			coaSet = queryCoa(start);
//
//			analysis(coaSet, periodFrom, periodTo);
//			
//			start=start+PAGE_SIZE;
//		} while (coaSet.size()>0);
		
		StringBuffer sb=new StringBuffer();
		sb.append("select ac_bill_book_id as bid,code_item_dept from ac_document ad where ac_bill_book_id in (select ac_bill_book_id from ac_document where code_item_cash<>'' and code_item_cash<>'999') and code_item_cash is null ");
		
		final Map<Integer, Integer> result=new HashMap<Integer, Integer>();
		
		DBUtils.select(DB, sb.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				
				while (rs.next()) {
					result.put(rs.getInt(1), rs.getInt(2));
				}
			}
		});
		
		String sql="";
		for(Integer k:result.keySet()){
			sql="update ac_document set code_item_dept="+result.get(k)+" where ac_bill_book_id="+k;
			//System.out.println(sql);
			DBUtils.insertUpdate(DB, sql);
		}
		
		return true;
	}
	
	
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return true;
	}
	
	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		RepairDocument task=new RepairDocument();
		
		try {
			
			task.clear(DateUtil.getDate("2013-04-30 00:00:00", "yyyy-MM-dd HH:mm:ss"));
			task.exec(DateUtil.getDate("2013-04-30 00:00:00", "yyyy-MM-dd HH:mm:ss"));
			
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
