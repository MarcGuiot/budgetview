package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.color.ColorService;
import org.uispec4j.UISpecTestCase;

import javax.swing.border.*;
import java.awt.*;

public class BorderUtilsTest extends UISpecTestCase {
  private ColorService colorService = new ColorService();

  public void testNoBorder() throws Exception {
    assertNull(BorderUtils.parse("none", colorService));
  }

  public void testEmpty() throws Exception {
    Border border = BorderUtils.parse("empty", colorService);
    assertTrue(border instanceof EmptyBorder);
  }

  public void testEmptyWithGeneralInsets() throws Exception {
    Border border = BorderUtils.parse("empty(8)", colorService);
    assertTrue(border instanceof EmptyBorder);
    EmptyBorder emptyBorder = (EmptyBorder)border;
    checkInsets(emptyBorder.getBorderInsets(), 8, 8, 8, 8);
  }

  public void testEmptyWithInsets() throws Exception {
    Border border = BorderUtils.parse("empty(2,3,4,5)", colorService);
    assertTrue(border instanceof EmptyBorder);
    EmptyBorder emptyBorder = (EmptyBorder)border;
    checkInsets(emptyBorder.getBorderInsets(), 2, 3, 4, 5);
  }

  public void testEtched() throws Exception {
    Border border = BorderUtils.parse("etched", colorService);
    assertTrue(border instanceof EtchedBorder);
  }

  public void testBevel() throws Exception {
    Border loweredBevel = BorderUtils.parse("bevel(lowered)", colorService);
    assertTrue(loweredBevel instanceof BevelBorder);

    Border raisedBevel = BorderUtils.parse("bevel(raised)", colorService);
    assertTrue(raisedBevel instanceof BevelBorder);
  }

  public void testMatteBorderWithFixedColor() throws Exception {
    Border border = BorderUtils.parse("matte(1,2,3,4,#00FF00)", colorService);
    assertTrue(border instanceof MatteBorder);

    MatteBorder matteBorder = (MatteBorder)border;
    checkInsets(matteBorder.getBorderInsets(), 1, 2, 3, 4);
    assertEquals(Color.GREEN, matteBorder.getMatteColor());
  }

  public void testMatteBorderWithNamedColor() throws Exception {
    colorService.set("my.color", Color.BLUE);

    Border border = BorderUtils.parse("matte(1,2,3,4,my.color)", colorService);
    assertTrue(border instanceof MatteBorder);

    MatteBorder matteBorder = (MatteBorder)border;
    assertEquals(Color.BLUE, matteBorder.getMatteColor());

    colorService.set("my.color", Color.GREEN);
    assertEquals(Color.GREEN, matteBorder.getMatteColor());
  }

  public void testLineBorderWithFixedColor() throws Exception {
    Border border = BorderUtils.parse("line(#00FF00)", colorService);
    assertTrue(border instanceof LineBorder);
    LineBorder lineBorder = (LineBorder)border;
    assertEquals(Color.GREEN, lineBorder.getLineColor());
  }

  public void testLineBorderWithNamedColor() throws Exception {
    colorService.set("my.color", Color.BLUE);

    Border border = BorderUtils.parse("line(my.color)", colorService);
    assertTrue(border instanceof LineBorder);

    LineBorder lineBorder = (LineBorder)border;
    assertEquals(Color.BLUE, lineBorder.getLineColor());

    colorService.set("my.color", Color.GREEN);
    assertEquals(Color.GREEN, lineBorder.getLineColor());
  }

  private void checkInsets(Insets insets, int top, int left, int bottom, int right) {
    assertEquals(top, insets.top);
    assertEquals(left, insets.left);
    assertEquals(bottom, insets.bottom);
    assertEquals(right, insets.right);
  }
}
