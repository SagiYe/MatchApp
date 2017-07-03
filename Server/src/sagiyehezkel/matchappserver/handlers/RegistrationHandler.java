package sagiyehezkel.matchappserver.handlers;

import org.json.JSONException;
import org.json.JSONObject;

import sagiyehezkel.matchappserver.database.UsersDBHelper;

public class RegistrationHandler extends Handler {
	
	private static final String REGID = "REGID";
	private static final String PHONE = "PHONE";

	@Override
	protected String getHandlerName() {
		return "RegistrationHandler";
	}
	
	@Override
	protected String handleMsg(String request) {
		try {		
			JSONObject requestJson = new JSONObject(request);
			String regid = requestJson.getString(REGID);
			String phone = requestJson.getString(PHONE);
			
			UsersDBHelper.getInstance().addNewUser(phone, regid);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		  
		return null;
	}
}
