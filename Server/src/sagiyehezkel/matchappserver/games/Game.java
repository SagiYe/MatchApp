package sagiyehezkel.matchappserver.games;

import java.util.ArrayList;


public abstract class Game {
	protected static final String JSON_FIELD_NAME_GAME_ID = "ID";
	protected static final String JSON_FIELD_NAME_GAME_TYPE = "GAME_TYPE";
	protected static final String JSON_FIELD_NAME_UPDATE_TIME = "UPDATE_TIME";
	protected static final String JSON_FIELD_NAME_PLAYERS_LIST = "PLAYERS_LIST";
	protected static final String JSON_FIELD_NAME_FIRST_PLAYER = "FIRST_PLAYER";
	protected static final String JSON_FIELD_NAME_GAME_STATUS = "GAME_STATUS";
	
	private final int mGameTypeCode;
	protected Integer mGameID;
	protected Integer mUpdateTime;
	protected ArrayList<String> mPlayersList;
	protected String mFirstPlayer;
	protected String mWinnerPlayer = null;
	
	public Game(int gameTypeCode) {
		mGameTypeCode = gameTypeCode;
	}
	
	public String getGameTypeName() {
		return GamesFactory.getGameTypeNameByGameTypeCode(mGameTypeCode);
	}
	
	public int getGameTypeCode() {
		return mGameTypeCode;
	}

	public Integer getGameID() {
		return mGameID;
	}
	
	public Integer getUpdateTime() {
		return mUpdateTime;
	}

	public ArrayList<String> getPlayersList() {
		return mPlayersList;
	}

	public String getFirstPlayer() {
		return mFirstPlayer;
	}
	
	public void setGameDetails(int gameID, int updateTime, ArrayList<String> playersList,
			String firstPlayer, String gameStatus) {
		
		mGameID = gameID;
		mUpdateTime = updateTime;
		mPlayersList = playersList;
		mFirstPlayer = firstPlayer;
		updateStatusFromString(null, gameStatus);	
	}
	
	public boolean isGameFinished() {
		return (mWinnerPlayer != null);
	}
	
	public abstract void updateStatusFromString(String player, String newStatus);
	
	public abstract boolean checkIfGameCompleted();
}
