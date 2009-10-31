package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.impl.DefaultSplitsContext;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.directory.DefaultDirectory;
import org.uispec4j.UISpecTestCase;

import javax.swing.border.*;
import java.awt.*;

public class BorderParserTest extends UISpecTestCase {
  private ColorService colorService = new ColorService();
  private SplitsContext context;

  protected void setUp() throws Exception {
    Directory directory = new DefaultDirectory();
    directory.add(colorService);
    context = new DefaultSplitsContext(directory);
  }

  public void testNoBorder() throws Exception {
    assertNull(parse("none"));
  }

  public void testEmpty() throws Exception {
    Border border = parse("empty");
    assertTrue(border instanceof EmptyBorder);
  }

  public void testEmptyWithGeneralInsets() throws Exception {
    Border border = parse("empty(8)");
    assertTrue(border instanceof EmptyBorder);
    EmptyBorder emptyBorder = (EmptyBorder)border;
    checkInsets(emptyBorder.getBorderInsets(), 8, 8, 8, 8);
  }

  public void testEmptyWithInsets() throws Exception {
    Border border = parse("empty(2,3,4,5)");
    assertTrue(border instanceof EmptyBorder);
    EmptyBorder emptyBorder = (EmptyBorder)border;
    checkInsets(emptyBorder.getBorderInsets(), 2, 3, 4, 5);
  }

  public void testEtched() throws Exception {
    Border border = parse("etched");
    assertTrue(border instanceof EtchedBorder);
  }

  public void testBevel() throws Exception {
    String text = "bevel(lowered)";
    Border loweredBevel = parse(text);
    assertTrue(loweredBevel instanceof BevelBorder);

    Border raisedBevel = parse("bevel(raised)");
    assertTrue(raisedBevel instanceof BevelBorder);
  }

  public void testMatteBorderWithFixedColor() throws Exception {
    Border border = parse("matte(1,2,3,4,#00FF00)");
    assertTrue(border instanceof MatteBorder);

    MatteBorder matteBorder = (MatteBorder)border;
    checkInsets(matteBorder.getBorderInsets(), 1, 2, 3, 4);
    assertEquals(Color.GREEN, matteBorder.getMatteColor());
  }

  public void testMatteBorderWithNamedColor() throws Exception {
    colorService.set("my.color", Color.BLUE);

    Border border = parse("matte(1,2,3,4,my.color)");
    assertTrue(border instanceof MatteBorder);

    MatteBorder matteBorder = (MatteBorder)border;
    assertEquals(Color.BLUE, matteBorder.getMatteColor());

    colorService.set("my.color", Color.GREEN);
    assertEquals(Color.GREEN, matteBorder.getMatteColor());
  }

  public void testLineBorderWithFixedColor() throws Exception {
    Border border = parse("line(#00FF00)");
    assertTrue(border instanceof LineBorder);
    LineBorder lineBorder = (LineBorder)border;
    assertEquals(Color.GREEN, lineBorder.getLineColor());
  }

  public void testLineBorderWithNamedColor() throws Exception {
    colorService.set("my.color", Color.BLUE);

    Border border = parse("line(my.color)");
    assertTrue(border instanceof LineBorder);

    LineBorder lineBorder = (LineBorder)border;
    assertEquals(Color.BLUE, lineBorder.getLineColor());

    colorService.set("my.color", Color.GREEN);
    assertEquals(Color.GREEN, lineBorder.getLineColor());
  }

  private Border parse(String text) {
    return BorderParser.parse(text, colorService, context);
  }

  private void checkInsets(Insets insets, int top, int left, int bottom, int right) {
    assertEquals(top, insets.top);
    assertEquals(left, insets.left);
    assertEquals(bottom, insets.bottom);
    assertEquals(right, insets.right);
  }
}
