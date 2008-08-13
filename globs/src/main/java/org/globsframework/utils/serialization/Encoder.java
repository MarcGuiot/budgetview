package org.globsframework.utils.serialization;

import org.globsframework.utils.exceptions.IOFailure;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Encoder {
  private Encoder() {
  }

  public static String byteToString(byte[] cryptedBytes) {
    BASE64Encoder b64 = new BASE64Encoder();
    return b64.encode(cryptedBytes);
  }

  public static byte[] stringToByte(String text) {
    try {
      BASE64Decoder b64 = new BASE64Decoder();
      return b64.decodeBuffer(text);
    }
    catch (Exception e) {
      throw new IOFailure(e);
    }
  }
}
