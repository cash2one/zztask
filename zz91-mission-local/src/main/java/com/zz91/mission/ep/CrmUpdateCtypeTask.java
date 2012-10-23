package com.zz91.mission.ep;

import java.util.Date;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;

public class CrmUpdateCtypeTask implements ZZTask {

	final static String DATE_FORMAT = "yyyy-MM-dd";
	final static String DB="crm";
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String date=DateUtil.toString(baseDate, DATE_FORMAT);
		updateCtype(date);
		return true;
	}
	public boolean updateCtype(String date){
		StringBuffer sql=new StringBuffer("update crm_company set ctype=3,gmt_modified=now() where member_code='10011001'");
		if(!StringUtils.isEmpty(date)){
			sql.append(" and date_format(gmt_modified,'%Y-%m-%d')='"+date+"'");
		}
		
		boolean isok=DBUtils.insertUpdate(DB, sql.toString());
		return isok;
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}
	
    public static void main(String[]args) {
    	DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
    	CrmUpdateCtypeTask task=new CrmUpdateCtypeTask();
    	try {
			task.exec(new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
