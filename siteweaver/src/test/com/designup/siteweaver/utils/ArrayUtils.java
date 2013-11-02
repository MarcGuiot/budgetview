package com.designup.siteweaver.utils;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import java.util.*;

public class ArrayUtils {

  public static String toString(Object[] objects) {
    StringBuffer buffer = new StringBuffer();
    appendLine(buffer, objects, ",");
    return buffer.toString();
  }

  public static String toString(Object[][] objects) {
    StringBuffer buffer = new StringBuffer();
    buffer.append('[');
    for (int i = 0; i < objects.length; i++) {
      if (i > 0) {
        buffer.append('\n');
        buffer.append(' ');
      }
      appendLine(buffer, objects[i], ",\t");
    }
    buffer.append(']');
    return buffer.toString();
  }

  private static void appendLine(StringBuffer buffer, Object[] objects, String separator) {
    buffer.append('[');
    for (int i = 0; i < objects.length; i++) {
      if (objects[i] == null) {
        buffer.append("null");
      }
      else if (objects[i].getClass().isArray()) {
        buffer.append(toString((Object[])objects[i]));
      }
      else {
        buffer.append(objects[i]);
      }
      if (i < (objects.length - 1)) {
        buffer.append(separator);
      }
    }
    buffer.append(']');
  }

  public static String toString(int[] ints) {
    StringBuffer buffer = new StringBuffer();
    buffer.append('[');
    for (int i = 0; i < ints.length; i++) {
      buffer.append(ints[i]);
      if (i < (ints.length - 1)) {
        buffer.append(',');
      }
    }
    buffer.append(']');
    return buffer.toString();
  }

  public static String toString(boolean[] booleans) {
    StringBuffer buffer = new StringBuffer();
    buffer.append('[');
    for (int i = 0; i < booleans.length; i++) {
      buffer.append(booleans[i]);
      if (i < (booleans.length - 1)) {
        buffer.append(',');
      }
    }
    buffer.append(']');
    return buffer.toString();
  }

  public static Boolean[][] toBooleanObjects(boolean[][] source) {
    Boolean[][] result = new Boolean[source.length][];
    for (int i = 0; i < result.length; i++) {
      result[i] = new Boolean[source[i].length];
      for (int j = 0; j < result[i].length; j++) {
        result[i][j] = new Boolean(source[i][j]);
      }
    }
    return result;
  }

  public static void assertEquals(String message, Object[] expected, Object[] actual) {
    if (!Arrays.equals(expected, actual)) {
      Assert.fail(message +
                  "\nExpected: " + toString(expected) +
                  "\nActual:   " + toString(actual));
    }
  }

  public static void assertEquals(Object[] expected, Object[] actual) {
    if (!Arrays.equals(expected, actual)) {
      Assert.fail("Expected: " + toString(expected) +
                  "\nActual:   " + toString(actual));
    }
  }

  public static void assertEquals(Object[][] expected, Object[][] actual) {
    Assert.assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals("Error at row " + i + ":", expected[i], actual[i]);
    }
  }

  public static void assertEquals(int[] expected, int[] actual) {
    if (!Arrays.equals(expected, actual)) {
      Assert.fail("expected: " + toString(expected) +
                  "\nbut was: " + toString(actual));
    }
  }

  public static void assertEquals(Object[] expectedArray, Iterator actualIterator) {
    int index = 0;
    List actualList = new ArrayList();
    while (actualIterator.hasNext()) {
      if (index >= expectedArray.length) {
        for (Iterator iterator = actualIterator; iterator.hasNext();) {
          actualList.add(iterator.next());
        }
        Assert.fail("The iterator contains too many elements: expected: " +
                    toString(expectedArray) + " but was: " + actualList);
      }
      Object obj = actualIterator.next();
      actualList.add(obj);
      if (!obj.equals(expectedArray[index])) {
        Assert.fail("Mismatch at index " + index + ". expected: " + expectedArray[index] +
                    " but was: " + obj);
      }
      index++;
    }
    if (index < expectedArray.length) {
      Assert.fail("Several elements are missing from the iterator : expected: " +
                  toString(expectedArray) +
                  " but was: " + actualList);
    }
  }

  public static void assertSetEquals(Object[] expected, Object[] actual) {
    assertSetEquals(toSet(expected), toSet(actual));
  }

  public static void assertSetEquals(Object[] expected, Set actual) {
    assertSetEquals(toSet(expected), actual);
  }

  public static void assertSetEquals(Set expected, Set actual) {
    Assert.assertEquals(expected.size(), actual.size());
    Comparator comparator = new Comparator() {
      public int compare(Object a, Object b) {
        return a.toString().compareTo(b.toString());
      }
    };
    Set missing = new TreeSet(comparator);
    missing.addAll(expected);
    missing.removeAll(actual);

    Set extra = new TreeSet(comparator);
    extra.addAll(actual);
    extra.removeAll(expected);

    int missingCount = missing.size();
    int extraCount = extra.size();

    if ((missingCount > 0) && (extraCount > 0)) {
      throw new AssertionFailedError("Different!\n\nexpected: \"" + missing + "\"" + ";\n\nactual: \"" + extra + "\"");
    }

    if (missingCount > 0) {
      throw new AssertionFailedError("\nMissing: \"" + missing + "\"");
    }

    if (extraCount > 0) {
      throw new AssertionFailedError("\nExtra: \"" + extra + "\"");
    }
  }

  public static void assertSetEquals(List expected, List actual) {
    assertSetEquals(new HashSet(expected), new HashSet(actual));
  }

  private static Set toSet(Object[] array) {
    return new HashSet(Arrays.asList(array));
  }

  private static Set toSet(List list) {
    return new HashSet(list);
  }

  private static Set toSet(Iterator iterator) {
    Set result = new HashSet();
    for (; iterator.hasNext();) {
      result.add(iterator.next());
    }
    return result;
  }
}

