/**
 * 
 */
package com.zz91.mission.huanbao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.zz91.task.common.AbstractIdxTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;
import com.zz91.util.search.SolrUtil;

/**
 * @author mays
 *
 */
public class IndexTradeSupplyTask extends AbstractIdxTask {

	final static String DB="ep";
	final static int LIMIT=25;
	
	final static String MODEL="tradesupply";
	
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
		
		if(dealCount[0]!=null && dealCount[0]>4){
			return true;
		}
		
		return false;
	}

	@Override
	public void idxPost(Long start, Long end) throws Exception {
		
		SolrServer server=SolrUtil.getInstance().getSolrServer(MODEL);
		
		int begin=0;
		int docsize=0;
		do {
			List<SolrInputDocument> docs = queryDocs(start, end, begin);
			
			if(docs.size()<=0){
				break;
			}
			
			server.add(docs);
			
			docsize=docs.size()+begin;
			
			begin=begin+LIMIT;
			
		} while (true);
		
		throw new Exception("共创建/更新"+docsize+"条索引");
	}
	
	@Override
	public void optimize() throws Exception{
		SolrUtil.getInstance().getSolrServer(MODEL).optimize();
	}
	
	private List<SolrInputDocument> queryDocs(Long start, Long end, int begin){
		final List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		
		StringBuffer sql=new StringBuffer();
		sql.append("select ");
		sql.append("ts.id,ts.cid,ts.photo_cover,ts.title,ts.province_code,")
			.append("ts.area_code,ts.price_num,ts.price_units,ts.property_query,")
			.append("ts.details_query,ts.gmt_refresh,ts.check_status,")
			.append("ts.pause_status,ts.del_status,ts.category_code");
		sql.append(" from trade_supply ts");
//		sql.append(" where gmt_modified >='").append(DateUtil.toString(new Date(start), FORMATE)).append("' ");
//		sql.append(" and gmt_modified <='").append(DateUtil.toString(new Date(end), FORMATE)).append("' ");
		sqlwhere(sql, start, end);
		sql.append(" limit ").append(begin).append(",").append(LIMIT);
		
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
					docs.add(doc);
				}
			}
		});
		
		for(SolrInputDocument doc:docs){
			parseCategory(doc);
			parseComp(doc, (Integer) doc.getFieldValue("cid"));
		}
		
		return docs;
	}
	
	private void sqlwhere(StringBuffer sb, Long start, Long end){
		sb.append(" where gmt_modified >='").append(DateUtil.toString(new Date(start), FORMATE)).append("' ");
		sb.append(" and gmt_modified <='").append(DateUtil.toString(new Date(end), FORMATE)).append("' ");
	}
	
	private void parseCategory(SolrInputDocument doc){
		String code=String.valueOf(doc.getFieldValue("categoryCode"));
//		String code="1000";
		if(StringUtils.isNotEmpty(code)){
			doc.addField("category4", substringCode(code, 4));
			doc.addField("category8", substringCode(code, 8));
			doc.addField("category12", substringCode(code, 12));
			doc.addField("category16", substringCode(code, 16));
			doc.addField("category20", substringCode(code, 20));
		}
	}
	
	private String substringCode(String code, int length){
		if(code.length()>=length){
			return  code.substring(0, length);
		}
		return code;
	}
	
	private void parseComp(SolrInputDocument doc, Integer cid){
		
		final Map<String, Object> result=new HashMap<String, Object>();
		DBUtils.select(DB, "select name, member_code, gmt_created from comp_profile where id="+cid,  new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					result.put("name", rs.getObject(1));
					result.put("memberCode", rs.getObject(2));
					result.put("gmtRegister", rs.getObject(3));
				}
			}
		});
		
		if(result.size()>0){
			doc.addField("name", result.get("name"));
			doc.addField("memberCode", result.get("memberCode"));
			doc.addField("gmtRegister", result.get("gmtRegister"));
		}
	}
	
	public static void main(String[] args) {
		SolrUtil.getInstance().init("file:/usr/tools/config/search/search.properties");
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		String start="2011-11-29 15:13:20";
		String end="2011-11-29 15:13:21";
		
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
	}


}
