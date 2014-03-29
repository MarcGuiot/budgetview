package org.globsframework.utils;

import junit.framework.TestCase;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public class TablePrinterTest extends TestCase {
  private StringWriter writer = new StringWriter();

  public void testStandardUsage() throws Exception {
    TablePrinter table = new TablePrinter(true);
    table.setHeader("t1", "t2", "t3");
    table.addRow("Value 1", 22.0, "Item 3");
    table.addRow(1.1111, "v2", "33");
    assertEquals("| t1      | t2    | t3     |\n" +
                 "| 1.11    | v2    | 33     |\n" +
                 "| Value 1 | 22.00 | Item 3 |\n",
                 table.toString());
  }

  public void testNoHeader() throws Exception {
    TablePrinter table = new TablePrinter(true);
    table.addRow("Value 1", 22.0, "Item 3");
    assertEquals("| Value 1 | 22.00 | Item 3 |\n",
                 table.toString());
  }

  public void testStaticPrint() throws Exception {
    TablePrinter.print(new Object[]{"Title 1", "Title 2"},
                       Arrays.asList(
                         new Object[]{"first value", "second value"},
                         new Object[]{"a", "b"}
                       ),
                       true, new PrintWriter(writer));

    assertEquals("| Title 1     | Title 2      |" + Strings.LINE_SEPARATOR +
                 "| a           | b            |" + Strings.LINE_SEPARATOR +
                 "| first value | second value |" + Strings.LINE_SEPARATOR,
                 writer.toString());
  }

  public void testRowsMustNotBeLargerThanTheHeader() throws Exception {
    try {
      TablePrinter.print(new Object[]{"Title 1", "Title 2"},
                         Arrays.asList(
                           new Object[]{"first value", "second value",},
                           new Object[]{"a", "b", "c"}
                         ),
                         true, new PrintWriter(writer));
    }
    catch (InvalidParameter e) {
      assertEquals("Row larger than the first row: [a, b, c]", e.getMessage());
    }

  }
}
