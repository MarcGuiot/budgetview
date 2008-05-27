package org.uispec4j.utils;

import org.uispec4j.assertion.dependency.InternalAssert;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class Utils {
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

  /**
   * Compare two objects in the case where both can be null
   */
  public static boolean equals(Object o1, Object o2) {
    if ((o1 == null) && (o2 == null)) {
      return true;
    }
    if (o1 != null) {
      return o1.equals(o2);
    }
    else {
      return o2.equals(o1);
    }
  }

  public static String normalize(String input, int size) {
    StringBuffer buffer = new StringBuffer();
    if (size < 1) {
      return "";
    }
    if (input.length() > size) {
      return input.substring(0, size - 1);
    }
    buffer.append(input);
    int blankCount = size - input.length();
    for (int i = 0; i < blankCount; i++) {
      buffer.append(" ");
    }
    return buffer.toString();
  }

  public static void sleep(long time) {
    try {
      Thread.sleep(time);
    }
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void assertEquals(Object[] expected, Object[] actual) {
    assertEquals(expected, actual, Stringifier.NULL);
  }

  public static void assertEquals(Object[] expected, Object[] actual, Stringifier stringifier) {
    assertSetEquals(expected, actual, stringifier);

    for (int i = 0; i < actual.length; i++) {
      InternalAssert.assertTrue("Unexpected order in the collection" + getMessage(expected, actual, stringifier),
                                expected[i].equals(actual[i]));
    }
  }

  public static void assertSetEquals(Object[] expected, Object[] actual, Stringifier stringifier) {
    int expectedLength = expected.length;
    int actualLength = actual.length;
    InternalAssert.assertTrue(expectedLength + " elements instead of " + actualLength + getMessage(expected, actual, stringifier),
                              expectedLength == actualLength);

    List list = Arrays.asList(expected);
    for (int i = 0; i < actual.length; i++) {
      Object element = actual[i];
      InternalAssert.assertTrue("Unexpected element '" + stringifier.toString(element) + "'" + getMessage(expected, actual, stringifier),
                                list.contains(element));
    }
  }

  private static String getMessage(Object[] expected, Object[] actual, Stringifier stringifier) {
    return new StringBuffer("\nExpected: ")
      .append(stringify(expected, stringifier))
      .append(",\nbut was: ")
      .append(stringify(actual, stringifier))
      .toString();
  }

  public static String stringify(Object[] objects, Stringifier stringifier) {
    StringBuffer buffer = new StringBuffer("[");
    for (int i = 0; i < objects.length; i++) {
      buffer.append(stringifier.toString(objects[i])).append((i == objects.length - 1) ? "]" : ",");
    }
    return buffer.toString();
  }

  public static void waitForPendingAwtEventsToBeProcessed() throws Exception {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
        }
      });
    }
  }
}
