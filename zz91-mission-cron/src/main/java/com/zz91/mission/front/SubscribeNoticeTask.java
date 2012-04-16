/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-3-25
 */
package com.zz91.mission.front;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zz91.mission.domain.subscribe.CompanyAccount;
import com.zz91.task.common.ZZTask;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.lang.StringUtils;
import com.zz91.util.mail.MailUtil;

/**
 * @author mays (mays@zz91.com)
 * 
 *         created on 2011-3-25
 */
public class SubscribeNoticeTask implements ZZTask {
	
	public static void main(String[] args) {
		
//		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
//		MailUtil.getInstance().init("file:/root/web.properties");
//		
//		SubscribeNoticeTask task = new SubscribeNoticeTask();
//		SubscribeNoticeTask.SEARCH_HOST="211.155.229.180";
//		
//		try {
//			task.exec(DateUtil.getDate("2011-12-14", DATE_FORMAT));
//		} catch (ParseException e) {
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
	}

//	final static String DATE_FORMAT = "yyyy-MM-dd";
//	final static String DATE_FORMAT_ZH_CN = "yyyy年MM月dd日";

//	final static String TRADE_PREFIX = "http://trade.zz91.com";
//	final static String PRICE_PREFIX = "http://price.zz91.com";
	
//	final static int MAX_PRODUCTS = 6;
	
//	public static String SEARCH_HOST="192.168.110.119";
//	public static int SEARCH_PORT= 9315;
 
	@Override
	public boolean exec(Date baseDate) throws Exception {
		// 查找当天需要发送邮件订阅的所有订阅信息
		// 根据每条订阅信息查找对应的待发送信息，并拼装email
		// 提交发送信息给email system

//		Date todate = DateUtil.getDateAfterDays(baseDate, -1);
		
		final List<Integer> companyIdList = new ArrayList<Integer>();
		String sqlcid = "select distinct company_id from subscribe where email is not null and email<>'' and is_send_by_email=1 ";
		DBUtils.select("ast", sqlcid, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					companyIdList.add(rs.getInt(1));
				}
			}
		});
		
		for (Integer cid : companyIdList) {
			
			//TODO 查询用户邮箱
			CompanyAccount account=queryTargetEmail(cid);
			String toemail=account.getEmail();
			if("1".equals(account.getIsUseBackEmail())){
				toemail=account.getBackEmail();
			}
			
			if(!StringUtils.isEmail(toemail)){
				continue ;
			}
			
//			if("x03570227@163.com".equals(toemail)){
				MailUtil.getInstance().sendMail(
						"假期期间暂停邮件订阅发送通知-ZZ91再生网",
						toemail, null,
						null, "zz91", "zz91-holiday-notice",
						null, MailUtil.PRIORITY_TASK);
//			}
			
		}
		return true;
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {

		return false;
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	private CompanyAccount queryTargetEmail(Integer cid){ //, String account
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

}
