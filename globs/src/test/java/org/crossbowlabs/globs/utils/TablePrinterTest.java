package org.crossbowlabs.globs.utils;

import junit.framework.TestCase;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public class TablePrinterTest extends TestCase {
  private StringWriter writer = new StringWriter();

  public void test() throws Exception {
    TablePrinter.print(new Object[]{"Title 1", "Title 2"},
                       Arrays.asList(
                         new Object[]{"first value", "second value"},
                         new Object[]{"a", "b"}
                       ),
                       new PrintWriter(writer));

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
                         new PrintWriter(writer));
    }
    catch (InvalidParameter e) {
      assertEquals("Row larger than the header row: [a, b, c]", e.getMessage());
    }

  }
}
