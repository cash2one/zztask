/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-6-2
 */
package com.zz91.mission.front;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zz91.mission.domain.subscribe.CompanyAccount;
import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;
import com.zz91.util.mail.MailUtil;

/**
 * <br />任务描述：
 * <br />再生通服务过期恢复为普通用户
 * <br />
 * <br />处理逻辑：
 * <br />1.筛选所有非普通会员的公司ID 假设结果集为a
 * <br />2.查找出当天全部正常状态的公司ID 假设结果集为b
 * <br />3.比较a和b，找出三部类公司信息
 * <br />x1.未过期，但会员类型是普通会员
 * <br />x2.过期客户，一一更新客户相关状态，并邮件通知客户已经过期
 * <br />x3.正常的客户
 * <br />
 * <br />4.筛选出问题数据，即同一时间未过期记录有2条或以上的公司 假设结果集为c
 * <br />所有x1和c的数据将通过email，邮寄给客服部，由客服手动处理
 * <br />
 * @author mays (mays@zz91.com)
 *
 * created on 2011-6-2
 */
public class ZstExpiredTask implements ZZTask {
	
	public static String DB="ast";
	final static String DATE_FORMATE="yyyy-MM-dd";
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		
		baseDate=DateUtil.getDate(baseDate, DATE_FORMATE);
		
		Set<Integer> x1=new HashSet<Integer>();
		doab(x1,baseDate);
		final Set<Integer> c=new HashSet<Integer>();
		queryTroubleCompany(c);
		
		List<CompanyAccount> x1Account=new ArrayList<CompanyAccount>();
		for(Integer i:x1){
			CompanyAccount account=queryAccount(i);
			if(account!=null){
				if(StringUtils.isEmpty(account.getAccount())){
					account.setAccount(String.valueOf(i));
					account.setContact("账号信息不存在，找技术部解决");
				}
				x1Account.add(account);
			}
		}
		
		List<CompanyAccount> caccount=new ArrayList<CompanyAccount>();
		for(Integer i:c){
			CompanyAccount account=queryAccount(i);
			if(account!=null){
				if(StringUtils.isEmpty(account.getAccount())){
					account.setAccount(String.valueOf(i));
					account.setContact("账号信息不存在，找技术部解决");
				}
				caccount.add(account);
			}
		}
		
		Map<String, Object> dataMap=new HashMap<String, Object>();
		dataMap.put("x1Account", x1Account);
		dataMap.put("caccount", caccount);
		
		MailUtil.getInstance().sendMail(
			"[重要]高会过期问题数据", 
//			"list:cs-expire-trouble", null,
			"zz91.expired.error@asto.mail", null,  //,joycesleep@gmail.com
			null, "zz91", "zz91-expire-cs",
			dataMap, MailUtil.PRIORITY_TASK);
		
		return true;
	}
	
	private void doab(final Set<Integer> x1,Date baseDate){
		final Set<Integer> a=new HashSet<Integer>();
		final Set<Integer> b=new HashSet<Integer>();
		
		String baseDateStr=DateUtil.toString(baseDate, "yyyy-MM-dd HH:mm:ss");
		String sql="select distinct company_id from crm_company_service where crm_service_code='1000' and gmt_start<='"+baseDateStr+"' and gmt_end>='"+baseDateStr+"' and apply_status=1";
		queryCid(sql, a);
		sql="select id from company where membership_code <> '10051000'";
		queryCid(sql, b);
		
		//TODO x1,x2,x3
		for(Integer i:a){
			if(b.contains(i)){
				b.remove(i);
			}else{
				x1.add(i);
			}
		}
		
		Set<CompanyAccount> expiredAccount=new HashSet<CompanyAccount>();
		for(Integer i:b){
			doExpire(i,expiredAccount);
		}
		
		Map<String, Object> dataMap=new HashMap<String, Object>();
		dataMap.put("expiredAccount", expiredAccount);
		
		if(expiredAccount.size()>0){
			MailUtil.getInstance().sendMail(
					"[重要]"+baseDateStr+"时刻过期的高级会员汇总", 
//			"list:cs-expire-trouble", null,
					"zz91.expired.report@asto.mail", null,  //,joycesleep@gmail.com
					null, "zz91", "zz91-expire-cs-notice",
					dataMap, MailUtil.PRIORITY_TASK);
		}
	}
	
	private void doExpire(Integer id, Set<CompanyAccount> expiredAccount){
		// 公司表 更新
		DBUtils.insertUpdate("ast", "update company set membership_code='10051000',zst_flag='0',gmt_modified=now() where id="+id);
		
		// 后台CS crm_cs_profile表过期更改
		DBUtils.insertUpdate("ast", "update crm_cs_profile set membership_code='10051000',gmt_modified=now() where company_id="+id);
		
		// 将过期高会的供求ID，插入products_zst_expired（过期客户供求表）
		queryProductIdForExpired(id);
		
		//过期通知
		CompanyAccount account=queryAccount(id);
		if(account==null){
			return ;
		}
		
		expiredAccount.add(account);
		
		String toemail=account.getEmail();
		if("1".equals(account.getIsUseBackEmail())){
			toemail=account.getBackEmail();
		}
		if(toemail!=null && StringUtils.isEmail(toemail)){
			//发送邮件
			Map<String, Object> dataMap=new HashMap<String, Object>();
			dataMap.put("accountInfo", account);
			MailUtil.getInstance().sendMail(
				"再生通服务过期通知-ZZ91再生网",
				toemail, null,
				null, "zz91", "zz91-zst-expired", 
				dataMap, MailUtil.PRIORITY_TASK);
		}
		
	}
	
	private CompanyAccount queryAccount(Integer cid){ //, String account
		String sql="select email,back_email,is_use_back_email,contact,sex,account from company_account where company_id="+cid+" limit 1"; //+" and account='"+account+"'";
		final CompanyAccount account=new CompanyAccount();
		DBUtils.select("ast", sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				if(rs.next()){
					account.setEmail(rs.getString(1));
					account.setBackEmail(rs.getString(2));
					account.setIsUseBackEmail(rs.getString(3));
					account.setContact(rs.getString(4));
					account.setSex(rs.getString(5));
					account.setAccount(rs.getString(6));
				}
			}
		});
		
		return account;
	}
	
	private void queryCid(String sql, final Set<Integer> set){
		DBUtils.select(DB, sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					set.add(rs.getInt(1));
				}
			}
		});
	}
	
	private void queryTroubleCompany(final Set<Integer> set){
		
		String sql="select company_id,count(company_id) as ct from crm_company_service where crm_service_code='1000' and gmt_start<=now() and gmt_end>now() and apply_status=1 group by company_id having ct>1";
		DBUtils.select(DB, sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					set.add(rs.getInt(1));
				}
			}
		});
	}
	
	private void queryProductIdForExpired(Integer companyId){
		String sql = "select id from products where check_status = '1' and company_id = "+companyId;
		final Set<Integer> set = new HashSet<Integer>();
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					set.add(rs.getInt(1));
				}
			}
		});
		// insert入 products_zst_expired 表
		for(Integer id:set){
			insertProductIdToExpiredTable(id);
		}
		// 更新 products 表中 高会审核 的标志
		for(Integer id:set){
			updateProductToNoCheckStatus(id);
		}
	}
	
	private void insertProductIdToExpiredTable(Integer productId){
		DBUtils.insertUpdate(DB, "INSERT INTO products_zst_expired(gmt_created,gmt_modified,product_id) VALUES(now(),now(),"+productId+")");
	}
	
	private void updateProductToNoCheckStatus(Integer productId){
		DBUtils.insertUpdate(DB, "update products set unchecked_check_status ='0',gmt_modified = now() where unchecked_check_status='1' and id ="+productId);
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}

	public static void main(String[] args) throws ParseException, Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");

		ZstExpiredTask task=new ZstExpiredTask();
		ZstExpiredTask.DB="ast";
		task.exec(DateUtil.getDate("2012-11-01", "yyyy-MM-dd"));
	}
}
