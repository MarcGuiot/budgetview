package org.globsframework.utils;

import org.globsframework.utils.exceptions.InvalidParameter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TablePrinter {

  private TablePrinter() {
  }

  public static String print(Object[] headerRow, List<Object[]> rows) {
    StringWriter writer = new StringWriter();
    print(headerRow, rows, new PrintWriter(writer));
    return writer.toString();
  }

  public static void print(Object[] headerRow, List<Object[]> rows, PrintWriter printer) {
    int[] sizes = new int[headerRow.length];
    Arrays.fill(sizes, 0);

    for (Object[] row : rows) {
      if (row.length > headerRow.length) {
        throw new InvalidParameter("Row larger than the header row: " + Arrays.toString(row));
      }
    }

    updateSizes(headerRow, sizes);
    for (Object[] row : rows) {
      updateSizes(row, sizes);
    }

    List<String> strings = new ArrayList<String>();
    for (Object[] row : rows) {
      strings.add(toString(row, sizes));
    }

    printer.println(toString(headerRow, sizes));
    for (String string : Utils.sort(strings)) {
      printer.println(string);
    }
  }

  private static void updateSizes(Object[] row, int[] sizes) {
    int index = 0;
    for (Object cell : row) {
      String str = toString(cell);
      sizes[index] = Math.max(sizes[index], str.length());
      index++;
    }
  }

  private static String toString(Object[] row, int[] sizes) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < row.length; i++) {
      builder.append("| ");
      String string = toString(row[i]);
      builder.append(string);
      int space = sizes[i] - string.length();
      for (int j = 0; j < space; j++) {
        builder.append(' ');
      }
      builder.append(' ');
    }
    builder.append('|');
    return builder.toString();
  }

  private static String toString(Object cell) {
    if (cell == null) {
      return "";
    }
    if (cell instanceof Double) {
      return new DecimalFormat("#.00", new DecimalFormatSymbols(Locale.US)).format(cell);
    }
    return cell.toString();
  }
}
