package com.zz91.mission.ep;

import java.io.IOException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class SolrUpdateUtils {
	
	public static final String TRADE_SUPPLY = "tradesupply";
	public static final String COMPANY = "company";
	public static final String TRADE_BUY = "tradebuy";
	public static final String NEWS = "news";
	public static final String EXHIBIT = "exhibit";
	public static final String DELTA_IMPORT = "?command=delta-import";
	public static final String FULL_IMPORT = "?command=full-import";
	/**
	 *  String url = TRADE_SUPPLY;
		if (!runUpdateSolr(url,"?command=full-import")) {
			System.out.println("更新成功.......");
		} else {
			System.out.println("更新失败.......");
		}
	 * @param type TRADE_SUPPLY  COMPANY
	 * @param commond null DELTA_IMPORT FULL_IMPORT
	 * @return
	 * @throws IOException
	 */
    public static boolean runUpdateSolr (String type,String command) throws Exception {
    	boolean result  = false;
    	String commond = "http://localhost:8081/solr/"+type+"/dataimport"+(command==null?"":command);
    	// 构建httpClient实例
    	HttpClient httpClient = new HttpClient();
    	// 创建get方法
    	GetMethod getMethod = new GetMethod(commond);
    	// 使用系统默认的恢复策略
    	getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
    	try {
        	int status = httpClient.executeMethod(getMethod);
        	if (status == HttpStatus.SC_OK) {
        		result = true;
    		}
		} finally {
    		//释放链接
    		getMethod.releaseConnection();
    	}
    	return result;
    }
}
