package app.newt.id.helper;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;


/**
 * Created by Erick Sumargo on 2/19/2018.
 */

public class AES {
    private static String CIPHER_NAME = "AES/CBC/PKCS5PADDING";

    private static String KEY = "&AqV0EzqI2$(@Ukz2p0OjU3MlHaDb_R_";
    private static String IV = "#aQkh)Z2HW9-Hx1e";

    public static String encrypt(String data) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(AES.IV.getBytes("UTF-8"));
            SecretKeySpec secretKey = new SecretKeySpec(AES.KEY.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance(AES.CIPHER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] encryptedData = cipher.doFinal((data.getBytes()));

            String encryptedDataInBase64 = Base64.encodeToString(encryptedData, Base64.DEFAULT);
            String ivInBase64 = Base64.encodeToString(AES.IV.getBytes("UTF-8"), Base64.DEFAULT);

            return encryptedDataInBase64 + ":" + ivInBase64;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String decrypt(String data) {
        try {
            String[] parts = data.split(":");

            IvParameterSpec iv = new IvParameterSpec(Base64.decode(parts[1], Base64.DEFAULT));
            SecretKeySpec secretKey = new SecretKeySpec(AES.KEY.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance(AES.CIPHER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

            byte[] decodedEncryptedData = Base64.decode(parts[0], Base64.DEFAULT);

            byte[] original = cipher.doFinal(decodedEncryptedData);

            return new String(original);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}