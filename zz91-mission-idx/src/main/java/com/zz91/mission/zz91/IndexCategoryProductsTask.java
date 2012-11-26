package com.zz91.mission.zz91;

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
import com.zz91.util.search.solr.SolrUpdateUtil;

public class IndexCategoryProductsTask extends AbstractIdxTask{

	final static String DB = "ast";
	final static int LIMIT = 50;

	final static String MODEL = "zzcategory";
	final static int RESET_LIMIT = 5000;

	@Override
	public Boolean idxReq(Long start, Long end) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from category_products");
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

		if (dealCount[0] != null) {
			return true;
		}

		return false;
	}

	@Override
	public void idxPost(Long start, Long end) throws Exception {
		SolrServer server = SolrUpdateUtil.getInstance().getSolrServer(MODEL);

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
		SolrUpdateUtil.getInstance().getSolrServer(MODEL).optimize();
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
		sql.append("select ");
		sql.append("id,code,label,sort,is_assist,is_del,old_id,cnspell,oldid1,oldid2,oldid3,oldid4,")
			.append("oldhiddenid,tags,gmt_created,gmt_modified ");
		sql.append("from category_products ");
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
					doc.addField("label", rs.getObject("label"));
					doc.addField("sort", rs.getObject("sort"));
					doc.addField("isAssist", rs.getObject("is_assist"));
					doc.addField("tags", rs.getObject("tags"));
					doc.addField("isDel", rs.getObject("is_del"));
					doc.addField("oldhiddenid", rs.getObject("oldhiddenid"));
					doc.addField("oldid4", rs.getObject("oldid4"));
					doc.addField("oldid3", rs.getObject("oldid3"));
					doc.addField("oldid2", rs.getObject("oldid2"));
					doc.addField("oldid1", rs.getObject("oldid1"));
					doc.addField("oldId", rs.getObject("old_id"));
					doc.addField("cnspell", rs.getObject("cnspell"));
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
	
	public static void main(String[] args) {
		SolrUpdateUtil.getInstance().init("file:/usr/tools/config/search/search.properties");
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		String start="2010-09-10 11:49:49";
		String end ="2012-09-13 17:10:41";
		
		IndexCategoryProductsTask task=new IndexCategoryProductsTask();
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
