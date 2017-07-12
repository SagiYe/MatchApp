package sagiyehezkel.matchappserver;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import sagiyehezkel.matchappserver.handlers.BackupUserDatabaseHandler;
import sagiyehezkel.matchappserver.handlers.NewGameHandler;
import sagiyehezkel.matchappserver.handlers.RegistrationHandler;
import sagiyehezkel.matchappserver.handlers.SyncContactsHandler;
import sagiyehezkel.matchappserver.handlers.UpdateGameHandler;

public class Main {

	public static void main(String[] args) {
		// Setting up a server
        HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(48123), 0);
			server.createContext("/register", new RegistrationHandler());
			server.createContext("/sync_contacts", new SyncContactsHandler());
			server.createContext("/new_game", new NewGameHandler());
			server.createContext("/update_game", new UpdateGameHandler());
			server.createContext("/user_database_backup", new BackupUserDatabaseHandler());
			server.setExecutor(null); // a default executor
	        server.start();
	        
	        System.out.println("Server is up . . .");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
