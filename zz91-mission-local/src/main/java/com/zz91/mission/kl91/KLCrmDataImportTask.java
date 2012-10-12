package com.zz91.mission.kl91;

import java.sql.PreparedStatement;
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
import com.zz91.util.db.IInsertUpdateHandler;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.http.HttpUtils;
import com.zz91.util.lang.StringUtils;

/**
 * @author 伍金成：搜索kl91外网表数据导入klcrm
 * 1,使用HttpUtils从接口中获取昨天注册或登录的客户
 * 2,根据公司id判断crm表是否存在如果存在更新到crm表，不存在就插入到crm表
 *
 */
public class KLCrmDataImportTask implements ZZTask{

	private final static String DATE_FORMAT_DETAIL = "yyyy-MM-dd HH:mm:ss";
	private static String API_HOST="http://admin1949.zz91.com:8311/kl91-admin";
	final static String DB="klcrm";
	static final Integer LIMIT = 10;
	
	@Override
	public boolean exec(Date baseDate) throws Exception {
		boolean result=false;
		String from = DateUtil.toString(
				DateUtil.getDateAfterDays(baseDate, -1), "yyyy-MM-dd");
		String to = DateUtil.toString(baseDate, "yyyy-MM-dd");
		do{
			String responseText = HttpUtils.getInstance().httpGet(API_HOST+"/queryYestodayCompanyCount.htm?from="+from+"&to="+to+"", HttpUtils.CHARSET_UTF8);
			JSONObject jb=JSONObject.fromObject(responseText);
			int count = jb.getInt("i");
			int page = getSize(count);
			List<JSONObject> list = new ArrayList<JSONObject>();
			
			// 循环获取所有数据
			for(int i=1;i<=page;i++){
				try {
					responseText = HttpUtils.getInstance().httpGet(API_HOST+"/queryYestodayCompany.htm?from="+from+"&to="+to+"", HttpUtils.CHARSET_UTF8);
				} catch (Exception e) {
					throw new Exception(e.getMessage()+"   start:"+i);
				}
				JSONArray js=JSONArray.fromObject(responseText);
				for (Iterator iter = js.iterator(); iter.hasNext();) {
					JSONObject object = (JSONObject) iter.next();
					list.add(object);
				}
			}
			if(list.size()<=0){
				break;
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
		final String[] str = new String[]{
				String.valueOf(cid),String.valueOf(1),cname,String.valueOf(cid),account,email,
				Short.toString(sex),mobile,phone,fax,position,address,addressZip,details,industryCode,
				memberCode,Short.toString(registerCode),businessCode,areaCode,String.valueOf(1),
				null,String.valueOf(1),null,String.valueOf(loginCount),String.valueOf(5),gmtLogin,
				gmtRegister,DateUtil.toString(new Date(), DATE_FORMAT_DETAIL),gmtRegister,
				DateUtil.toString(new Date(), DATE_FORMAT_DETAIL),name
		};
		String sql="";
		if (!isExsitCompany(cid)) {
			sql ="INSERT INTO crm_company ( "
				+"cid,regist_status,cname,uid,account,"
				+"email,sex,mobile,phone,"
				+"fax,position,address,address_zip,details,industry_code,"
				+"member_code,register_code,business_code,area_code,main_buy,"
				+"main_product_buy,main_supply,main_product_supply,login_count,ctype,gmt_login,"
				+"gmt_register,gmt_input,gmt_created,gmt_modified,name) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?,?)";
			DBUtils.insertUpdate(DB, sql, new IInsertUpdateHandler() {
				@Override
				public void handleInsertUpdate(PreparedStatement ps)
						throws SQLException {
					ps.setInt(1, Integer.parseInt(str[0]));
					ps.setInt(2, Integer.parseInt(str[1]));
					ps.setString(3, str[2]);
					ps.setInt(4, Integer.parseInt(str[3]));
					ps.setString(5, str[4]);
					ps.setString(6, str[5]);
					ps.setShort(7, Short.valueOf(str[6]));
					ps.setString(8, str[7]);
					ps.setString(9, str[8]);
					ps.setString(10, str[9]);
					ps.setString(11, str[10]);
					ps.setString(12, str[11]);
					ps.setString(13, str[12]);
					ps.setString(14, str[13]);
					ps.setString(15, str[14]);
					ps.setString(16, str[15]);
					ps.setShort(17, Short.valueOf(str[16]));
					ps.setString(18, str[17]);
					ps.setString(19, str[18]);
					ps.setInt(20, Integer.parseInt(str[19]));
					ps.setString(21, str[20]);
					ps.setInt(22, Integer.parseInt(str[21]));
					ps.setString(23, str[22]);
					ps.setInt(24, Integer.parseInt(str[23]));
					ps.setInt(25, Integer.parseInt(str[24]));
					ps.setString(26, str[25]);
					ps.setString(27, str[26]);
					ps.setString(28, str[27]);
					ps.setString(29, str[28]);
					ps.setString(30, str[29]);
					ps.setString(31, str[30]);
					ps.execute();
				}
			});
		} else {
			sql="update crm_company set cname=?,uid=?,account=?,email=?,name=?,sex=?,mobile=?,phone=?,fax=?,position=?," +
					"address=?,address_zip=?,details=?,industry_code=?,member_code=?,business_code=?,area_code=?,login_count=?,gmt_login=?,gmt_modified=? where cid=?";
			DBUtils.insertUpdate(DB, sql,new IInsertUpdateHandler() {
				@Override
				public void handleInsertUpdate(PreparedStatement ps)
						throws SQLException {
					ps.setString(1, str[2]);
					ps.setInt(2, Integer.parseInt(str[3]));
					ps.setString(3, str[4]);
					ps.setString(4, str[5]);
					ps.setString(5, str[30]);
					ps.setShort(6, Short.valueOf(str[6]));
					ps.setString(7, str[7]);
					ps.setString(8, str[8]);
					ps.setString(9, str[9]);
					ps.setString(10, str[10]);
					ps.setString(11, str[11]);
					ps.setString(12, str[12]);
					ps.setString(13, str[13]);
					ps.setString(14, str[14]);
					ps.setString(15, str[15]);
					ps.setString(16, str[17]);
					ps.setString(17, str[18]);
					ps.setInt(18, Integer.parseInt(str[23]));
					ps.setString(19, str[25]);
					ps.setString(20, str[29]);
					ps.setInt(21, Integer.parseInt(str[0]));
					ps.execute();
				}
			});
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
		KLCrmDataImportTask obj = new KLCrmDataImportTask();
		API_HOST = "http://test.kl91.zz91.com:8089";
		Date date = DateUtil.getDate("2012-09-29", "yyyy-MM-dd");
		obj.exec(date);
	}

}
