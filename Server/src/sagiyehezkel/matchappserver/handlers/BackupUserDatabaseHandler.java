package sagiyehezkel.matchappserver.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import sagiyehezkel.matchappserver.Util;
import sagiyehezkel.matchappserver.database.GamesDBHelper;
import sagiyehezkel.matchappserver.security.AdvancedEncryptionStandard;

public class BackupUserDatabaseHandler extends Handler {

	private static final String JSON_FIELD_NAME_USER = "USER";
	private static final String JSON_FIELD_NAME_DATABASE = "DATABASE";
	private static final String JSON_FIELD_NAME_MD5 = "MD5";
	
	@Override
	protected String getHandlerName() {
		return "BackupUserDatabaseHandler";
	}
	
	@Override
	protected String handleMsg(String request) {
		try {
			
			
			System.out.println(request);
			
			
			JSONObject requestJson = new JSONObject(request);
			String user = requestJson.getString(JSON_FIELD_NAME_USER);
			String md5 = requestJson.getString(JSON_FIELD_NAME_MD5);
			byte[] fileAsBytes = Util.fromJsonArrayToByteArray(requestJson.getJSONArray(JSON_FIELD_NAME_DATABASE));
			
			System.out.println(user);
			System.out.println(md5);
			System.out.println(new String(fileAsBytes));
			
			Path path = Paths.get(System.getProperty("user.home")
			+ "/Documents/GitHub/MatchApp/Server/database/"
			+ user + ".db");
			
			Files.write(path, fileAsBytes);
			
			String fileMd5 = calculateMD5(path.toFile());
			
			if (fileMd5.equals(md5))
				System.out.println("MD5 Checksum Success!!");
			else {
				System.out.println("MD5 Checksum Failure!!\nBackup failed...");
				path.toFile().delete();
			}
			
//			GamesDBHelper.getInstance().addNewGame(gameType, playersList);
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
		return null;
	}
	
	private String calculateMD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception while getting digest");
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
        	System.out.println("Exception while getting FileInputStream");
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            	System.out.println("Exception on closing MD5 input stream");
            }
        }
    }
}
