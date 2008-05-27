package org.designup.picsou.importer;

import java.io.*;

public class TypedInputStream {
  private TYPE type;
  private RepetableInputStream stream;
  private boolean notUTF8;
  private static final String DEFAULT_ENCODING = "ISO-8859-15";
  private String fileName = "undef";

  public TypedInputStream(File file) throws IOException {
    stream = new RepetableInputStream(new FileInputStream(file));
    checkCoding();
    fileName = file.getName().toLowerCase();
    if (fileName.endsWith(".ofx")) {
      type = TYPE.ofx;
    }
    else if (fileName.endsWith(".qif")) {
      type = TYPE.qif;
    }
  }

  public TypedInputStream(InputStream inputStream) throws IOException {
    stream = new RepetableInputStream(inputStream);
    checkCoding();

    Reader reader = new InputStreamReader(stream);
    BufferedReader bufferedReader = new BufferedReader(reader);
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      String loLine = line.trim().toLowerCase();
      if (loLine.startsWith("!type:bank")) {
        type = TYPE.qif;
        break;
      }
      if (loLine.startsWith("<ofx>")) {
        type = TYPE.ofx;
        break;
      }
    }
  }

  public RepetableInputStream getRepetableStream() {
    return stream;
  }

  public TYPE getType() {
    return type;
  }

  public boolean isNotUTF8() {
    return notUTF8;
  }

  public String getName() {
    return fileName;
  }

  enum TYPE {
    ofx,
    qif
  }

  public Reader getBestProbableReader() {
    stream.reset();
    try {
      if (notUTF8) {
        return new InputStreamReader(stream, DEFAULT_ENCODING);
      }
      return new InputStreamReader(stream, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return new InputStreamReader(stream);
    }
  }

  private void checkCoding() throws IOException {
    int byt;
    notUTF8 = false;
    UTF8Detector.Coder coder = UTF8Detector.first;
    while ((byt = stream.read()) != -1) {
      coder = coder.push(byt);
      if (coder == UTF8Detector.undef) {
        notUTF8 = true;
        break;
      }
    }

    stream.reset();
  }
}
