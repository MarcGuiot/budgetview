package com.budgetview.shared.utils;

import org.globsframework.utils.Files;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.zip.GZIPInputStream;

public class Crypt {
  private PBEParameterSpec spec;
  private SecretKey secretKey;

  public Crypt(final char[] password, String salt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
    spec = new PBEParameterSpec(salt.getBytes(), 5);
    PBEKeySpec keySpec = new PBEKeySpec(password);
    secretKey = factory.generateSecret(keySpec);
  }

  public String decodeAndUnzipData(final byte[] bytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException {
    Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
    cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
    byte[] decoded = cipher.doFinal(bytes);
    GZIPInputStream unzipInputStream = new GZIPInputStream(new ByteArrayInputStream(decoded));
    return Files.loadStreamToString(unzipInputStream, "UTF-8");
  }

  public byte[] encodeData(final byte[] bytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException {
    Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
    return cipher.doFinal(bytes);
  }

  public static String encodeSHA1AndHex(byte[] bytes){
    try {
      MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
      digest.update(bytes, 0, bytes.length);
      byte[] result = digest.digest();
      return encodeHex(result);
    }
    catch (Exception e) {
      throw new RuntimeException("In SHA1", e);
    }
  }

  // code from common-codex Hex class

  private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

  protected static String encodeHex(byte[] data) {
    int l = data.length;
    char[] out = new char[l << 1];
    // two characters form the hex value.
    for (int i = 0, j = 0; i < l; i++) {
      out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
      out[j++] = DIGITS_LOWER[0x0F & data[i]];
    }
    return new String(out);
  }

}
