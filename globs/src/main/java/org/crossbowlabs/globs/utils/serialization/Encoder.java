package org.crossbowlabs.globs.utils.serialization;

import sun.misc.BASE64Encoder;

public class Encoder {
  private Encoder() {
  }

  public static String b64Decode(byte[] cryptedBytes) {
    BASE64Encoder b64 = new BASE64Encoder();
    return b64.encode(cryptedBytes);
  }
}
