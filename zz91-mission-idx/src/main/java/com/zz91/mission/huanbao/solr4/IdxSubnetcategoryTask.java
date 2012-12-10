package com.zz91.mission.huanbao.solr4;

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
import com.zz91.util.lang.StringUtils;
import com.zz91.util.search.solr.SolrUpdateUtil;

public class IdxSubnetcategoryTask extends AbstractIdxTask {

	final static String DB = "ep";
	final static int LIMIT = 20;
	// final static int RESET_LIMIT=5000;
	final static String MODEL = "hbsubnetcategory";

	@Override
	public Boolean idxReq(Long start, Long end) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from subnet_category ");
		sqlwhere(sql, start, end, null);
		final Integer[] dealCount = new Integer[1];
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					dealCount[0] = rs.getInt(1);
				}
			}
		});

		if (dealCount[0] != null && dealCount[0] > 0) {
			return true;
		}

		return false;
	}

	@Override
	public void idxPost(Long start, Long end) throws Exception {
		SolrServer server = SolrUpdateUtil.getInstance().getSolrServer(MODEL);
		int id = 0;
		int docsize = 0;
		do {
			List<SolrInputDocument> docs = queryDocs(start, end, id);
			if (docs.size() <= 0) {
				break;
			}
			server.add(docs);
			docsize = docsize + docs.size();
			// start = resetStart(docs.get(docs.size()-1));
			id = resetId(docs.get(docs.size() - 1));

			// System.out.println("subnetcategory>>>>>"+docsize);

		} while (true);
		throw new Exception("共创建/更新" + docsize + "条索引");
	}

	@Override
	public void optimize() throws Exception {
		SolrUpdateUtil.getInstance().getSolrServer(MODEL).optimize();
	}

	private void sqlwhere(StringBuffer sb, Long start, Long end, Integer resetId) {
		sb.append(" where gmt_modified >='")
				.append(DateUtil.toString(new Date(start), FORMATE))
				.append("' ");
		sb.append(" and gmt_modified <='")
				.append(DateUtil.toString(new Date(end), FORMATE)).append("' ");
		
		if(resetId!=null){
			sb.append(" and id > ").append(resetId);
		}
		
	}

	// private Long resetStart(SolrInputDocument doc){
	// Date d=(Date) doc.getFieldValue("gmtModified");
	// return d.getTime();
	// }

	private Integer resetId(SolrInputDocument doc) {
		Integer id = (Integer) doc.getFieldValue("id");
		return id;
	}

	private List<SolrInputDocument> queryDocs(Long start, Long end, int resetId) {
		final List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		StringBuffer sql = new StringBuffer();
		sql.append("select ");
		sql.append("sc.id,sc.parent_id,sc.code,sc.name,sc.keyword,").append(
				"sc.sort,sc.show_index,sc.gmt_created");
		sql.append(" from subnet_category sc");
		sqlwhere(sql, start, end, resetId);
		sql.append(" order by sc.id asc limit ").append(LIMIT);
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				SolrInputDocument doc = null;
				while (rs.next()) {
					doc = new SolrInputDocument();
					doc.addField("id", rs.getObject("id"));
					doc.addField("parentId", rs.getObject("parent_id"));
					doc.addField("code", rs.getObject("code"));
					doc.addField("name", rs.getObject("name"));
					doc.addField("keyword", rs.getObject("keyword"));
					doc.addField("sort", rs.getObject("sort"));
					doc.addField("showIndex", rs.getObject("show_index"));
					String gmtCreated = rs.getString("gmt_created");
					doc.addField("gmtCreated", getTime(gmtCreated));
					// doc.addField("gmtModified",
					// rs.getObject("gmt_modified"));
					docs.add(doc);
				}

			}
		});
		return docs;
	}

	private long getTime(String str) {
		long longTime = 0;
		if (StringUtils.isNotEmpty(str)) {
			try {
				longTime = DateUtil.getDate(str, "yyyy-MM-dd HH:mm:ss")
						.getTime();
			} catch (ParseException e) {

			}
		}

		return longTime;
	}

	public static void main(String[] args) {
		SolrUpdateUtil.getInstance().init(
				"file:/usr/tools/config/search/search.properties");
		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");

		String start = "2011-11-19 20:06:20";
		String end = "2012-12-03 14:39:42";

		IdxSubnetcategoryTask task = new IdxSubnetcategoryTask();
		try {
			// System.out.println(task.idxReq(DateUtil.getDate(start,
			// FORMATE).getTime(), DateUtil.getDate(end, FORMATE).getTime()));
			task.idxPost(DateUtil.getDate(start, FORMATE).getTime(), DateUtil
					.getDate(end, FORMATE).getTime());
			// task.optimize();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
