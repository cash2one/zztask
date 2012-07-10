/**
 * 
 */
package com.zz91.mission.ep;

import java.util.Date;

import org.apache.log4j.Logger;

import com.zz91.task.common.ZZTask;

/**
 * @author root
 * 公司库信息搜索引擎更新
 */
public class CompanyDITask implements ZZTask {

	Logger LOG=Logger.getLogger(CompanyDITask.class);
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		boolean result=false;
		do{
			// 验证是否在更新如果在更新，传null验证是否正在更新中
			if (SolrUpdateUtils.runUpdateSolr(
					SolrUpdateUtils.COMPANY,
					SolrUpdateUtils.DELTA_IMPORT)) {
				result=true;
			} else {
				result=false;
			}
		}while(false);
		return result;
	}

	@Override
	public boolean init() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
}
