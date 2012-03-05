package org.designup.picsou.utils;

import junit.extensions.TestSetup;
import junit.framework.TestCase;
import org.designup.picsou.functests.CsvImportTest;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;

import java.io.FileWriter;
import java.io.IOException;

public class CsvBuilder {
  String fileName;
  private String separator;
  private final FileWriter writer;

  public CsvBuilder(TestCase test, String separator) {
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
        if (i < length - 1){
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
  
  public String getFile(){
    try {
      writer.close();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    return fileName;
  }
}
