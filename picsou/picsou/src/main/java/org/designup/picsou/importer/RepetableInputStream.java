package org.designup.picsou.importer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RepetableInputStream extends InputStream {
  private StreamPart firstStreamPart;
  private StreamPart currentPart;
  private InputStream currentInputStream;

  private static class StreamPart {
    private byte[] bytes = new byte[1024];
    private StreamPart nextStreamPart;

    public StreamPart(InputStream stream) throws IOException {
      int readCount = stream.read(bytes);
      if (readCount == -1) {
        bytes = new byte[0];
        return;
      }
      else if (readCount != bytes.length) {
        byte[] tmp = new byte[readCount];
        System.arraycopy(bytes, 0, tmp, 0, readCount);
        bytes = tmp;
      }
      nextStreamPart = new StreamPart(stream);
      if (nextStreamPart.bytes.length == 0) {
        nextStreamPart = null;
      }
    }
  }


  public RepetableInputStream(InputStream stream) throws IOException {
    firstStreamPart = readPart(stream);
    init();
  }

  private void init() {
    currentPart = firstStreamPart;
    currentInputStream = new ByteArrayInputStream(currentPart.bytes);
  }

  private StreamPart readPart(InputStream stream) throws IOException {
    return new StreamPart(stream);
  }

  public void reset() {
    init();
  }

  public int read() throws IOException {
    if (currentPart == null) {
      return -1;
    }
    if (currentInputStream.available() != 0) {
      return currentInputStream.read();
    }
    currentPart = currentPart.nextStreamPart;
    if (currentPart == null) {
      return -1;
    }
    currentInputStream = new ByteArrayInputStream(currentPart.bytes);
    return read();
  }

  public int available() throws IOException {
    if  (currentPart == null){
      return 0;
    }
    if (currentInputStream != null) {
      return currentInputStream.available();
    }
    return 0;
  }
}
