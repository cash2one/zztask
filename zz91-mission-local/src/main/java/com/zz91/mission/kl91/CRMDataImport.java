package com.zz91.mission.kl91;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.http.HttpUtils;
import com.zz91.util.lang.StringUtils;

/**
 * Author : kongsj 
 * Creation time : 上午11:53:53 - 2012-8-6 
 */
public class CRMDataImport implements ZZTask {

	private static final String DATE_FORMAT="yyyy-MM-dd";
	private final static String DATE_FORMAT_DETAIL = "yyyy-MM-dd HH:mm:ss";
	private static String API_HOST="http://www.kl91.com";
	final static String DB="klcrm";
	static final Integer LIMIT = 10;
	
/**
 * {"account":"kl2012331171956996","address":"","areaCode":"","business":"","companyName":"",
 * "contact":"kl2012331171956996","department":"","domain":"","email":"","fax":"",
 * "gmtCreated":{"date":6,"day":1,"hours":17,"minutes":21,"month":7,"seconds":34,"time":1344244894000,"timezoneOffset":-480,"year":112},
 * "gmtLastLogin":{"date":3,"day":5,"hours":17,"minutes":21,"month":7,"seconds":34,"time":1343985694000,"timezoneOffset":-480,"year":112},
 * "gmtModified":{"date":3,"day":5,"hours":17,"minutes":21,"month":7,"seconds":34,"time":1343985694000,"timezoneOffset":-480,"year":112},
 * "id":9068,"industryCode":"1000","introduction":"","isActive":0,"membershipCode":"10051000",
 * "mobile":"","numLogin":1,"numPass":0,"password":"","position":"","qq":"","registFlag":0,"sex":0,
 * "showTime":{"date":3,"day":5,"hours":17,"minutes":21,"month":7,"seconds":34,"time":1343985694000,"timezoneOffset":-480,"year":112},
 * "tel":"","website":"","zip":""},
 */
	
	@Override
	public boolean exec(Date baseDate) throws Exception {
		boolean result=false;
		String targetDate = DateUtil.toString(baseDate, DATE_FORMAT);
		do{
			String responseText = HttpUtils.getInstance().httpGet(API_HOST+"/list/todayDataCount.htm?today="+targetDate, HttpUtils.CHARSET_UTF8);
			JSONObject jb=JSONObject.fromObject(responseText);
			int count = jb.getInt("i");
			int page = getSize(count);
			List<JSONObject> list = new ArrayList<JSONObject>();
			
			// 循环获取所有数据
			for(int i=1;i<=page;i++){
				try {
					responseText = HttpUtils.getInstance().httpGet(API_HOST+"/list/todayData.htm?today="+targetDate+"&start="+getStart(i), HttpUtils.CHARSET_UTF8);
				} catch (Exception e) {
					throw new Exception(e.getMessage()+"   start:"+i);
				}
				JSONArray js=JSONArray.fromObject(responseText);
				for (Iterator iter = js.iterator(); iter.hasNext();) {
					JSONObject object = (JSONObject) iter.next();
					list.add(object);
				}
			}
			
			// 循环导入
			for(JSONObject object:list){
				String gmtLogin = buildData(object.getString("gmtLastLogin"));
				String gmtRegister = buildData(object.getString("gmtCreated"));
				//插入公司信息
				loadCompany(object.getInt("id"), object.getString("companyName") ,
						object.getString("account") , object.getString("email") , object.getString("contact") , Short.valueOf(object.getString("sex")) ,
						object.getString("mobile") , object.getString("tel") ,
						object.getString("fax") , object.getString("address") ,
						object.getString("zip") , object.getString("introduction") , object.getString("industryCode") ,
						object.getString("membershipCode") , Short.valueOf(object.getString("registFlag")) , object.getString("business") ,
						object.getString("areaCode") , 
						object.getInt("numLogin") ,  gmtLogin, gmtRegister,object.getString("position"));
				if (!isExsitCompany(object.getInt("id"))) {
//					LOG.info(">>>>>>>>>>>>>>>>>插入失败:数据ID:"+object.getInt("id")+";帐号:"+object.getString("account")
//							+";公司:"+object.getString("companyName"));
				}
			}
			//统计今天注册客户
			tongji(targetDate);
			result=true;
		}while(false);
		return result;
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
	
	@SuppressWarnings("unchecked")
	private void tongji(String targetDate) {
		//100110001010:浙江 100110001009:江苏  100110001008:上海 100110001018:广东 
		//100110001014:山东 100110001000:北京 100110001002:河北
		final Map<String,Integer> map= new HashMap<String, Integer>();
		String tongjiSql = "SELECT substring(province_code,1,12) as code,count(*) as count FROM crm_company "
		+"where date_format(gmt_register,'%Y-%m-%d') = date_format(date_add(now(), interval -1 day),'%Y-%m-%d') "
		+"group by code";
		DBUtils.select(DB, tongjiSql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					map.put(rs.getString("code"), rs.getInt("count"));
				}
			}
		});
		Integer zhejiang = 0;
		Integer jiangsu = 0;
		Integer shanghai = 0;
		Integer guangdong = 0;
		Integer shandong = 0;
		Integer beijing = 0;
		Integer hebei = 0;
		Integer other = 0;
		for (Iterator iter = map.keySet().iterator();iter.hasNext();) {
			String key = (String) iter.next();
			if ("100110001010".equals(key)) {
				zhejiang = map.get(key);
			} else if("100110001009".equals(key)) {
				jiangsu = map.get(key);
			} else if("100110001008".equals(key)) {
				shanghai = map.get(key);
			} else if("100110001018".equals(key)) {
				guangdong = map.get(key);
			} else if("100110001014".equals(key)) {
				shandong = map.get(key);
			} else if("100110001000".equals(key)) {
				beijing = map.get(key);
			} else if("100110001002".equals(key)) {
				hebei = map.get(key);
			} else {
				other += map.get(key);
			}
		}
		StringBuilder sql=new StringBuilder();
		sql.append("insert into crm_sale_statistics(`gmt_target`,`zhejiang`,`jiangsu`,`shanghai`,`guangdong`,`shandong`,`beijing`,`hebei`,`other`,`gmt_created`,`gmt_modified`) values (");
		sql.append("date_format(date_add(now(), interval -1 day),'%Y-%m-%d'),");
		sql.append(zhejiang);
		sql.append(",");
		sql.append(jiangsu);
		sql.append(",");
		sql.append(shanghai);
		sql.append(",");
		sql.append(guangdong);
		sql.append(",");
		sql.append(shandong);
		sql.append(",");
		sql.append(beijing);
		sql.append(",");
		sql.append(hebei);
		sql.append(",");
		sql.append(other);
		sql.append(",");
		sql.append("now(),now())");
		boolean result = DBUtils.insertUpdate(DB, sql.toString());
		if(!result) {
//			LOG.info(">>>>>>>>>>>>>>>>>统计昨天注册人数失败:"+sql.toString());
		}
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
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
	
	/**
	 * 插入公司表
	 */
	private void loadCompany(Integer cid, String cname,
			String account, String email, String name, Short sex,
			String mobile, String phone,
			String fax, String address,
			String addressZip, String details, String industryCode,
			String memberCode, Short registerCode, String businessCode,
			String areaCode, 
			Integer loginCount, String gmtLogin, String gmtRegister, String position) {
		String sql = "";
		details = details.replaceAll("'", "");
		// 判断导入数据来源
		if (registerCode == 1) {
			cname +="(ZZ91)";
		} else if(registerCode == 2) {
			cname +="(其他网站)";
		}
		if (!isExsitCompany(cid)) {
			sql ="INSERT INTO crm_company ( "
				+"cid,regist_status,cname,uid,account,"
				+"email,name,sex,mobile,phone,"
				+"fax,position,address,address_zip,details,industry_code,"
				+"member_code,register_code,business_code,area_code,main_buy,"
				+"main_product_buy,main_supply,main_product_supply,login_count,ctype,gmt_login,"
				+"gmt_register,gmt_input,gmt_created,gmt_modified) VALUES ("
				+cid+",1,'"+cname+"',"+cid+",'"+account+"','"
				+email+"','"+name+"',"+sex+",'"+mobile+"','"+phone+"','"
				+fax+"','"+position+"','"+address+"','"+addressZip+"','"+details+"','"+industryCode+"','"
				+memberCode+"','"+registerCode+"','"+businessCode+"','"+areaCode+"',"+"1"+",'"
				+""+"',"+"1"+",'"+""+"',"+loginCount+",2,'"+gmtLogin+"','"
				+gmtRegister+"',now(),now(),now())";
		} else {
			sql="UPDATE crm_company SET "
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
		}
		boolean result=DBUtils.insertUpdate(DB, sql);
		if (result) {
			backupCompany( cid,  cname,  
					account,  email,  name,  sex,
					 mobile,   phone,
					 fax,  address,
					 addressZip,  details,  industryCode,
					 memberCode,  registerCode,  businessCode,
					 areaCode,
					 loginCount,  gmtLogin,  gmtRegister, position);
		} else {
//			LOG.info(">>>>>>>>>>>>>>>>>创建客户失败:"+sql);
		}
	}
	
	/**
	 * 插入备份表
	 */
	private void backupCompany(Integer cid, String cname, 
			String account, String email, String name, Short sex,
			String mobile, String phone,
			String fax, String address,
			String addressZip, String details, String industryCode,
			String memberCode, Short registerCode, String businessCode,
			String areaCode,
			Integer loginCount, String gmtLogin, String gmtRegister,String position) {
		if (!isExsitCompanyBackup(cid)) {
			String sql="INSERT INTO crm_company_backup ( "
				+"id,cname,uid,account,"
				+"email,name,sex,mobile,phone,"
				+"fax,position,address,address_zip,details,industry_code,"
				+"member_code,register_code,business_code,area_code,main_buy,"
				+"main_product_buy,main_supply,main_product_supply,login_count,gmt_login,"
				+"gmt_register,gmt_input,gmt_created,gmt_modified) VALUES ("
				+cid+",'"+cname+"',"+cid+",'"+account+"','"
				+email+"','"+name+"',"+sex+",'"+mobile+"','"+phone+"','"
				+fax+"','"+position+"','"+address+"','"+addressZip+"','"+details+"','"+industryCode+"','"
				+memberCode+"','"+registerCode+"','"+businessCode+"','"+areaCode+"',"+"1"+",'"
				+""+"',"+"1"+",'"+""+"',"+loginCount+",'"+gmtLogin+"','"
				+gmtRegister+"',now(),now(),now())";
			boolean result = DBUtils.insertUpdate(DB, sql);
			if (!result) {
//				LOG.info(">>>>>>>>>>>>>>>>>备份新创建客户失败:"+sql);
			}
		}
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
	
	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		CRMDataImport obj = new CRMDataImport();
//		API_HOST = "http://localhost:8090/front";
		Date date = DateUtil.getDate("2012-08-04", "yyyy-MM-dd");
		obj.exec(date);
	}

}
