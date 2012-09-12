/**
 * @author qizj
 * @email  qizhenj@gmail.com
 * @create_time  2012-9-5 上午10:13:43
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

public class CrmCompanyAssignTask implements ZZTask{
	
	Logger LOG=Logger.getLogger(CrmCompanyAssignTask.class);
	
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
			Integer start=0;
			Integer limit=10;
			//注册数据导入
			regData(targetDate,start,limit);
			//非注册数据导入
			unRegData(targetDate,start,limit);
			
			//统计今天注册客户
//			tongji(targetDate);
			result=true;
		}while(false);
		return result;
	}
	
	//注册的
	public void regData(String  targetDate , Integer start , Integer limit) throws Exception{
		//查询昨天新注册的公司数量
		String responseText = HttpUtils.getInstance().httpGet(API_HOST+"/regCompanyCount.htm?date="+targetDate, HttpUtils.CHARSET_UTF8);
		JSONObject object = JSONObject.fromObject(responseText);
		Integer count = Integer.valueOf(object.getString("regTotals"));
		Integer n=((count-1)/limit)+1;
		for (int i = 0; i < n; i++) {
			//插入
			insertRegData(targetDate, start, limit);
			start+=limit;
		}
	}
	
	//非注册
	public void unRegData(String  targetDate , Integer start , Integer limit) throws Exception{
		//查询昨天新注册的公司数量
		String responseText = HttpUtils.getInstance().httpGet(API_HOST+"/unRegCompanyCount.htm?date="+targetDate, HttpUtils.CHARSET_UTF8);
		JSONObject object = JSONObject.fromObject(responseText);
		Integer count = Integer.valueOf(object.getString("unRegTotals"));
		Integer n=((count-1)/limit)+1;
		for (int i = 0; i < n; i++) {
			//插入非注册
//			insertUnRegData(targetDate, start, limit);
			start+=limit;
		}
	}
	
	//插入新注册客户数据
	private int insertRegData(String targetDate,Integer start,Integer limit)  throws Exception{
		Integer result=0;
		
		String responseText = HttpUtils.getInstance().httpGet(API_HOST+"/regCompany.htm?date="+targetDate+"&start="+start+"&limit="+limit, HttpUtils.CHARSET_UTF8);
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
		return result;
	}
	
//	//更新非注册客户数据
//	private int insertUnRegData(String targetDate,Integer start,Integer limit)  throws Exception{
//		Integer result=0;
//		
//		String responseText = HttpUtils.getInstance().httpGet(API_HOST+"/unRegCompany.htm?date="+targetDate+"&start="+start+"&limit="+limit, HttpUtils.CHARSET_UTF8);
//		JSONArray jsonarray=JSONArray.fromObject(responseText);
//		for (Iterator iter = jsonarray.iterator(); iter.hasNext();) {
//			JSONObject object = (JSONObject) iter.next();
//			String gmtLogin = buildData(object.getString("gmtLogin"));
//			String gmtRegister = buildData(object.getString("gmtRegister"));
//			//插入公司信息
//			loadCompany(object.getInt("id"), object.getString("cname") , object.getInt("uid"),
//					object.getString("account") , object.getString("email") , object.getString("name") , Short.valueOf(object.getString("sex")) ,
//					object.getString("mobile") , object.getString("phoneCountry") , object.getString("phoneArea") , object.getString("phone") ,
//					object.getString("faxCountry") , object.getString("faxArea") , object.getString("fax") , object.getString("address") ,
//					object.getString("addressZip") , object.getString("details") , object.getString("industryCode") ,
//					object.getString("memberCode") , Short.valueOf(object.getString("registerCode")) , object.getString("businessCode") ,
//					object.getString("provinceCode") , object.getString("areaCode") , Short.valueOf(object.getString("mainBuy")) ,
//					object.getString("mainProductBuy") , Short.valueOf(object.getString("mainSupply")) , object.getString("mainProductSupply") ,
//					object.getInt("loginCount") ,  gmtLogin, gmtRegister, object.getString("contact"), object.getString("position"));
//			if (!isExsitCompany(object.getInt("id"))) {
//				LOG.info(">>>>>>>>>>>>>>>>>插入失败:数据ID:"+object.getInt("id")+";帐号:"+object.getString("account")
//						+";公司:"+object.getString("cname"));
//			}
//		}
//		return result;
//	}
	
	
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

}
