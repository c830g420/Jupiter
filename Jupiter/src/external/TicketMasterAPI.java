package external;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class TicketMasterAPI {
	
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = "";
	private static final String API_KEY = "B7GBuCjvgagcfzYs6p5EXDk9TO12ydpU";
	private static final String EMBEDDED = "_embedded";
	private static final String EVENTS = "events";
	private static final String NAME = "name";
	private static final String ID = "id";
	private static final String URL_STRING = "url";
	private static final String RATING = "rating";
	private static final String DISTANCE = "distance";
	private static final String VENUES = "venues";
	private static final String ADDRESS = "address";
	private static final String LINE1 = "line1";
	private static final String LINE2 = "line2";
	private static final String LINE3 = "line3";
	private static final String CITY = "city";
	private static final String IMAGES = "images";
	private static final String CLASSIFICATIONS = "classifications";
	private static final String SEGMENT = "segment";
//	private static final String CITY = "city";
//	private static final String CITY = "city";
	
	public List<Item> search(double lat, double lon, String keyword) {
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		try {
			keyword = java.net.URLEncoder.encode(keyword, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		String geoHash = GeoHash.encodeGeohash(lat, lon, 8);
		
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY, geoHash, keyword, 50);
		
//		BufferedReader in;
		
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection();
			int responseCode = connection.getResponseCode();
			
			System.out.println("\nSending 'GET' to request to URL :" + URL + "?" + query);
			System.out.println("Response Code: " + responseCode );
			
			StringBuilder response = new StringBuilder();
			try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			
				String inputLine;
//				StringBuilder response = new StringBuilder();
	
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
			}
//			in.close();
			
			JSONObject obj = new JSONObject(response.toString());
			if (obj.isNull(EMBEDDED)) {
				return new ArrayList<>();
			}
			JSONObject embedded = obj.getJSONObject(EMBEDDED);
			
			JSONArray events = embedded.getJSONArray(EVENTS);
			
			return getItemList(events);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
//		finally {
////			return new JSONArray();
//		}
		return new ArrayList<>();
		
	}
	
	private void queryAPI(double lat, double lon) {
		List<Item> events = search(lat, lon, null); 
		try {
			for (int i = 0; i < events.size(); ++i) {
				Item event = events.get(i);
				System.out.println(event.toJSONObject());
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
     * Helper methods
     */

    //  {
    //    "name": "laioffer",
              //    "id": "12345",
              //    "url": "www.laioffer.com",
    //    ...
    //    "_embedded": {
    //        "venues": [
    //            {
    //                "address": {
    //                   "line1": "101 First St,",
    //                   "line2": "Suite 101",
    //                   "line3": "...",
    //                },
    //                "city": {
    //                    "name": "San Francisco"
    //                }
    //                ...
    //            },
    //            ...
    //        ]
    //    }
    //    ...
    //  }

	private String getAddress(JSONObject event) throws JSONException {
        if (!event.isNull(EMBEDDED)) {
        	JSONObject embedded = event.getJSONObject(EMBEDDED);
        	if (!embedded.isNull(VENUES)) {
        		JSONArray venues = embedded.getJSONArray(VENUES);
        		
        		for (int i = 0; i < venues.length(); i++) {
        			JSONObject venue = venues.getJSONObject(i);
        			
        			StringBuilder sb = new StringBuilder();
        			
        			if (!venue.isNull(ADDRESS)) {
        				JSONObject address = venue.getJSONObject(ADDRESS);
        				
        				if (!address.isNull(LINE1)) {
        					sb.append(address.get(LINE1));
        				}
        				if (!address.isNull(LINE2)) {
        					sb.append('\n');
        					sb.append(address.get(LINE2));
        				}
        				if (!address.isNull(LINE3)) {
        					sb.append('\n');
        					sb.append(address.get(LINE3));
        				}
        			}
        			
        			if (!venue.isNull(CITY)) {
        				JSONObject city = venue.getJSONObject(CITY);
        				if (!city.isNull(NAME)) {
        					sb.append('\n');
        					sb.append(city.getString(NAME));
        				}
        			}
        			
        			String addr = sb.toString();
        			
        			if (!addr.equals("")) {
        				return addr;
        			}
        		}
        	}
        }
		return "";
    }


    // {"images": [{"url": "www.example.com/my_image.jpg"}, ...]}
    private String getImageUrl(JSONObject event) throws JSONException {
    	if (!event.isNull(IMAGES)) {
    		JSONArray images = event.getJSONArray(IMAGES);
    		for (int i = 0; i < images.length(); i ++) {
    			JSONObject image = images.getJSONObject(i);
    			if (!image.isNull(URL_STRING)) {
    				return image.getString(URL_STRING);
    			}
    		}
    	}
        return "";
    }

    // {"classifications" : [{"segment": {"name": "music"}}, ...]}
    private Set<String> getCategories(JSONObject event) throws JSONException {
        Set<String> categories = new HashSet<>();
        
        if (!event.isNull(CLASSIFICATIONS)) {
        	JSONArray classifications = event.getJSONArray(CLASSIFICATIONS);
        	
        	for (int i = 0; i < classifications.length(); i ++) {
        		JSONObject classification = classifications.getJSONObject(i);
        		if (!classification.isNull(SEGMENT)) {
        			JSONObject segment = classification.getJSONObject(SEGMENT);
	        		if (!segment.isNull(NAME)) {
	        			categories.add(segment.getString(NAME));
	        		}
        		}
        	}
        	
        }

        return categories;
    }

    // Convert JSONArray to a list of item objects.
    private List<Item> getItemList(JSONArray events) throws JSONException {
        List<Item> itemList = new ArrayList<>();
        
        for (int i = 0; i < events.length(); i ++) {
        	JSONObject event = events.getJSONObject(i);
        	ItemBuilder builder = new ItemBuilder();
        	
        	if (!event.isNull(NAME)) {
        		builder.setName(event.getString(NAME));
        	}
        	if (!event.isNull(ID)) {
        		builder.setItemId(event.getString(ID));
        	}
        	if (!event.isNull(URL_STRING)) {
        		builder.setUrl(event.getString(URL_STRING));
        	}
        	if (!event.isNull(RATING)) {
        		builder.setRating(event.getDouble(RATING));
        	}
        	if (!event.isNull(DISTANCE)) {
        		builder.setDistance(event.getDouble(DISTANCE));
        	}
        	
        	builder.setAddress(getAddress(event));
        	builder.setCategories(getCategories(event));
        	builder.setImageUrl(getImageUrl(event));
        	
        	itemList.add(builder.build());
        }
        return itemList;
    }

	public static void main(String[] args) {
        TicketMasterAPI tmApi = new TicketMasterAPI();
        // Mountain View, CA
        // tmApi.queryAPI(37.38, -122.08);
        // London, UK
        // tmApi.queryAPI(51.503364, -0.12);
        // Houston, TX
        tmApi.queryAPI(29.682684, -95.295410);
    }


}
