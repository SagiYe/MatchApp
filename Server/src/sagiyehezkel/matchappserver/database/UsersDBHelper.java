package sagiyehezkel.matchappserver.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UsersDBHelper {
	public static final String TABLE_NAME = "USERS";
	public static final String COLUMN_PHONE_NUM = "PHONE";
	public static final String COLUMN_REG_ID = "REGID";
	
	private static UsersDBHelper instance = null;
	
	public static UsersDBHelper getInstance() {
		if (instance == null)
			instance = new UsersDBHelper();
		
		return instance;
	}
	
	private UsersDBHelper() {
		makeSureTableExists();
	}
	
	private void makeSureTableExists() {
		final String SQL_CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" 
				+ COLUMN_PHONE_NUM + " TEXT PRIMARY KEY,"
				+ COLUMN_REG_ID + " TEXT NOT NULL"
				+ ")";
		
		PreparedStatement preparedStatement;
		
		try {
			preparedStatement = DBManager.getDbConnection().prepareStatement(SQL_CREATE_USERS_TABLE);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void addNewUser(String phone, String regid) {
		final String SQL_INSERT_INTO_USERS_TABLE = "INSERT OR REPLACE INTO " + TABLE_NAME + " "
				+ "(" + COLUMN_PHONE_NUM + ", " + COLUMN_REG_ID + ") VALUES "
				+ "(?,?)";
		
		PreparedStatement preparedStatement;
		try {
			preparedStatement = DBManager.getDbConnection().prepareStatement(SQL_INSERT_INTO_USERS_TABLE);
			preparedStatement.setString(1, phone);
			preparedStatement.setString(2, regid);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getRegisterdUsersFromContactsList(ArrayList<String> userContacts) {
		final String HELPER_TABLE_NAME = "SYNC_CONT_HELPER";

		final String SQL_CREATE_HELPER_TABLE = "CREATE TABLE IF NOT EXISTS " + HELPER_TABLE_NAME + " (" + 
				COLUMN_PHONE_NUM + " TEXT NOT NULL, " +
				"FOREIGN KEY (" + COLUMN_PHONE_NUM + ") REFERENCES " + TABLE_NAME + "(" + COLUMN_PHONE_NUM +")" +
				")";
		
		final String SQL_CLEAR_HELPER_TABLE = "DELETE FROM " + HELPER_TABLE_NAME;
		
		final String SQL_INSERT_INTO_HELPER_TABLE = "INSERT INTO " + HELPER_TABLE_NAME + " (" + COLUMN_PHONE_NUM + ") VALUES (?)";
		
		final String SQL_SELECT_FROM_USERS_INTERSECT_WITH_HELPER = 
				"SELECT " + COLUMN_PHONE_NUM + 
				" FROM " + TABLE_NAME + 
				" WHERE " + COLUMN_PHONE_NUM + " IN (SELECT " + COLUMN_PHONE_NUM + " FROM " + HELPER_TABLE_NAME + ");";
		
		ArrayList<String> registerdContacts = new ArrayList<>();
		
		Connection connection;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			connection = DBManager.getDbConnection();
			
			connection.setAutoCommit(false);
			
			// Make sure the helper table exists
			connection.createStatement().execute(SQL_CREATE_HELPER_TABLE);
			connection.commit();
			
			// Make sure the helper table empty
			connection.createStatement().execute(SQL_CLEAR_HELPER_TABLE);
			connection.commit();
			
			preparedStatement = connection.prepareStatement(SQL_INSERT_INTO_HELPER_TABLE);

			for (String phone : userContacts) {
			    preparedStatement.setString(1, phone);
			    preparedStatement.execute();
			}
			
			connection.commit();
			connection.setAutoCommit(true);
			
			preparedStatement = connection.prepareStatement(SQL_SELECT_FROM_USERS_INTERSECT_WITH_HELPER);
			rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				registerdContacts.add(rs.getString(COLUMN_PHONE_NUM));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
			}
			if (preparedStatement != null) {
				try { preparedStatement.close(); } catch (SQLException e) { e.printStackTrace(); }
			}
		}
		
		return registerdContacts;
	}
	
	public String getPlayerRegIdByPlayerPhone(String playerPhoneNum) {
		final String SQL_SELECT_PLAYER_REG_ID =
				"SELECT " + COLUMN_REG_ID + " " +
				"FROM " + TABLE_NAME + " " +
				"WHERE " + COLUMN_PHONE_NUM + " = ?";

		String playerRegId = null;
		
		PreparedStatement preparedStatement;
		try {
			preparedStatement = DBManager.getDbConnection().prepareStatement(SQL_SELECT_PLAYER_REG_ID);
			preparedStatement.setString(1, playerPhoneNum);
			
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				playerRegId = rs.getString(COLUMN_REG_ID);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
			
		return playerRegId;
	}
}
