package com.zz91.mission.ep;

import java.util.Date;

import com.zz91.task.common.ZZTask;
/**
 * @author root
 * 子网类别搜索引擎更新
 */
public class SubnetCategoryDITask implements ZZTask {

	@Override
	public boolean clear(Date baseDate) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		boolean result=false;
		do{
			// 验证是否在更新如果在更新，传null验证是否正在更新中
			if (SolrUpdateUtils.runUpdateSolr(
					SolrUpdateUtils.SUBNETCATEGORY,
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
