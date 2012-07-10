/**
 * 
 */
package com.zz91.mission.ep;

import java.util.Date;

import org.apache.log4j.Logger;

import com.zz91.task.common.ZZTask;

/**
 * @author root
 * 资讯信息搜索引擎更新
 */
public class NewsDITask implements ZZTask {

	Logger LOG=Logger.getLogger(NewsDITask.class);
	
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
					SolrUpdateUtils.NEWS,
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
