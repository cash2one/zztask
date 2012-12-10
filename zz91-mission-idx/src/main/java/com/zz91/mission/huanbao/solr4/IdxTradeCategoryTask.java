package com.zz91.mission.huanbao.solr4;

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

public class IdxTradeCategoryTask extends AbstractIdxTask {

	final static String DB = "ep";
	final static int LIMIT = 50;

	final static String MODEL = "hbtradecategory";

	// final static int RESET_LIMIT = 5000;

	@Override
	public Boolean idxReq(Long start, Long end) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from trade_category");
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
		Map<String, String> categoryMap = new HashMap<String, String>();
		do {
			List<SolrInputDocument> docs = queryDocs(start, end, id,
					categoryMap);

			if (docs.size() <= 0) {
				break;
			}

			server.add(docs);

			docsize = docsize + docs.size();

			// start = resetStart(docs.get(docs.size()-1));
			id = resetId(docs.get(docs.size() - 1));

			// System.out.println("tradecategory>>>>>"+docsize);

		} while (true);

		throw new Exception("共创建/更新" + docsize + "条索引");

	}

	@Override
	public void optimize() throws Exception {
		SolrUpdateUtil.getInstance().getSolrServer(MODEL).optimize();
	}

	private void sqlwhere(StringBuffer sql, Long start, Long end,
			Integer resetId) {
		sql.append(" where gmt_modified >='")
				.append(DateUtil.toString(new Date(start), FORMATE))
				.append("' ");
		sql.append(" and gmt_modified <='")
				.append(DateUtil.toString(new Date(end), FORMATE)).append("' ");
		if(resetId!=null){
			sql.append(" and id >").append(resetId);
		}
		
	}

	// private Long resetStart(SolrInputDocument doc) {
	// Date d = (Date) doc.getFieldValue("gmtModified");
	// return d.getTime();
	// }

	private Integer resetId(SolrInputDocument doc) {
		Integer id = (Integer) doc.getFieldValue("id");
		return id;
	}

	private List<SolrInputDocument> queryDocs(Long start, Long end,
			Integer resetId, Map<String, String> categoryMap) {
		final List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		StringBuffer sql = new StringBuffer();
		sql.append("select ");
		sql.append("tc.id,tc.code,tc.name,tc.sort,tc.leaf,").append(
				"tc.tags,tc.show_index,tc.gmt_created ");
		sql.append("from trade_category tc");
		sqlwhere(sql, start, end, resetId);
		sql.append(" order by tc.id asc limit ").append(LIMIT);
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				SolrInputDocument doc = null;
				while (rs.next()) {
					doc = new SolrInputDocument();
					doc.addField("id", rs.getObject("id"));
					doc.addField("code", rs.getObject("code"));
					doc.addField("name", rs.getObject("name"));
					doc.addField("sort", rs.getObject("sort"));
					doc.addField("leaf", rs.getObject("leaf"));
					doc.addField("tags", rs.getObject("tags"));
					doc.addField("showIndex", rs.getObject("show_index"));
					String gmtCreated = rs.getString("gmt_created");
					doc.addField("gmtCreated", getTime(gmtCreated));
					// doc.addField("gmtModified",
					// rs.getObject("gmt_modified"));
					docs.add(doc);
				}
			}
		});

		for (SolrInputDocument doc : docs) {
			parseCategory(doc, categoryMap);
		}

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

	private void parseCategory(SolrInputDocument doc,
			Map<String, String> categoryMap) {
		String code = String.valueOf(doc.getFieldValue("code"));
		if (StringUtils.isNotEmpty(code)) {
			doc.addField("code4", substringCode(code, 4));
			doc.addField("code8", substringCode(code, 8));
			doc.addField("code12", substringCode(code, 12));
			doc.addField("code16", substringCode(code, 16));
			doc.addField("code20", substringCode(code, 20));
		}
	}

	private String substringCode(String code, int length) {
		if (code.length() >= length) {
			return code.substring(0, length);
		}
		return code;
	}

	public static void main(String[] args) {
		SolrUpdateUtil.getInstance().init(
				"file:/usr/tools/config/search/search.properties");
		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");

		String start = "2011-11-19 18:10:09";
		String end = "2012-12-03 16:39:48";

		IdxTradeCategoryTask task = new IdxTradeCategoryTask();
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
