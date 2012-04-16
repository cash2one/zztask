package com.zz91.mission.front;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.lang.StringUtils;

/**
 *@Author:kongsj
 *@Date:2012-3-31
 */
public class ZstCreditCorrectTask implements ZZTask{
	
	private static String DB = "ast";
	
	@Override
	public boolean init() throws Exception {
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		do{
			Set<Integer> ids = queryCompanyIds();
			if(ids.size()<1){
				break;
			}
			for(Integer id : ids){
				//清空id历史记录
				cleanByIds(id);
				
				//公司详细积分计算
				queryCompanyInfoById(id);

				//资信参考人积分计算
				queryReferenceById(id);
				
				//证书积分计算
				queryFileById(id);
				
				//再生通年限积分计算
				queryZSTById(id);
				
			}
			
		}while(false);
		return true;
	}
	
	private Set<Integer> queryCompanyIds(){
		final Set<Integer> ids = new HashSet<Integer>();
		String sql = "select distinct c.company_id from crm_company_service c where crm_service_code = '1000' and apply_status=1";
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					ids.add(rs.getInt(1));
				}
			}
		});
		return ids; 
	}
	
	private void cleanByIds(Integer companyId){
		DBUtils.insertUpdate(DB, "delete from credit_integral_details where company_id="+companyId);
	}
	
	private void queryCompanyInfoById(Integer id){
		String sql = "select c.id,c.name,ca.contact,ca.tel,ca.mobile,c.address from company c left join company_account ca on c.id=ca.company_id where c.id="+id;
		final Map<String,Integer> integrityMap = new HashMap<String,Integer>();
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					//公司名称
					if(StringUtils.isNotEmpty(rs.getString(2))){
						integrityMap.put("company_name", 2);
					}
					//公司联系人
					if(StringUtils.isNotEmpty(rs.getString(3))){
						integrityMap.put("company_contact", 2);
					}
					//手机 & 电话
					Integer integrity = 0;
					if(StringUtils.isNotEmpty(rs.getString(4))){
						integrity = integrity + 2;
					}
					if(StringUtils.isNotEmpty(rs.getString(5))){
						integrity = integrity + 2;
					}
					if(integrity>0){
						integrityMap.put("company_phone", integrity);
					}
					//地址
					if(StringUtils.isNotEmpty(rs.getString(6))){
						integrityMap.put("company_address", 2);
					}
				}
			}
		});
		for(String key: integrityMap.keySet()){
			saveToDB(key, id, integrityMap.get(key), id, "");
		}
	}
	
	private void queryReferenceById(Integer id){
		final Set<Integer> idSet = new HashSet<Integer>();
		String sql = "SELECT id from credit_reference where check_status=1 and company_id ="+id;
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					if(rs.getInt(1)>0){
						idSet.add(rs.getInt(1));
					}
				}
			}
		});
		for(Integer key: idSet){
			saveToDB("credit_reference", key, 5, id, "");
		}
	}
	
	private void queryFileById(Integer id){
		final Set<Integer> idSet = new HashSet<Integer>();
		String sql = "SELECT id from credit_file where check_status=1 and company_id ="+id;
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					if(rs.getInt(1)>0){
						idSet.add(rs.getInt(1));
					}
				}
			}
		});
		for(Integer key: idSet){
			saveToDB("credit_file", key, 2, id, "");
		}
	}
	
	private void queryZSTById(Integer id){
		String sql  = "select membership_code,gmt_start,gmt_end,id from crm_company_service where crm_service_code = '1000' and apply_status=1 and company_id="+id;
		final Map<Integer,Integer> integrityMap = new HashMap<Integer,Integer>();
		DBUtils.select(DB, sql, new IReadDataHandler() {
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					Integer i = 0;
					try {
						Date from = DateUtil.getDate(rs.getString(2), "yyyy-MM-dd");
						Date to = DateUtil.getDate(rs.getString(3), "yyyy-MM-dd");
						Integer day = DateUtil.getIntervalDays(DateUtil.getDateAfterMonths(to, 1), from);
						i = day/365;
					} catch (ParseException e) {
					}
					String zstCode = rs.getString(1);
					if(i>0&&i<5){
						if("100510021000".equals(zstCode)){
							i = i *12;
						}
						else if("100510021001".equals(zstCode)){
							i = i *15;
						}
						else if("100510021002".equals(zstCode)){
							i = i *20;
						}else{
							i = i*10;
						}
					}
					else if(i>5){
						i = 50;
					}
					if(i>0){
						integrityMap.put(rs.getInt(4), i);
					}
				}
			}
		});
		
		for(Integer key: integrityMap.keySet()){
			saveToDB("service_zst_year", key, integrityMap.get(key), id, "");
		}
	}
	
	private void saveToDB(String key, Integer relatedId, Integer integral,
			Integer companyId, String account) {
		String sql = "insert into credit_integral_details(operation_key, related_id, integral, company_id, account, gmt_created, gmt_modified) values";
		sql = sql + "('"+key+"',"+relatedId+","+integral+",'"+companyId+"','"+account+"',now(),now())";
		DBUtils.insertUpdate(DB, sql);
	}

	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}
	
	public static void main(String[] args) throws ParseException, Exception{
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		long start=System.currentTimeMillis();
		ZstCreditCorrectTask task=new ZstCreditCorrectTask();
		task.clear(DateUtil.getDate("2011-12-30", "yyyy-MM-dd"));
		task.exec(DateUtil.getDate("2011-12-30", "yyyy-MM-dd"));
		long end=System.currentTimeMillis();
		System.out.println("共耗时："+(end-start));
	}

}