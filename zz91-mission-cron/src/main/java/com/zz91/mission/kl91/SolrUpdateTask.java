package com.zz91.mission.kl91;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

import com.zz91.mission.ep.SolrUpdateUtils;
import com.zz91.task.common.ZZTask;
import com.zz91.util.http.HttpUtils;

/**
 * @Author:kongsj
 * @Date:2012-6-28
 */
public class SolrUpdateTask implements ZZTask {
	Logger LOG = Logger.getLogger(SolrUpdateTask.class);

	private static final String PRODUCTS_SOLR = "kl91Products";
	private static final String COMPANYS_SOLR = "kl91Companys";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {

		boolean result = false;
		do {
			// 供求更新
			if (runUpdateSolr(PRODUCTS_SOLR, SolrUpdateUtils.DELTA_IMPORT)) {
				result = true;
			} else {
				result = false;
				break;
			}
			// 公司更新
			if (runUpdateSolr(COMPANYS_SOLR, SolrUpdateUtils.DELTA_IMPORT)) {
				result = true;
			} else {
				result = false;
			}
		} while (false);
		return result;
	}

	private Boolean runUpdateSolr(String type, String command) {
		String url = "http://192.168.110.130:8089/solr/" + type + "/dataimport"
				+ (command == null ? "" : command);
		
		try {
			HttpUtils.getInstance().httpGet(url, HttpUtils.CHARSET_UTF8);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	@Override
	public boolean init() throws Exception {
		return false;
	}

	public static void main(String[] args) throws HttpException, IOException {
		HttpUtils.getInstance().httpGet("http://211.155.229.180:8201/solr/kl91Products/dataimport?command=delta-import", HttpUtils.CHARSET_UTF8);
	}
}
