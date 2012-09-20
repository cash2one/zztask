/**
 * @author qizj
 * @email  qizhenj@gmail.com
 * @create_time  2012-9-5 上午10:13:43
 */
package com.zz91.mission.ep;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpException;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IInsertUpdateHandler;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.http.HttpUtils;
import com.zz91.util.lang.StringUtils;

public class CrmCompanyAssignTask implements ZZTask{
	
	final static String DATE_FORMAT = "yyyy-MM-dd";
	final static String DATE_FORMAT_DETAIL = "yyyy-MM-dd HH:mm:ss";
	final static String API_HOST="http://huanbaoadmin.zz91.com:8081/ep-admin/api";
	final static String DB="crm";
	
	final static int LIMIT=25;
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		
		Date yesterDate=DateUtil.getDateAfterDays(baseDate, -1);
		String from = DateUtil.toString(yesterDate, DATE_FORMAT);
		String to=DateUtil.toString(DateUtil.getDateAfterDays(yesterDate, 1), DATE_FORMAT);
		
		syncProfile(from, to);
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private void syncProfile(String from, String to) throws HttpException, IOException{
		JSONArray list=null;
		int start=0;
		do{
			String responseText = HttpUtils.getInstance().httpGet(API_HOST+"/crm/syncProfile.htm?from="+from+"&to="+to+"&start="+start+"&limit="+LIMIT, HttpUtils.CHARSET_UTF8);
			if(StringUtils.isEmpty(responseText) || !responseText.startsWith("[")){
				break;
			}
			
			list=JSONArray.fromObject(responseText);
			
			if(list.size()<=0){
				break;
			}
			
			Integer cid=0;
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				final JSONObject obj = (JSONObject) iter.next();
				
				cid=obj.getInt("id");
				if(isExsitCompany(cid)){
					updateProfile(obj);
				}else{
					insertProfile(obj);
				}
				
				if(!isExsitCompanyBackup(cid)){
					insertBackup(obj);
				}
			}
			
			start=start+LIMIT;
		}while(true);
	}
	
	private void insertProfile(final JSONObject obj){
		
		StringBuffer sql= new StringBuffer();
		sql.append("insert into crm_company (");
		sql.append("cid,regist_status,cname,uid,account,"); //5
		sql.append("email,name,sex,mobile,phone_country,phone_area,phone,"); //7
		sql.append("fax_country,fax_area,fax,position,contact,address,address_zip,details,industry_code,"); //9
		sql.append("member_code,register_code,business_code,province_code,area_code,main_buy,"); //6
		sql.append("main_product_buy,main_supply,main_product_supply,login_count,ctype,gmt_login,"); //6
		sql.append("gmt_register,gmt_input,gmt_created,gmt_modified)"); //4
		sql.append("values(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?, now(),now(),now())");  //34
		
		DBUtils.insertUpdate(DB, sql.toString(), new IInsertUpdateHandler() {
			
			@Override
			public void handleInsertUpdate(PreparedStatement ps)
					throws SQLException {
				ps.setObject(1, obj.get("id"));
				ps.setObject(2, 1);
				ps.setObject(3, obj.get("cname"));
				ps.setObject(4, obj.get("uid"));
				ps.setObject(5, obj.get("account"));
				ps.setObject(6, obj.get("email"));
				ps.setObject(7, obj.get("name"));
				ps.setObject(8, obj.get("sex"));
				ps.setObject(9, obj.get("mobile"));
				ps.setObject(10, obj.get("phoneCountry"));
				ps.setObject(11, obj.get("phoneArea"));
				ps.setObject(12, obj.get("phone"));
				ps.setObject(13, obj.get("faxCountry"));
				ps.setObject(14, obj.get("faxArea"));
				ps.setObject(15, obj.get("fax"));
				ps.setObject(16, obj.get("position"));
				ps.setObject(17, obj.get("contact"));
				ps.setObject(18, obj.get("address"));
				ps.setObject(19, obj.get("addressZip"));
				ps.setObject(20, obj.get("details"));
				ps.setObject(21, obj.get("industryCode"));
				ps.setObject(22, obj.get("memberCode"));
				ps.setObject(23, obj.get("registerCode"));
				ps.setObject(24, obj.get("businessCode"));
				ps.setObject(25, obj.get("provinceCode"));
				ps.setObject(26, obj.get("areaCode"));
				ps.setObject(27, obj.get("mainBuy"));
				ps.setObject(28, obj.get("mainProductBuy"));
				ps.setObject(29, obj.get("mainSupply"));
				ps.setObject(30, obj.get("mainProductSupply"));
				ps.setObject(31, obj.get("loginCount"));
				if(obj.getInt("registerCode")!=1){
					ps.setObject(32, 5);  //CTYPE
				}else{
					ps.setObject(32, 0);
				}
				ps.setObject(33, buildData(obj.getString("gmtLogin")));
				ps.setObject(34, buildData(obj.getString("gmtRegister")));
				
				ps.execute();
			}
		});
	}
	
	private void insertBackup(final JSONObject obj){
		StringBuffer sql= new StringBuffer();
		sql.append("insert into crm_company_backup (");
		sql.append("id,cname,uid,account,");  //4
		sql.append("email,name,sex,mobile,phone_country,phone_area,phone,"); //7
		sql.append("fax_country,fax_area,fax,position,contact,address,address_zip,details,industry_code,");  //9
		sql.append("member_code,register_code,business_code,province_code,area_code,main_buy,"); //6
		sql.append("main_product_buy,main_supply,main_product_supply,login_count,gmt_login,"); //5
		sql.append("gmt_register,gmt_input,gmt_created,gmt_modified) ");  //1+3
		sql.append("values(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,now(),now(),now()) ");
		
		DBUtils.insertUpdate(DB, sql.toString(), new IInsertUpdateHandler() {
			
			@Override
			public void handleInsertUpdate(PreparedStatement ps)
					throws SQLException {
				ps.setObject(1, obj.get("id"));
				ps.setObject(2, obj.get("cname"));
				ps.setObject(3, obj.get("uid"));
				ps.setObject(4, obj.get("account"));
				
				ps.setObject(5, obj.get("email"));
				ps.setObject(6, obj.get("name"));
				ps.setObject(7, obj.get("sex"));
				ps.setObject(8, obj.get("mobile"));
				ps.setObject(9, obj.get("phoneCountry"));
				ps.setObject(10, obj.get("phoneArea"));
				ps.setObject(11, obj.get("phone"));
				
				ps.setObject(12, obj.get("faxCountry"));
				ps.setObject(13, obj.get("faxArea"));
				ps.setObject(14, obj.get("fax"));
				ps.setObject(15, obj.get("position"));
				ps.setObject(16, obj.get("contact"));
				ps.setObject(17, obj.get("address"));
				ps.setObject(18, obj.get("addressZip"));
				ps.setObject(19, obj.get("details"));
				ps.setObject(20, obj.get("industryCode"));
				
				ps.setObject(21, obj.get("memberCode"));
				ps.setObject(22, obj.get("registerCode"));
				ps.setObject(23, obj.get("businessCode"));
				ps.setObject(24, obj.get("provinceCode"));
				ps.setObject(25, obj.get("areaCode"));
				ps.setObject(26, obj.get("mainBuy"));
			
				ps.setObject(27, obj.get("mainProductBuy"));
				ps.setObject(28, obj.get("mainSupply"));
				ps.setObject(29, obj.get("mainProductSupply"));
				ps.setObject(30, obj.get("loginCount"));
				ps.setObject(31, buildData(obj.getString("gmtLogin"))); 
				
				ps.setObject(32, buildData(obj.getString("gmtRegister")));
				
				ps.execute();
			}
		});
	}
	
	private void updateProfile(final JSONObject obj){
		StringBuffer sql= new StringBuffer();
		sql.append("update crm_company set ");
		
		sql.append(" cname=?,");
		sql.append(" uid=?,");
		sql.append(" account=?,");
		sql.append(" email=?,");
		sql.append(" name=?,");  //5
		
		sql.append(" sex=?,");
		sql.append(" mobile=?,");
		sql.append(" phone_country=?,");
		sql.append(" phone_area=?,");
		sql.append(" phone=?,");  //10
		
		sql.append(" fax_country=?,");
		sql.append(" fax_area=?,");
		sql.append(" fax=?,");
		sql.append(" position=?,");
		sql.append(" contact=?,"); //15
		
		sql.append(" address=?,");
		sql.append(" address_zip=?,");
		sql.append(" details=?,");
		sql.append(" industry_code=?,");
		sql.append(" member_code=?,");  //20
		
		sql.append(" business_code=?,");
		sql.append(" province_code=?,");
		sql.append(" area_code=?,");
		sql.append(" main_buy=?,");
		sql.append(" main_product_buy=?,"); //25
		
		sql.append(" main_supply=?,");
		sql.append(" main_product_supply=?,");
		sql.append(" login_count=?,");
		sql.append(" gmt_login=?,");  //29
		sql.append(" gmt_modified=now() ");
		
		sql.append(" where cid=?");
		
		DBUtils.insertUpdate(DB, sql.toString(), new IInsertUpdateHandler() {
			
			@Override
			public void handleInsertUpdate(PreparedStatement ps)
					throws SQLException {
				
				ps.setObject(1, obj.get("cname"));
				ps.setObject(2, obj.get("uid"));
				ps.setObject(3, obj.get("account"));
				ps.setObject(4, obj.get("email"));
				ps.setObject(5, obj.get("name"));

				ps.setObject(6, obj.get("sex"));
				ps.setObject(7, obj.get("mobile"));
				ps.setObject(8, obj.get("phoneCountry"));
				ps.setObject(9, obj.get("phoneArea"));
				ps.setObject(10, obj.get("phone"));
				
				ps.setObject(11, obj.get("faxCountry"));
				ps.setObject(12, obj.get("faxArea"));
				ps.setObject(13, obj.get("fax"));
				ps.setObject(14, obj.get("position"));
				ps.setObject(15, obj.get("contact"));
				
				ps.setObject(16, obj.get("address"));
				ps.setObject(17, obj.get("addressZip"));
				ps.setObject(18, obj.get("details"));
				ps.setObject(19, obj.get("industryCode"));
				ps.setObject(20, obj.get("memberCode"));
				
				ps.setObject(21, obj.get("businessCode"));
				ps.setObject(22, obj.get("provinceCode"));
				ps.setObject(23, obj.get("areaCode"));
				ps.setObject(24, obj.get("mainBuy"));
				ps.setObject(25, obj.get("mainProductBuy"));
				
				ps.setObject(26, obj.get("mainSupply"));
				ps.setObject(27, obj.get("mainProductSupply"));
				ps.setObject(28, obj.get("loginCount"));
				ps.setObject(29, buildData(obj.getString("gmtLogin")));  //TODO 日期类型
				
				ps.setObject(30, obj.get("id"));
				
				ps.execute();
			}
		});
	}

	private boolean isExsitCompanyBackup(Integer cid) {
		final Map<String, Integer> map=new HashMap<String, Integer>();
		String sql = "select count(*) from crm_company_backup where id="+cid;
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					map.put("count", rs.getInt(1));
				}
			}
		});
		if (map.get("count") > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isExsitCompany(Integer cid) {
		final Integer[] count=new Integer[1];
		String sql = "select count(*) from crm_company where cid="+cid;
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					count[0]=rs.getInt(1);
				}
				
			}
		});
		if(count[0]!=null && count[0].intValue()>0){
			return true;
		}
		return false;
	}
	
	private String buildData(String dateString){
		String date = "";
		if (StringUtils.isNotEmpty(dateString)) {
			JSONObject dateObject = JSONObject.fromObject(dateString);
			date = DateUtil.toString(new Date(dateObject.getLong("time")), DATE_FORMAT_DETAIL);
		}
		return date;
	}
	
	@Override
	public boolean init() throws Exception {
		return false;
	}

	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		CrmCompanyAssignTask task = new CrmCompanyAssignTask();
		try {
			task.exec(DateUtil.getDate("2012-08-20", "yyyy-MM-dd"));
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
