package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.GridPos;
import org.uispec4j.UISpecTestCase;

import javax.swing.*;
import java.awt.*;

public class SplitsUtilsTest extends UISpecTestCase {

  public static Integer DUMMY_FIELD = 2405;
  private static Integer DUMMY_FIELD_PRIVATE = 3; // used for test below
  public static String DUMMY_FIELD_STRING = "";
  public static Integer DUMMY_FIELD_NOT_SET = null;

  public void testStandardGridPosParsing() throws Exception {
    assertEquals(new GridPos(0, 2, 1, 1), SplitsUtils.parseGridPos("(0,2)"));
    assertEquals(new GridPos(2, 3, 4, 5), SplitsUtils.parseGridPos("(2,3,4,5)"));
  }

  public void testIgnoresSpacesInGridPosParsing() throws Exception {
    assertEquals(new GridPos(0, 2, 1, 1), SplitsUtils.parseGridPos("  (  0  ,   2  )   "));
    assertEquals(new GridPos(2, 3, 4, 5), SplitsUtils.parseGridPos("   (  2   ,  3  ,  4  ,  5   )  "));
  }

  public void testGridPosParsingErrors() throws Exception {
    checkGridPosParsingError("");
    checkGridPosParsingError("a");
    checkGridPosParsingError("(a,b)");
    checkGridPosParsingError("(0,2");
    checkGridPosParsingError("(0,2,3,a)");
  }

  public void testDimensionParsing() throws Exception {
    assertEquals(new Dimension(12, 34), SplitsUtils.parseDimension("(12,34)"));
    assertEquals(new Dimension(12, 34), SplitsUtils.parseDimension("  (  12  , 34  )  "));
  }

  public void testDimensionParsingErrors() throws Exception {
    checkDimensionParsingError("");
    checkDimensionParsingError("a");
    checkDimensionParsingError("(a,b)");
    checkDimensionParsingError("(0,2");
  }


  public void testIntParsing() throws Exception {
    assertEquals(new Integer(25), SplitsUtils.parseInt("25"));
    assertEquals(new Integer(JLabel.BOTTOM), SplitsUtils.parseInt("JLabel.BOTTOM"));
    assertEquals(new Integer(JLabel.BOTTOM), SplitsUtils.parseInt("javax.swing.JLabel.BOTTOM"));
    assertEquals(DUMMY_FIELD, SplitsUtils.parseInt(getClass().getName() + ".DUMMY_FIELD"));
  }

  public void testIntParsingErrors() throws Exception {
    checkIntParsingError("Blah",
                         "Field 'BLAH' not found in class: javax.swing.SwingConstants - value should be either" +
                         " an integer, one of the constants of the SwingConstants class or a reference to a class " +
                         "constant such as 'JLabel.RIGHT'");
    checkIntParsingError("JLabel.UNKNOWN",
                         "Field 'UNKNOWN' not found in class: javax.swing.JLabel");
    checkIntParsingError(getClass().getName() + ".DUMMY_FIELD_PRIVATE",
                         "Field 'DUMMY_FIELD_PRIVATE' not found in class: org.globsframework.gui.splits.utils.SplitsUtilsTest");
    checkIntParsingError(getClass().getName() + ".DUMMY_FIELD_STRING",
                         "Field 'DUMMY_FIELD_STRING' in class 'org.globsframework.gui.splits.utils.SplitsUtilsTest' is not an integer");
    checkIntParsingError(getClass().getName() + ".DUMMY_FIELD_NOT_SET",
                         "Field 'DUMMY_FIELD_NOT_SET' in class 'org.globsframework.gui.splits.utils.SplitsUtilsTest' is not set");
  }

  private void checkIntParsingError(String desc, String expectedMessage) {
    try {
      SplitsUtils.parseInt(desc);
      fail();
    }
    catch (SplitsException e) {
      assertEquals(expectedMessage, e.getMessage());
    }
  }

  private void checkGridPosParsingError(String desc) {
    try {
      SplitsUtils.parseGridPos(desc);
      fail();
    }
    catch (SplitsException e) {
      assertEquals(SplitsUtils.GRIDPOS_ERROR_MESSAGE, e.getMessage());
    }
  }

  private void checkDimensionParsingError(String desc) {
    try {
      SplitsUtils.parseDimension(desc);
      fail();
    }
    catch (SplitsException e) {
      assertEquals(SplitsUtils.DIMENSION_ERROR_MESSAGE, e.getMessage());
    }
  }
}
