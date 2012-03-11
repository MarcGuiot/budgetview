package org.designup.picsou.importer.csv;

import org.globsframework.utils.Strings;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {
  public static List<String> readLine(String line, final char sep) {
    if (line == null){
      return null;
    }
    List<String> elements = new ArrayList<String>();
    String name = "";
    for (int i = 0; i < line.length(); ++i) {
      char c = line.charAt(i);
      if (c == sep) {
        if (Strings.isNotEmpty(name)) {
          elements.add(normalize(name));
        }
        else {
          elements.add("");
        }
        name = "";
      }
      else {
        name += c;
      }
    }
    elements.add(normalize(name));
    return elements;
  }

  private static String normalize(String name) {
    return name.replaceAll("^\"(.*)\"$", "$1");
  }

  public static char findSeparator(BufferedReader reader) {
    try {
      String line = reader.readLine();
      int tabs = 0;
      int semicolons = 0;
      int commas = 0;
      int colons = 0;
      for (int i = 0; i < line.length(); i++) {
        char c = line.charAt(i);
        if (c == '\t') {
          tabs++;
        }
        if (c == ';') {
          semicolons++;
        }
        if (c == ',') {
          commas++;
        }
        if (c == ':') {
          colons++;
        }
      }
      if (tabs > 1) {
        return '\t';
      }
      else if (semicolons > 1) {
        return ';';
      }
      else if (commas > 1) {
        return ',';
      }
      else if (colons > 1) {
        return ':';
      }
    }
    catch (Exception e) {
    }
    return 0;
  }
}
