package com.zz91.mission.kl91;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.zz91.task.common.ZZTask;
import com.zz91.util.datetime.DateUtil;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.http.HttpUtils;
/**
 * @author 伍金成：查询昨天登录过的客户，更新登录次数和最后登录时间到crm并且放入公海
 * 1,搜索昨天登录过的客户
 * 2,得到公司的登录时间，登陆次数和id
 * 3,根据登录过的公司id来更新为公海
 */
public class KlCrmNumLoginImportTask implements ZZTask{

	static final Integer LIMIT = 10;
	private static String API_HOST="http://admin1949.zz91.com:8311/kl91-admin";
	final static String DB="kl91_crm_test";
	
	@Override
	public boolean exec(Date baseDate) throws Exception {
		boolean result=false;
		String from = DateUtil.toString(
				DateUtil.getDateAfterDays(baseDate, -1), "yyyy-MM-dd");
		String to = DateUtil.toString(baseDate, "yyyy-MM-dd");
		do{
			String responseText = HttpUtils.getInstance().httpGet(API_HOST+"/queryYestodayCompanyCount.htm?from="+from+"&to="+to+"", HttpUtils.CHARSET_UTF8);
			JSONObject jb=JSONObject.fromObject(responseText);
			int count = jb.getInt("i");
			int page = getSize(count);
			List<JSONObject> list = new ArrayList<JSONObject>();
			
			// 循环获取所有数据
			for(int i=1;i<=page;i++){
				try {
					responseText = HttpUtils.getInstance().httpGet(API_HOST+"/queryYestodayCompany.htm?from="+from+"&to="+to+"", HttpUtils.CHARSET_UTF8);
				} catch (Exception e) {
					throw new Exception(e.getMessage()+"   start:"+i);
				}
				JSONArray js=JSONArray.fromObject(responseText);
				for (Iterator iter = js.iterator(); iter.hasNext();) {
					JSONObject object = (JSONObject) iter.next();
					list.add(object);
				}
			}
			
			// 循环更新
			for(JSONObject object:list){
				//更新公司信息
				updateCompany(object.getInt("id"), object.getString("gmtLastLogin") ,
						object.getInt("numLogin"));
			}
			result=true;
		}while(false);
		return result;
	}
	
	private void updateCompany(Integer cid,String gmtLogin,Integer loginCount) {
		String sql ="UPDATE crm_company SET gmt_login='"+gmtLogin+"',gmt_modified=now(),login_count="+loginCount+"  where cid="+cid;
		DBUtils.insertUpdate(DB, sql);
		updateCtype(cid);
	}
	
	private void updateCtype(Integer cid) {
		String sql="update crm_company SET ctype=2 where ctype=5 and login_count>0 and cid="+cid+"";
		DBUtils.insertUpdate(DB, sql);
	}
	
	private int getSize(int count){
		if(count%10==0){
			count = count/LIMIT;
		}else{
			count = count/LIMIT+1;
		}
		return count;
	}
	
	@Override
	public boolean init() throws Exception {
		return false;
	}
	
	@Override
	public boolean clear(Date baseDate) throws Exception {
		return false;
	}

	public static void main(String[] args) throws Exception {
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		KlCrmNumLoginImportTask obj = new KlCrmNumLoginImportTask();
		API_HOST = "http://test.kl91.zz91.com:8089";
		Date date = DateUtil.getDate("2012-09-29", "yyyy-MM-dd");
		obj.exec(date);
	}

}
