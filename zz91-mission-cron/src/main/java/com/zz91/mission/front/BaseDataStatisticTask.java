/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-3-16
 */
package com.zz91.mission.front;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IInsertUpdateHandler;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.mail.MailUtil;

/**
 * <br />
 * 统计网站基础数据，并将统计结果写入web_base_data_stat <br />
 * <br />
 * 待统计的数据： <br />
 * <br />
 * REGISTER:统计一天的用户注册数据,统计方法:company表regtime在basedate前一天 <br />
 * PUBLISH_PRODUCTS:统计一天的供求信息发布数量,统计方法:products表的gmt_created在basedate前一天 <br />
 * PUBLISH_INQUIRY:统计一天的询盘数量,统计方法:inquiry的gmt_created <br />
 * PUBLISH_COMPANY_PRICE:统计一天的企业报价发布数量,统计方法:company_price的gmt_created <br />
 * PUBLISH_BBS_POST:统计一天的发贴量,统计方法:bbs_post的gmt_modified和company_id!=0 <br />
 * PUBLISH_BBS_REPLY:统计一天的回贴量,统计方法:bbs_post_reply的gmt_created <br />
 * 
 * @author mays (mays@zz91.com) stat_cay created on 2011-3-16
 */
public class BaseDataStatisticTask implements ZZTask {
	private static final String REGISTER = "register";// 统计一天注册人数
	private static final String PUBLISH_PRODUCTS = "publish_products";// 统计一天的供求信息量
	private static final String PUBLISH_INQUIRY = "publish_inquiry";// 询盘
	private static final String PUBLISH_COMPANY_PRICE = "publish_company_price";// 企业报价
	private static final String PUBLISH_BBS_POST = "publish_bbs_post";// 发帖
	private static final String PUBLISH_BBS_REPLY = "publish_bbs_reply";// 回帖

	@Override
	public boolean clear(Date baseDate) throws Exception {
		Date targetDate = new Date(DateUtil.getTheDayZero(new Date(), -1)*1000l);
		
		// 清理要统计这天的数据
		String sql = "delete from web_base_data_stat where gmt_stat_date='"+DateUtil.toString(targetDate, "yyyy-MM-dd")+"'";
	
		DBUtils.insertUpdate("ast", sql);
		
		return true;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		Date startDate = new Date(DateUtil.getTheDayZero(baseDate, -1)*1000l);
		Date endDate = new Date(DateUtil.getTheDayZero(baseDate, 0)*1000l);
		
		Map<String, Object> result=new HashMap<String, Object>();
		
		final Date targetDate = new Date(DateUtil.getTheDayZero(baseDate, -1)*1000l);
		result.put("targetDate", DateUtil.toString(targetDate, "yyyy-MM-dd"));
		//注册量
		String sql1 = "select count(*) from company where '"
			+ DateUtil.toString(startDate, "yyyy-MM-dd")
			+ "'<= regtime and regtime < '"
			+ DateUtil.toString(endDate, "yyyy-MM-dd")
			+ "'";
		
		result.put(REGISTER, analysis(REGISTER, targetDate, sql1));
		//供求信息量
		String sql2 = "select count(*) from products where '"
			+ DateUtil.toString(startDate, "yyyy-MM-dd")
			+ "'<= gmt_created and gmt_created < '"
			+ DateUtil.toString(endDate, "yyyy-MM-dd") 
			+ "'";
		
		result.put(PUBLISH_PRODUCTS, analysis(PUBLISH_PRODUCTS, targetDate, sql2));
		
		//询盘
		String sql3 = "select count(*) from inquiry where '"
			+ DateUtil.toString(startDate, "yyyy-MM-dd")
			+ "'<= gmt_created and gmt_created < '"
			+ DateUtil.toString(endDate, "yyyy-MM-dd")
			+ "'";
		
		result.put(PUBLISH_INQUIRY, analysis(PUBLISH_INQUIRY, targetDate, sql3));
		
		//企业报价
		String sql4 = "select count(*) from company_price where '"
			+ DateUtil.toString(startDate, "yyyy-MM-dd")
			+ "'<= gmt_created and gmt_created < '"
			+ DateUtil.toString(endDate, "yyyy-MM-dd") + "'";
		
		result.put(PUBLISH_COMPANY_PRICE, analysis(PUBLISH_COMPANY_PRICE, targetDate, sql4));
		
		//发帖量
		String sql5 = "select count(*) from bbs_post where '"
			+ DateUtil.toString(startDate, "yyyy-MM-dd")
			+ "'<= gmt_created and gmt_created < '"
			+ DateUtil.toString(endDate, "yyyy-MM-dd") 
			+ "' and company_id !=0";
		
		result.put(PUBLISH_BBS_POST, analysis(PUBLISH_BBS_POST, targetDate, sql5));
		
		//回帖量
		String sql6 = "select count(*) from bbs_post_reply where '"
			+ DateUtil.toString(startDate, "yyyy-MM-dd")
			+ "'<= gmt_created and gmt_created < '"
			+ DateUtil.toString(endDate, "yyyy-MM-dd") 
			+ "'";
		
		result.put(PUBLISH_BBS_REPLY, analysis(PUBLISH_BBS_REPLY, targetDate, sql6));
		
		MailUtil.getInstance().sendMail("zz91网站基础数据统计（"+result.get("targetDate")+"）", 
				"analysis.bd@asto.mail", null, null,
				"zz91", "zz91-task-basedata", result, null);
		
		return true;
	}
	
	private Integer analysis(final String key, final Date targetDate, String sql){
		final Integer[] count=new Integer[1];
		DBUtils.select("ast", sql, new IReadDataHandler() {

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				if (rs.next()) {
					count[0]=rs.getInt(1);
				}
			}
		});
		String sql1="insert into web_base_data_stat (`stat_cate`,`stat_cate_name`,`gmt_stat_date`,`stat_count`,`gmt_created`) values(?,?,?,?,now())";
		
		DBUtils.insertUpdate("ast", sql1, new IInsertUpdateHandler() {
			@Override
			public void handleInsertUpdate(PreparedStatement ps) throws SQLException {
				ps.setString(1, key);
				ps.setString(2, "");
				ps.setString(3, DateUtil.toString(targetDate,"yyyy-MM-dd").toString());
				ps.setInt(4, count[0]);
				ps.execute();
			}
		});
		
		return count[0];
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}

	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		BaseDataStatisticTask base = new BaseDataStatisticTask();
		Date d = new Date();
		base.clear(d);
		base.exec(d);
	}

}
