/**
 * 
 */
package com.zz91.mission.huanbao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import com.zz91.task.common.AbstractIdxTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;
import com.zz91.util.search.SolrUtil;

/**
 * 导入会员：0
 * 普通会员：100
 * 付费会员：200
 * @author mays
 *
 */
public class IndexTradeSupplyTask extends AbstractIdxTask {

	final static String DB="ep";
	final static int LIMIT=25;
	
	final static String MODEL="tradesupply";
	final static int IMPORT_ID_SPLIT=50000000;
	final static String MEMBER="10011000";
	
	final static int RESET_LIMIT=5000;
	final static Map<String, Integer> SORT_MEMBER = new HashMap<String, Integer>();
	
	static{
		SORT_MEMBER.put("10011000", 100);
		SORT_MEMBER.put("10011001", 200);
	}
	
	@Override
	public Boolean idxReq(Long start, Long end) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from trade_supply ");
		sqlwhere(sql, start, end);
		final Integer[] dealCount=new Integer[1];
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					dealCount[0]=rs.getInt(1);
				}
			}
		});
		
		if(dealCount[0]!=null && dealCount[0] >0 ){
			return true;
		}
		
		return false;
	}

	@Override
	public void idxPost(Long start, Long end) throws Exception {
		
		SolrServer server=SolrUtil.getInstance().getSolrServer(MODEL);
		
		int begin=0;
		int docsize=0;
		Map<String, String> categoryMap = new HashMap<String, String>();
		do {
			List<SolrInputDocument> docs = queryDocs(start, end, begin, categoryMap);
			
			if(docs.size()<=0){
				break;
			}
			
			server.add(docs);
			
			docsize=docsize+docs.size();
			
			begin=begin+LIMIT;
			
			if(begin>=RESET_LIMIT){
				start = resetStart(docs.get(docs.size()-1));
				begin=0;
//				Long resetStart = resetStart(docs.get(docs.size()-1));
//				if(start.intValue() == resetStart.longValue()){
//					System.out.println("<<<<<<<<<<<<<<<<<<<<<"+resetStart.longValue());
//					break;
//				}else {	
//					start = resetStart;
//				}
			}
			
//			System.out.println(">>>>>"+docsize+">>>>>>"+begin);
			
		} while (true);
		
		throw new Exception("共创建/更新"+docsize+"条索引");
	}
	
	@Override
	public void optimize() throws Exception{
		SolrUtil.getInstance().getSolrServer(MODEL).optimize();
	}
	
	private Long resetStart(SolrInputDocument doc){
		Date d=(Date) doc.getFieldValue("gmtModified");
		return d.getTime();
	}
	
	private List<SolrInputDocument> queryDocs(Long start, Long end, int begin, Map<String, String> categoryMap){
		final List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		
		StringBuffer sql=new StringBuffer();
		sql.append("select ");
		sql.append("ts.id,ts.cid,ts.photo_cover,ts.title,ts.province_code,")
			.append("ts.area_code,ts.price_num,ts.price_units,ts.property_query,")
			.append("ts.details_query,ts.gmt_refresh,ts.check_status,")
			.append("ts.pause_status,ts.del_status,ts.category_code,ts.gmt_modified,ts.uid,")
			.append("ts.integrity,ts.view_count,ts.favorite_count,ts.plus_count");
		sql.append(" from trade_supply ts");
		sqlwhere(sql, start, end);
		sql.append(" order by gmt_modified asc limit ").append(begin).append(",").append(LIMIT);
		
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					SolrInputDocument doc=new SolrInputDocument();
					doc.addField("id", rs.getObject(1));
					doc.addField("cid", rs.getObject(2));
					doc.addField("photoCover", rs.getObject(3));
					doc.addField("title", rs.getObject(4));
					doc.addField("provinceCode", rs.getObject(5));
					doc.addField("areaCode", rs.getObject(6));
					doc.addField("priceNum", rs.getObject(7));
					doc.addField("priceUnits", rs.getObject(8));
					doc.addField("propertyQuery", rs.getObject(9));
					doc.addField("detailsQuery", rs.getObject(10));
					doc.addField("gmtRefresh", rs.getObject(11));
					doc.addField("checkStatus", rs.getObject(12));
					doc.addField("pauseStatus", rs.getObject(13));
					doc.addField("delStatus", rs.getObject(14));
					doc.addField("categoryCode", rs.getObject(15));
					doc.addField("gmtModified", rs.getObject(16));
					doc.addField("uid", rs.getObject(17));
					doc.addField("integrity", rs.getObject(18));
					doc.addField("viewCount", rs.getObject(19));
					doc.addField("favoriteCount", rs.getObject(20));
					doc.addField("plusCount", rs.getObject(21));
					docs.add(doc);
				}
			}
		});
		
		for(SolrInputDocument doc:docs){
			parseCategory(doc, categoryMap);
			parseComp(doc, (Integer) doc.getFieldValue("cid"));
		}
		
		return docs;
	}
	
	private void sqlwhere(StringBuffer sb, Long start, Long end){
		sb.append(" where gmt_modified >='").append(DateUtil.toString(new Date(start), FORMATE)).append("' ");
		sb.append(" and gmt_modified <='").append(DateUtil.toString(new Date(end), FORMATE)).append("' ");
	}
	
	private void parseCategory(SolrInputDocument doc, Map<String, String> categoryMap){
		String code=String.valueOf(doc.getFieldValue("categoryCode"));
//		String code="1000";
		if(StringUtils.isNotEmpty(code)){
			doc.addField("category4", substringCode(code, 4));
			doc.addField("category8", substringCode(code, 8));
			doc.addField("category12", substringCode(code, 12));
			doc.addField("category16", substringCode(code, 16));
			doc.addField("category20", substringCode(code, 20));

			//处理categoryName
			if(!categoryMap.containsKey(code)){
				categoryMap.put(code, queryCategoryName(code));
			}
			doc.addField("categoryName", categoryMap.get(doc.getFieldValue("categoryCode")));
		}
	}
	
	private String queryCategoryName(String code){
		final String[] str=new String[1];
		
		DBUtils.select(DB,"select name from trade_category where code='"+code+"' limit 1", new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					str[0]=rs.getString(1);
				}
			}
		});
		return str[0];
	}
	
	private String substringCode(String code, int length){
		if(code.length()>=length){
			return  code.substring(0, length);
		}
		return code;
	}
	
	private void parseComp(SolrInputDocument doc, Integer cid){
		
		final Map<String, Object> result=new HashMap<String, Object>();
		StringBuffer sql = new StringBuffer();
			sql.append("select name,(select qq from comp_account where cid = ")
			.append(cid)
			.append(") as qq,")
			.append("member_code ,member_code_block, gmt_created from comp_profile where id= ")
			.append(cid);
		DBUtils.select(DB, sql.toString() ,  new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					result.put("name", rs.getObject("name"));
					result.put("qq",rs.getObject("qq"));
					result.put("memberCode", rs.getObject("member_code"));
					String codeBlock = rs.getString("member_code_block");
					if(codeBlock==null || "".equals(codeBlock)){
						
						result.put("memberCodeBlock", "-1");
						
					}else{
						
						result.put("memberCodeBlock", codeBlock);
					
					}
					result.put("gmtRegister", rs.getObject("gmt_created"));
				}
			}
		});
		
		for(String k:result.keySet()){
			doc.addField(k, result.get(k));
		}
		
		//parse gcid
		doc.addField("gcid", "g"+String.valueOf(cid));
		
		Date refresh=(Date) doc.getFieldValue("gmtRefresh");
		
		//parse sortMember
		if(cid.intValue()<IMPORT_ID_SPLIT&&!"10011001".equals(result.get("memberCode"))){
			doc.addField("sortMember", 0);
		}else{
			if((new Date().getTime()-refresh.getTime()) < 3*86400000){
				doc.addField("sortMember", SORT_MEMBER.get(result.get("memberCode")));
			}else{
				doc.addField("sortMember", 100);
			}
		}
		
		
		//parse sortRefresh
		try {
			if(!MEMBER.equals(result.get("memberCode")) && (new Date().getTime()-refresh.getTime()) < 3*86400000){
				doc.addField("sortRefresh", DateUtil.getDate(DateUtil.getDateAfterDays(refresh, 3), "yyyy-MM-dd").getTime());
			}else{
				doc.addField("sortRefresh", DateUtil.getDate(refresh, "yyyy-MM-dd").getTime());
			}
		} catch (ParseException e) {
		}
	}
	
	
	
	public static void main(String[] args) throws SolrServerException, IOException {
		SolrUtil.getInstance().init("file:/usr/tools/config/search/search.properties");
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
//		SolrServer server=SolrUtil.getInstance().getSolrServer("ts");
//		
//		SolrInputDocument doc=new SolrInputDocument();
//		doc.addField("id", 96557);
//		doc.addField("title", "杭州阿思拓-供应金属喷咀喷头");
//		doc.addField("provinceCode", "100110001009");
		
//		server.add(doc);
//		server.commit();
		
//		String start="2011-11-29 15:13:20";
//		String end="2011-11-29 15:13:21";
		String start="2012-07-21 11:49:49";
		String end ="2012-11-25 17:10:41";
		
		AbstractIdxTask task=new IndexTradeSupplyTask();
		try {
//			System.out.println(task.idxReq(DateUtil.getDate(start, FORMATE).getTime(), DateUtil.getDate(end, FORMATE).getTime()));
			task.idxPost(DateUtil.getDate(start, FORMATE).getTime(), DateUtil.getDate(end, FORMATE).getTime());
//			task.optimize();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		>>>>>1766683>>>>>>1700
//		1761891
//		java.lang.Exception: 共创建/更新1766683条索引
		
	}


}
