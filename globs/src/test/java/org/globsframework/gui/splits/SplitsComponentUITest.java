package org.globsframework.gui.splits;

import org.globsframework.gui.splits.font.Fonts;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLabelUI;
import java.awt.*;

public class SplitsComponentUITest extends SplitsTestCase {

  public void test() throws Exception {
    JLabel label = parse(
      "<styles>" +
      "  <ui name='myUI' class='" + MyLabelUI.class.getName() + "' shadowWidth='12'/>" +
      "</styles>" +
      "<label ui='myUI'/>"
    );

    MyLabelUI ui = (MyLabelUI)label.getUI();
    assertEquals(12, ui.shadowWidth.intValue());
  }

  public void testManagesColors() throws Exception {
    colorService.set("myColor", Color.PINK);

    JLabel label = parse(
      "<styles>" +
      "  <ui name='myUI' class='" + MyLabelUI.class.getName() + "' glowColor='myColor'/>" +
      "</styles>" +
      "<label ui='myUI'/>"
    );

    MyLabelUI ui = (MyLabelUI)label.getUI();
    assertEquals(Color.PINK, ui.color);

    colorService.set("myColor", Color.BLUE);
    assertEquals(Color.BLUE, ui.color);
  }

  public void testManagesFonts() throws Exception {
    Font font = Fonts.parseFont("Arial,bold,16");
    fontService.set("myFont", font);

    JLabel label = parse(
      "<styles>" +
      "  <ui name='myUI' class='" + MyLabelUI.class.getName() + "' shadowFont='$myFont'/>" +
      "</styles>" +
      "<label ui='myUI'/>"
    );

    MyLabelUI ui = (MyLabelUI)label.getUI();
    assertEquals(font, ui.font);
  }

  public void testUnknownUIError() throws Exception {
    checkParsingError("<label ui='unknown'/>",
                      "Unknown UI");
  }

  public void testUIMustBeAComponentUISubclass() throws Exception {
    checkParsingError("<styles>" +
                      "  <ui name='myUI' class='" + String.class.getName() + "'/>" +
                      "</styles>" +
                      "<button ui='myUI'/>",
                      "must extend a  subclass");
  }

  public void testUIMustMatchComponentClass() throws Exception {
    checkParsingError("<styles>" +
                      "  <ui name='myUI' class='" + MyLabelUI.class.getName() + "'/>" +
                      "</styles>" +
                      "<button ui='myUI'/>",
                      "argument type mismatch");
  }

  public static class MyLabelUI extends BasicLabelUI {
    private Integer shadowWidth;
    private Color color;
    private Font font;

    public void setShadowWidth(Integer shadowWidth) {
      this.shadowWidth = shadowWidth;
    }

    public void setGlowColor(Color color) {
      this.color = color;
    }

    public void setShadowFont(Font font) {
      this.font = font;
    }

  }
}
