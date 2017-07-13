package sagiyehezkel.matchappserver.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.nio.charset.StandardCharsets;
import third.part.android.util.Base64;

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
	        byte[] plainTextByteArr = plainStr.getBytes();
	        SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
	        Cipher cipher = Cipher.getInstance(ALGORITHM);
	        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

			byte[] encryptByteArr = cipher.doFinal(plainTextByteArr);
			String encodedStr = Base64.encodeToString(encryptByteArr, Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);

	        return encodedStr;
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
			byte[] cipherText = Base64.decode(cipherStr, Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);
			SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);

	        return new String(cipher.doFinal(cipherText));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
}