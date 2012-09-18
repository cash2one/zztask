package com.zz91.mission.ep;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IInsertUpdateHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.search.SolrUtil;

public class TradeSupplyNumTask implements ZZTask {
	
	final static String DB="ep";
	
	@Override
	public boolean init() throws Exception {
		
		return false;
	}

	@Override
	public boolean exec(final Date baseDate) throws Exception {
		//用solr统计出各个类的信息量
		final Map<String, Object> map = new HashMap<String, Object>();
		for(int i= 0;i<8;i++){
		SolrServer server = SolrUtil.getInstance().getSolrServer("tradesupply");
		map.put("code", "1000100"+i);
		SolrQuery query = new SolrQuery();
		query.setQuery("category16:"+map.get("code"));
		QueryResponse rsp=server.query(query);
		map.put("num",(int)rsp.getResults().getNumFound());
		
		//将数据插入统计表
		StringBuffer sql = new StringBuffer();
			sql.append("update trade_category_num set num_leaf = ?,gmt_modified = ? where code = ?");
		DBUtils.insertUpdate(DB, sql.toString(), new IInsertUpdateHandler() {
			
			@Override
			public void handleInsertUpdate(PreparedStatement ps)
					throws SQLException {
				ps.setInt(1, (Integer)map.get("num"));
				ps.setString(2, convertDate(baseDate));
				ps.setString(3, map.get("code").toString());
				ps.execute();
			}
		});
	};
		return true;
	}
	
	private String convertDate(Date date){
		
		return DateUtil.toString(date, "yyyy-MM-dd HH:mm:ss");
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {
		
		return false;
	}

	public static void main(String[] args) {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		TradeSupplyNumTask task = new TradeSupplyNumTask();
		try {
			task.exec(new Date());
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
}
