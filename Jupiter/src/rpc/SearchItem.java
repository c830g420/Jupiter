package rpc;

//import java.awt.List;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;
import external.TicketMasterAPI;

/**
 * Servlet implementation class SearchItem
 */
@WebServlet(name = "search", urlPatterns = { "/search" })
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
//		request.get
		
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		String keyword = request.getParameter("term");
		
//		TicketMasterAPI tmAPI = new TicketMasterAPI();
//		List<Item> items = tmAPI.search(lat, lon, keyword);
//		
		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			List<Item> items = connection.searchItems(lat, lon, keyword);
			
			
			JSONArray array = new JSONArray();
			
			for (Item item: items) {
				array.put(item.toJSONObject());
			}
			RpcHelper.writeJsonArray(response, array);
		} finally {
			connection.close();
		}
		
//		try {
//			array.put(new JSONObject().put("username", "1233"));
//			array.put(new JSONObject().put("username", "12333"));
//			array.put(new JSONObject().put("username", "123333"));
//		} catch (JSONException e){
//			e.printStackTrace();
//		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
