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

public class NewsTask implements ZZTask{
	
	final static String DB="ep";
	final static int LIMIT=1000;

	@Override
	public boolean init() throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		
		Date date =  new Date(baseDate.getTime()+1000);
		deal(DateUtil.toString(baseDate, "yyyy-MM-dd HH:mm:ss"),DateUtil.toString(date, "yyyy-MM-dd HH:mm:ss"));	
		
		return false;
	}
	
	private void deal(String basedate,String date){
		int docsize=0;
		do {
			List<Integer> ids=queryIds(basedate,date);
			if(ids.size()<=0){
				break;
			}
			docsize=docsize+ids.size();
			updateGmtModified(basedate, ids);
		} while (true);
		
	//System.out.println(dt+":>>>>>>>>>"+docsize);
	}
	
	private List<Integer> queryIds(String basedate,String date){
		final List<Integer> ids=new ArrayList<Integer>();
		
		DBUtils.select(DB, "select id from news where gmt_modified >= '"+basedate+"' and gmt_modified < '"+date+"' limit "+LIMIT, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				
				while (rs.next()) {	
					ids.add(rs.getInt(1));
				}
			}
		});
		
		return ids;
	}
	
	private void updateGmtModified(String basedate, List<Integer> ids){
		
		Date dm = null;
		try {
			dm = DateUtil.getDate(basedate, "yyyy-MM-dd HH:mm:ss");
		} catch (ParseException e) {
			return ;
		}
		
		
		Long nd=dm.getTime();
		String sql="";
		for(Integer id:ids){
			nd=nd+1000;
			sql="update news set gmt_modified='"+DateUtil.toString(new Date(nd),"yyyy-MM-dd HH:mm:ss")+"' where id="+id;

			DBUtils.insertUpdate(DB, sql);
		}
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}
	
	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		NewsTask task=new NewsTask();
		task.exec(new Date());
	}

}
