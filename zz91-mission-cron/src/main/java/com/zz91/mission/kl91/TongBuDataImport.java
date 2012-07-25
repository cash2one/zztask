package com.zz91.mission.kl91;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
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
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.encrypt.MD5;
import com.zz91.util.lang.StringUtils;
/**
 * kl91网供求、公司信息与zz91的公司供求数据 同步
 * 
 *
 */
public class TongBuDataImport implements ZZTask {

	private final static String DB_AST = "ast";
	private final static String DB_KL91 = "kl91";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String from = DateUtil.toString(DateUtil.getDateAfterDays(baseDate, -1), "yyyy-MM-dd");
		String to = DateUtil.toString(baseDate, "yyyy-MM-dd");
		// 搜出当天注册的供求，并且得到公司id
		String sqlId = "select company_id,id from products where refresh_time >= '" + from + "' and refresh_time < '" + to + "' and check_status=1 and is_del=0 and is_pause=0 and category_products_main_code like '1001%'";
		final List<Integer> companyIdlist = new ArrayList<Integer>();
		final List<Integer> productIdlist = new ArrayList<Integer>();
		DBUtils.select(DB_AST, sqlId, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					companyIdlist.add(rs.getInt(1));
					productIdlist.add(rs.getInt(2));
				}
			}
		});
		//导入公司
		for (Integer companyId : companyIdlist) {
			selectKLCompanyIdAndImport(companyId);
		}
		//导入供求
		for (Integer productId:productIdlist){
			selectProducts(productId);
		}
		
		return false;
	}

	private void updateCompanyInsert(String email, String account,
			String introduction, String membershipCode, String business,
			String contact, Integer sex, String name, String mobile,
			String tel, Integer numLogin, String gmtLastLogin,
			String industryCode, String domain, Integer isActive,
			Integer registFlag, String password, Integer oldId) {
		String sql = "update company set email='" + email + "',account='"
				+ account + "',introduction='" + introduction
				+ "',membership_code='" + membershipCode + "',business='"
				+ business + "'" + ",contact='" + contact + "',sex=" + sex
				+ ",company_name='" + name + "',mobile='" + mobile + "',tel='"
				+ tel + "',num_login=" + numLogin + "," + "gmt_last_login='"
				+ gmtLastLogin + "',industry_code='" + industryCode
				+ "',domain='" + domain + "',is_active=" + isActive
				+ ",regist_flag=" + registFlag + "," + "password='" + password
				+ "' where old_id=" + oldId + "";
		DBUtils.insertUpdate(DB_KL91, sql);
	}
	
	private void selectKLCompanyIdAndImport(Integer companyId) {
		String sql = "select old_id from company where old_id=" + companyId + "";
		final int[] id = {0};
		DBUtils.select(DB_KL91, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					id[0] = rs.getInt(1);
				}
			}
		});
		sql = "select c.id,ca.email,ca.account,c.address,c.introduction,c.business,ca.contact,ca.sex,c.name,ca.mobile,ca.tel,c.old_id,ca.password,c.website from company c left join company_account ca on ca.company_id=c.id where c.id=" + companyId;
		final Map<String,Object> map = new HashMap<String,Object>();
		DBUtils.select(DB_AST, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					// 默认登录总数
					map.put("numLogin",0);
					// 默认创建时间
					map.put("gmtLastLogin",  DateUtil.toString(new Date(),"yyyy-MM-dd HH:mm:ss"));
					// 给定类别为废塑料
					map.put("industryCode","1000");
					// 默认激活
					map.put("isActive",0);
					// 默认注册来源为后台导入
					map.put("registFlag",1);
					
					map.put("cid", rs.getInt(1));
					String accounst = rs.getString(3);
					// 判断账户是不是邮箱如果是就取@以前的为账户
					String account = accounst;
					if (StringUtils.isEmail(accounst)) {
						account = (String) account.substring(0, accounst.indexOf("@"));
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
					map.put("account", account);
					// 如果email为空就给定账户加上@kl91.com
					String email = rs.getString(2);
					if (email == null) {
						email = account + "@kl91.com";
					}
					map.put("email", email);
					// 联系人
					String contact = rs.getString(7);
					if (contact == null) {
						contact = "";
					}
					map.put("contact",contact);
					// 性别
					String se = rs.getString(8);
					// ast表取出来的性别为char类型的，为了匹配kl91表的Integer
					Integer sex = 0;
					if (se.equals("M")) {
						sex = 0;
					} else {
						sex = 1;
					}
					map.put("sex", sex);
					// 联系人名称
					String companyName = rs.getString(9);
					if (companyName == null) {
						companyName = "";
					}
					map.put("companyName",companyName);
					// 手机
					String mobile = rs.getString(10);
					if (mobile == null) {
						mobile = "";
					}
					map.put("mobile", mobile);
					Integer oldId = rs.getInt(12);
					map.put("oldId", oldId);
					// 密码
					String password = (String) rs.getString(13);
					// 给密码加密
					try {
						password = MD5.encode(password, MD5.LENGTH_32);
					} catch (NoSuchAlgorithmException e1) {
						e1.printStackTrace();
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}
					map.put("password", password);
					// 地址
					map.put("address", rs.getString(4));
					// 公司介绍
					map.put("introduction", rs.getString(5));
					// 主营业务
					map.put("business", rs.getString(6));
					// 企业网站
					map.put("website", rs.getString(14));
					// 座机
					map.put("tel", rs.getString(11));
				}
			}
		});
		if(id[0]!=0){
			updateCompanyInsert(map.get("email").toString(), map.get("account").toString(), 
					map.get("introduction").toString(),"10051000", map.get("business").toString(), 
					map.get("contact").toString(), Integer.valueOf(map.get("sex").toString()), map.get("companyName").toString(),
					map.get("mobile").toString(),map.get("tel").toString(), 1, 
					DateUtil.toString(new Date(),"yyyy-MM-dd HH:mm:ss"),"1000", map.get("website").toString(),
					0,1,map.get("password").toString(), 
					companyId);
		}else{
			saveToKL(map.get("email").toString(), map.get("account").toString(), 
					map.get("introduction").toString(),"10051000", map.get("business").toString(), 
					map.get("contact").toString(), Integer.valueOf(map.get("sex").toString()), map.get("companyName").toString(),
					map.get("mobile").toString(),map.get("tel").toString(), 1, 
					DateUtil.toString(new Date(),"yyyy-MM-dd HH:mm:ss"),"1000", map.get("website").toString(),
					0,1,companyId, 
					map.get("password").toString());
		}
	}

	private void updateProducts(Integer klcid, String productCategoryCode,
			String typeCode, String title, String details, String detailsQuery,
			Integer checkedFlag, Integer deletedFlag, Integer imptFlag,
			Integer publishFlag, String location, String useful,
			String gmtPost, String gmtRefresh, String gmtExpired, String color,
			String priceUnit, String quantityUnit, Integer quantity,
			Integer minPrice, Integer maxPrice, Integer productId) {
		String sql = "update products set cid=" + klcid
				+ ",products_category_code='" + productCategoryCode
				+ "',type_code='" + typeCode + "',title='" + title
				+ "',details='" + details + "'," + "details_query='"
				+ detailsQuery + "',checked_flag=" + checkedFlag
				+ ",deleted_flag=" + deletedFlag + ",impt_flag=" + imptFlag
				+ ",publish_flag=" + publishFlag + ",location='" + location
				+ "',useful='" + useful + "',gmt_post='" + gmtPost + "',"
				+ "gmt_refresh='" + gmtRefresh + "',gmt_expired='" + gmtExpired
				+ "',color='" + color + "',price_unit='" + priceUnit
				+ "',quantity_unit='" + quantityUnit + "',quantity=" + quantity
				+ "," + "min_price=" + minPrice + ",max_price=" + maxPrice
				+ " where old_id=" + productId + "";
		DBUtils.insertUpdate(DB_KL91, sql);
	}

	private Integer getCompanyId(Integer cid) {
		final Integer[] ids = new Integer[1];
		ids[0] = 0;
		String sql = "select id from company where old_id=" + cid;
		DBUtils.select(DB_KL91, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					ids[0] = rs.getInt(1);
				}
			}
		});
		return ids[0];
	}
	
	private Integer getKLProductId(Integer pid){
		final Integer[] ids = new Integer[1];
		ids[0] = 0;
		String sql = "select id from products where old_id=" + pid;
		DBUtils.select(DB_KL91, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					ids[0] = rs.getInt(1);
				}
			}
		});
		return ids[0];
	}
	
	private void selectProducts(Integer productId) {
		// 循环遍历得到供求id并且根据id搜出供求信息
		String sql = "select products_type_code,title,details,location,price_unit,quantity_unit,quantity,color,useful,min_price,max_price,company_id from products where id= " + productId;
			final Map<String, Object> map = new HashMap<String, Object>();
			DBUtils.select(DB_AST, sql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while (rs.next()) {
						map.put("productsTypeCode", rs.getString(1));
						map.put("title", rs.getString(2));
						map.put("details", rs.getString(3));
						map.put("location", rs.getString(4));
						map.put("priceUnit", rs.getString(5));
						map.put("quantityUnit", rs.getString(6));
						map.put("quantity", rs.getString(7));
						map.put("color", rs.getString(8));
						map.put("useful", rs.getString(9));
						map.put("minPrice", rs.getFloat(10));
						map.put("maxPrice", rs.getFloat(11));
						map.put("companyId", rs.getInt(12));
					}
				}
			});
			// 默认产品类别为废塑料
			String productCategoryCode = "1000";
			// 默认摘要
			String detailsQuery = "";
			// 默认审核为通过
			Integer checkedFlag = 1;
			// 指定供求未删除
			Integer deletedFlag = 0;
			// 指定为导入数据
			Integer imptFlag = 1;
			// 默认为已发布
			Integer publishFlag = 1;
			// 默认时间为当前
			String gmtPost = DateUtil.toString(new Date(),
					"yyyy-MM-dd HH:mm:ss");
			// ast表里面供求类型复杂所以这里指定供求类型为了匹配kl91表
			String typeCode = (String) map.get("productsTypeCode");
			if (typeCode.equals("10331001")) {
				typeCode = "0";
			}
			if (typeCode.equals("10331000")) {
				typeCode = "1";
			}
			// 标题
			String title = (String) map.get("title");
			if (title == null) {
				title = "";
			}
			// 产品详情
			String details = (String) map.get("details");
			if (details == null) {
				details = "";
			}
			// 发货地址
			String location = (String) map.get("location");
			if (location == null) {
				location = "";
			}
			// 供求用处
			String useful = (String) map.get("useful");
			if (useful == null) {
				useful = "";
			}
			// 价格单位
			String priceUnit = (String) map.get("priceUnit");
			if (priceUnit == null) {
				priceUnit = "";
			}
			// 产品颜色
			String color = (String) map.get("color");
			if (color == null) {
				color = "";
			}
			// 数量单位
			String quantityUnit = (String) map.get("quantityUnit");
			if (quantityUnit == null) {
				quantityUnit = "";
			}
			// 数量
			Integer quantitys = (Integer) map.get("quantitys");
			Integer quantity = 0;
			if (quantitys == null) {
				quantity = 0;
			}

			// 最大价格和最小价格强制转换成int匹配kl91表
			Float minPrices = (Float) map.get("minPrice");
			String i = minPrices.toString();
			i = i.substring(0, i.indexOf("."));
			Integer minPrice = Integer.valueOf(i);
			if (minPrice == null) {
				minPrice = 0;
			}

			Float maxPrices = (Float) map.get("maxPrices");
			Integer maxPrice = 0;
			if (maxPrices == null) {
				maxPrice = 0;
			} else {
				i = maxPrices.toString();
				i = i.substring(0, i.indexOf("."));
				maxPrice = Integer.valueOf(i);
			}
			Integer cid = getCompanyId(Integer.valueOf(map.get("companyId").toString()));
			
			Integer pid = getKLProductId(productId);
			
			if(pid>0){
				updateProducts(cid, productCategoryCode, typeCode, title, details,
						detailsQuery, checkedFlag, deletedFlag, imptFlag, publishFlag, 
						location, useful, gmtPost, gmtPost, 
						DateUtil.toString(DateUtil.getDateAfterMonths(new Date(), +6), "yyyy-MM-dd HH:mm:ss"), color,
						priceUnit, quantityUnit, quantity, minPrice, maxPrice, productId);
			}else{
				insertProducts(cid, "1000", typeCode, title, details,
						"", 1, 0, 1,
						1, location, useful, gmtPost, gmtPost,
						DateUtil.toString(DateUtil.getDateAfterMonths(
								new Date(), +6), "yyyy-MM-dd HH:mm:ss"), color, priceUnit, quantityUnit, quantity,
								minPrice, maxPrice, productId);
			}
			
		}

	private void insertProducts(Integer cid, String productCategoryCode,
			String typeCode, String title, String details, String detailsQuery,
			Integer checkedFlag, Integer deletedFlag, Integer imptFlag,
			Integer publishFlag, String location, String useful,
			String gmtPost, String gmtRefresh, String gmtExpired, String color,
			String priceUnit, String quantityUnit, Integer quantity,
			Integer minPrice, Integer maxPrice, Integer oldId) {
		String sql = "insert into products(cid,products_category_code,type_code,title,details,details_query,checked_flag,"
				+ "deleted_flag,impt_flag,publish_flag,location,useful,gmt_post,gmt_refresh,gmt_expired,color,price_unit,quantity_unit,quantity,min_price,max_price,old_id,show_time,gmt_created,gmt_modified,gmt_check)"
				+ "values('"
				+ cid
				+ "','"
				+ productCategoryCode
				+ "','"
				+ typeCode
				+ "','"
				+ title
				+ "','"
				+ details
				+ "','"
				+ detailsQuery
				+ "','"
				+ checkedFlag
				+ "','"
				+ deletedFlag
				+ "',"
				+ "'"
				+ imptFlag
				+ "','"
				+ publishFlag
				+ "','"
				+ location
				+ "','"
				+ useful
				+ "','"
				+ gmtPost
				+ "','"
				+ gmtRefresh
				+ "','"
				+ gmtExpired
				+ "','"
				+ color
				+ "','"
				+ priceUnit
				+ "','"
				+ quantityUnit
				+ "','"
				+ quantity
				+ "','"
				+ minPrice
				+ "','"
				+ maxPrice
				+ "',"
				+ oldId + ",now(),now(),now(),now())";
		DBUtils.insertUpdate(DB_KL91, sql);
	}

	private void saveToKL(String email, String account,
			String introduction, String membershipCode, String business,
			String contact, Integer sex, String name, String mobile,
			String tel, Integer numLogin, String gmtLastLogin,
			String industryCode, String domain, Integer isActive,
			Integer registFlag, Integer oldId, String password) {
		String sql = "insert into company(account,password,company_name,membership_code,sex,contact,mobile,email,tel,business,introduction,num_login,gmt_last_login,is_active,domain,industry_code,regist_flag,show_time,gmt_created,gmt_modified,old_id)values('"
				+ account
				+ "','"
				+ password
				+ "','"
				+ name
				+ "','"
				+ membershipCode
				+ "',"
				+ sex
				+ ",'"
				+ contact
				+ "','"
				+ mobile
				+ "','"
				+ email
				+ "','"
				+ tel
				+ "','"
				+ business
				+ "','"
				+ introduction
				+ "','"
				+ numLogin
				+ "','"
				+ gmtLastLogin
				+ "','"
				+ isActive
				+ "','"
				+ domain
				+ "','"
				+ industryCode
				+ "','" + registFlag + "',now(),now(),now()," + oldId + ")";
		DBUtils.insertUpdate(DB_KL91, sql);
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}

	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");
		TongBuDataImport obj = new TongBuDataImport();
		Date date = DateUtil.getDate("2012-07-25", "yyyy-MM-dd");
		obj.exec(date);
	}

}
