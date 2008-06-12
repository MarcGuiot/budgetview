package org.designup.picsou.importer.utils;

import org.designup.picsou.importer.utils.RepeatableInputStream;
import org.designup.picsou.importer.utils.UTF8Detector;
import org.designup.picsou.importer.BankFileType;

import java.io.*;

public class TypedInputStream {
  private BankFileType type;
  private RepeatableInputStream stream;
  private boolean notUTF8;
  private static final String DEFAULT_ENCODING = "ISO-8859-15";
  private String fileName = "undef";

  public TypedInputStream(File file) throws IOException {
    stream = new RepeatableInputStream(new FileInputStream(file));
    checkCoding();
    fileName = file.getName().toLowerCase();
    if (fileName.endsWith(".ofx")) {
      type = BankFileType.OFX;
    }
    else if (fileName.endsWith(".qif")) {
      type = BankFileType.QIF;
    }
  }

  public TypedInputStream(InputStream inputStream) throws IOException {
    stream = new RepeatableInputStream(inputStream);
    checkCoding();

    Reader reader = new InputStreamReader(stream);
    BufferedReader bufferedReader = new BufferedReader(reader);
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      String loLine = line.trim().toLowerCase();
      if (loLine.startsWith("!type:bank")) {
        type = BankFileType.QIF;
        break;
      }
      if (loLine.startsWith("<ofx>")) {
        type = BankFileType.OFX;
        break;
      }
    }
  }

  public RepeatableInputStream getRepetableStream() {
    return stream;
  }

  public BankFileType getType() {
    return type;
  }

  public boolean isNotUTF8() {
    return notUTF8;
  }

  public String getName() {
    return fileName;
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
