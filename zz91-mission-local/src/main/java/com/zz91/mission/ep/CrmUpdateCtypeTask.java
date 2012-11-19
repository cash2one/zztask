package com.zz91.mission.ep;

import java.util.Date;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;

/**
 * @author qizj
 * 更改高会ctype=3(对应crm表示高级客户库),更新crm_sale_comp表sale_type字段为1(1代表客服类型);
 */
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
		updateSaleTypeByCtype(date);
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
	
	public boolean updateSaleTypeByCtype(String date){
		StringBuffer sql = new StringBuffer("update crm_sale_comp csc set csc.gmt_modified=now(),csc.sale_type=1");
		sql.append("where csc.status=1 and csc.cid in (select id from crm_company where ctype=3 and gmt_modified='"+date+"')");
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
