/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-6-3
 */
package com.zz91.mission.front;

import java.util.Date;

import org.junit.Test;

import com.zz91.mission.BaseMissionTest;
import com.zz91.util.db.DBUtils;

/**
 * @author mays (mays@zz91.com)
 *
 * created on 2011-6-3
 */
public class BaseDataStatisticTaskTest extends BaseMissionTest{

	/**
	 * Test method for {@link com.zz91.mission.front.BaseDataStatisticTask#clear(java.util.Date)}.
	 */
	@Test
	public void testClear() {
		//clear();	
	}

	/**
	 * Test method for {@link com.zz91.mission.front.BaseDataStatisticTask#exec(java.util.Date)}.
	 */
	@Test
	public void testExec() {
		clean();
		StringBuffer sql1=new StringBuffer();
		sql1.append("insert into `company`(`name`,`industry_code`,`business`,`area_code`,`membership_code`,");
		sql1.append("`is_block`,`zst_flag`,`regtime`,`gmt_created`,`gmt_modified`) values");
		sql1.append("('1','1','1','1','1','0','0','2011-06-06','2011-06-06','2011-06-06')");
		DBUtils.insertUpdate("ast", sql1.toString());
		
		StringBuffer sql2=new StringBuffer();
		sql2.append("insert into `company`(`name`,`industry_code`,`business`,`area_code`,`membership_code`,");
		sql2.append("`is_block`,`zst_flag`,`regtime`,`gmt_created`,`gmt_modified`) values");
		sql2.append("('2','2','2','2','2','2','2','2011-06-06','2011-06-06','2011-06-06')");
		DBUtils.insertUpdate("ast", sql2.toString());
		
		StringBuffer sql3=new StringBuffer();
		sql3.append("insert into products(`company_id`,`is_del`,`category_products_main_code`,");
		sql3.append("`category_products_assist_code`,`check_status`,`unchecked_check_status`,`unpass_reason`,`gmt_created`) values");
		sql3.append("(1,'0','1','1','0','0','reason1','2011-06-06')");
		DBUtils.insertUpdate("ast", sql3.toString());
		
		StringBuffer sql4=new StringBuffer();
		sql4.append("insert into products(`company_id`,`is_del`,`category_products_main_code`,");
		sql4.append("`category_products_assist_code`,`check_status`,`unchecked_check_status`,`unpass_reason`,`gmt_created`) values");
		sql4.append("(1,'1','1','1','0','0','reason2','2011-06-06')");
		DBUtils.insertUpdate("ast", sql4.toString());
		
		StringBuffer sql5=new StringBuffer();
		sql5.append("insert into inquiry (`title`,`group_id`,`be_inquired_type`,`be_inquired_id`,`sender_id`,`receiver_id`,`batch_send_type`,`gmt_created`) values");
		sql5.append("('tiletest1','1','0',1,1,1,'0','2011-06-06')");
		DBUtils.insertUpdate("ast", sql5.toString());
		
		StringBuffer sql6=new StringBuffer();
		sql6.append("insert into inquiry (`title`,`group_id`,`be_inquired_type`,`be_inquired_id`,`sender_id`,`receiver_id`,`batch_send_type`,`gmt_created`) values");
		sql6.append("('tiletest2','1','0',1,1,1,'0','2011-06-06')");
		DBUtils.insertUpdate("ast", sql6.toString());
		
		StringBuffer sql7=new StringBuffer();
		sql7.append("insert into company_price (`company_id`,`account`,`product_id`,`title`,`is_checked`,`gmt_created`) values");
		sql7.append("(1,'testaccount1','1','testtitle1','0','2011-06-06')");
		DBUtils.insertUpdate("ast", sql7.toString());
		
		StringBuffer sql8=new StringBuffer();
		sql8.append("insert into company_price (`company_id`,`account`,`product_id`,`title`,`is_checked`,`gmt_created`) values");
		sql8.append("(2,'testaccount2','2','testtitle2','1','2011-06-06')");
		DBUtils.insertUpdate("ast", sql8.toString());
		
		StringBuffer sql9=new StringBuffer();
		sql9.append("insert into bbs_post(`company_id`,`account`,`title`,`unpass_reason`,`is_hot_post`,`gmt_created`) values");
		sql9.append("(1,'testaccount1','testtitle1','testunpassreason1','0','2011-06-06')");
		DBUtils.insertUpdate("ast", sql9.toString());
		
		StringBuffer sql10=new StringBuffer();
		sql10.append("insert into bbs_post(`company_id`,`account`,`title`,`unpass_reason`,`is_hot_post`,`gmt_created`) values");
		sql10.append("(1,'testaccount2','testtitle2','testunpassreason2','1','2011-06-06')");
		DBUtils.insertUpdate("ast", sql10.toString());
		
		StringBuffer sql11=new StringBuffer();
		sql11.append("insert into bbs_post_reply(`company_id`,`account`,`bbs_post_id`,`title`,`is_del`,`gmt_created`) values");
		sql11.append("('1','testaccount1',1,'testtitle','0','2011-06-06')");
		DBUtils.insertUpdate("ast", sql11.toString());
		

		StringBuffer sql12=new StringBuffer();
		sql12.append("insert into bbs_post_reply(`company_id`,`account`,`bbs_post_id`,`title`,`is_del`,`gmt_created`) values");
		sql12.append("('2','testaccount2',2,'testtitle2','1','2011-06-06')");
		DBUtils.insertUpdate("ast", sql11.toString());
		BaseDataStatisticTask bdst=new BaseDataStatisticTask();
		Date baseDate=new Date();
		try {
			bdst.exec(baseDate);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	private void clean(){
		DBUtils.insertUpdate("ast", "delete from company");
		DBUtils.insertUpdate("ast", "delete from products");
		DBUtils.insertUpdate("ast", "delete from inquiry");
		DBUtils.insertUpdate("ast", "delete from company_price");
		DBUtils.insertUpdate("ast", "delete from bbs_post");
		DBUtils.insertUpdate("ast", "delete from bbs_post_reply");
	}
}
