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

public class IndexSubnetcategoryTask extends AbstractIdxTask {

	final static String DB="ep";
	final static int LIMIT=25;
	final static int RESET_LIMIT=5000;
	final static String MODEL="subnetcategory";
	
	@Override
	public Boolean idxReq(Long start, Long end) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from subnet_category ");
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
		sql.append("sc.id,sc.parent_id,sc.code,sc.name,sc.keyword")
			.append("sc.sort,sc.show_index,sc.gmt_created,sc.gmt_modified");
		sql.append(" from subnet_category sc");
		sqlwhere(sql, start, end);
		sql.append(" order by gmt_modified asc limit ").append(begin).append(",").append(LIMIT);
		
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				SolrInputDocument doc = null;
				while(rs.next()){
					doc = new SolrInputDocument();
					doc.addField("id", rs.getObject("id"));
					doc.addField("parent_id", rs.getObject("parentId"));
					doc.addField("code", rs.getObject("code"));
					doc.addField("name", rs.getObject("name"));
					doc.addField("keyword", rs.getObject("keyword"));
					doc.addField("sort", rs.getObject("sort"));
					doc.addField("showIndex", rs.getObject("show_index"));
					doc.addField("gmtCreated", rs.getObject("gmt_created"));
					doc.addField("gmtModified", rs.getObject("gmt_modified"));
					docs.add(doc);
				}
				
			}
		});
		return  docs;
	}

}
