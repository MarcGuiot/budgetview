package org.globsframework.gui.splits.utils;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.components.ArrowIcon;
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

  public void testStandardImage() throws Exception {
    Icon icon = parse(DummyImageLocator.ICON1_NAME);
    assertEquals(DummyImageLocator.ICON1, icon);
    assertEquals(DummyImageLocator.ICON1_NAME, imageLocator.lastRequestedImageName);
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
