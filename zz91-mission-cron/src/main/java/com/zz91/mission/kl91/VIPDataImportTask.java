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
import com.zz91.util.db.pool.DBPoolFactory;

public class VIPDataImportTask implements ZZTask {

	private final static String DB_AST = "astoback";
	private final static String DB_KL91 = "kl91";

	@Override
	public boolean clear(Date baseDate) throws Exception {

		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		final List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String sql = "select c.id,ca.email,ca.account,c.address,c.introduction,c.business,ca.contact,ca.sex,c.name,ca.mobile,ca.tel from company c left join company_account ca on ca.company_id=c.id where c.membership_code <> '10051000' and c.industry_code='10001000'";
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
			saveToKL(cid,email,account,membershipCode,business,contact,sex,name,mobile,tel,
					numLogin,gmtLastLogin,industryCode,domain,isActive,registFlag);
			
			
		}
		return false;

	}

	private void selectProducts(Integer cid,Integer companyId) {
		final List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String sql="select products_type_code,title,details,location,price_unit,quantity_unit,quantity,color,useful,min_price,max_price from products where company_id="+cid+" and check_status=1 and is_del=0 and is_pause=1";
		DBUtils.select(DB_AST, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					Map<String,Object> map = new HashMap<String, Object>();
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
					map.put("maxPrice", rs.getFloat(10));
					list.add(map);
				}
			}
		});
		for(Map<String, Object> product:list){
			//Integer cpid=0;
			String productCategoryCode="废塑料";
			String typeCode=(String)product.get("productsTypeCode");
			if(typeCode.equals("10331001")){
				typeCode="0";
			}
			if(typeCode.equals("10331000")){
				typeCode="1";
			}
			String title=(String)product.get("title");
			if(title==null){
				title="";
			}
			String details=(String)product.get("details");
			if(details==null){
				details="";
			}
			String detailsQuery="";
			Integer checkedFlag=1;
			Integer deletedFlag=0;
			Integer imptFlag=0;
			Integer publishFlag=1;
			String gmtPost= DateUtil.toString(new Date(),"yyyy-MM-dd HH:mm:ss");
			String gmtRefresh=DateUtil.toString(new Date(),"yyyy-MM-dd HH:mm:ss");
			String gmtExpired=DateUtil.toString(DateUtil.getDateAfterMonths(new Date(), +6), "yyyy-MM-dd HH:mm:ss");
			String location=(String)product.get("location");
			if(location==null){
				location="";
			}
			String useful=(String)product.get("useful");
			if(useful==null){
				useful="";
			}
			String priceUnit=(String)product.get("priceUnit");
			if(priceUnit==null){
				priceUnit="";
			}
			
			String color=(String)product.get("color");
			if(color==null){
				color="";
			}
			String quantityUnit=(String)product.get("quantityUnit");
			if(quantityUnit==null){
				quantityUnit="";
			}
			String quantitys=(String)product.get("quantity");
			Integer quantity=0;
			if(quantitys.equals("")){
				quantity=0;
			}
			Float minPrices=(Float)product.get("minPrice");
			Integer minPrice=0;
			if(minPrices==null){
				minPrice=0;
			}
			Float maxPrices=(Float)product.get("maxPrice");
			Integer maxPrice=0;
			if(maxPrices==null){
				maxPrice=0;
			}
			if(list.size()>0){
				insertProducts(companyId,productCategoryCode,typeCode,title,details,detailsQuery,checkedFlag,deletedFlag,imptFlag
						,publishFlag,location,useful,gmtPost,gmtRefresh,gmtExpired,color,priceUnit,quantityUnit,quantity,minPrice,maxPrice);
			}
		}
	}

	private void insertProducts(Integer companyId,String productCategoryCode, String typeCode,
			String title, String details, String detailsQuery,
			Integer checkedFlag, Integer deletedFlag, Integer imptFlag,
			Integer publishFlag, String location, String useful,String gmtPost,String gmtRefresh,String gmtExpired,String color,
			String priceUnit, String quantityUnit, Integer quantity,
			Integer minPrice, Integer maxPrice) {
		String sql="insert into products(cid,products_category_code,type_code,title,details,details_query,checked_flag," +
				"deleted_flag,impt_flag,publish_flag,location,useful,gmt_post,gmt_refresh,gmt_expired,color,price_unit,quantity_unit,quantity,min_price,max_price,show_time,gmt_created,gmt_modified,gmt_check)" +
				"values('"+companyId+"','"+productCategoryCode+"','"+typeCode+"','"+title+"','"+details+"','"+detailsQuery+"','"+checkedFlag+"','"+deletedFlag+"'," +
						"'"+imptFlag+"','"+publishFlag+"','"+location+"','"+useful+"','"+gmtPost+"','"+gmtRefresh+"','"+gmtExpired+"','"+color+"','"+priceUnit+"','"+quantityUnit+"','"+quantity+"','"+minPrice+"','"+maxPrice+"',now(),now(),now(),now())";
		DBUtils.insertUpdate(DB_KL91, sql);
	}

	private void saveToKL(Integer cid,String email, String account, String membershipCode,
			String business, String contact, Integer sex, String name,
			String mobile, String tel, Integer numLogin, String gmtLastLogin,
			String industryCode, String domain, Integer isActive,
			Integer registFlag) {
		String sql="insert into company(account,company_name,membership_code,sex,contact,mobile,email,tel,business,num_login,gmt_last_login,is_active,domain,industry_code,regist_flag,show_time,gmt_created,gmt_modified)values('"+account+"','"+name+"','"+membershipCode+"',"+sex+",'"+contact+"','"+mobile+"','"+email+"','"+tel+"','"+business+"','"+numLogin+"','"+gmtLastLogin+"','"+isActive+"','"+domain+"','"+industryCode+"','"+registFlag+"',now(),now(),now())";
		DBUtils.insertUpdate(DB_KL91, sql );
		final Integer [] i = new Integer[1];
		DBUtils.select(DB_KL91, "select last_insert_id()", new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					i[0] = rs.getInt(1);
				}
			}
		});
		selectProducts(cid,i[0]);
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

	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");
		VIPDataImportTask obj = new VIPDataImportTask();
		Date date = DateUtil.getDate("2012-04-20", "yyyy-MM-dd");
		obj.exec(date);
	}

}
