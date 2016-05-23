package com.budgetview.io.importer.csv;

import com.budgetview.gui.importer.csv.CsvSeparator;
import com.budgetview.io.importer.csv.utils.InvalidCsvFileFormat;
import org.globsframework.utils.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class CsvReader {
  private static final Pattern DATE_PATTERN = Pattern.compile("[0-9]{2,4}[\\.\\-/][0-9]{2,4}[\\.\\-/][0-9]{2,4}");
  private static final Pattern PATTERN_NORMALIZE = Pattern.compile("^\"(.*)\"$");

  public static CsvSeparator findSeparator(String firstLine) throws InvalidCsvFileFormat {
    List<CsvSeparatorCounter> counters = new ArrayList<CsvSeparatorCounter>();
    for (CsvSeparator separator : CsvSeparator.values()) {
      counters.add(new CsvSeparatorCounter(separator));
    }

    for (int i = 0; i < firstLine.length(); i++) {
      char c = firstLine.charAt(i);
      for (CsvSeparatorCounter counter : counters) {
        if (c == counter.separator.getSeparator()) {
          counter.increment();
        }
      }
    }

    Collections.sort(counters);

    List<CsvSeparator> separators = new ArrayList<CsvSeparator>();
    for (CsvSeparatorCounter counter : counters) {
      if (counter.count > 1) {
        separators.add(counter.separator);
      }
    }

    if (separators.isEmpty()) {
      throw new InvalidCsvFileFormat("Could not find separator for line: " + firstLine);
    }

    return separators.get(0);
  }

  public static List<String> parseLine(String line, CsvSeparator separator) {
    if (line == null) {
      return null;
    }
    List<String> elements = new ArrayList<String>();
    String name = "";
    boolean startQuote = false;
    boolean first = true;
    for (int i = 0; i < line.length(); ++i) {
      char c = line.charAt(i);
      if (c == '\"' && (first || startQuote)) {
        if (first) {
          startQuote = true;
        }
        else {
          startQuote = false;
        }
        first = false;
      }
      else if (c == separator.getSeparator() && !startQuote) {
        if (Strings.isNotEmpty(name)) {
          elements.add(normalizeItem(name));
        }
        else {
          elements.add("");
        }
        name = "";
        startQuote = false;
        first = true;
      }
      else {
        name += c;
        first = false;
      }
    }
    elements.add(normalizeItem(name));
    return elements;
  }

  private static String normalizeItem(String name) {
    return PATTERN_NORMALIZE.matcher(name).replaceAll("$1");
  }

  public static TextType getTextType(String str) {
    if (Strings.isNullOrEmpty(str)) {
      return null;
    }
    str = str.replaceAll("\\s", "");
    if (DATE_PATTERN.matcher(str).matches()) {
      return TextType.DATE;
    }
    int count = 0;
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (c >= '0' && c <= '9' || c == '.' || c == ',' || c == '-' || c == '+') {
        count++;
      }
    }
    if (count == str.length()) {
      return TextType.NUMBER;
    }
    return TextType.TEXT;
  }

  private static class CsvSeparatorCounter implements Comparable<CsvSeparatorCounter> {
    private final CsvSeparator separator;
    private int count;

    private CsvSeparatorCounter(CsvSeparator separator) {
      this.separator = separator;
    }

    public void increment() {
      count++;
    }

    public int compareTo(CsvSeparatorCounter other) {
      return Integer.signum(other.count - count);
    }
  }

  public enum TextType {
    DATE,
    NUMBER,
    TEXT
  }
}
