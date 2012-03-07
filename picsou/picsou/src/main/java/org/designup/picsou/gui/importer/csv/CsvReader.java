package org.designup.picsou.gui.importer.csv;

import org.globsframework.utils.Strings;

import java.util.ArrayList;
import java.util.List;

public class CsvReader {
  public static List<String> readLine(String line, final char sep) {
    if (line == null){
      return null;
    }
    String name = "";
    List<String> elements = new ArrayList<String>();
    for (int i = 0; i < line.length(); ++i) {
      char c = line.charAt(i);
      if (c == sep) {
        if (Strings.isNotEmpty(name)) {
          elements.add(name);
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
    elements.add(name);
    return elements;
  }
}
