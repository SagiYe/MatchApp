package sagiyehezkel.matchappserver.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Created by Sagi on 08/07/2017.
 */
public class AdvancedEncryptionStandard {
	private static final String DEFAULT_KEY = "YINON&SAGI123456"; // MUST be length 16 
	private static final String ALGORITHM = "AES";

    private byte[] key;
    
    public AdvancedEncryptionStandard() {
    	this(DEFAULT_KEY);
    }
    
    public AdvancedEncryptionStandard(String key) {
        setKey(key);
    }
    
    public void setKey(String key) {
    	if (key.length() != 16)
    		key = DEFAULT_KEY;
    	this.key = key.getBytes();
    }

    /**
     * Encrypts the given plain text
     *
     * @param plainStr The plain text to encrypt
     */
    public String encrypt(String plainStr) {
    	try {
	        byte[] plainText = plainStr.getBytes(StandardCharsets.UTF_8);
	        SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
	        Cipher cipher = Cipher.getInstance(ALGORITHM);
	        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
	
	        return new String(
	        		Base64.getUrlEncoder().encode(cipher.doFinal(plainText))
	        		, StandardCharsets.UTF_8);
	    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }

    /**
     * Decrypts the given byte array
     *
     * @param cipherStr The data to decrypt
     */
    public String decrypt(String cipherStr) {
		try {
			String rectifiedString = cipherStr.replace("\\","");
			
			byte[] cipherText = Base64.getUrlDecoder().decode(rectifiedString.getBytes(StandardCharsets.UTF_8));
			SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);

	        return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
}
