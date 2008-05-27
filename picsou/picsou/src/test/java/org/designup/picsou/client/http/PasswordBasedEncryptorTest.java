package org.designup.picsou.client.http;

import junit.framework.TestCase;

public class PasswordBasedEncryptorTest extends TestCase {
  private static final String SOME_DATA = "some data";

  public void test() throws Exception {
    PasswordBasedEncryptor passwordBasedEncryptor = new PasswordBasedEncryptor(new byte[]{0x43, 0x34, 0x43, 0x34, 0x43, 0x34, 0x43, 0x34},
                                                                               "password".toCharArray(), 20);
    byte[] bytes = passwordBasedEncryptor.encrypt(SOME_DATA.getBytes());
    assertFalse(SOME_DATA.equals(new String(bytes)));
    byte[] decrypted = passwordBasedEncryptor.decrypt(bytes);
    assertEquals(SOME_DATA, new String(decrypted));
  }
}
