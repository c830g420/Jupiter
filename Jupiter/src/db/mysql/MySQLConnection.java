package db.mysql;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
//import java.sql.Statement;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

public class MySQLConnection implements DBConnection {
	
	private Connection conn;
//	private PreparedStatement saveItemStmt;
//	
	public MySQLConnection() {
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private PreparedStatement getSaveItemStmt() {
//		if (conn == null) {
//			System.out.println("DB connection failed");
//			return;
//		}
//		if (saveItemStmt == null) {
//			saveItemStmt = conn.prepareStatement(sql)
//		}
//	}
	
	@Override
	public void close() {
		// TODO Auto-generated method stub
		if (conn != null) {
			try {
				conn.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		// TODO Auto-generated method stub
		if (conn == null) {
			System.out.println("DB connection failed");
			return;
		}
		
		try {
			String sql = "INSERT IGNORE INTO history(user_id, item_id) VALUES(?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			for (String itemid: itemIds) {
				stmt.setString(2, itemid);
				stmt.execute();
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		// TODO Auto-generated method stub
		if (conn == null) {
			System.out.println("DB connection failed");
			return;
		}
		
		try {
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			for (String itemid: itemIds) {
				stmt.setString(2, itemid);
				stmt.execute();
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		// TODO Auto-generated method stub
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
			
		}
		
		Set<String> favoriteItemIds = new HashSet<>();
		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				favoriteItemIds.add(rs.getString("item_id"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItemIds;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		// TODO Auto-generated method stub
		if (conn == null) {
			System.out.println("DB connection failed");
			return new HashSet<>();
		}
		
		Set<Item> favoriteItems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);
		try {
			String sql = "SELECT * FROM items WHERE item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			
			for (String itemId : itemIds) {
                stmt.setString(1, itemId);
                
                ResultSet rs = stmt.executeQuery();
                
                ItemBuilder builder = new ItemBuilder();
                
                while (rs.next()) {
                    builder.setItemId(rs.getString("item_id"));
                    builder.setName(rs.getString("name"));
                    builder.setAddress(rs.getString("address"));
                    builder.setImageUrl(rs.getString("image_url"));
                    builder.setUrl(rs.getString("url"));
                    builder.setCategories(getCategories(itemId));
                    builder.setRating(rs.getDouble("rating"));
                    builder.setDistance(rs.getDouble("distance"));
                    
                    favoriteItems.add(builder.build());
                }
            }

		} catch (SQLException e ) {
			e.printStackTrace();
		}
		
		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		// TODO Auto-generated method stu
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
			
		}
		
		Set<String> categories = new HashSet<>();
		try {
			String sql = "SELECT category FROM categories WHERE item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, itemId);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				categories.add(rs.getString("category"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		// TODO Auto-generated method stub
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);
		for (Item i: items) {
			saveItem(i);
		}
		
		return items;
	}

	@Override
	public void saveItem(Item item) {
		// TODO Auto-generated method stub
		if (conn == null) {
			System.out.println("DB connection failed");
			return;
		}
		
		try {
			
//			sql injection:
//			select * from users where username = '<username>'  and password = '<password>'
//			username: asdfeeresere or 1=1 -- (comment)
//			username: asdfefefe
//			password: adfdfeeafe' or '1' = '1
			/*
			String sql = String.format( "INSERT IGNORE INTO items(%s, %s, %s, %s, %s, %s, %s)", 
					item.getItemId(), item.getName(), item.getRating(), item.getAddress(),
					item.getImageUrl(), item.getUrl(), item.getDistance());
			*/
			
			String sql = "INSERT IGNORE INTO items VALUES(?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, item.getItemId());
			stmt.setString(2, item.getName());
			stmt.setDouble(3, item.getRating());
			stmt.setString(4, item.getAddress());
			stmt.setString(5, item.getImageUrl());
			stmt.setString(6, item.getUrl());
			stmt.setDouble(7, item.getDistance());
			stmt.execute();
			
			sql = "INSERT IGNORE INTO categories VALUES(?, ?)";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, item.getItemId());
			for (String category: item.getCategories()) {
//				System.out.println(category);
//				stmt.setString(1, item.getItemId());
				stmt.setString(2, category);
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getFullname(String userId) {
		
		if (conn == null) {
			System.err.println("DB connection failed");
			return "";
			
		}
		String fullname = "";
		try {
			String sql = "SELECT first_name, last_name FROM users WHERE user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			ResultSet rs = stmt.executeQuery();
			fullname = rs.getString("first_name") + " " + rs.getString("last_name");
			
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return fullname;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		// TODO Auto-generated method stub
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
			
		}
		
		try {
			String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			stmt.setString(2, password);
			ResultSet re = stmt.executeQuery();
			if (re.next())
				return true;
			else
				return false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

}
