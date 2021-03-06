package getdata;


import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ys.entity.Village;

import util.DBUtil;
import util.HttpClientUtil;
import util.ThreadPoolUtils;

/**
 * 按尾号跑
 * @author Administrator
 *
 */
public class JspGet4 {
	public static void main(String[] args) throws FileNotFoundException {
		MyThreadRule7 tr = new MyThreadRule7();
		ThreadPoolUtils.execute(new MyThreadRule7());
		//tr.start();		
	}

	

}
class MyThreadRule7 extends Thread{
	@Override
	public void run() {
		Map<String,String> map = new HashMap<String,String>();
		map.put("思明区","350203");
		map.put("海沧区","350205");
		map.put("湖里区","350206");
		map.put("集美区","350211");
		map.put("同安区","350212");
		map.put("翔安区","350213");
		List<Village> list = new ArrayList<Village>();
		//list = queryInfo();
		String url = "";
				int number = 145;
				while(number <= 181){
						url = "http://zawb.fjgat.gov.cn/weixin/zhfw/czw_qwjs_cx.jsp?sunitname=台湾街" + number
						+ "&phrase=福建省厦门市湖里区"
						+ "&rowpage=0"
						+"&ss_qx=350206&rPageSize=15";
						System.out.println("url:" + url);
						String a = HttpClientUtil.doGet(url);
						System.out.println(a);
						number++;
							String[] b = a.split("</li>");		
							for (int j = 0; j < b.length - 1; j++) {
								String c = b[j].substring(b[j].indexOf("pic(") + 4, b[j].indexOf(");"));
								String d = c.substring(c.indexOf(",") + 1, c.length()).replace("'", "");
								String urlPath = "http://www.fjadd.com/addr?id=" + d;
								System.out.println(urlPath);
								try{
									getInfo(d,urlPath,"新景天湖");
								}catch(Exception e){
									System.out.println("插入数据库报错了！");
									e.printStackTrace();
								}
							}
					
				}						
	
	}
	
	public static List<Village> queryInfo(){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Village> list = null;
		try{
			conn = DBUtil.getConn();			
			String sql = "select * from village_temp";
			ps = conn.prepareStatement(sql);
			rs= ps.executeQuery();
			list = new ArrayList<Village>();
			while (rs.next()){
	            list.add(new Village(rs.getInt("id"),rs.getString("village"),rs.getString("location"),rs.getString("county")));
	        }
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			 DBUtil.closeConn(conn);
	            if (null != ps) {
	                try {
	                    ps.close();
	                } catch (SQLException e) {
	                    e.printStackTrace();
	               }
	           }
	         
		}
		return list;
	}
	
	public void insertTemp(String village,String location,String county){
		Connection conn = null;
		PreparedStatement ps = null;
		try{
			conn = DBUtil.getConn();			
				String sql = "insert into village_temp_two (village,location,county)values(?,?,?)";
				ps = conn.prepareStatement(sql);
				ps.setString(1, village);
				ps.setString(2, location);
				ps.setString(3, county);
				ps.executeUpdate();
				//System.out.println(Thread.currentThread().getName());
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			 DBUtil.closeConn(conn);
	            if (null != ps) {
	                try {
	                    ps.close();
	                } catch (SQLException e) {
	                    e.printStackTrace();
	               }
	           }
		}
	}
	
	public void getInfo(String id,String str,String village){
		Connection conn = null;
		PreparedStatement ps = null;
		try{
			conn = DBUtil.getConn();			
			String a = HttpClientUtil.doGet(str);
			System.out.println("url地址："+str);
			int i = a.indexOf("coord:");
			int j = a.indexOf("title:");
			int c = a.indexOf("addr:");
			int d = a.indexOf("key=");
			if (i>0&&j>0) {
				int imgFrom = a.indexOf("showimg('");
				String imgUrl = null;
				if(imgFrom>0){
					String imgStr = a.substring(imgFrom);
					int imgTo = imgStr.indexOf("')");
					imgUrl = imgStr.substring(9, imgTo);
				}
				String[] bb = a.substring(i+6,j-1).split(",");
				String addr = a.substring(c+5,d-1);
				String sql = "insert into village_add (id,latitude,longitude,address,picurl,police,village,createtime)values(?,?,?,?,?,?,?,?)";
				ps = conn.prepareStatement(sql);
				ps.setString(1, id.toString());
				ps.setString(2, bb[0]);
				ps.setString(3, bb[1]);
				ps.setString(4, addr);
				int policeInt = a.indexOf("line-height: 20px;\">");
				ps.setString(5, imgUrl);
				if(policeInt>0){
					String policeStr = a.substring(policeInt+1);
					String police = policeStr.substring(0, policeStr.indexOf("</a>"));
					police=police.substring(police.indexOf(">")+1);
					if(police.length()>10){
						ps.setString(6, null);
					}else{
						ps.setString(6, police);
					}
				}else{
					ps.setString(6, null);
				}
				ps.setString(7,village);
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				ps.setString(8,df.format(new Date()));
				ps.execute();
				System.out.println(Thread.currentThread().getName());
				System.out.println("【获得的纬度："+bb[0]+"】,【地址："+addr+"】,【图片地址:"+village+"】"+"】,【获得的经度："+bb[1]+"】");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			 DBUtil.closeConn(conn);
	            if (null != ps) {
	                try {
	                    ps.close();
	                } catch (SQLException e) {
	                    e.printStackTrace();
	               }
	           }
		}

	}
	
}

