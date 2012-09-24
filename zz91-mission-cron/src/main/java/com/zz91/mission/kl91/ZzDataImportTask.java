package com.zz91.mission.kl91;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
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
 * zz91外网数据导入kl91
 * 1，搜索外网公司信息id，判断kl91库的old_id是否与id相同，如果相同就更新，不同就插入
 * 
 */

public class ZzDataImportTask implements ZZTask{
	
	private final static String DB_AST = "astoback";
	private final static String DB_KL91 = "kl91";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		String sql="select count(*) from products where check_status = 1 and is_del = 0 and is_pause = 0 and category_products_main_code like '1001%'";
		final Integer[] count=new Integer[1];
		count[0]=0;
		DBUtils.select(DB_AST, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					count[0]=rs.getInt(1);
				}
			}
		});
		Integer total=count[0]/100;	
		for(Integer i=1;i<=total;i++){
			String sqlId = "select company_id,id from products where check_status=1 and is_del=0 and is_pause=0 and category_products_main_code like '1001%' limit "+100*(i-1) +"," + 100;
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
		}
		return true;
	}

	private void updateCompanyInsert(String email, String account,
			String introduction, String membershipCode, String business,
			String contact, Integer sex, String name, String mobile,
			String tel, Integer numLogin, String gmtLastLogin,
			String industryCode, String domain, Integer isActive,
			Integer registFlag, String password, Integer oldId,String gmtCreated) {
		String sql = "update company set email='" + email + "',account='"
				+ account + "',introduction='" + introduction
				+ "',membership_code='" + membershipCode + "',business='"
				+ business + "'" + ",contact='" + contact + "',sex=" + sex
				+ ",company_name='" + name + "',mobile='" + mobile + "',tel='"
				+ tel + "',num_login=" + numLogin + "," + "gmt_last_login='"
				+ gmtLastLogin + "',industry_code='" + industryCode
				+ "',domain='" + domain + "',is_active=" + isActive
				+ ",regist_flag=" + registFlag + "," + "password='" + password
				+ "',gmt_created='"+gmtCreated+"' where old_id=" + oldId + "";
		DBUtils.insertUpdate(DB_KL91, sql);
	}
	
	//搜索公司，并且判断ast表old_id是死海表中,如果存在就更新，如果不存在就插入新公司
	private void selectKLCompanyIdAndImport(Integer companyId) throws ParseException {
		//判断ast表old_id是否存在私海
		String sql = "select count(0) from crm_sh where com_id="+companyId;
		final Integer[] count=new Integer[1];
		count[0]=0;
		DBUtils.select(DB_KL91, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					count[0]=rs.getInt(1);
				}
			}
		});
		sql = "select c.id,ca.email,ca.account,c.address,c.introduction,c.business,ca.contact,ca.sex,c.name,ca.mobile,ca.tel,c.old_id,ca.password,c.website,c.gmt_created,c.membership_code from company c left join company_account ca on ca.company_id=c.id where c.id=" + companyId;
		final Map<String,Object> map = new HashMap<String,Object>();
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
					map.put("gmtCreated", rs.getString(15));
					map.put("membershipCode", rs.getString(16));
				}
			}
		});
		//邮箱
		String email=(String)map.get("email");
		if(email == null){
			email= "" ;
		}
		//账户
		// 判断账户是不是邮箱如果是就取@以前的为账户
		String account = (String)map.get("account");
		if(account==null){
			account="kl91admin";
		}
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
		//地址
		String address=(String)map.get("address");
		if(address == null){
			address= "" ;
		}
		//介绍
		String introduction=(String)map.get("introduction");
		if(email == null){
			introduction= "" ;
		}
		//主营业务
		String business=(String)map.get("business");
		if(email == null){
			business= "" ;
		}
		//联系人
		String contact=(String)map.get("contact");
		if(contact == null){
			contact= "" ;
		}
		//性别
		String se=(String)map.get("sex");
		if(se==null){
			se="M";
		}
		Integer sex=0;
		if (se.equals("M")) {
			sex=1;
		} else {
			sex=0;
		}
		//公司名称
		String companyName=(String)map.get("name");
		if(companyName == null){
			companyName= "" ;
		}
		//手机
		String mobile=(String)map.get("mobile");
		if(mobile == null){
			mobile= "" ;
		}
		//座机
		String tel=(String)map.get("tel");
		if(tel == null){
			tel= "" ;
		}
		//密码
		String password=(String)map.get("password");
		if(password==null){
			password="123456";
		}
		try {
			password = MD5.encode(password, MD5.LENGTH_32);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		//域名
		String website=(String)map.get("website");
		if(website == null){
			website= "" ;
		}
		//创建时间
		String gmtCreated=(String)map.get("gmtCreated");
		if(gmtCreated == null){
			gmtCreated= DateUtil.toString(new Date(), "yyyy-MM-dd HH:mm:ss");
		}
		//会员类型
		String membershipCode=(String)map.get("membershipCode");
		if(membershipCode !=null && !membershipCode.equals("10051000")){
			membershipCode="10051001";
		}else{
			membershipCode="10051000";
		}
		// sihai :3
		//普会
		Integer regesiterFlag = 4;
		//如果存在私海
		if(count[0]!=0){
			regesiterFlag = 6;
		}
		// company is exist kl91
		sql = "select count(0) from company where old_id="+companyId;
		final Integer[] counts=new Integer[1];
		counts[0]=0;
		DBUtils.select(DB_KL91, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					counts[0]=rs.getInt(1);
				}
			}
		});
		
		if(counts[0]!=0){
			updateCompanyInsert(email,account,introduction,membershipCode,business,contact, sex, companyName,mobile,tel, 1, 
				DateUtil.toString(new Date(),"yyyy-MM-dd HH:mm:ss"),"1000", website,0,regesiterFlag,password, companyId,gmtCreated);
		}else{
			saveToKL(email, account, introduction,membershipCode, business, contact, sex,companyName,mobile,tel, 1, 
					DateUtil.toString(new Date(),"yyyy-MM-dd HH:mm:ss"),"1000", website,0,regesiterFlag,companyId, password,gmtCreated);
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

	private void selectProducts(Integer productId) throws Exception {
		// 循环遍历得到供求id并且根据id搜出供求信息
		String sql = "select products_type_code,title,details,location,price_unit,quantity_unit,quantity,color,useful," +
				"min_price,max_price,company_id,right(p.products_type_code,1) as pdt_kind,c.label AS label0, " +
				"c1.label AS label1, c2.label AS label2, c3.label AS label3,c4.label AS label4 FROM ast.products AS p LEFT OUTER JOIN " +
				"ast.category_products AS c on p.category_products_main_code = c.code LEFT OUTER JOIN ast.category_products" +
				" AS c1 on LEFT(p.category_products_main_code, 4) = c1.code LEFT OUTER JOIN ast.category_products AS c2 on" +
				" LEFT(p.category_products_main_code, 8) = c2.code LEFT OUTER JOIN ast.category_products AS c3 ON LEFT" +
				"(p.category_products_main_code, 12) = c3.code " +
				"LEFT OUTER JOIN ast.category_products AS c4 ON LEFT(p.category_products_main_code, 16) = c4.code where p.id= " + productId;
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
						map.put("label0", rs.getString(14));
						map.put("label1", rs.getString(15));
						map.put("label2", rs.getString(16));
						map.put("label3", rs.getString(17));
						map.put("label4", rs.getString(18));
					}
				}
			});
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
			String label1=(String) map.get("label1");
		
			String label2=(String) map.get("label2");
			
			String label3=(String) map.get("label3");
			
			String label4=(String) map.get("label4");
			
			String productsCategoryCode=label1+">"+label2+">"+label3+">"+label4;
			// 标题
			String title = (String) map.get("title");
			if (title == null) {
				title = "";
			}
			// 产品详情
			String details=StringUtils.controlLength((String) map.get("details"), 3000);
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
			//判断ast表的old_id是否存在kl91_test和死海表里面
			
			
			
			sql = "select count(0) from products where old_id="+productId;
			final Integer[] count=new Integer[1];
			count[0]=0;
			DBUtils.select(DB_KL91, sql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()){
						count[0]=rs.getInt(1);
					}
				}
			});
			
			if(count[0]>0){
				updateProducts(cid, productsCategoryCode, typeCode, title, details,"", 1, 0, 5, 1, location, useful, gmtPost, gmtPost, 
						DateUtil.toString(DateUtil.getDateAfterMonths(new Date(), +6), "yyyy-MM-dd HH:mm:ss"), color,
						priceUnit, quantityUnit, quantity, minPrice, maxPrice, productId);
			}else{
				insertProducts(cid, productsCategoryCode, typeCode, title, details,"", 1, 0, 5,1, location, useful, gmtPost, gmtPost,
					DateUtil.toString(DateUtil.getDateAfterMonths(new Date(), +6), "yyyy-MM-dd HH:mm:ss"), 
					color, priceUnit, quantityUnit, quantity,minPrice, maxPrice, productId);
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
			Integer registFlag, Integer oldId, String password,String gmtCreated) {
		String sql = "insert into company(account,password,company_name,membership_code,sex,contact," +
				"mobile,email,tel,business,introduction,num_login,gmt_last_login,is_active,domain,industry_code," +
				"regist_flag,show_time,gmt_created,gmt_modified,old_id)values('"
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
				+ "','" + registFlag + "',now(),'"+gmtCreated+"',now()," + oldId + ")";
		DBUtils.insertUpdate(DB_KL91, sql);
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");
		ZzDataImportTask obj = new ZzDataImportTask();
		Date date = DateUtil.getDate("2010-09-11", "yyyy-MM-dd");
		obj.exec(date);
		}

}
