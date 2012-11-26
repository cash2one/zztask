package com.zz91.mission.huanbao;

import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.GroupParams;
import org.junit.Test;

import com.zz91.util.datetime.DateUtil;

public class IndexTradeSupplyTaskTest {

	@Test
	public void test() {
		fail("Not yet implemented");
	}

	public static void main(String[] args) throws SolrServerException, MalformedURLException {
//		SolrUtil.getInstance().init("file:/usr/tools/config/search/search.properties");
//		SolrServer server = new StreamingUpdateSolrServer("http://nsolr.huanbao.com/solr/tradesupply", 100, 3);
		SolrQuery query = new SolrQuery();
//		query.setQuery("*:*");
		query.setQuery("sortRefresh:1345996800000");
		
		query.addSortField("sortRefresh", ORDER.desc);
		query.addSortField("sortMember", ORDER.desc);
		query.addSortField("score", ORDER.desc);
		query.addSortField("gmtRefresh", ORDER.desc);
		
//		query.set(GroupParams.GROUP_FORMAT, "simple");
		query.set(GroupParams.GROUP, true);
		query.set(GroupParams.GROUP_FIELD, "gcid");
		query.set(GroupParams.GROUP_MAIN, true);
		query.set(GroupParams.GROUP_LIMIT,2);
//		query.set(GroupParams.GROUP_TRUNCATE, true);
//		query.set(GroupParams.GROUP_TOTAL_COUNT, true);
		
		query.setRows(100);
//		query.setStart(7700);
		
		
	//	QueryResponse rsp = server.query(query);
		Map<String, Object> m = new HashMap<String, Object>();
		
//		for(SolrDocument doc:rsp.getResults()){
////			for(String names:doc.getFieldNames()){
////				m.put(names, doc.getFieldValue(names));
////			}
//			m.put("memberCode", doc.getFieldValue("memberCode"));
//			m.put("id", doc.getFieldValue("id"));
//			m.put("cid", doc.getFieldValue("cid"));
//			m.put("name", doc.getFieldValue("name"));
//			m.put("uid", doc.getFieldValue("uid"));
//			Date d= (Date) doc.getFieldValue("gmtRefresh");
//			m.put("gmtRefresh",DateUtil.toString(d, "yyyy-MM-dd HH:mm:ss"));
//			
//			System.out.println(JSONObject.fromObject(m).toString());
//		}
		
		//group response demo
	//	GroupResponse grsp=rsp.getGroupResponse();
//		System.out.println("ngroup:"+grsp.getValues().get(0).getNGroups());  //count of group
//		
//		for(Group group: grsp.getValues().get(0).getValues()){
//			for(SolrDocument doc:group.getResult()){
//				
//				m.put("memberCode", doc.getFieldValue("memberCode"));
//				m.put("id", doc.getFieldValue("id"));
//				m.put("cid", doc.getFieldValue("cid"));
//				m.put("name", doc.getFieldValue("name"));
//				m.put("uid", doc.getFieldValue("uid"));
//				Date d= (Date) doc.getFieldValue("gmtRefresh");
//				m.put("gmtRefresh",DateUtil.toString(d, "yyyy-MM-dd HH:mm:ss"));
//				
//				System.out.println(group.getGroupValue()+":"+JSONObject.fromObject(m).toString());
//			}
//		}
		
//		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>"+rsp.getResults().getNumFound());
	}
}
