/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-3-25
 */
package com.zz91.mission.front;

import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zz91.mission.domain.subscribe.CompanyAccount;
import com.zz91.mission.domain.subscribe.Price;
import com.zz91.mission.domain.subscribe.Product;
import com.zz91.mission.domain.subscribe.Subscribe;
import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.http.HttpUtils;
import com.zz91.util.lang.StringUtils;
import com.zz91.util.mail.MailUtil;
import com.zz91.util.search.SearchEngineUtils;
import com.zz91.util.search.SphinxClient;
import com.zz91.util.search.SphinxException;
import com.zz91.util.search.SphinxMatch;
import com.zz91.util.search.SphinxResult;

/**
 * @author mays (mays@zz91.com)
 * 
 *         created on 2011-3-25
 */
public class SubscribeTask implements ZZTask {
	
	public static void main(String[] args) {
		
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		MailUtil.getInstance().init("file:/root/web.properties");
		
		SubscribeTask task = new SubscribeTask();
		SubscribeTask.SEARCH_HOST="211.155.229.180";
		
		try {
			task.exec(DateUtil.getDate("2011-12-14", DATE_FORMAT));
		} catch (ParseException e) {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	final static String DATE_FORMAT = "yyyy-MM-dd";
	final static String DATE_FORMAT_ZH_CN = "yyyy年MM月dd日";

	final static String TRADE_PREFIX = "http://trade.zz91.com";
	final static String PRICE_PREFIX = "http://price.zz91.com";
	
	final static int MAX_PRODUCTS = 6;
	
	public static String SEARCH_HOST="192.168.110.119";
	public static int SEARCH_PORT= 9315;
 
	@Override
	public boolean exec(Date baseDate) throws Exception {
		// 查找当天需要发送邮件订阅的所有订阅信息
		// 根据每条订阅信息查找对应的待发送信息，并拼装email
		// 提交发送信息给email system

		Date todate = DateUtil.getDateAfterDays(baseDate, -1);
		
		final List<Integer> companyIdList = new ArrayList<Integer>();
		String sqlcid = "select distinct company_id from subscribe where email is not null and email<>'' and is_send_by_email=1 ";
		DBUtils.select("ast", sqlcid, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					companyIdList.add(rs.getInt(1));
				}
			}
		});
		
		for (Integer cid : companyIdList) {
			
			//TODO 排除黑名单
			if(isBlocked(cid)){
				continue;
			}
			
			//TODO 查询用户邮箱
			CompanyAccount account=queryTargetEmail(cid);
			String toemail=account.getEmail();
			if("1".equals(account.getIsUseBackEmail())){
				toemail=account.getBackEmail();
			}
			
			if(!StringUtils.isEmail(toemail)){
				continue ;
			}
			
			// 报价订阅
			List<Subscribe> subscribeList=querySubscribe("1", cid);
			Map<String, Object> dataMap=new HashMap<String, Object>();
			String titleKeywords="";
			dataMap.put("accountInfo", account);
			dataMap.put("companyId", cid);
			
			if(subscribeList!=null && subscribeList.size()>0 ){ //&& priceDataMap.size()>0
				for (Subscribe subscribe : subscribeList) {
					
					subscribe.setPriceTypeName(queryPriceTypeName(subscribe.getPriceTypeId()));
					if(titleKeywords==null || "".equals(titleKeywords)){
						titleKeywords=subscribe.getPriceTypeName();
					}
					
					List<Price> priceList=price(subscribe, subscribe.getPriceTypeName(), baseDate);
					if(priceList!=null && priceList.size()>0){
						subscribe.setPriceList(priceList);
					}
				}
				dataMap.put("subscribeList", subscribeList);
//				System.out.println(JSONObject.fromObject(dataMap).toString());
				
//				if("x03570227@163.com".equals(toemail)){
					MailUtil.getInstance().sendMail(
						DateUtil.toString(todate, DATE_FORMAT_ZH_CN)+"-"+titleKeywords+"实时行情资讯-ZZ91再生网", 
						toemail, null,
						null, "zz91", "zz91-subscribe-price", 
						dataMap, MailUtil.PRIORITY_TASK);
//				}
			}
			
			//供求订制
			subscribeList=querySubscribe("0", cid);
			titleKeywords="";
			if(subscribeList!=null && subscribeList.size()>0 ){
				for (Subscribe subscribe : subscribeList) {
					if(titleKeywords==null || "".equals(titleKeywords)){
						titleKeywords=subscribe.getKeywords();
						
					}
					
					if(subscribe.getProductsTypeCode()==null){
						subscribe.setProductsTypeCode("");
					}
					
					subscribe.setKeywordsEncode(URLEncoder.encode(subscribe.getKeywords(), HttpUtils.CHARSET_UTF8));
					
					List<Product> productsList=product(subscribe, baseDate);
					if(productsList!=null && productsList.size()>0){
						subscribe.setProductList(productsList);
					}
				}
				
				dataMap.put("subscribeList", subscribeList);
//				System.out.println(JSONObject.fromObject(dataMap).toString());
//				if("x03570227@163.com".equals(toemail)){
					MailUtil.getInstance().sendMail(
						DateUtil.toString(todate, DATE_FORMAT_ZH_CN)+"-"+titleKeywords+"实时商机-ZZ91再生网", 
						toemail, null,
						null, "zz91", "zz91-subscribe-product",
						dataMap, MailUtil.PRIORITY_TASK);
//				}
			}
			
		}
		return true;
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {

		return false;
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	private List<Subscribe> querySubscribe(String subscribeType, Integer cid){
		final List<Subscribe> list = new ArrayList<Subscribe>();
		StringBuilder sql = new StringBuilder();
		sql.append("select `id`,`company_id`,`account`,`keywords`,");
		sql.append("`is_search_by_area`,`area_code`");
		sql.append(",`products_type_code`,`price_type_id`");
		sql.append(",`price_assist_type_id`,`is_send_by_email`");
		sql.append(",`subscribe_type`");
		sql.append(",`is_must_see`,`email`");
		sql.append(" from `subscribe` ");
		sql.append(" where email is not null and email<>'' and is_send_by_email=1 and subscribe_type='")
			.append(subscribeType)
			.append("' and company_id=").append(cid);
		sql.append(" order by gmt_modified desc");
		sql.append(" limit 5 ");
		
		DBUtils.select("ast", sql.toString(), new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					Subscribe subscribe = new Subscribe(rs.getInt(1),
							rs.getInt(2), rs.getString(3), rs
							.getString(4), rs.getString(5), rs
							.getString(6), rs.getString(7), rs
							.getInt(8), rs.getInt(9), rs
							.getString(10), rs.getString(11),null, null, rs
							.getString(12), rs.getString(13));
					list.add(subscribe);
				}
			}
		});
		return list;
	}

//	private String buildupPriceEmailBody(Price price, Date baseDate) {
//		//TODO 需要完善内容显示格式
//		StringBuffer sb = new StringBuffer();
//		sb.append("<li>");
//		sb.append("<a href='").append(PRICE_PREFIX).append("/priceDetails").append(price.getId()).append(".htm' target='_blank'>");
//		sb.append(price.getTitle());
//		sb.append("</a>");
//		sb.append("</li>");
//		return sb.toString();
//	}

//	private String buildupProductEmailBody(Product product, Date baseDate) {
//		//TODO 需要完善信息显示格式
//		StringBuffer sb = new StringBuffer();
//		sb.append("<li>");
//		if(StringUtils.isNotEmpty(product.getProductTypeCode())){
//			if("10331000".equals(product.getProductTypeCode())){
//				sb.append("供应 ");
//			}
//			if("10331001".equals(product.getProductTypeCode())){
//				sb.append("求购 ");
//			}
//		}
//		sb.append("<a href='").append(TRADE_PREFIX).append("/productdetails").append(product.getId()).append(".htm' target='_blank'>");
//		sb.append(product.getTitle());
//		sb.append("</a>");
//		sb.append(" <span style='color:#999;' >").append(product.getPrice()).append(product.getPriceUnit()).append("</span>");
//		sb.append("</li>");
//		return sb.toString();
//	}

	private List<Product> product(Subscribe subscribe, Date baseDate) throws SphinxException {
		
		StringBuffer sb=new StringBuffer();
		SphinxClient cl=new SphinxClient(SEARCH_HOST, SEARCH_PORT);
		
		List<Product> list = new ArrayList<Product>();
		
		sb=buildStringQuery(sb, "(title,tags)", subscribe.getKeywords());
		
		if (StringUtils.isNotEmpty(subscribe.getProductsTypeCode())) {
			String typeName=queryProductType(subscribe.getProductsTypeCode());
			if(StringUtils.isNotEmpty(typeName)){
				sb=buildStringQuery(sb, "(label0,label1,label2,label3,label4)", typeName);
			}
		}
		if (StringUtils.isNotEmpty(subscribe.getAreaCode())) {
			String areaName=queryAreaName(subscribe.getAreaCode());
			if(StringUtils.isNotEmpty(areaName)){
				sb=buildStringQuery(sb, "(city,province)", areaName);
			}
		}
		
		cl.SetMatchMode(SphinxClient.SPH_MATCH_BOOLEAN);
		cl.SetLimits(0, 6, 100);
		cl.SetSortMode(SphinxClient.SPH_SORT_EXTENDED, "refresh_time desc");
		
		SphinxResult res=cl.Query(sb.toString(),"offersearch_new,offersearch_new_vip");
		
		if(res==null){
			return null;
		}else{
			//company_id, pdt_date, prodatediff, pdt_kind, ptitle, viptype, refresh_time, length_price, havepic
			for ( int i=0; i<res.matches.length; i++ ){
				SphinxMatch info = res.matches[i];
				if ( res.attrNames==null || res.attrTypes==null ) {
					continue;
				}
				Map<String, Object> resultMap=SearchEngineUtils.getInstance().resolveResult(res,info);
				Product p=new Product();
				p.setId(Integer.valueOf(info.docId+""));
				p.setTitle(resultMap.get("ptitle").toString());
				Long data=Long.valueOf(resultMap.get("pdt_date").toString());
				if(data>0){
					p.setGmtDate(DateUtil.toString(new Date(data*1000), "yyyy-MM-dd"));
				}
				p.setCompanyId(Integer.valueOf(resultMap.get("company_id").toString()));
				Integer havepic=Integer.valueOf(resultMap.get("havepic").toString());
				if(havepic>0){
					p.setPic(queryProductPic(p.getId()));
				}
				Integer pdtKind=Integer.valueOf(resultMap.get("pdt_kind").toString());
				if(pdtKind!=null && pdtKind.intValue()==2){
					p.setProductType("求购");
				}else{
					p.setProductType("供应");
				}
				list.add(p);
			}
		}
			
		
		return list;
	}

	private List<Price> price(Subscribe subscribe, String priceName, Date baseDate) throws SphinxException {
		StringBuffer sb=new StringBuffer();
		SphinxClient cl=new SphinxClient(SEARCH_HOST, SEARCH_PORT);
		
		List<Price> list = new ArrayList<Price>();
		
		int[] typeId=new int[1];
		typeId[0]=subscribe.getPriceTypeId().intValue();
		cl.SetFilter("type_id", typeId, false);
		if (subscribe.getPriceAssistTypeId() != null&& subscribe.getPriceAssistTypeId().intValue() > 0) {
			int[] assistTypeId=new int[1];
			assistTypeId[0]=subscribe.getPriceAssistTypeId().intValue();
			cl.SetFilter("assist_type_id", assistTypeId, false);
		}
		
		cl.SetMatchMode(SphinxClient.SPH_MATCH_BOOLEAN);
		cl.SetLimits(0, 6, 100);
		cl.SetSortMode(SphinxClient.SPH_SORT_EXTENDED, "gmt_order desc");
		
		SphinxResult res=cl.Query(sb.toString(),"price");
		
		if(res==null){
			return null;
		}else{
			//[pid, type_id, ptitle, assist_type_id, gmt_time, gmt_order]
			for ( int i=0; i<res.matches.length; i++ ){
				SphinxMatch info = res.matches[i];
				if ( res.attrNames==null || res.attrTypes==null ) {
					continue;
				}
				Map<String, Object> resultMap=SearchEngineUtils.getInstance().resolveResult(res,info);
				Price p=new Price();
				p.setId(Integer.valueOf(resultMap.get("pid").toString()));
				p.setTitle(resultMap.get("ptitle").toString());
				p.setGmtTime(resultMap.get("gmt_time").toString());
				p.setTypeId(Integer.valueOf(resultMap.get("type_id").toString()));
				list.add(p);
			}
		}
		
		return list;
		
	}
	
	private String queryPriceTypeName(Integer priceTypeId){
		String sql="select name from price_category where id="+priceTypeId;
		final String[] name=new String[1];
		DBUtils.select("ast", sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				if(rs.next()){
					name[0]=rs.getString(1);
				}
			}
		});
		
		return name[0];
	}
	
	private CompanyAccount queryTargetEmail(Integer cid){ //, String account
		String sql="select email,back_email,is_use_back_email,contact,sex,account from company_account where company_id="+cid+" limit 1"; //+" and account='"+account+"'";
		final CompanyAccount account=new CompanyAccount();
		DBUtils.select("ast", sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				if(rs.next()){
					account.setEmail(rs.getString(1));
					account.setBackEmail(rs.getString(2));
					account.setIsUseBackEmail(rs.getString(3));
					account.setContact(rs.getString(4));
					account.setSex(rs.getString(5));
					account.setAccount(rs.getString(6));
				}
			}
		});
		
		return account;
	}
	
	private String queryProductType(String type){ //, String account
		String sql="select label from category_products where code='"+type+"' limit 1"; //+" and account='"+account+"'";
		final String[] categoryTypeName=new String[1];
		DBUtils.select("ast", sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				if(rs.next()){
					categoryTypeName[0]=rs.getString(1);
				}
			}
		});
		
		return categoryTypeName[0];
	}
	
	private String queryAreaName(String code){ //, String account
		String sql="select label from category where code='"+code+"' limit 1"; //+" and account='"+account+"'";
		final String[] areaName=new String[1];
		DBUtils.select("ast", sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				if(rs.next()){
					areaName[0]=rs.getString(1);
				}
			}
		});
		
		return areaName[0];
	}
	
	private StringBuffer buildStringQuery(StringBuffer sb, String column, String keywords){
		if(StringUtils.isNotEmpty(keywords)){
			if(sb.indexOf("@")!=-1){
				sb.append("&");
			}
			sb.append("@").append(column).append(" ").append(keywords);
		}
		return sb;
	}
	
	private boolean isBlocked(Integer cid){
		
		final Integer[] i=new Integer[1];
		
		DBUtils.select("ast", "select is_block from company where id="+cid, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					i[0]=rs.getInt(1);
				}
			}
		});
		
		if(i[0]!=null && i[0]==0){
			return false;
		}
		return true;
	}
	
	private String queryProductPic(Integer pid){
		String sql="select pic_address from products_pic where product_id="+pid+" limit 1";
		final String[] pic=new String[1];
		DBUtils.select("ast", sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				if(rs.next()){
					pic[0]=rs.getString(1);
				}
			}
		});
		return pic[0];
	}

}
