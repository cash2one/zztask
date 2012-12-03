package com.zz91.mission.kl91;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IInsertUpdateHandler;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.encrypt.MD5;
import com.zz91.util.lang.StringUtils;
/**
 * @author 伍金成：同步每天在zz91注册的客户到kl91库
 *1,搜出当天在zz91注册的客户id并且主营行业是废塑料
 *2,根据id搜出需要的公司信息字段
 *3,搜出kl91的old_id判断是否和zz91的id一样
 *4,如果一样就更新，相反就插入
 */
public class KLTonbuDataImportTask implements ZZTask {

	private final static String DB_AST = "zz91";
	private final static String DB_KL91 = "kl91";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String from = DateUtil.toString(
				DateUtil.getDateAfterDays(baseDate, -1), "yyyy-MM-dd HH:mm:ss");
		String to = DateUtil.toString(baseDate, "yyyy-MM-dd HH:mm:ss");
		int i=0;
		int docs=0;
		do{
			// 搜出当天注册的供求，并且得到公司id
			String sqlId = "select id from company where regtime >= '"
				+ from
				+ "' and regtime < '"
				+ to
				+ "'and industry_code like '10001000%' limit "+i*10+",10";
			
			final List<Integer> companyIdlist = new ArrayList<Integer>();
			DBUtils.select(DB_AST, sqlId, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while (rs.next()) {
						companyIdlist.add(rs.getInt(1));
					}
				}
			});
			if(companyIdlist.size()<=0){
				break;
			}
			// 导入公司
			for (Integer companyId : companyIdlist) {
				selectKLCompanyIdAndImport(companyId);
			}
			
			i++;
			docs=docs+companyIdlist.size();
		}while(true);
		
		if(i>0){
			throw new Exception("共更新数据"+docs+"条");
		}
		
		return false;
	}

	private void updateCompanyInsert(String email, String account,
			String introduction, String membershipCode, String business,
			String contact, Integer sex, String name, String mobile,
			String tel, Integer numLogin, String gmtLastLogin,
			String industryCode, String domain, Integer isActive,
			Integer registFlag, String password, String regtime,Integer oldId) {
		final String[] str = new String[]{email,account,introduction,membershipCode,business,contact,
				String.valueOf(sex),name,mobile,tel,String.valueOf(numLogin),
				gmtLastLogin,industryCode,domain,String.valueOf(isActive),String.valueOf(registFlag),password,
				regtime,DateUtil.toString(new Date(), "yyyy-MM-dd HH:mm:ss"),String.valueOf(oldId)};
		String sql="update company set email=?,account=?,introduction=?,membership_code=?,business=?,contact=?,sex=?," +
				"company_name=?,mobile=?,tel=?,num_login=?,gmt_last_login=?,industry_code=?,domain=?,is_active=?,regist_flag=?," +
				"password=?,gmt_created=?,gmt_modified=? where old_id=?";
		DBUtils.insertUpdate(DB_KL91, sql,new IInsertUpdateHandler() {
			@Override
			public void handleInsertUpdate(PreparedStatement ps)
					throws SQLException {
				ps.setString(1, str[0]);
				ps.setString(2, str[1]);
				ps.setString(3, str[2]);
				ps.setString(4, str[3]);
				ps.setString(5, str[4]);
				ps.setString(6, str[5]);
				ps.setInt(7, Integer.parseInt(str[6]));
				ps.setString(8, str[7]);
				ps.setString(9, str[8]);
				ps.setString(10, str[9]);
				ps.setInt(11, Integer.parseInt(str[10]));
				ps.setString(12, str[11]);
				ps.setString(13, str[12]);
				ps.setString(14, str[13]);
				ps.setInt(15, Integer.parseInt(str[14]));
				ps.setInt(16, Integer.parseInt(str[15]));
				ps.setString(17, str[16]);
				ps.setString(18, str[17]);
				ps.setString(19, str[18]);
				ps.setInt(20, Integer.parseInt(str[19]));
				ps.execute();
			}
		});
	}

	private void selectKLCompanyIdAndImport(Integer companyId) {
		String sql = "select count(*) from company where old_id=" + companyId
				+ "";
		final int[] id = { 0 };
		DBUtils.select(DB_KL91, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					id[0] = rs.getInt(1);
				}
			}
		});
		
		sql = "select c.id,ca.email,ca.account,c.address,c.introduction,c.business,ca.contact,ca.sex,c.name,ca.mobile,ca.tel,c.old_id,ca.password,c.website,c.regtime from company c left join company_account ca on ca.company_id=c.id where c.id="
				+ companyId;
		
		final Map<String, Object> map = new HashMap<String, Object>();
		DBUtils.select(DB_AST, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					map.put("cid", rs.getString(1));
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
					map.put("oldId", rs.getInt(12));
					map.put("password", rs.getString(13));
					map.put("website", rs.getString(14));
					map.put("regtime", rs.getDate(15));
				}
			}
		});
		// 邮箱
		String email = (String) map.get("email");
		if (email == null) {
			email = "";
		}
		// 账户
		String account = (String) map.get("account");
		if(account==null){
			account="";
		}
		// 判断账户是不是邮箱如果是就取@以前的为账户
		if (StringUtils.isEmail(account)) {
			account = (String) account.substring(0, account.indexOf("@"));
		}
		// 如果账户里面包含.则删除.
		if (account.contains(".")) {
			account = (String) account.replace(".", "");
		}
		if (account.contains("_")) {
			account = (String) account.replace("_", "");
		}

		if (account.contains("-")) {
			account = (String) account.replace("-", "");
		}
		// 地址
		String address = (String) map.get("address");
		if (address == null) {
			address = "";
		}
		// 介绍
		String introduction = (String) map.get("introduction");
		if (introduction == null) {
			introduction = "";
		}else {
			introduction=Jsoup.clean((String) map.get("introduction"), Whitelist.none());
		}
		// 主营业务
		String business = (String) map.get("business");
		if (business == null) {
			business = "";
		}
		// 联系人
		String contact = (String) map.get("contact");
		if (contact == null) {
			contact = "";
		}
		// 性别
		String se = (String) map.get("sex");
		Integer sex = 0;
		if (se!=null && se.equals("M")) {
			sex = 1;
		} else {
			sex = 0;
		}
		// 公司名称
		String companyName = (String) map.get("name");
		if (companyName == null) {
			companyName = "";
		}
		// 手机
		String mobile = (String) map.get("mobile");
		if (mobile == null) {
			mobile = "";
		}
		// 座机
		String tel = (String) map.get("tel");
		if (tel == null) {
			tel = "";
		}
		// 密码
		String password = (String) map.get("password");
		try {
			password = MD5.encode(password, MD5.LENGTH_32);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		// 域名
		String website = (String) map.get("website");
		if (website == null) {
			website = "";
		}
		Date regtimes = (Date) map.get("regtime");
		String regtime = null;
		try {
			regtime = DateUtil.toString(DateUtil.getDate(regtimes, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (regtime == null) {
			regtime = "";
		}
		
		if (id[0] != 0) {
			updateCompanyInsert(email, account, introduction, "10051000",
					business, contact, sex, companyName, mobile, tel, 0,
					regtime,"1000", website, 0, 1, password,regtime, companyId);
		} else {
			saveToKL(email, account, introduction, "10051000", business,
					contact, sex, companyName, mobile, tel, 0,regtime,
					"1000", website, 0, 1, companyId, regtime,password);
		}
	}

	private void saveToKL(String email,String account, String introduction,
			String membershipCode, String business, String contact,
			Integer sex, String name, String mobile, String tel,
			Integer numLogin, String gmtLastLogin, String industryCode,
			String domain, Integer isActive, Integer registFlag, Integer oldId,String regtime,
			String password) {
		final String[] str = new String[]{account,password,name,membershipCode,String.valueOf(sex),
				contact,mobile,email,tel,business,introduction,String.valueOf(numLogin),
				gmtLastLogin,String.valueOf(isActive),domain,industryCode,String.valueOf(registFlag),
				DateUtil.toString(new Date(), "yyyy-MM-dd HH:mm:ss"),
				regtime,DateUtil.toString(new Date(), "yyyy-MM-dd HH:mm:ss"),String.valueOf(oldId)};
		String sql = "insert into company(account,password,company_name,membership_code,sex,contact,"
				+ "mobile,email,tel,business,introduction,num_login,gmt_last_login,is_active,domain,industry_code,"
				+ "regist_flag,show_time,gmt_created,gmt_modified,old_id)values(?,?,?,?,?,?,? ,?,?,?,?,?,?,?, ?,?,?,?,?,?,?)";
		DBUtils.insertUpdate(DB_KL91, sql, new IInsertUpdateHandler() {
			@Override
			public void handleInsertUpdate(PreparedStatement ps)
					throws SQLException {
				ps.setString(1, str[0]);
				ps.setString(2, str[1]);
				ps.setString(3, str[2]);
				ps.setString(4, str[3]);
				ps.setInt(5, Integer.parseInt(str[4]));
				ps.setString(6, str[5]);
				ps.setString(7, str[6]);
				ps.setString(8, str[7]);
				ps.setString(9, str[8]);
				ps.setString(10, str[9]);
				ps.setString(11, str[10]);
				ps.setInt(12, Integer.parseInt(str[11]));
				ps.setString(13, str[12]);
				ps.setInt(14, Integer.parseInt(str[13]));
				ps.setString(15, str[14]);
				ps.setString(16, str[15]);
				ps.setInt(17, Integer.parseInt(str[16]));
				ps.setString(18, str[17]);
				ps.setString(19, str[18]);
				ps.setString(20, str[19]);
				ps.setInt(21, Integer.parseInt(str[20]));
				ps.execute();
			}
		});
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}

	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");
		KLTonbuDataImportTask obj = new KLTonbuDataImportTask();
		Date date = DateUtil.getDate("2008-11-25", "yyyy-MM-dd");
		obj.exec(date);
	}

}
