package com.zz91.mission.huanbao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.zz91.task.common.AbstractIdxTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.search.SolrUtil;

public class IndexNewsTask extends AbstractIdxTask {

	final static String DB = "ep";
	final static int LIMIT = 25;

	final static String MODEL = "news";
	final static int RESET_LIMIT = 5000;

	@Override
	public Boolean idxReq(Long start, Long end) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from news");
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
	
	private List<SolrInputDocument> queryDocs(Long start,Long end,Integer begin){
		final List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		StringBuffer sql = new StringBuffer();
		sql.append("select")
			.append("n.id,n.title,n.title_index,n.category_code,n.description,n.tags,n.news_source,")
			.append("n.view_count,n.pause_status,n.gmt_publish,n.gmt_modified");
		sql.append("from news n");
		sqlwhere(sql, start, end);
		sql.append(" order by n.gmt_modified asc limit ").append(begin).append(",").append(LIMIT);
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				SolrInputDocument doc = null;
				while(rs.next()){
					doc = new SolrInputDocument();
					doc.addField("id", rs.getObject("id"));
					doc.addField("title", rs.getObject("title"));
					doc.addField("titleIndex", rs.getObject("title_index"));
					doc.addField("categoryCode", rs.getObject("category_code"));
					doc.addField("description", rs.getObject("description"));
					doc.addField("tags", rs.getObject("tags"));
					doc.addField("newsSource", rs.getObject("news_source"));
					doc.addField("viewCount", rs.getObject("view_count"));
					doc.addField("pauseStatus", rs.getObject("pause_status"));
					doc.addField("gmtPublish", rs.getObject("gmt_publish"));
					doc.addField("gmtModified", rs.getObject("gmt_modified"));
					docs.add(doc);
				}
			}
		});
		
		return docs;
	}
}
