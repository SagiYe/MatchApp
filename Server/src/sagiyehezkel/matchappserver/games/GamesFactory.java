package sagiyehezkel.matchappserver.games;

import sagiyehezkel.matchappserver.database.GamesDBHelper;

public class GamesFactory {
	public static final int CONNECT_FOUR = 0;
	private static final String[] GAMES_NAMES = {
			"ConnectFour"
	};
	
	public static Game createEmptyGame(int gameTypeCode) {
		Game gameBase = null;
		
		switch (gameTypeCode) {
		case CONNECT_FOUR:
			gameBase = new ConnectFour();
			break;

		default:
			break;
		}
		
		return gameBase;
	}
	
	public static String getGameTypeNameByGameTypeCode(int gameTypeCode) {
		return GAMES_NAMES[gameTypeCode];
	}
	
	public static int getGameTypeCodeByGameTypeName(String gameTypeName) {
		int i = -1;
		
		for (int j = 0; j < GAMES_NAMES.length; j++) {
			if (gameTypeName.equals(GAMES_NAMES[j])) {
				i = j;
				break;
			}
		}
		
		return i;
	}
	
	public static Game getGameFromDB(int gameId) {
		GamesDBHelper gamesDBHelper = GamesDBHelper.getInstance();
		String gameTypeName = gamesDBHelper.getGameTypeByID(gameId);
		Game game = createEmptyGame(getGameTypeCodeByGameTypeName(gameTypeName));
		gamesDBHelper.loadGameDataByID(game, gameId);
		
		return game;
	}
}
