package com.budgetview.client.http;

import org.globsframework.utils.exceptions.GlobsException;

public interface PasswordBasedEncryptor {
  public static class EncryptFail extends GlobsException {

    public EncryptFail(Exception cause) {
      super(cause);
    }
  }

  public byte[] encrypt(byte[] data);

  public byte[] decrypt(byte[] data);

}
