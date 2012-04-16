package com.zz91.mission.front;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.zz91.mission.BaseMissionTest;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;

public class ZstExpiredTaskTest extends BaseMissionTest{

	@Test
	public void testClear() {
		
	}

	@Test
	public void testExec() {
		clean();
		//创建服务
		StringBuffer sql1 = new StringBuffer();
		String sql="insert into `crm_company_service` (`company_id`,`crm_service_code`,`apply_group`,`signed_type`,`gmt_pre_start`," +
				"`gmt_pre_end`,`gmt_signed`,`gmt_start`,`gmt_end`,`apply_status`,`category`,`remark`,`gmt_created`,`gmt_modified`) values";
		sql1.append(sql);
		sql1.append("(1,'1000','888888','',now(),now(),now(),'2010-5-6 00:00:00','2011-5-6 00:00:00','1',0,'',now(),now())");
		DBUtils.insertUpdate("ast",sql1.toString());
		
		StringBuffer sql2 = new StringBuffer();
		sql2.append(sql);
		sql2.append("(2,'1000','888666','',now(),now(),now(),'2010-5-6 00:00:00','2011-5-6 00:00:00','1',0,'',now(),now())");
		DBUtils.insertUpdate("ast",sql2.toString());
		//创建公司
		String sql_1="insert into `company` (`id`,`name`,`industry_code`,`business`,`service_code`,`area_code`,`foreign_city`,`category_garden_id`," +
		"`membership_code`,`star_sys`,`star`,`num_visit_month`,`gmt_visit`,`domain`,`domain_zz91`,`classified_code`,`address`,`address_zip`," +
		"`business_type`,`sale_details`,`buy_details`,`regfrom_code`,`is_block`,`tags`,`zst_flag`,`zst_year`,`regtime`,`gmt_created`,`gmt_modified`," +
		"`old_id`,`website`,`introduction`) values";
		
		StringBuffer sql3=new StringBuffer();
		sql3.append(sql_1);
		sql3.append("(1,'A公司','1','1','1','1','',1,'10051001',0,0,1,now(),'','',");
		sql3.append("'','jj','','','','','','','','1',1,'2011-6-2 00:00:00','2010-1-2 00:00:00',now(),0,'','')");
		DBUtils.insertUpdate("ast", sql3.toString());
		
		StringBuffer sql4=new StringBuffer();
		sql4.append(sql_1);
		sql4.append("(2,'B公司','1','1','1','1','',1,'10051001',0,0,1,now(),'','',");
		sql4.append("'','jj','','','','','','','','1',1,'2011-6-2 00:00:00','2010-1-2 00:00:00',now(),0,'','')");
		DBUtils.insertUpdate("ast", sql4.toString());
		
		ZstExpiredTask task=new ZstExpiredTask();
		Date baseDate=new Date();
		try {
			task.exec(baseDate);
			Map<String, Integer> map=queryCompanyById(2);
			Assert.assertEquals("10051000", map.get("membership_code").toString());
			Assert.assertEquals("0", map.get("zst_flag").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testInit() {
		
	}
	
	private void clean(){
		DBUtils.insertUpdate("ast", "delete from company");
		DBUtils.insertUpdate("ast", "delete from crm_company_service");
	}
	
	public Map<String, Integer> queryCompanyById(Integer companyId){
		final Map<String, Integer> map=new HashMap<String, Integer>();
		String sql="select membership_code,zst_flag from company where id="+companyId;
		DBUtils.select("ast", sql.toString(), new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					map.put("membership_code", rs.getInt("membership_code"));
					map.put("zst_flag", rs.getInt("zst_flag"));
				}
			}
		});
		return map;
	}
}
