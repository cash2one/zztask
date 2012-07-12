package com.zz91.mission.kl91;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;

public class PuHuiDataImportTask implements ZZTask{
	
	private final static String DB_AST = "astoback";
	private final static String DB_KL91 = "kl91";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		final List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String sql="select c.id,ca.email,ca.account,c.address,c.introduction,c.business,ca.contact,ca.sex,c.name,ca.mobile,ca.tel from company c left join company_account ca on ca.company_id=c.id " +
				"where c.membership_code= '10051000' and c.industry_code='10001000'";
		DBUtils.select(DB_AST, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					Map<String,Object> map = new HashMap<String, Object>();
					map.put("id", rs.getInt(1));
					map.put("email", rs.getString(2));
					map.put("account", rs.getString(3));
					map.put("address", rs.getString(4));
					map.put("introduction", rs.getString(5));
					map.put("business", rs.getString(6));
					map.put("contact", rs.getString(7));
					map.put("sex", rs.getString(8));
					map.put("name", rs.getString(9));
					map.put("mobile", rs.getString(10));
					map.put("tel", rs.getString(11));
					list.add(map);
				}
			}
		});
		for(Map<String, Object> company:list){
					
			Integer cid=(Integer) company.get("id");
					
			Integer numLogin=1;;
		
			String gmtLastLogin= DateUtil.toString(new Date(),"yyyy-MM-dd HH:mm:ss");
					
			String industryCode="";
				
			String domain="zz91.com";
					
			Integer isActive=0;
					
			Integer registFlag=1;
					
			String membershipCode="10051001";
					
			String email=(String) company.get("email");
			if(email==null){
				email="kl91@.com";
			}
			String account=(String) company.get("account");
			if(account==null){
				account="zz91@.com";
			}
			String business=(String) company.get("business");
			if(business==null){
				business="废塑料";
			}
			String contact=(String) company.get("contact");
			if(contact==null){
				contact="zz91";
			}
			
			String se=(String) company.get("sex");
			Integer sex=0;
			if(se.equals("M")){
				sex=0;
			}else{
				sex=1;
			}
					
			String name=(String) company.get("name");
			if(name==null){
				name="无名";
			}
			String mobile=(String) company.get("mobile");
			if(mobile==null){
				mobile="";
			}
			String tel=(String) company.get("tel");
			if(tel==null){
				tel="";
			}
					//判断是否是未过期的高会
			String svrCode="1000";			
			Integer data=validatePeriod(cid, svrCode);
			if(data.intValue()>1){
				return false;
			}
//			saveToKL(cid,email,account,membershipCode,business,contact,sex,name,mobile,tel,
//				numLogin,gmtLastLogin,industryCode,domain,isActive,registFlag);					
			}
		return false;
	}
	
	private Integer validatePeriod(Integer cid, String svrCode) {
		final Integer[] count=new Integer[1];
		count[0]=0;
		String sql="select count(*) from crm_company_service where company_id="+cid+" and crm_service_code="+svrCode+" and apply_status='1' and gmt_end>now() and now()>=gmt_start";
		DBUtils.select(DB_AST, sql, new IReadDataHandler() {			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					count[0] = rs.getInt(1);
				}
			}
		});
		return count[0];
	}

	@Override
	public boolean init() throws Exception {
		
		return false;
	}

}
