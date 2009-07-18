package org.designup.picsou.client.http;

import org.globsframework.utils.exceptions.GlobsException;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.model.GlobList;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.SerializableGlobSerializer;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.designup.picsou.server.serialization.SerializationManager;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class MD5PasswordBasedEncryptor implements PasswordBasedEncryptor{
  private SecretKey secretKey;
  private Cipher cipher;
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
      cipher = Cipher.getInstance("PBEWithMD5AndDES");
    }
    catch (InvalidKeySpecException e) {
      throw new EncryptFail(e);
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public synchronized static void init() throws NoSuchAlgorithmException {
    if (factory == null){
      factory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
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