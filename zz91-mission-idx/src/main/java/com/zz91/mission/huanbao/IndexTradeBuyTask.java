package com.zz91.mission.huanbao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.zz91.task.common.AbstractIdxTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.search.SolrUtil;

public class IndexTradeBuyTask extends AbstractIdxTask{
	
	final static String DB="ep";
	final static int LIMIT=25;
	final static int RESET_LIMIT=5000;
	final static String MODEL="tradebuy";
	
	@Override
	public Boolean idxReq(Long start, Long end) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from trade_buy ");
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
		
		if(dealCount[0]!=null){
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
			docsize=docsize+docs.size();
			begin=begin+LIMIT;	
			if(begin>=RESET_LIMIT){
				start = resetStart(docs.get(docs.size()-1));
				begin=0;
			}
			
		} while (true);	
		throw new Exception("共创建/更新"+docsize+"条索引");	
	}

	@Override
	public void optimize() throws Exception {
		SolrUtil.getInstance().getSolrServer(MODEL).optimize();
	}
	
	private void sqlwhere(StringBuffer sb, Long start, Long end){
		sb.append(" where gmt_modified >='").append(DateUtil.toString(new Date(start), FORMATE)).append("' ");
		sb.append(" and gmt_modified <='").append(DateUtil.toString(new Date(end), FORMATE)).append("' ");
	}
	
	private Long resetStart(SolrInputDocument doc){
		Date d=(Date) doc.getFieldValue("gmtModified");
		return d.getTime();
	}
	
	private List<SolrInputDocument> queryDocs(Long start, Long end, int begin){
		final List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		StringBuffer sql=new StringBuffer();
		sql.append("select ");
		sql.append("tb.id,tb.cid,tb.title,tb.province_code,tb.area_code,tb.quantity,tb.message_count,tb.quantity_year,tb.quantity_untis,")
			.append("tb.details_query,tb.valid_days,tb.gmt_refresh,tb.gmt_expired,tb.pause_status,tb.check_status,tb.del_status,tb.view_count,tb.favorite_count,tb.plus_count,tb.gmt_modified,tb.tags_sys");
		sql.append(" from trade_buy tb");
		sqlwhere(sql, start, end);
		sql.append(" order by gmt_modified asc limit ").append(begin).append(",").append(LIMIT);
		
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				SolrInputDocument doc = null;
				while(rs.next()){
					doc = new SolrInputDocument();
					doc.addField("id", rs.getObject("id"));
					doc.addField("cid", rs.getObject("cid"));
					doc.addField("title", rs.getObject("title"));
					doc.addField("provinceCode", rs.getObject("province_code"));
					doc.addField("areaCode", rs.getObject("area_code"));
					doc.addField("quantity", rs.getObject("quantity"));
					doc.addField("messageCount", rs.getObject("message_count"));
					doc.addField("quantityYear",rs.getObject("quantity_year"));
					doc.addField("quantityUntis", rs.getObject("quantity_untis"));
					doc.addField("detailsQuery", rs.getObject("details_query"));
					doc.addField("validDays", rs.getObject("valid_days"));
					doc.addField("gmtRefresh",rs.getObject("gmt_refresh"));
					doc.addField("gmtExpired", rs.getObject("gmt_expired"));
					doc.addField("pauseStatus", rs.getObject("pause_status"));
					doc.addField("checkStatus", rs.getObject("check_status"));
					doc.addField("delStatus", rs.getObject("del_status"));
					doc.addField("viewCount", rs.getObject("view_count"));
					doc.addField("favoriteCount", rs.getObject("favorite_count"));
					doc.addField("plusCount", rs.getObject("plus_count"));
					doc.addField("gmtModified", rs.getObject("gmt_modified"));
					doc.addField("tagsSys", rs.getObject("tags_sys"));
					docs.add(doc);
				}
				
			}
		});
		
		for(SolrInputDocument doc : docs){
			parseCodeBlock(doc,(Integer)doc.getFieldValue("cid"));
		}
		return  docs;
	}
	
	 private void parseCodeBlock( SolrInputDocument doc,Integer cid){
		  final String [] result =new String [1];
		 DBUtils.select(DB, "select member_code_block  from comp_profile where id ="+cid, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					result[0] = rs.getString("member_code_block");
				}
			}
		});
		 if(result[0]!=null&&"".equals("")){
			 doc.addField("memberCodeBlock",result[0]);
		 }
		
	 }
	
	public static void main(String[] args) {
		SolrUtil.getInstance().init("file:/usr/tools/config/search/search.properties");
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		String start="2012-05-21 11:49:49";
		String end ="2012-11-25 17:10:41";
		
		IndexTradeBuyTask task=new IndexTradeBuyTask();
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
