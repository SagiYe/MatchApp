package sagiyehezkel.matchappserver.handlers;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import sagiyehezkel.matchappserver.Util;
import sagiyehezkel.matchappserver.database.GamesDBHelper;

public class NewGameHandler extends Handler {

	private static final String JSON_FIELD_NAME_GAME_TYPE = "GAME_TYPE";
	private static final String JSON_FIELD_NAME_PLAYERS_LIST = "PLAYERS";	
	
	@Override
	protected String getHandlerName() {
		return "NewGameHandler";
	}
	
	@Override
	protected String handleMsg(String request) {
		try {
			JSONObject requestJson = new JSONObject(request);
			String gameType = requestJson.getString(JSON_FIELD_NAME_GAME_TYPE);
			ArrayList<String> playersList = Util.fromJsonArrayToArrayList(requestJson.getJSONArray(JSON_FIELD_NAME_PLAYERS_LIST));
			
			GamesDBHelper.getInstance().addNewGame(gameType, playersList);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		  
		return null;
	}
}
