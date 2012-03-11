package org.designup.picsou.utils;

import junit.framework.TestCase;
import org.globsframework.utils.TestUtils;

import java.io.FileWriter;
import java.io.IOException;

public class CsvBuilder {
  String fileName;
  private char separator;
  private final FileWriter writer;

  public static CsvBuilder init(TestCase test, char separator) {
    return new CsvBuilder(test, separator);
  }

  private CsvBuilder(TestCase test, char separator) {
    this.separator = separator;
    fileName = TestUtils.getFileName(test, ".csv");
    try {
      writer = new FileWriter(fileName);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public CsvBuilder add(String... headers) {

    try {
      for (int i = 0, length = headers.length; i < length; i++) {
        String header = headers[i];
        writer.append(header);
        if (i < length - 1) {
          writer.append(separator);
        }
      }
      writer.append("\n");
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  public String getFile() {
    try {
      writer.close();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    return fileName;
  }

  public CsvBuilder addEmpty() {
    try {
      writer.append("\n");
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    return this;
  }
  
}
