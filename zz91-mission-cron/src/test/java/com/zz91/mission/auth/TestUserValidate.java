/**
 * Copyright 2011 ASTO.
 * All right reserved.
 * Created on 2011-9-2
 */
package com.zz91.mission.auth;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.tags.TagsUtils;

/**
 * @author mays (mays@zz91.com)
 * 
 *         created on 2011-9-2
 */
public class TestUserValidate {

	public static void main(String[] args) {
		TagsUtils.getInstance().init("web.properties");
		DBPoolFactory.getInstance().init(
				"file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		DBUtils.select("ast", "select `name` from bbs_tags limit 1000", new IReadDataHandler() {

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					TagsUtils.getInstance().createTags(rs.getString(1));
				}
			}
			
		});
		System.out.println("已添加互助标签");
		
		for(int i=0;i<50;i++){
			DBUtils.select("ast", "select `name` from tags_info limit "+(i*2000)+","+((i+1)*2000), new IReadDataHandler() {

				@Override
				public void handleRead(ResultSet rs) throws SQLException {
					while(rs.next()){
						TagsUtils.getInstance().createTags(rs.getString(1));
					}
				}

			});
			System.out.println("已添加标签：从"+(i*2000)+"到"+((i+1)*2000));
		}

	}

}
