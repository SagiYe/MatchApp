package sagiyehezkel.matchappserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GCMMsgContent implements Serializable {
	
	private static final long serialVersionUID = 1617476983936865779L;
	private static String apiKey = "AIzaSyCYed-Seo4H0f-pbvoICvZK34Ympdp-Mr4";
	
	public List<String> registration_ids;
    public Map<String,String> data;

    public void addRegId(String regId){
        if(registration_ids == null)
            registration_ids = new LinkedList<String>();
        registration_ids.add(regId);
    }
    
    public void updateClients(String title, String message) {
        try {
	        URL url = new URL("https://android.googleapis.com/gcm/send");
	
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/json");
	        conn.setRequestProperty("Authorization", "key="+apiKey);
	
	        conn.setDoOutput(true);
	
	        ObjectMapper mapper = new ObjectMapper();
	        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
	
	        if(data == null)
	            data = new HashMap<String,String>();

	        data.put("title", title);
	        data.put("message", message);
	        
	        mapper.writeValue(wr, this);
	
	        wr.flush();
	
	        wr.close();

	        int responseCode = conn.getResponseCode();
	        
	        if (responseCode != 200) {
	        	System.out.println("Sent message respone code:" + Integer.toString(responseCode));
	        }
	        	
	        BufferedReader in = new BufferedReader(
	                new InputStreamReader(conn.getInputStream()));
	        String inputLine;
	        StringBuffer response = new StringBuffer();
	
	        while ((inputLine = in.readLine()) != null) {
	            response.append(inputLine);
	        }
	        in.close();
	        
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
