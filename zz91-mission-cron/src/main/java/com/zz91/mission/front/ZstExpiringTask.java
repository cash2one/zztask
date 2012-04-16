/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-6-2
 */
package com.zz91.mission.front;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zz91.mission.domain.subscribe.CompanyAccount;
import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;
import com.zz91.util.mail.MailUtil;

/**
 * <br />任务描述：
 * <br />提前7天提醒7天后将要的用户
 * <br />
 * <br />处理逻辑：
 * <br />1.筛选7天后过期的所有过期用户信息a
 * <br />2.发送Email给a
 * 
 * @author mays (mays@zz91.com)
 *
 * created on 2011-6-2
 */
public class ZstExpiringTask implements ZZTask {
	
	public static String DB="ast";
	final static String DATE_FORMATE="yyyy-MM-dd";
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		
		baseDate=DateUtil.getDate(baseDate, DATE_FORMATE);
		
		Date expireDate=DateUtil.getDateAfterDays(baseDate, 7);
		
//		select distinct company_id from crm_company_service c
//		where c.crm_service_code='1000' and c.apply_status=1 and c.gmt_end='2012-02-29'
//		and not exists (select company_id from crm_company_service ccs
//				where ccs.company_id=c.company_id and ccs.crm_service_code='1000' and ccs.apply_status='1' and
//				ccs.gmt_end>'2012-02-29')
		
		StringBuffer sb=new StringBuffer();
		sb.append("select distinct company_id from crm_company_service c");
		sb.append(" where c.crm_service_code='1000' and c.apply_status=1 and c.gmt_end='").append(DateUtil.toString(expireDate, DATE_FORMATE)).append("'");
		sb.append(" and not exists (select company_id from crm_company_service ccs");
		sb.append(" where ccs.company_id=c.company_id and ccs.crm_service_code='1000' and ccs.apply_status='1' and");
		sb.append(" ccs.gmt_end>'").append(DateUtil.toString(expireDate, DATE_FORMATE)).append("')");
		
		final List<Integer> companyIdList=new ArrayList<Integer>();
		DBUtils.select(DB, sb.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					companyIdList.add(rs.getInt(1));
				}
			}
		});
		
		for(Integer id:companyIdList){
			sendEmail(id, DateUtil.toString(expireDate, DATE_FORMATE));
		}
		
		return true;
	}
	
	
	private void sendEmail(Integer id, String expireDate){
		
		CompanyAccount account=queryAccount(id);
		if(account==null){
			return ;
		}
		
		String toemail=account.getEmail();
		if("1".equals(account.getIsUseBackEmail())){
			toemail=account.getBackEmail();
		}
		if(toemail!=null && StringUtils.isEmail(toemail)){
			//发送邮件
			Map<String, Object> dataMap=new HashMap<String, Object>();
			dataMap.put("expireDate", expireDate);
			
			MailUtil.getInstance().sendMail(
					"再生通服务过期提醒 - ZZ91再生网", 
					toemail, null,
//					"x03570227@163.com", null,
					null, "zz91", "zz91-zst-expiring",
					dataMap, MailUtil.PRIORITY_TASK);
		}
		
	}
	
	private CompanyAccount queryAccount(Integer cid){ //, String account
		String sql="select email,back_email,is_use_back_email,contact,sex,account from company_account where company_id="+cid+" limit 1"; //+" and account='"+account+"'";
		final CompanyAccount account=new CompanyAccount();
		DBUtils.select("ast", sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				if(rs.next()){
					account.setEmail(rs.getString(1));
					account.setBackEmail(rs.getString(2));
					account.setIsUseBackEmail(rs.getString(3));
					account.setContact(rs.getString(4));
					account.setSex(rs.getString(5));
					account.setAccount(rs.getString(6));
				}
			}
		});
		
		return account;
	}
	
	@Override
	public boolean init() throws Exception {
		return false;
	}

	public static void main(String[] args) throws ParseException, Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");

		ZstExpiringTask task=new ZstExpiringTask();
		ZstExpiringTask.DB="ast_test";
		task.exec(DateUtil.getDate("2011-12-01", "yyyy-MM-dd"));
	}
}
