package com.zz91.mission.huanbao;

import java.sql.ResultSet;
import java.sql.SQLException;
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
import com.zz91.util.lang.StringUtils;
import com.zz91.util.search.SolrUtil;

public class IndexTradeCategoryTask extends AbstractIdxTask {


	final static String DB = "ep";
	final static int LIMIT = 25;

	final static String MODEL = "tradecategory";
	final static int RESET_LIMIT = 5000;

	@Override
	public Boolean idxReq(Long start, Long end) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from trade_category");
		sqlwhere(sql, start, end);
		final Integer[] dealCount = new Integer[1];
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					dealCount[0] = rs.getInt(1);
				}
			}
		});

		if (dealCount[0] != null && dealCount[0] > 4) {
			return true;
		}

		return false;
	}

	@Override
	public void idxPost(Long start, Long end) throws Exception {
		SolrServer server = SolrUtil.getInstance().getSolrServer(MODEL);

		int begin = 0;
		int docsize = 0;
		Map<String, String> categoryMap = new HashMap<String, String>();
		do {
			List<SolrInputDocument> docs = queryDocs(start, end, begin,categoryMap);
			
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

	private void sqlwhere(StringBuffer sql, Long start, Long end) {
		sql.append(" where gmt_modified >='")
				.append(DateUtil.toString(new Date(start), FORMATE))
				.append("' ");
		sql.append(" and gmt_modified <='")
				.append(DateUtil.toString(new Date(end), FORMATE)).append("' ");
	}

	private Long resetStart(SolrInputDocument doc) {
		Date d = (Date) doc.getFieldValue("gmtModified");
		return d.getTime();
	}
	
	private List<SolrInputDocument> queryDocs(Long start,Long end,Integer begin,Map<String, String> categoryMap){
		final List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		StringBuffer sql = new StringBuffer();
		sql.append("select");
		sql.append("tc.id,tc.code,tc.name,tc.sort,tc.leaf,")
			.append("tc.tags,tc.show_index,tc.gmt_created,tc.gmt_modified");
		sql.append("from trade_category tc");
		sqlwhere(sql, start, end);
		sql.append(" order by gmt_modified asc limit ").append(begin).append(",").append(LIMIT);
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				SolrInputDocument doc = null;
				while(rs.next()){
					doc = new SolrInputDocument();
					doc.addField("id", rs.getObject("id"));
					doc.addField("code", rs.getObject("code"));
					doc.addField("name", rs.getObject("name"));
					doc.addField("sort", rs.getObject("sort"));
					doc.addField("leaf", rs.getObject("leaf"));
					doc.addField("tags", rs.getObject("tags"));
					doc.addField("showIndex", rs.getObject("show_index"));
					doc.addField("gmtCreated", rs.getObject("gmt_created"));
					doc.addField("gmtModified", rs.getObject("gmt_modified"));
					docs.add(doc);
				}
			}
		});
		
		for(SolrInputDocument doc:docs){
			parseCategory(doc, categoryMap);
		}
		
		return docs;
	}
	

	private void parseCategory(SolrInputDocument doc, Map<String, String> categoryMap){
		String code=String.valueOf(doc.getFieldValue("code"));
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
}
