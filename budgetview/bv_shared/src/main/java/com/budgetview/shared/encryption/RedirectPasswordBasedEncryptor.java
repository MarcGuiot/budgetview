package com.budgetview.shared.encryption;

public class RedirectPasswordBasedEncryptor implements PasswordBasedEncryptor {
  PasswordBasedEncryptor passwordBasedEncryptor;

  public RedirectPasswordBasedEncryptor() {
  }

  public byte[] encrypt(byte[] data) {
    if (passwordBasedEncryptor == null){
      throw new NullPointerException("not connected");
    }
    return passwordBasedEncryptor.encrypt(data);
  }

  public byte[] decrypt(byte[] data) {
    if (passwordBasedEncryptor == null){
      throw new NullPointerException("not connected");
    }
    return passwordBasedEncryptor.decrypt(data);
  }

  public void setPasswordBasedEncryptor(PasswordBasedEncryptor passwordBasedEncryptor) {
    this.passwordBasedEncryptor = passwordBasedEncryptor;
  }
}
