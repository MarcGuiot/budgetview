package org.designup.picsou.client.http;

import org.crossbowlabs.globs.utils.exceptions.GlobsException;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.spec.InvalidKeySpecException;

public class PasswordBasedEncryptor {
  private SecretKey secretKey;
  private Cipher cipher;
  private PBEParameterSpec spec;

  public static class EncryptFail extends GlobsException {

    public EncryptFail(Exception cause) {
      super(cause);
    }
  }

  public PasswordBasedEncryptor(byte[] salt, char[] password, int count) throws EncryptFail {
    try {
      spec = new PBEParameterSpec(salt, count);
      PBEKeySpec keySpec = new PBEKeySpec(password);
      SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
      secretKey = secretKeyFactory.generateSecret(keySpec);
      cipher = Cipher.getInstance("PBEWithMD5AndDES");
    }
    catch (InvalidKeySpecException e) {
      throw new EncryptFail(e);
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
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
