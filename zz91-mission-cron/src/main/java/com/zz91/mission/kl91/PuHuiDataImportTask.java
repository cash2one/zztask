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

public class PuHuiDataImportTask implements ZZTask {

	private final static String DB_AST = "ast";
	private final static String DB_KL91 = "kl91";
	@Override
	public boolean clear(Date baseDate) throws Exception {
		
		return false;
	}
	@Override
	public boolean exec(Date baseDate) throws Exception {
		String sql="select count(*) from company c where c.membership_code = '10051000' and c.industry_code='10001000' and gmt_created > '2012-01-01 00:00:00' and gmt_created < '2012-07-01 00:00:00'";
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
			String sqlId="select c.id from company c where c.membership_code = '10051000' and c.industry_code='10001000' and gmt_created > '2012-01-01 00:00:00' and gmt_created < '2012-07-01 00:00:00' limit "+100*(i-1) +"," + 100;
			final List<Integer> list=new ArrayList<Integer>();
			DBUtils.select(DB_AST, sqlId, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()){
						int companyId = rs.getInt(1);
						list.add(companyId);
					}
				}
			});
			for(Integer companyId:list){
				selectCompany(companyId);
			}
		}
		return true;
	}
	//搜索ast公司
	private void selectCompany(Integer companyId) {
		String sql="select c.id,ca.email,ca.account,c.address,c.introduction,c.business,ca.contact,ca.sex,c.name,ca.mobile,ca.tel,c.old_id,ca.password from company c left join company_account ca on ca.company_id=c.id where company_id="+companyId+"";
		DBUtils.select(DB_AST, sql, new IReadDataHandler() {			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					Integer id=rs.getInt(1);
				
					Integer numLogin=1;;
					String gmtLastLogin= DateUtil.toString(new Date(),"yyyy-MM-dd HH:mm:ss");
								
					String industryCode="1000";
	
					String domain="zz91.com";
								
					Integer isActive=0;
								
					Integer registFlag=1;
								
					String membershipCode="10051001";
								
					String accounst=rs.getString(3);
					String account=accounst;
					if(StringUtils.isEmail(accounst)){
						account=(String) account.substring(0, accounst.indexOf("@"));
					}
					if(account.contains(".")){
						account=(String) account.replace(".", "");
					}
					if(account.contains("_")){
						account=(String) account.replace("_", "");
					}
					if(account.contains("-")){
						account=(String) account.replace("-", "");
					}
					
					String email=rs.getString(2);
					if(email==null){
						email=account+"@kl91.com";
					}
					
					String address=rs.getString(4);
					if(address==null){
						address="";
					}
					
					String introduction=rs.getString(5);
					if(introduction==null){
						introduction="";
					}
					String business=rs.getString(6);
					if(business==null){
						business="";
					}
					
					String contact=rs.getString(7);
					if(contact==null){
						contact="";
					}
					
					String se=rs.getString(8);
					
					Integer sex=0;
					if(se.equals("M")){
						sex=0;
					}else{
						sex=1;
					}
					
					String name=rs.getString(9);
					if(name==null){
						name="";
					}
					
					String mobile=rs.getString(10);
					if(mobile==null){
						mobile="";
					}
					
					String tel=rs.getString(11);
					if(tel==null){
						tel="";
					}
					
					Integer oldId=rs.getInt(12);
					
					String password=(String)rs.getString(13);
					
					try {
						password=MD5.encode(password,MD5.LENGTH_32);
					} catch (NoSuchAlgorithmException e1) {
						e1.printStackTrace();
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}
					
					Boolean bl = false;
					try {
						bl = compareForId(oldId);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if(bl==true){
						try {
							saveToKL(id,email,account,introduction,membershipCode,business,contact,sex,name,mobile,tel,
								numLogin,gmtLastLogin,industryCode,domain,isActive,registFlag,oldId,password);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
					try {
						execProducts(oldId);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	
	//根据公司ID获取公司的old_id并且判断kl91表的old_id
	private Boolean compareForId(Integer oldId) {
		String sql="select count(0) from company where old_id="+oldId+"";
		final Integer[] ids=new Integer[1];
		ids[0]=0;
		DBUtils.select(DB_KL91, sql, new IReadDataHandler() {			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					ids[0]=rs.getInt(1);
				}
			}
		});
		if(ids[0]!=0){
			return false;
		}
		return true;
	}
	//插入到kl91的公司表
	private void saveToKL(Integer id, String email, String account,String introduction,
			String membershipCode, String business, String contact,
			Integer sex, String name, String mobile, String tel,
			Integer numLogin, String gmtLastLogin, String industryCode,
			String domain, Integer isActive, Integer registFlag,Integer oldId,String password){
		String sql="insert into company(account,password,company_name,membership_code,sex,contact,mobile,email,tel,business,introduction,num_login,gmt_last_login,is_active,domain,industry_code,regist_flag,show_time,gmt_created,gmt_modified,old_id)values('"+account+"','"+password+"','"+name+"','"+membershipCode+"',"+sex+",'"+contact+"','"+mobile+"','"+email+"','"+tel+"','"+business+"','"+introduction+"','"+numLogin+"','"+gmtLastLogin+"','"+isActive+"','"+domain+"','"+industryCode+"','"+registFlag+"',now(),now(),now(),"+oldId+")";
		DBUtils.insertUpdate(DB_KL91, sql );
		
	}
	//开始执行供求	
	private void execProducts(Integer oldCompanyId) throws Exception{
		Integer companyId = getCompanyId(oldCompanyId);
		String sql="select id from products  where company_id="+oldCompanyId+" and check_status=1 and is_del=0 and is_pause=0 and gmt_created > '2012-01-01 00:00:00' and gmt_created < '2012-07-01 00:00:00' and category_products_main_code like '1001%'";
		final List<Integer> list =new ArrayList<Integer>();
		DBUtils.select(DB_AST, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					list.add(rs.getInt(1));
				}
			}
		});
		for(Integer productId:list){
			final Map<String, Object> map=new HashMap<String, Object>();
			sql = "select products_type_code,title,details,location,price_unit,quantity_unit,quantity,color,useful,min_price,max_price from products where id= "+productId;
			DBUtils.select(DB_AST, sql, new IReadDataHandler() {				
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()){						
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
					}
				}
			});
				String productCategoryCode="1000";
				String detailsQuery="";
				Integer checkedFlag=1;
				Integer deletedFlag=0;
				Integer imptFlag=1;
				Integer publishFlag=1;
				String gmtPost= DateUtil.toString(new Date(),"yyyy-MM-dd HH:mm:ss");
				String gmtRefresh=DateUtil.toString(new Date(),"yyyy-MM-dd HH:mm:ss");
				String gmtExpired=DateUtil.toString(DateUtil.getDateAfterMonths(new Date(), +6), "yyyy-MM-dd HH:mm:ss");
				String typeCode=(String) map.get("productsTypeCode");
				if(typeCode.equals("10331001")){
					typeCode="0";
				}if(typeCode.equals("10331000")){
					typeCode="1";
				}
				String title=(String)map.get("title");
				if(title==null){
					title="";
				}
				String details=(String)map.get("details");
				if(details==null){
					details="";
				}
				String location=(String)map.get("location");
				if(location==null){
					location="";
				}
				String useful=(String)map.get("useful");
				if(useful==null){
					useful="";
				}
				String priceUnit=(String)map.get("priceUnit");
				if(priceUnit==null){
					priceUnit="";
				}
				
				String color=(String)map.get("color");
				if(color==null){
					color="";
				}
				String quantityUnit=(String)map.get("quantityUnit");
				if(quantityUnit==null){
					quantityUnit="";
				}
				Integer quantitys=(Integer)map.get("quantitys");
				Integer quantity=0;
				if(quantitys==null){
					quantity=0;
				}
				
				Float minPrices=(Float)map.get("minPrice");
				String i = minPrices.toString();
				i = i.substring(0, i.indexOf("."));
				Integer minPrice =Integer.valueOf(i);
				if(minPrice==null){
					minPrice=0;
				}
				Float maxPrices=(Float)map.get("maxPrices");
				Integer maxPrice = 0;
				if(maxPrices==null){
					maxPrice=0;
				}else{
					i = maxPrices.toString();
					i = i.substring(0, i.indexOf("."));
					maxPrice =Integer.valueOf(i);
				}
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
				if(count[0] ==0){
					insertProducts(companyId, productCategoryCode, typeCode, title, details, detailsQuery, checkedFlag, deletedFlag, imptFlag, publishFlag, location, useful, gmtPost, gmtRefresh, gmtExpired, color, priceUnit, quantityUnit, quantity, minPrice, maxPrice,productId);
				}
			}
	}
		
	private Integer getCompanyId(Integer oldCompanyId) {
		final Integer[] companyId=new Integer[1];
		companyId[0]=0;
		String sql="select id from company where old_id="+oldCompanyId+"";
		DBUtils.select(DB_KL91, sql, new IReadDataHandler() {			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					companyId[0]=rs.getInt(1);
				}
			}
		});
		return companyId[0];
	}
	//插入供求到kl91表
	private void insertProducts(Integer cid,
			String productCategoryCode, String typeCode, String title,
			String details, String detailsQuery, Integer checkedFlag,
			Integer deletedFlag, Integer imptFlag, Integer publishFlag,
			String location, String useful, String gmtPost,
			String gmtRefresh, String gmtExpired, String color,
			String priceUnit, String quantityUnit, Integer quantity,
			Integer minPrice, Integer maxPrice,Integer oldId) {
		String sql="insert into products(cid,products_category_code,type_code,title,details,details_query,checked_flag," +
		"deleted_flag,impt_flag,publish_flag,location,useful,gmt_post,gmt_refresh,gmt_expired,color,price_unit,quantity_unit,quantity,min_price,max_price,old_id,show_time,gmt_created,gmt_modified,gmt_check)" +
		"values('"+cid+"','"+productCategoryCode+"','"+typeCode+"','"+title+"','"+details+"','"+detailsQuery+"','"+checkedFlag+"','"+deletedFlag+"'," +
				"'"+imptFlag+"','"+publishFlag+"','"+location+"','"+useful+"','"+gmtPost+"','"+gmtRefresh+"','"+gmtExpired+"','"+color+"','"+priceUnit+"','"+quantityUnit+"','"+quantity+"','"+minPrice+"','"+maxPrice+"',"+oldId+",now(),now(),now(),now())";
		DBUtils.insertUpdate(DB_KL91, sql);
	}

	@Override
	public boolean init() throws Exception {
		
		return false;
	}

	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");
		PuHuiDataImportTask obj = new PuHuiDataImportTask();
		Date date = DateUtil.getDate("2012-04-20", "yyyy-MM-dd");
		obj.exec(date);
		}
	}

