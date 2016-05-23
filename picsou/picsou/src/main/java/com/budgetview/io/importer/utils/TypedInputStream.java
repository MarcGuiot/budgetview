package com.budgetview.io.importer.utils;

import com.budgetview.io.importer.BankFileType;

import java.io.*;

public class TypedInputStream {
  private BankFileType type;
  private RepeatableInputStream stream;
  private boolean notUTF8;
  private static final String DEFAULT_ENCODING = "ISO-8859-15";
  private String fileName = "undef";
  private boolean isWindows;

  public TypedInputStream(File file) throws IOException {
    stream = new RepeatableInputStream(new FileInputStream(file));
    checkCoding();
    fileName = file.getName().toLowerCase();
    type = BankFileType.getTypeFromName(fileName);
    if (type == null) {
      findType();
    }
  }

  public TypedInputStream(InputStream inputStream) throws IOException {
    stream = new RepeatableInputStream(inputStream);
    checkCoding();
    findType();
  }

  private void findType() throws IOException {
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
    stream.reset();
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
      InputStream ignoreCR = stream;
      if (isWindowsType()) {
        ignoreCR = new RemoveCRInputStream();
      }
      if (notUTF8) {
        return new InputStreamReader(ignoreCR, DEFAULT_ENCODING);
      }
      return new InputStreamReader(ignoreCR, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      return new InputStreamReader(stream);
    }
  }

  private void checkCoding() throws IOException {
    int byt;
    notUTF8 = false;
    UTF8Detector.Coder coder = UTF8Detector.first;
    isWindows = false;
    int newLineCount = 0;
    boolean previousIsCR = false;
    while ((byt = stream.read()) != -1) {
      if (byt == '\n') {
        newLineCount++;
        if (previousIsCR) {
          isWindows = true;
        }
      }
      previousIsCR = byt == '\r';
      coder = coder.push(byt);
      if (coder == UTF8Detector.undef) {
        notUTF8 = true;
        break;
      }
    }
    while (byt != -1 && newLineCount < 10) {
      byt = stream.read();
      if (byt == '\r' && (byt = stream.read()) == '\n') {
        isWindows = true;
        break;
      }
      if (byt == '\n') {
        newLineCount++;
      }
    }
    stream.reset();
  }

  public boolean isWindowsType() {
    return isWindows;
  }

  public void close() throws IOException {
    stream.close();
  }

  private class RemoveCRInputStream extends InputStream {
    boolean previousIsCR;

    public int read() throws IOException {
      int r = stream.read();
      if (r == '\n') {
        if (previousIsCR) {
          previousIsCR = false;
          return '\n';
        }
        else {
          return ' ';
        }
      }
      previousIsCR = r == '\r';
      return r;
    }
  }
}
