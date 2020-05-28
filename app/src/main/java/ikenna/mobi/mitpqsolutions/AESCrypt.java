package ikenna.mobi.mitpqsolutions;

import android.util.Base64;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESCrypt
{
public static final String ALGORITHM = "AES";
public static final String KEY = "2Icei778begEFI89";
public static String Encrypt(String value) throws Exception
{
    Key key = generateKey();
    Cipher cipher = Cipher.getInstance(AESCrypt.ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE,key);
    byte[] cipherText =  cipher.doFinal(value.getBytes("utf-8"));
    String cipherText64 = Base64.encodeToString(cipherText, Base64.DEFAULT);
    return cipherText64;
}
public static String Decrypt(String cipherText) throws Exception
{
    Key key = generateKey();
    Cipher cipher = Cipher.getInstance(AESCrypt.ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE,key);
    byte[] sourcefrom64 = Base64.decode(cipherText,Base64.DEFAULT);
    byte[] decryptedByteValue = cipher.doFinal(sourcefrom64);
    String decryptedText = new String(decryptedByteValue,"utf-8");
    return decryptedText;
}

public static Key generateKey() throws Exception
{
  return new SecretKeySpec(AESCrypt.KEY.getBytes(),AESCrypt.ALGORITHM);
}
}
