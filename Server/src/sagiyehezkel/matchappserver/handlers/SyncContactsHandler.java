package sagiyehezkel.matchappserver.handlers;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sagiyehezkel.matchappserver.Util;
import sagiyehezkel.matchappserver.database.UsersDBHelper;

public class SyncContactsHandler extends Handler {

	private static final String CONTACTS = "CONTACTS";
	private static final String USERS = "USERS";
	
	@Override
	protected String getHandlerName() {
		return "SyncContactsHandler";
	}
	
	@Override
	protected String handleMsg(String request) {
		try {
			JSONObject jsonObject = new JSONObject(request);
			
			ArrayList<String> contacts = Util.fromJsonArrayToArrayList(jsonObject.getJSONArray(CONTACTS)); 
			ArrayList<String> users = UsersDBHelper.getInstance().getRegisterdUsersFromContactsList(contacts);
			
			JSONObject jsonOutput = new JSONObject();
			JSONArray jsonArray = new JSONArray(users);
	        jsonOutput.put(USERS, jsonArray);
	        
	        String response = jsonOutput.toString();

	        return response;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		  
		return null;
	}
}
