package sagiyehezkel.matchappserver.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
	private static final String DATABASE_ASSRESS = "jdbc:sqlite:"
			+ System.getProperty("user.home")
			+ "/Documents/GitHub/MatchApp/Server/database"
			+ "/matchapp_server_database.db";
	static Connection connection = null;
	
	public static Connection getDbConnection() {
		if (connection == null)
			try {
				connection = DriverManager.getConnection(DATABASE_ASSRESS);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return connection;
	}
}
