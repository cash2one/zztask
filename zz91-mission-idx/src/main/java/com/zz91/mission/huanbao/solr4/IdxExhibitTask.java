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

public class IdxExhibitTask extends AbstractIdxTask {

	final static String DB = "ep";
	final static int LIMIT = 25;
	// final static int RESET_LIMIT=5000;
	final static String MODEL = "hbexhibit";

	@Override
	public Boolean idxReq(Long start, Long end) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(*) from exhibit");
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
		int resetId = 0;
		int docsize = 0;
		do {
			List<SolrInputDocument> docs = queryDocs(start, end, resetId);
			if (docs.size() <= 0) {
				break;
			}
			server.add(docs);
			docsize = docsize + docs.size();

			resetId = resetId(docs.get(docs.size() - 1));

			// System.out.println("exhibit>>>>>"+docsize);

		} while (true);
		throw new Exception("共创建/更新" + docsize + "条索引");
	}

	@Override
	public void optimize() throws Exception {

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
		Integer resetId = (Integer) doc.getFieldValue("id");
		return resetId;
	}

	private List<SolrInputDocument> queryDocs(Long start, Long end, int resetId) {
		final List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		StringBuffer sql = new StringBuffer();
		sql.append("select ");
		sql.append(
				"ex.id,ex.name,ex.plate_category_code,ex.industry_code,ex.province_code,ex.area_code,ex.pause_status,")
				.append("ex.gmt_start,ex.gmt_end,ex.gmt_publish,ex.gmt_sort ");
		sql.append("from exhibit ex");
		sqlwhere(sql, start, end, resetId);
		sql.append(" order by ex.id asc limit ").append(LIMIT);
		DBUtils.select(DB, sql.toString(), new IReadDataHandler() {

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				SolrInputDocument doc = null;
				while (rs.next()) {
					doc = new SolrInputDocument();
					doc.addField("id", rs.getObject("id"));
					doc.addField("name", rs.getObject("name"));
					doc.addField("plateCategoryCode",
							rs.getObject("plate_category_code"));
					doc.addField("industryCode", rs.getObject("industry_code"));
					// doc.addField("showName", rs.getObject("show_name"));
					// doc.addField("organizers", rs.getObject("organizers"));
					doc.addField("provinceCode", rs.getObject("province_code"));
					doc.addField("areaCode", rs.getObject("area_code"));
					doc.addField("pauseStatus", rs.getObject("pause_status"));
					String gmtStart = rs.getString("gmt_start");
					String gmtEnd = rs.getString("gmt_end");
					String gmtPublish = rs.getString("gmt_publish");
					String gmtSort = rs.getString("gmt_sort");
					doc.addField("gmtStart", getTime(gmtStart));

					doc.addField("gmtEnd", getTime(gmtEnd));

					doc.addField("gmtPublish", getTime(gmtPublish));

					doc.addField("gmtSort", getTime(gmtSort));
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

		String start = "2011-10-19 09:40:48";
		String end = "2012-11-19 15:34:15";

		IdxExhibitTask task = new IdxExhibitTask();
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
