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

public class PuHuiSecondDataImport implements ZZTask {

	private final static String DB_AST = "ast";
	private final static String DB_KL91 = "kl91";
	@Override
	public boolean clear(Date baseDate) throws Exception {
		
		return false;
	}
	
	@Override
	public boolean exec(Date baseDate) throws Exception {
		//搜出ast表的所有符合普会检索条件（普会，时间，废塑料类别）的公司id
		String sql="select count(*) from company c where c.membership_code = '10051000' and c.industry_code='10001000' and gmt_created >= '2010-11-29 00:00:00' and gmt_created < '2010-11-30 00:00:00'";
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
		//获得company总数并且分页搜索ast库公司的id
		Integer total=count[0]/1000;	
		for(Integer i=1;i<=1000;i++){
			String sqlId="select c.id from company c where c.membership_code = '10051000' and c.industry_code='10001000'  and gmt_created >= '2010-11-29 00:00:00' and gmt_created < '2010-11-30 00:00:00' limit "+total*(i-1) +"," + total;
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
				//1.根据公司id搜出ast表这个公司的供求总数 2,如果总数等于0则不搜公司和供求，如果总数大于0则搜索公司和公司发布的供求
				selectCountProducts(companyId);
			}
		}
		return true;
	}
	
	private void selectCountProducts(Integer companyId) {
		do{
			final Integer[] count=new Integer[1];
			count[0]=0;
			//根据公司id搜索此公司发布的供求总数
			String sql="select count(*) from products where company_id="+companyId+"";
			DBUtils.select(DB_AST, sql, new IReadDataHandler() {
				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()){
						count[0]=rs.getInt(1);
					}
				}
			});
			//如果供求总数为0就跳出不导入公司和供求
			if(count[0]==0){
				break;
			}
			//如果供求总数不等于0则搜出公司的信息
			selectCompany(companyId);
		}while(false);
	}

	
	private void selectCompany(Integer companyId) {
		String sql="select c.id,ca.email,ca.account,c.address,c.introduction,c.business,ca.contact,ca.sex,c.name,ca.mobile,ca.tel,c.old_id,ca.password from company c left join company_account ca on ca.company_id=c.id where company_id="+companyId+"";
		DBUtils.select(DB_AST, sql, new IReadDataHandler() {			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					Integer id=rs.getInt(1);
					//默认登录总数
					Integer numLogin=1;;
					//默认创建时间
					String gmtLastLogin= DateUtil.toString(new Date(),"yyyy-MM-dd HH:mm:ss");
					//给定类别为废塑料
					String industryCode="1000";
					//默认域名
					String domain="zz91.com";
					//默认激活			
					Integer isActive=0;
					//默认注册来源为后台导入	
					Integer registFlag=1;
					//指定会员类型为普会
					String membershipCode="10051000";
								
					String accounst=rs.getString(3);
					//判断账户是不是邮箱如果是就取@以前的为账户
					String account=accounst;
					if(StringUtils.isEmail(accounst)){
						account=(String) account.substring(0, accounst.indexOf("@"));
					}
					//如果账户里面包含.则删除.
					if(account.contains(".")){
						account=(String) account.replace(".", "");
					}
					if(account.contains("_")){
						account=(String) account.replace("_", "");
					}
					if(account.contains("-")){
						account=(String) account.replace("-", "");
					}
					//如果email为空就给定账户加上@kl91.com
					String email=rs.getString(2);
					if(email==null){
						email=account+"@kl91.com";
					}
					//如果地址为空就给空
					String address=rs.getString(4);
					if(address==null){
						address="";
					}
					//公司介绍
					String introduction=rs.getString(5);
					if(introduction==null){
						introduction="";
					}
					//主营业务
					String business=rs.getString(6);
					if(business==null){
						business="";
					}
					//联系人
					String contact=rs.getString(7);
					if(contact==null){
						contact="";
					}
					//性别
					String se=rs.getString(8);
					//ast表取出来的性别为char类型的，为了匹配kl91表的Integer
					Integer sex=0;
					if(se.equals("M")){
						sex=0;
					}else{
						sex=1;
					}
					//联系人名称
					String name=rs.getString(9);
					if(name==null){
						name="";
					}
					//手机
					String mobile=rs.getString(10);
					if(mobile==null){
						mobile="";
					}
					//座机
					String tel=rs.getString(11);
					if(tel==null){
						tel="";
					}
					
					Integer oldId=rs.getInt(12);
					//密码
					String password=(String)rs.getString(13);
					//给密码加密
					try {
						password=MD5.encode(password,MD5.LENGTH_32);
					} catch (NoSuchAlgorithmException e1) {
						e1.printStackTrace();
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}
					
					Boolean bl = false;
					try {
						//根据公司ID获取公司的old_id并且判断kl91表的old_id是否一样
						bl = compareForId(oldId);
					} catch (Exception e) {
						e.printStackTrace();
					}
					//如果不存在oldId插入到kl91的公司表
					if(bl==true){
						try {
							saveToKL(id,email,account,introduction,membershipCode,business,contact,sex,name,mobile,tel,
								numLogin,gmtLastLogin,industryCode,domain,isActive,registFlag,oldId,password);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
					try {
						//开始执行供求	根据导进去的kl91表的old_id得到ast表的公司id并且根据ast表id搜出ast表的供求
						execProducts(oldId);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
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
		//如果kl91的公司表存在oldId则不插入
		if(ids[0]!=0){
			return false;
		}
		return true;
	}
	
	
	private void saveToKL(Integer id, String email, String account,
			String introduction, String membershipCode,
			String business, String contact, Integer sex, String name,
			String mobile, String tel, Integer numLogin,
			String gmtLastLogin, String industryCode, String domain,
			Integer isActive, Integer registFlag, Integer oldId,
			String password) {
		String sql="insert into company(account,password,company_name,membership_code,sex,contact,mobile,email,tel,business,introduction,num_login,gmt_last_login,is_active,domain,industry_code,regist_flag,show_time,gmt_created,gmt_modified,old_id)values('"+account+"','"+password+"','"+name+"','"+membershipCode+"',"+sex+",'"+contact+"','"+mobile+"','"+email+"','"+tel+"','"+business+"','"+introduction+"','"+numLogin+"','"+gmtLastLogin+"','"+isActive+"','"+domain+"','"+industryCode+"','"+registFlag+"',now(),now(),now(),"+oldId+")";
		DBUtils.insertUpdate(DB_KL91, sql );
	}
	
	private void execProducts(Integer oldCompanyId) {
		//根据导进去的公司old_id得到ast表的公司id并且根据id搜出ast库的符合条件的供求
		Integer companyId = getCompanyId(oldCompanyId);
		String sql="select id from products  where company_id="+companyId+" and check_status=1 and is_del=0 and is_pause=0 and gmt_created >= '2010-11-29 00:00:00' and gmt_created < '2010-11-30 00:00:00' and category_products_main_code like '1001%'";
		final List<Integer> list =new ArrayList<Integer>();
		DBUtils.select(DB_AST, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					list.add(rs.getInt(1));
				}
			}
		});
		//循环遍历得到供求id并且根据id搜出供求信息
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
				//默认产品类别为废塑料
				String productCategoryCode="1000";
				//默认摘要
				String detailsQuery="";
				//默认审核为通过
				Integer checkedFlag=1;
				//指定供求未删除
				Integer deletedFlag=0;
				//指定为导入数据
				Integer imptFlag=1;
				//默认为已发布
				Integer publishFlag=1;
				//默认时间为当前
				String gmtPost= DateUtil.toString(new Date(),"yyyy-MM-dd HH:mm:ss");
				String gmtRefresh=DateUtil.toString(new Date(),"yyyy-MM-dd HH:mm:ss");
				String gmtExpired=DateUtil.toString(DateUtil.getDateAfterMonths(new Date(), +6), "yyyy-MM-dd HH:mm:ss");
				//ast表里面供求类型复杂所以这里指定供求类型为了匹配kl91表
				String typeCode=(String) map.get("productsTypeCode");
				if(typeCode.equals("10331001")){
					typeCode="0";
				}if(typeCode.equals("10331000")){
					typeCode="1";
				}
				//标题
				String title=(String)map.get("title");
				if(title==null){
					title="";
				}
				//产品详情
				String details=(String)map.get("details");
				if(details==null){
					details="";
				}
				//发货地址
				String location=(String)map.get("location");
				if(location==null){
					location="";
				}
				//供求用处
				String useful=(String)map.get("useful");
				if(useful==null){
					useful="";
				}
				//价格单位
				String priceUnit=(String)map.get("priceUnit");
				if(priceUnit==null){
					priceUnit="";
				}
				//产品颜色
				String color=(String)map.get("color");
				if(color==null){
					color="";
				}
				//数量单位
				String quantityUnit=(String)map.get("quantityUnit");
				if(quantityUnit==null){
					quantityUnit="";
				}
				//数量
				Integer quantitys=(Integer)map.get("quantitys");
				Integer quantity=0;
				if(quantitys==null){
					quantity=0;
				}
				
				//最大价格和最小价格强制转换成int匹配kl91表
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
				//判断供求表的old_id是否存在
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
				//如果不存在则插入供求
				if(count[0] ==0){
					insertProducts(companyId, productCategoryCode, typeCode, title, details, detailsQuery, checkedFlag, deletedFlag, imptFlag, publishFlag, location, useful, gmtPost, gmtRefresh, gmtExpired, color, priceUnit, quantityUnit, quantity, minPrice, maxPrice,productId);
				}
			}
	}
	
	private Integer getCompanyId(Integer oldCompanyId) {
		final Integer[] companyId=new Integer[1];
		companyId[0]=0;
		String sql="select id from company where old_id="+oldCompanyId+"";
		DBUtils.select(DB_AST, sql, new IReadDataHandler() {			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					companyId[0]=rs.getInt(1);
				}
			}
		});
		return companyId[0];
	}
	
	private void insertProducts(Integer cid, String productCategoryCode,
			String typeCode, String title, String details, String detailsQuery,
			Integer checkedFlag, Integer deletedFlag, Integer imptFlag,
			Integer publishFlag, String location, String useful,
			String gmtPost, String gmtRefresh, String gmtExpired, String color,
			String priceUnit, String quantityUnit, Integer quantity,
			Integer minPrice, Integer maxPrice, Integer oldId) {
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
		PuHuiSecondDataImport obj = new PuHuiSecondDataImport();
		Date date = DateUtil.getDate("2012-04-20", "yyyy-MM-dd");
		obj.exec(date);
		}
	}

