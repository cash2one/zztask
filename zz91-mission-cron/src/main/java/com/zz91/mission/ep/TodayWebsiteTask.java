/**
 * 
 */
package com.zz91.mission.ep;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.apache.log4j.Logger;
import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;

/**
 * @author root 今日环保网相关数据统计
 */
public class TodayWebsiteTask implements ZZTask {

	Logger LOG = Logger.getLogger(TodayWebsiteTask.class);

	final static String DB = "ep";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		boolean result = false;
		do {
			// 获取统计日期（默认为当前时间）
			String gmtData = DateUtil.toString(new Date(), "yyyy-MM-dd");
			// 如果手动执行，使用执行时间
			if (baseDate != null) {
				 gmtData = DateUtil.toString(baseDate, "yyyy-MM-dd");
			}
			// 获取完成第一步的客户数目
			Integer registerStp1 = getRegisterStp1(gmtData);
			// 获取完成第二步的客户数目
			Integer registerStp2 = getRegisterStp2(gmtData);
			// 获取发布供应询盘信息数目
			Integer messageSupply = getMessageSupply(gmtData);
			// 获取发布求购询盘信息数目
			Integer messageBuy = getMessageBuy(gmtData);
			// 获取发布公司询盘信息数目
			Integer messageCompany = getMessageCompany(gmtData);
			// 获取后台代发询盘信息数目
			Integer messageAdmin = getMessageAdmin(gmtData);
			// 获取发布公司文章信息数目
			Integer companyNews = getCompanyNews(gmtData);
			// 获取发布供应信息数目
			Integer supply = getSupply(gmtData);
			// 获取发布求购信息数目
			Integer buy = getBuy(gmtData);
			// 获取发布信息公司数目
			Integer publishCompany = getPublishCompany(gmtData);
			// 获取发布文章公司数目
			Integer publishNews = getPublishNews(gmtData);
			// 获取登录总次数
			Integer loginCount = getLoginCount(gmtData);
			//插入统计表
			insertWebsiteStatistics(gmtData, registerStp1, registerStp2,
					messageSupply, messageBuy, messageCompany, messageAdmin,
					companyNews, supply, buy, publishCompany, publishNews,
					loginCount);
			result = true;
		} while (false);
		return result;
	}

	/**
	 * 将数据插入统计表
	 * @param gmtData
	 * @param registerStp1
	 * @param registerStp2
	 * @param messageSupply
	 * @param messageBuy
	 * @param messageCompany
	 * @param messageAdmin
	 * @param companyNews
	 * @param supply
	 * @param buy
	 * @param publishCompany
	 * @param publishNews
	 * @param loginCount
	 */
	private void insertWebsiteStatistics(String gmtData, Integer registerStp1,
			Integer registerStp2, Integer messageSupply, Integer messageBuy,
			Integer messageCompany, Integer messageAdmin, Integer companyNews,
			Integer supply, Integer buy, Integer publishCompany,
			Integer publishNews, Integer loginCount) {
		String sql="INSERT INTO `website_statistics` (`gmt_data`,`register_stp1`,`register_stp2`," +
				"`message_supply`,`message_buy`,`message_company`,`message_admin`,`company_news`," +
				"`supply`,`buy`,`publish_company`,`publish_news`,`login_count`,`gmt_created`," +
				"`gmt_modified`) VALUES ('"+gmtData+"',"+registerStp1+","+registerStp2+","+messageSupply
				+","+messageBuy+","+messageCompany+","+messageAdmin+","+companyNews+","
				+supply+","+buy+","+publishCompany+","+publishNews+","+loginCount+",now(),now())";
		boolean result=DBUtils.insertUpdate(DB, sql);
		if (!result) {
			LOG.info(">>>>>>>>>>>>>>>>>统计网站数据失败:"+sql);
		}
	}
	
	// 获取完成第一步的客户数目
	private int getRegisterStp1(String gmtData) {
		String sql = "SELECT count(*) as count FROM comp_account ca,comp_profile cp where ca.cid=cp.id and cp.register_code=1 and date_format(ca.gmt_register,'%Y-%m-%d') = date_format('"+gmtData+"','%Y-%m-%d') and ca.name=''";
		return getCount(sql);
	}
	
	// 获取完成第二步的客户数目
	private int getRegisterStp2(String gmtData) {
		String sql = "SELECT count(*) as count FROM comp_account ca,comp_profile cp where ca.cid=cp.id and cp.register_code=1 and date_format(ca.gmt_register,'%Y-%m-%d') = date_format('"+gmtData+"','%Y-%m-%d') and ca.name!=''";
		return getCount(sql);
	}
	
	// 获取发布供应询盘信息数目
	private int getMessageSupply(String gmtData) {
		String sql = "SELECT count(*) as count FROM message where uid!=0 and target_type=0 and date_format(gmt_created,'%Y-%m-%d') = date_format('"+gmtData+"','%Y-%m-%d')";
		return getCount(sql);
	}
	
	// 获取发布供应询盘信息数目
	private int getMessageBuy(String gmtData) {
		String sql = "SELECT count(*) as count FROM message where uid!=0 and target_type=1 and date_format(gmt_created,'%Y-%m-%d') = date_format('"+gmtData+"','%Y-%m-%d')";
		return getCount(sql);
	}
	
	// 获取发布供应询盘信息数目
	private int getMessageCompany(String gmtData) {
		String sql = "SELECT count(*) as count FROM message where uid!=0 and target_type=2 and date_format(gmt_created,'%Y-%m-%d') = date_format('"+gmtData+"','%Y-%m-%d')";
		return getCount(sql);
	}
	
	// 获取发布供应询盘信息数目
	private int getMessageAdmin(String gmtData) {
		String sql = "SELECT count(*) as count FROM message where uid=0 and date_format(gmt_created,'%Y-%m-%d') = date_format('"+gmtData+"','%Y-%m-%d')";
		return getCount(sql);
	}
	
	// 获取发布公司文章信息数目
	private int getCompanyNews(String gmtData) {
		String sql = "SELECT count(*) as count FROM comp_news where date_format(gmt_created,'%Y-%m-%d') = date_format('"+gmtData+"','%Y-%m-%d')";
		return getCount(sql);
	}
	
	// 获取发布供应信息数目(不是数据导入)
	private int getSupply(String gmtData) {
		String sql = "SELECT count(*) as count FROM trade_supply where id > 10000000 and date_format(gmt_created,'%Y-%m-%d') = date_format('"+gmtData+"','%Y-%m-%d')";
		return getCount(sql);
	}
	
	// 获取发布求购信息数目(不是数据导入)
	private int getBuy(String gmtData) {
		String sql = "SELECT count(*) as count FROM trade_buy where id > 10000000 and date_format(gmt_created,'%Y-%m-%d') = date_format('"+gmtData+"','%Y-%m-%d')";
		return getCount(sql);
	}
	
	// 获取发布信息公司数目
	private int getPublishCompany(String gmtData) {
		String sql = "select cid,count(*) as count FROM trade_supply where id > 10000000 and date_format(gmt_created,'%Y-%m-%d') = date_format('"+gmtData+"','%Y-%m-%d') group by cid";
		final Integer[] count=new Integer[1];
		count[0]=0;
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					count[0]++;
				}
			}
		});
		return count[0];
	}
	
	// 获取发布文章公司数目
	private int getPublishNews(String gmtData) {
		String sql = "select cid,count(*) as count FROM comp_news where date_format(gmt_created,'%Y-%m-%d') = date_format('"+gmtData+"','%Y-%m-%d') group by cid";
		final Integer[] count=new Integer[1];
		count[0]=0;
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					count[0]++;
				}
			}
		});
		return count[0];
	}
	
	// 获取登录总次数
	private int getLoginCount(String gmtData) {
		String sql = "SELECT count(*) as count FROM comp_account where date_format(gmt_login,'%Y-%m-%d') = date_format('"+gmtData+"','%Y-%m-%d')";
		return getCount(sql);
	}
	
	//查询数目通用函数
	private int getCount(String sql) {
		final Integer[] count=new Integer[1];
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()) {
					count[0]=rs.getInt("count");
				}
			}
		});
		return count[0];
	}

	@Override
	public boolean init() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
}
