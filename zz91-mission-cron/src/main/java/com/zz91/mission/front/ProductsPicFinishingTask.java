package com.zz91.mission.front;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zz91.task.common.ZZTask;
import com.zz91.util.db.DBUtils;
import com.zz91.util.db.IReadDataHandler;
import com.zz91.util.db.pool.DBPoolFactory;
import com.zz91.util.file.ScaleImage;
/**
 * 用于处理某一段时间内上传的供求信息图片，如果图片大小大于800×800，则将图片缩小
 * */
public class ProductsPicFinishingTask implements ZZTask{
	
	final static String FROME_DATE="2005-01-01";
	final static String TO_DATE="2012-02-01";
	public static String RES_ROOT="/usr/data/resources-back";
	final static String DB="ast";

	@Override
	public boolean clear(Date baseDate) throws Exception {
		
		return false;
	}

	@Override
	public boolean exec(Date baseDate) throws Exception {
		
		//找出要处理的所有图片
		//判断图片大小 800×800
		//删除图片或做缩略图
		
		final Integer[] dealCount=new Integer[1];
		
		String sql="select count(*) from products_pic where product_id<>0 and gmt_created >= '"+FROME_DATE+"' and gmt_created < '"+TO_DATE+"'";
		System.out.println(sql);
		
		DBUtils.select(DB, sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while(rs.next()){
					dealCount[0]=rs.getInt(1);
				}
			}
		});
		
		if(dealCount[0] !=null && dealCount[0]>0){
			for(int i=0;i<dealCount[0];i=i+500){
				dealPic(i, 500);
			}
		}
		
		return true;
	}
	
	private void dealPic(int start, int limit){
		System.out.println(start+" - 500");
//		final Map<Integer, String> delMap=new HashMap<Integer, String>(); //暂时不删除图片
		final List<String> scaleMap=new ArrayList<String>();
		String sql="select id, pic_address, product_id from products_pic where product_id<>0 and gmt_created >= '"+FROME_DATE+"' and gmt_created < '"+TO_DATE+"' limit "+start+","+limit;
		DBUtils.select(DB, sql, new IReadDataHandler() {
			
			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					scaleMap.add(rs.getString(2));
				}
			}
		});
		
		ScaleImage is=new ScaleImage();
		
		for(String address:scaleMap){
			File fromFile = new File(RES_ROOT+"/"+address);
			
			BufferedImage srcImage;
			try {
				srcImage = javax.imageio.ImageIO.read(fromFile);
				int imageWideth = srcImage.getWidth(null);
				int imageHeight = srcImage.getHeight(null);
				if(imageWideth>800 || imageHeight>800){
					is.saveImageAsJpg(RES_ROOT+"/"+address, RES_ROOT+"/"+address, 800, 800);
				}
			} catch (IOException e) {
			} catch (Exception e) {
			}
			
		}
	}

	@Override
	public boolean init() throws Exception {
		return false;
	}

	public static void main(String[] args) throws Exception {
		
		DBPoolFactory.getInstance().init("file:/usr/tools/config/db/db-zztask-jdbc.properties");
		
		ProductsPicFinishingTask task= new ProductsPicFinishingTask();
		ProductsPicFinishingTask.RES_ROOT ="/usr/data/new_res";
		
		task.exec(new Date());
		
	}
}
