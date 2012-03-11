package org.globsframework.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ReplacementInputStream extends InputStream {
  Map<byte[], byte[]> replace = new HashMap<byte[], byte[]>();
  private ByteArrayInputStream stream;

  public ReplacementInputStream(ByteArrayInputStream stream) {
    this.stream = stream;
  }

  public int read() throws IOException {
    return 0;
  }

  public void replace(String from, String to) {
    byte[] fromBytes = from.getBytes();
    replace.put(fromBytes, to.getBytes());
  }

  public interface State {

  }
}
