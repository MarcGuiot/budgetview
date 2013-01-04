package org.globsframework.gui.splits.utils;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.components.ArrowIcon;
import org.globsframework.gui.splits.components.CircledArrowIcon;
import org.globsframework.gui.splits.components.EmptyIcon;
import org.globsframework.gui.splits.components.RoundedRectIcon;
import org.globsframework.gui.splits.impl.DefaultSplitsContext;
import org.globsframework.utils.directory.DefaultDirectory;

import javax.swing.*;
import java.awt.*;

public class IconParserTest extends TestCase {
  private ColorService colorService = new ColorService();
  private DummyImageLocator imageLocator = new DummyImageLocator();

  public void testNoIcon() throws Exception {
    Assert.assertNull(parseArrow(""));
  }

  public void testLeftArrowWithFixedColor() throws Exception {
    checkArrow(parseArrow("leftArrow(30, 40, 15,25,#00FF00)"),
               30, 40, 15, 25, "left", Color.GREEN);
  }

  public void testLeftArrowWithNamedColor() throws Exception {
    colorService.set("my.color", Color.BLUE);

    ArrowIcon icon = parseArrow("leftArrow(100, 200, 10, 20,my.color)");
    checkArrow(icon, 100, 200, 10, 20, "left", Color.BLUE);

    colorService.set("my.color", Color.GREEN);
    assertEquals(Color.GREEN, icon.getColor());
  }

  public void testCircledArrowWithFixedColor() throws Exception {
    CircledArrowIcon icon = (CircledArrowIcon)parse("circledArrow(#FF0000)");
    assertEquals(Color.RED, icon.getColor());
  }

  public void testCircledArrowWithNamedColor() throws Exception {
    colorService.set("my.color", Color.BLUE);

    CircledArrowIcon icon = (CircledArrowIcon)parse("circledArrow(my.color)");
    assertEquals(Color.BLUE, icon.getColor());

    colorService.set("my.color", Color.GREEN);
    assertEquals(Color.GREEN, icon.getColor());
  }

  public void testStandardImage() throws Exception {
    Icon icon = parse(DummyImageLocator.ICON1_NAME);
    assertEquals(DummyImageLocator.ICON1, icon);
    assertEquals(DummyImageLocator.ICON1_NAME, imageLocator.lastRequestedImageName);
  }

  public void testEmptyIcon() throws Exception {
    EmptyIcon icon = (EmptyIcon)parse("empty(12,15)");
    assertEquals(12, icon.getIconWidth());
    assertEquals(15, icon.getIconHeight());
  }

  public void testRoundedRect() throws Exception {
    RoundedRectIcon icon = (RoundedRectIcon)parse("roundedRect(10,20,1,2,#FF0000,#00FF00)");
    assertEquals(10, icon.getIconWidth());
    assertEquals(20, icon.getIconHeight());
    assertEquals(1, icon.getArcX());
    assertEquals(2, icon.getArcY());
    assertEquals(Color.RED, icon.getBackgroundColor());
    assertEquals(Color.GREEN, icon.getBorderColor());
  }

  public void testRoundedRectWithNamedColors() throws Exception {
    colorService.set("rect.bg", Color.BLUE);
    colorService.set("rect.border", Color.WHITE);

    RoundedRectIcon icon = (RoundedRectIcon)parse("roundedRect(15,25,5,10,rect.bg,rect.border)");
    assertEquals(15, icon.getIconWidth());
    assertEquals(25, icon.getIconHeight());
    assertEquals(5, icon.getArcX());
    assertEquals(10, icon.getArcY());
    assertEquals(Color.BLUE, icon.getBackgroundColor());
    assertEquals(Color.WHITE, icon.getBorderColor());

    colorService.set("rect.bg", Color.PINK);
    colorService.set("rect.border", Color.GREEN);
    assertEquals(Color.PINK, icon.getBackgroundColor());
    assertEquals(Color.GREEN, icon.getBorderColor());
  }


  private ArrowIcon parseArrow(String text) {
    Icon icon = parse(text);
    return (ArrowIcon)icon;
  }

  private Icon parse(String text) {
    return IconParser.parse(text,
                            colorService,
                            imageLocator,
                            new DefaultSplitsContext(new DefaultDirectory()));
  }

  private void checkArrow(ArrowIcon icon, int iconWidth, int iconHeight, int arrowWidth, int arrowHeight, String orientation, Color color) {
    assertEquals(toArrowString(iconWidth, iconHeight,
                               arrowWidth, arrowHeight,
                               orientation, color),
                 toArrowString(icon.getIconWidth(), icon.getIconHeight(),
                               icon.getArrowWidth(), icon.getArrowHeight(),
                               icon.getOrientation().name(), icon.getColor()));
  }

  private String toArrowString(int iconWidth, int iconHeight,
                               int arrowWidth, int arrowHeight,
                               String orientation, Color color) {
    StringBuilder builder = new StringBuilder();
    builder
      .append("(")
      .append(iconWidth)
      .append(",")
      .append(iconHeight)
      .append(") - ")
      .append("(")
      .append(arrowWidth)
      .append(",")
      .append(arrowHeight)
      .append(") - ")
      .append(orientation.toLowerCase())
      .append(" - ")
      .append(Colors.toString(color));
    return builder.toString();
  }
}
