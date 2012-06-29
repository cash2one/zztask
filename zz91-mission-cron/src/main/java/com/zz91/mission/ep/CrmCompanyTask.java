/**
 * 
 */
package com.zz91.mission.ep;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.http.HttpUtils;
import com.zz91.util.lang.StringUtils;

/**
 * @author root
 * 从外网导入新注册或修改过信息客户资源,再统计今天注册情况
 */
public class CrmCompanyTask implements ZZTask {

	Logger LOG=Logger.getLogger(CrmCompanyTask.class);
	
	final static String DATE_FORMAT = "yyyy-MM-dd";
	final static String DATE_FORMAT_DETAIL = "yyyy-MM-dd HH:mm:ss";
	final static String API_HOST="http://huanbaoadmin.zz91.com:8081/ep-admin/api";
	final static String DB="crm";
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		boolean result=false;
		String targetDate = DateUtil.toString(baseDate, DATE_FORMAT);
		do{
			String responseText = HttpUtils.getInstance().httpGet(API_HOST+"/todayUpdateCompany.htm", HttpUtils.CHARSET_UTF8);
			JSONArray jsonarray=JSONArray.fromObject(responseText);
			for (Iterator iter = jsonarray.iterator(); iter.hasNext();) {
				JSONObject object = (JSONObject) iter.next();
				String gmtLogin = buildData(object.getString("gmtLogin"));
				String gmtRegister = buildData(object.getString("gmtRegister"));
				//插入公司信息
				loadCompany(object.getInt("id"), object.getString("cname") , object.getInt("uid"),
						object.getString("account") , object.getString("email") , object.getString("name") , Short.valueOf(object.getString("sex")) ,
						object.getString("mobile") , object.getString("phoneCountry") , object.getString("phoneArea") , object.getString("phone") ,
						object.getString("faxCountry") , object.getString("faxArea") , object.getString("fax") , object.getString("address") ,
						object.getString("addressZip") , object.getString("details") , object.getString("industryCode") ,
						object.getString("memberCode") , Short.valueOf(object.getString("registerCode")) , object.getString("businessCode") ,
						object.getString("provinceCode") , object.getString("areaCode") , Short.valueOf(object.getString("mainBuy")) ,
						object.getString("mainProductBuy") , Short.valueOf(object.getString("mainSupply")) , object.getString("mainProductSupply") ,
						object.getInt("loginCount") ,  gmtLogin, gmtRegister, object.getString("contact"), object.getString("position"));
				if (!isExsitCompany(object.getInt("id"))) {
					LOG.info(">>>>>>>>>>>>>>>>>插入失败:数据ID:"+object.getInt("id")+";帐号:"+object.getString("account")
							+";公司:"+object.getString("cname"));
				}
			}
			//统计今天注册客户
			tongji(targetDate);
			result=true;
		}while(false);
		return result;
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
			LOG.info(">>>>>>>>>>>>>>>>>统计昨天注册人数失败:"+sql.toString());
		}
	}
	
	@Override
	public boolean init() throws Exception {
		return false;
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

	/**
	 * 插入公司表
	 */
	private void loadCompany(Integer cid, String cname, Integer uid,
			String account, String email, String name, Short sex,
			String mobile, String phoneCountry, String phoneArea, String phone,
			String faxCountry, String faxArea, String fax, String address,
			String addressZip, String details, String industryCode,
			String memberCode, Short registerCode, String businessCode,
			String provinceCode, String areaCode, Short mainBuy,
			String mainProductBuy, Short mainSupply, String mainProductSupply,
			Integer loginCount, String gmtLogin, String gmtRegister, String contact, String position) {
		String sql = "";
		details = details.replaceAll("'", "");
		if (!isExsitCompany(cid)) {
			if (registerCode == 1) {
				sql ="INSERT INTO crm_company ( "
					+"cid,regist_status,cname,uid,account,"
					+"email,name,sex,mobile,phone_country,phone_area,phone,"
					+"fax_country,fax_area,fax,position,contact,address,address_zip,details,industry_code,"
					+"member_code,register_code,business_code,province_code,area_code,main_buy,"
					+"main_product_buy,main_supply,main_product_supply,login_count,gmt_login,"
					+"gmt_register,gmt_input,gmt_created,gmt_modified) VALUES ("
					+cid+",1,'"+cname+"',"+uid+",'"+account+"','"
					+email+"','"+name+"',"+sex+",'"+mobile+"','"+phoneCountry+"','"+phoneArea+"','"+phone+"','"
					+faxCountry+"','"+faxArea+"','"+fax+"','"+position+"','"+contact+"','"+address+"','"+addressZip+"','"+details+"','"+industryCode+"','"
					+memberCode+"','"+registerCode+"','"+businessCode+"','"+provinceCode+"','"+areaCode+"',"+mainBuy+",'"
					+mainProductBuy+"',"+mainSupply+",'"+mainProductSupply+"',"+loginCount+",'"+gmtLogin+"','"
					+gmtRegister+"',now(),now(),now())";
			} else {
				sql ="INSERT INTO crm_company ( "
					+"cid,regist_status,cname,uid,account,"
					+"email,name,sex,mobile,phone_country,phone_area,phone,"
					+"fax_country,fax_area,fax,position,contact,address,address_zip,details,industry_code,"
					+"member_code,register_code,business_code,province_code,area_code,main_buy,"
					+"main_product_buy,main_supply,main_product_supply,login_count,ctype,gmt_login,"
					+"gmt_register,gmt_input,gmt_created,gmt_modified) VALUES ("
					+cid+",1,'"+cname+"',"+uid+",'"+account+"','"
					+email+"','"+name+"',"+sex+",'"+mobile+"','"+phoneCountry+"','"+phoneArea+"','"+phone+"','"
					+faxCountry+"','"+faxArea+"','"+fax+"','"+position+"','"+contact+"','"+address+"','"+addressZip+"','"+details+"','"+industryCode+"','"
					+memberCode+"','"+registerCode+"','"+businessCode+"','"+provinceCode+"','"+areaCode+"',"+mainBuy+",'"
					+mainProductBuy+"',"+mainSupply+",'"+mainProductSupply+"',"+loginCount+",5,'"+gmtLogin+"','"
					+gmtRegister+"',now(),now(),now())";
			}
		} else {
			sql="UPDATE crm_company SET "
				+"cname = '" + cname + "', "
				+"uid = " + uid + ", "
				+"account = '" + account + "', "
				+"email = '" + email + "', "
				+"name = '" + name + "', "
				+"sex = " + sex + ", "
				+"mobile = '" + mobile + "', "
				+"phone_country = '" + phoneCountry + "', "
				+"phone_area = '" + phoneArea  + "', "
				+"phone = '" + phone + "', "
				+"fax_country = '" + faxCountry + "', "
				+"fax_area = '" + faxArea + "', "
				+"fax = '" + fax + "', "
				+"position = '" + position + "', "
				+"contact = '" + contact + "', "
				+"address = '" + address + "', "
				+"address_zip = '"+ addressZip + "', "
				+"details = '" + details.replace("'", "") + "', "
				+"industry_code = '" + industryCode + "', "
				+"member_code = '" + memberCode + "', "
				+"business_code = '" + businessCode + "', "
				+"province_code = '" + provinceCode + "', "
				+"area_code = '" + areaCode + "', "
				+"main_buy = " + mainBuy + ", "
				+"main_product_buy = '" + mainProductBuy + "', "
				+"main_supply = " + mainSupply + ", "
				+"main_product_supply = '" + mainProductSupply + "', "
				+"login_count = " + loginCount + ", "
				+"gmt_login = '" + gmtLogin + "'"
				+"WHERE cid="+cid;
		}
		boolean result=DBUtils.insertUpdate(DB, sql);
		if (result) {
			backupCompany( cid,  cname,  uid,
					 account,  email,  name,  sex,
					 mobile,  phoneCountry,  phoneArea,  phone,
					 faxCountry,  faxArea,  fax,  address,
					 addressZip,  details,  industryCode,
					 memberCode,  registerCode,  businessCode,
					 provinceCode,  areaCode,  mainBuy,
					 mainProductBuy,  mainSupply,  mainProductSupply,
					 loginCount,  gmtLogin,  gmtRegister, contact, position);
		} else {
			LOG.info(">>>>>>>>>>>>>>>>>创建客户失败:"+sql);
		}
	}
	
	/**
	 * 插入备份表
	 */
	private void backupCompany(Integer cid, String cname, Integer uid,
			String account, String email, String name, Short sex,
			String mobile, String phoneCountry, String phoneArea, String phone,
			String faxCountry, String faxArea, String fax, String address,
			String addressZip, String details, String industryCode,
			String memberCode, Short registerCode, String businessCode,
			String provinceCode, String areaCode, Short mainBuy,
			String mainProductBuy, Short mainSupply, String mainProductSupply,
			Integer loginCount, String gmtLogin, String gmtRegister, String contact, String position) {
		if (!isExsitCompanyBackup(cid)) {
			String sql="INSERT INTO crm_company_backup ( "
				+"id,cname,uid,account,"
				+"email,name,sex,mobile,phone_country,phone_area,phone,"
				+"fax_country,fax_area,fax,position,contact,address,address_zip,details,industry_code,"
				+"member_code,register_code,business_code,province_code,area_code,main_buy,"
				+"main_product_buy,main_supply,main_product_supply,login_count,gmt_login,"
				+"gmt_register,gmt_input,gmt_created,gmt_modified) VALUES ("
				+cid+",'"+cname+"',"+uid+",'"+account+"','"
				+email+"','"+name+"',"+sex+",'"+mobile+"','"+phoneCountry+"','"+phoneArea+"','"+phone+"','"
				+faxCountry+"','"+faxArea+"','"+fax+"','"+position+"','"+contact+"','"+address+"','"+addressZip+"','"+details+"','"+industryCode+"','"
				+memberCode+"','"+registerCode+"','"+businessCode+"','"+provinceCode+"','"+areaCode+"',"+mainBuy+",'"
				+mainProductBuy+"',"+mainSupply+",'"+mainProductSupply+"',"+loginCount+",'"+gmtLogin+"','"
				+gmtRegister+"',now(),now(),now())";
			boolean result = DBUtils.insertUpdate(DB, sql);
			if (!result) {
				LOG.info(">>>>>>>>>>>>>>>>>备份新创建客户失败:"+sql);
			}
		}
	}
	
	private String buildData(String dateString){
		String date = "";
		if (StringUtils.isNotEmpty(dateString)) {
			JSONObject dateObject = JSONObject.fromObject(dateString);
			date = DateUtil.toString(new Date(dateObject.getLong("time")), DATE_FORMAT_DETAIL);
		}
		return date;
	}
}
