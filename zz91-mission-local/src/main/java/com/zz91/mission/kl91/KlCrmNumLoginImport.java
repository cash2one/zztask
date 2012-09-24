package com.zz91.mission.kl91;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.http.HttpUtils;
import com.zz91.util.lang.StringUtils;

public class KlCrmNumLoginImport implements ZZTask{

	final static String DB="klcrm";
	static final Integer LIMIT = 10;
	private static final String DATE_FORMAT="yyyy-MM-dd";
	private final static String DATE_FORMAT_DETAIL = "yyyy-MM-dd HH:mm:ss";
	private static String API_HOST="http://www.kl91.com";
	
	@Override
	public boolean exec(Date baseDate) throws Exception {
		boolean result=false;
		String targetDate = DateUtil.toString(baseDate, DATE_FORMAT);
		do{
			String responseText = HttpUtils.getInstance().httpGet(API_HOST+"/list/queryYestodayCompanyCount.htm?gmtLogin="+targetDate, HttpUtils.CHARSET_UTF8);
			JSONObject jb=JSONObject.fromObject(responseText);
			int count = jb.getInt("i");
			int page = getSize(count);
			List<JSONObject> list = new ArrayList<JSONObject>();
			
			// 循环获取所有数据
			for(int i=1;i<=page;i++){
				try {
					responseText = HttpUtils.getInstance().httpGet(API_HOST+"/list/queryYestodayCompany.htm?gmtLogin="+targetDate+"&start="+getStart(i), HttpUtils.CHARSET_UTF8);
				} catch (Exception e) {
					throw new Exception(e.getMessage()+"   start:"+i);
				}
				JSONArray js=JSONArray.fromObject(responseText);
				for (Iterator iter = js.iterator(); iter.hasNext();) {
					JSONObject object = (JSONObject) iter.next();
					list.add(object);
				}
			}
			
			// 循环更新
			for(JSONObject object:list){
				String gmtLogin = buildData(object.getString("gmtLastLogin"));
				String gmtRegister = buildData(object.getString("gmtCreated"));
				//更新公司信息
				updateCompany(object.getInt("id"), object.getString("companyName") ,
						object.getString("account") , object.getString("email") , object.getString("contact") , Short.valueOf(object.getString("sex")) ,
						object.getString("mobile") , object.getString("tel") ,
						object.getString("fax") , object.getString("address") ,
						object.getString("zip") , object.getString("introduction") , object.getString("industryCode") ,
						object.getString("membershipCode") , Short.valueOf(object.getString("registFlag")) , object.getString("business") ,
						object.getString("areaCode") , 
						object.getInt("numLogin") ,  gmtLogin, gmtRegister,object.getString("position"));
				updateCtype();
			}
			result=true;
		}while(false);
		return result;
	}
	private void updateCtype() {
		String sql="update crm_company SET ctype=2 where ctype=5";
		DBUtils.insertUpdate(DB, sql);
	}
	private void updateCompany(Integer cid, String cname,
			String account, String email, String name, Short sex,
			String mobile, String phone,
			String fax, String address,
			String addressZip, String details, String industryCode,
			String memberCode, Short registerCode, String businessCode,
			String areaCode, 
			Integer loginCount, String gmtLogin, String gmtRegister, String position) {
		details = details.replaceAll("'", "");
		String sql ="UPDATE crm_company SET "
				+"cname = '" + cname + "', "
				+"uid = " + cid + ", "
				+"account = '" + account + "', "
				+"email = '" + email + "', "
				+"name = '" + name + "', "
				+"sex = " + sex + ", "
				+"mobile = '" + mobile + "', "
				+"phone = '" + phone + "', "
				+"fax = '" + fax + "', "
				+"position = '" + position + "', "
				+"address = '" + address + "', "
				+"address_zip = '"+ addressZip + "', "
				+"details = '" + details.replace("'", "") + "', "
				+"industry_code = '" + industryCode + "', "
				+"member_code = '" + memberCode + "', "
				+"business_code = '" + businessCode + "', "
				+"area_code = '" + areaCode + "', "
				+"login_count = " + loginCount + ", "
				+"gmt_login = '" + gmtLogin + "'"
				+"WHERE cid="+cid;
		DBUtils.insertUpdate(DB, sql);
	}
	private int getSize(int count){
		if(count%10==0){
			count = count/LIMIT;
		}else{
			count = count/LIMIT+1;
		}
		return count;
	}
	
	private int getStart(int size){
		return (size-1)*LIMIT;
	}
	private String buildData(String dateString){
		String date = "";
		if (StringUtils.isNotEmpty(dateString)) {
			JSONObject dateObject = JSONObject.fromObject(dateString);
			date = DateUtil.toString(new Date(dateObject.getLong("time")), DATE_FORMAT_DETAIL);
		}
		return date;
	}

////搜出昨天登录的客户
//		String from=DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1), "yyyy-MM-dd HH:mm:ss");
//		String to=DateUtil.toString(baseDate, "yyyy-MM-dd HH:mm:ss");
//		String sqlId = "select id from company where gmt_last_login > '"+from+"' and '"+to+"' > gmt_last_login";
//		final List<Integer> companyId = new ArrayList<Integer>();
//		DBUtils.select(DB_kl91, sqlId, new IReadDataHandler() {
//			@Override
//			public void handleRead(ResultSet rs) throws SQLException {
//				while (rs.next()) {
//					companyId.add(rs.getInt(1));
//				}
//			}
//		});
////根据id搜出公司的id，和numLogin登陆次数
//		for (Integer cid : companyId) {
//			selectCompany(cid);
//		}
//		return true;
//		
//	}
//搜出kl91的公司登陆次数
//	private void selectCompany(Integer cid) {
//		String sql="select id,num_login,gmt_last_login from company where id="+cid+" ";
//		DBUtils.select(DB_kl91, sql, new IReadDataHandler() {
//			@Override
//			public void handleRead(ResultSet rs) throws SQLException {
//				while(rs.next()){
//					Integer loginCount=rs.getInt(2);
//					Integer companyId=rs.getInt(1);
//					String gmtLastLogin=rs.getString(3);
//					if(loginCount!=null){
//						updateLoginCount(companyId,loginCount,gmtLastLogin);
//					}
//					selectCrmCompanyStatus();
//				}
//			}
//		});
//	}
//	
////搜索crm的未激活客户放到公海
//	private void selectCrmCompanyStatus() {
//		String sql="update crm_company set ctype = 2 where ctype=5";
//		DBUtils.insertUpdate(DB, sql);
//	}
//	
////更新crm的登录次数
//	private void updateLoginCount(Integer companyId, Integer loginCount,String gmtLastLogin) {
//		String sql="update crm_company set login_count="+loginCount+",gmt_login='"+gmtLastLogin+"' where cid="+companyId+"";
//		DBUtils.insertUpdate(DB, sql);
//	}

	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		KlCrmNumLoginImport obj = new KlCrmNumLoginImport();
		Date date = DateUtil.getDate("2012-09-25", "yyyy-MM-dd");
		obj.exec(date);
	}

}
