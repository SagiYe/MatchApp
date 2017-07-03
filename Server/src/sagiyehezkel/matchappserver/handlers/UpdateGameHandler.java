package sagiyehezkel.matchappserver.handlers;

import org.json.JSONException;
import org.json.JSONObject;

import sagiyehezkel.matchappserver.database.GamesDBHelper;
import sagiyehezkel.matchappserver.games.Game;
import sagiyehezkel.matchappserver.games.GamesFactory;

public class UpdateGameHandler extends Handler {
	private static final String JSON_FIELD_GAME_ID = "GAME_ID";
	private static final String JSON_FIELD_GAME_STATUS = "STATUS";
	private static final String JSON_FIELD_LAST_PLAYER = "PLAYER";
	
	@Override
	protected String getHandlerName() {
		return "UpdateGameHandler";
	}
	
	@Override
	protected String handleMsg(String request) {
		try {		
			JSONObject requestJson = new JSONObject(request);
			int gameId = requestJson.getInt(JSON_FIELD_GAME_ID);
			String newStatus = requestJson.getString(JSON_FIELD_GAME_STATUS);
			String lastPlayer = requestJson.getString(JSON_FIELD_LAST_PLAYER);
			
			Game game = GamesFactory.getGameFromDB(gameId);
			game.updateStatusFromString(lastPlayer, newStatus);
			GamesDBHelper.getInstance().updateGame(gameId, newStatus, lastPlayer, game.isGameFinished());
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		  
		return null;
	}
}
