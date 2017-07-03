package sagiyehezkel.matchappserver.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sagiyehezkel.matchappserver.GCMMsgContent;
import sagiyehezkel.matchappserver.Util;
import sagiyehezkel.matchappserver.games.Game;

public class GamesDBHelper {
	public static final String TABLE_NAME = "GAMES";
	public static final String COLUMN_GAME_ID = "ID";
	public static final String COLUMN_GAME_TYPE = "GAME_TYPE";
	public static final String COLUMN_UPDATE_TIME = "UPDATE_TIME";
	public static final String COLUMN_PLAYERS_LIST = "PLAYERS_LIST";
	public static final String COLUMN_FIRST_PLAYER = "FIRST_PLAYER";
	public static final String COLUMN_GAME_STATUS = "GAME_STATUS";
	
	private static GamesDBHelper instance = null;
	
	public static GamesDBHelper getInstance() {
		if (instance == null)
			instance = new GamesDBHelper();
		
		return instance;
	}
	
	private GamesDBHelper() {
		makeSureTableExists();
	}
	
	private void makeSureTableExists() {
		final String SQL_CREATE_GAMES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" 
				+ COLUMN_GAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ COLUMN_GAME_TYPE + " TEXT NOT NULL,"
				+ COLUMN_UPDATE_TIME + " INTEGER NOT NULL,"
				+ COLUMN_PLAYERS_LIST + " TEXT NOT NULL,"
				+ COLUMN_FIRST_PLAYER + " TEXT NOT NULL,"
				+ COLUMN_GAME_STATUS + " TEXT NOT NULL"
				+ ")";
		
		PreparedStatement preparedStatement;
		
		try {
			preparedStatement = DBManager.getDbConnection().prepareStatement(SQL_CREATE_GAMES_TABLE);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int addNewGame(String gameType, ArrayList<String> playersList) {
		final String SQL_INSERT_INTO_GAMES_TABLE = "INSERT INTO " + TABLE_NAME + "(" + 
				COLUMN_GAME_TYPE + ", " +
				COLUMN_UPDATE_TIME + ", " +
				COLUMN_PLAYERS_LIST + ", " +
				COLUMN_FIRST_PLAYER + ", " +
				COLUMN_GAME_STATUS  + " " +
				") VALUES (?,?,?,?,?)";
		
		// Selecting randomly the first player
		Random rand = new Random();
		String firstPlayer = playersList.get(rand.nextInt(playersList.size()));
		
		PreparedStatement preparedStatement;
		try {
			preparedStatement = DBManager.getDbConnection().prepareStatement(SQL_INSERT_INTO_GAMES_TABLE, 
					Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, gameType);
			preparedStatement.setString(2, Long.toString(System.currentTimeMillis()));
			preparedStatement.setString(3, Util.fromListToString(playersList));
			preparedStatement.setString(4, firstPlayer);
			preparedStatement.setString(5, "EMPTY");
			preparedStatement.executeUpdate();
			
			ResultSet rs = preparedStatement.getGeneratedKeys();
			int lastInsertedId = 0;
            if(rs.next()) {
                 lastInsertedId = rs.getInt(1);
            }
            
            notifyPlayersAboutNewGame(lastInsertedId, gameType, playersList, firstPlayer);
       
            return lastInsertedId;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public int updateGame(int gameId, String newStatus, String lastPlayer, boolean isFinished) {
		final String SQL_UPDATE_GAMES_TABLE = "UPDATE " + TABLE_NAME + " SET " + 
				COLUMN_UPDATE_TIME + " = ?, " + 
				COLUMN_GAME_STATUS + " = ? WHERE " + 
				COLUMN_GAME_ID + " = ?";
		
		PreparedStatement preparedStatement;
		try {
			preparedStatement = DBManager.getDbConnection().prepareStatement(SQL_UPDATE_GAMES_TABLE);
			preparedStatement.setString(1, Long.toString(System.currentTimeMillis()));
			preparedStatement.setString(2, newStatus);
			preparedStatement.setString(3, Integer.toString(gameId));
			preparedStatement.executeUpdate();
			
			if (isFinished)
				notifyPlayersAboutGameCompletion(gameId, newStatus, lastPlayer);
			else
				notifyPlayersAboutGameUpdate(gameId, newStatus, lastPlayer);
			
			return -1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	private void notifyPlayersAboutNewGame(int gameId, 
			String gameType, 
			ArrayList<String> playersList, 
			String firstPlayer) {
		
		GCMMsgContent content = new GCMMsgContent();
		
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("ID", gameId);
			jsonObject.put("TYPE", gameType);
			jsonObject.put("PLAYERS", new JSONArray(playersList));
			jsonObject.put("FIRST_PLAYER", firstPlayer);
		
			ArrayList<String> playersRegId = getPlayersRegId(playersList);
			
			for (String regId : playersRegId) {
				content.addRegId(regId);
			}
			
			content.updateClients("NEW_GAME", jsonObject.toString());
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	private void notifyPlayersAboutGameCompletion(int gameId, String newStatus, String winner) {
		
		GCMMsgContent content = new GCMMsgContent();
		
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("ID", gameId);
			jsonObject.put("STATUS", newStatus);
			jsonObject.put("WINNER", winner);
			
			ArrayList<String> players  = getPlayersByGameId(gameId);
			ArrayList<String> playersRegId = getPlayersRegId(players);
			
			for (String regId : playersRegId) {
				content.addRegId(regId);
			}
			
			content.updateClients("GAME_COMPLETION", jsonObject.toString());
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	private void notifyPlayersAboutGameUpdate(int gameId, String newStatus, String lastPlayer) {
		
		GCMMsgContent content = new GCMMsgContent();
		
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("ID", gameId);
			jsonObject.put("STATUS", newStatus);
			ArrayList<String> players  = getPlayersByGameId(gameId);
			ArrayList<String> playersRegId = getPlayersRegId(players);
			
			for (String player : players) {
				if (!player.equals(lastPlayer)) {
					jsonObject.put("NEXT_PLAYER", player);
					break;
				}
			}
			
			for (String regId : playersRegId) {
				content.addRegId(regId);
			}
			
			content.updateClients("UPDATE_GAME", jsonObject.toString());
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public String getGameTypeByID(int gameId) {
		final String SQL_SELECT_GAME_TYPE =
				"SELECT " + COLUMN_GAME_TYPE + " " +
				"FROM " + TABLE_NAME + " " +
				"WHERE " + COLUMN_GAME_ID + " = ?";

		String gameType = null;
		
		PreparedStatement preparedStatement;
		try {
			preparedStatement = DBManager.getDbConnection().prepareStatement(SQL_SELECT_GAME_TYPE);
			preparedStatement.setString(1, Integer.toString(gameId));
			
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				gameType = rs.getString(COLUMN_GAME_TYPE);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
			
		return gameType;
	}
	
	public void loadGameDataByID(Game game, int gameId) {
		final String SQL_SELECT_GAME_DETAILS =
				"SELECT " + COLUMN_GAME_ID + ", " + COLUMN_UPDATE_TIME
				 + ", " + COLUMN_PLAYERS_LIST + ", " + COLUMN_FIRST_PLAYER
				 + ", " + COLUMN_GAME_STATUS + " " +
				"FROM " + TABLE_NAME + " " +
				"WHERE " + COLUMN_GAME_ID + " = ?";

		
		PreparedStatement preparedStatement;
		try {
			preparedStatement = DBManager.getDbConnection().prepareStatement(SQL_SELECT_GAME_DETAILS);
			preparedStatement.setString(1, Integer.toString(gameId));
			
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {	
				int gameID = rs.getInt(COLUMN_GAME_ID);
				int updateTime = rs.getInt(COLUMN_UPDATE_TIME);
				ArrayList<String> playersList = Util.fromStringToList(rs.getString(COLUMN_PLAYERS_LIST));
				String firstPlayer = rs.getString(COLUMN_FIRST_PLAYER);
				String gameStatus = rs.getString(COLUMN_GAME_STATUS);
				
				game.setGameDetails(gameID, updateTime, playersList, firstPlayer, gameStatus);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private ArrayList<String> getPlayersRegId(ArrayList<String> players) {
		ArrayList<String> playersRegId = new ArrayList<String>();
		
		for (String player : players) {
			playersRegId.add(UsersDBHelper.getInstance().getPlayerRegIdByPlayerPhone(player));
		}
		
		return playersRegId;
	}
	
	
	private ArrayList<String> getPlayersByGameId(int gameId) {
		final String SQL_SELECT_PLAYERS =
				"SELECT " + COLUMN_PLAYERS_LIST + " " +
				"FROM " + TABLE_NAME + " " +
				"WHERE " + COLUMN_GAME_ID + " = ?";

		ArrayList<String> players = new ArrayList<String>();
		
		PreparedStatement preparedStatement;
		try {
			preparedStatement = DBManager.getDbConnection().prepareStatement(SQL_SELECT_PLAYERS);
			preparedStatement.setString(1, Integer.toString(gameId));
			
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				players = Util.fromStringToList(rs.getString(COLUMN_PLAYERS_LIST));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
			
		return players;
	}
}
