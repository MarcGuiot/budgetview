package com.budgetview.client.http;

import org.globsframework.utils.Log;
import org.globsframework.utils.exceptions.GlobsException;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class MD5PasswordBasedEncryptor implements PasswordBasedEncryptor {
  private SecretKey secretKey;
  private static Cipher cipher;
  private PBEParameterSpec spec;
  private static SecretKeyFactory factory;

  public static class EncryptFail extends GlobsException {

    public EncryptFail(Exception cause) {
      super(cause);
    }
  }

  public MD5PasswordBasedEncryptor(byte[] salt, char[] password, int count) throws EncryptFail {
    try {
      init();
      spec = new PBEParameterSpec(salt, count);
      PBEKeySpec keySpec = new PBEKeySpec(password);
      secretKey = factory.generateSecret(keySpec);
    }
    catch (InvalidKeySpecException e) {
      throw new EncryptFail(e);
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public synchronized static void init() throws NoSuchAlgorithmException, NoSuchPaddingException {
    if (factory == null) {
      long start = System.currentTimeMillis();
      factory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
      cipher = Cipher.getInstance("PBEWithMD5AndDES");
      long stop = System.currentTimeMillis();
      long duration = stop - start;
      if (duration > 1000) {
        Log.write("Take too long to compute MD5 : " + duration + "ms");
      }
    }
  }

  public byte[] encrypt(byte[] data) {
    try {
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
      return cipher.doFinal(data);
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public byte[] decrypt(byte[] data) {
    try {
      cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
      return cipher.doFinal(data);
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

}