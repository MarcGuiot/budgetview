package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.impl.DefaultSplitProperties;
import org.globsframework.gui.splits.impl.DefaultSplitsContext;
import org.globsframework.gui.splits.font.FontService;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.styles.StyleContext;
import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.directory.DefaultDirectory;
import org.uispec4j.UISpecTestCase;

import javax.swing.*;
import java.awt.*;

public class PropertySetterTest extends UISpecTestCase {

  private ColorService colorService;
  private DefaultSplitsContext context;
  private JButton button = new JButton();

  protected void setUp() throws Exception {
    colorService = new ColorService();
    Directory directory = new DefaultDirectory();
    directory.add(colorService);
    directory.add(IconLocator.class, new DummyIconLocator());
    directory.add(TextLocator.class, new DummyTextLocator());
    directory.add(FontLocator.class, new FontService());
    context = new DefaultSplitsContext(directory);
  }

  public void testStringParameter() throws Exception {
    setButtonProperty("text", "blah");
    assertEquals("blah", button.getText());
  }

  public void testLocatedStringParameter() throws Exception {
    setButtonProperty("text", "$aa.bb.cc");
    assertEquals("aa bb cc", button.getText());

    setButtonProperty("text", "\\$aa.bb.cc");
    assertEquals("$aa.bb.cc", button.getText());
  }

  public void testStringLocationError() throws Exception {
    checkError("text", "$this is not a valid property", "Invalid format for 'this is not a valid property'");
  }

  public void testBooleanParameter() throws Exception {
    setButtonProperty("visible", "true");
    assertTrue(button.isVisible());

    setButtonProperty("visible", "false");
    assertFalse(button.isVisible());

    setButtonProperty("visible", "yes");
    assertTrue(button.isVisible());

    setButtonProperty("visible", "no");
    assertFalse(button.isVisible());
  }

  public void testIntParameter() throws Exception {
    setButtonProperty("horizontalAlignment", "2");
    assertEquals(2, button.getHorizontalAlignment());
  }

  public void testIntParameterWithConstant() throws Exception {
    setButtonProperty("horizontalAlignment", "JLabel.RIGHT");
    assertEquals(JLabel.RIGHT, button.getHorizontalAlignment());
  }

  public void testIntParameterWithImplicitSwingConstant() throws Exception {
    setButtonProperty("horizontalAlignment", "right");
    assertEquals(JLabel.RIGHT, button.getHorizontalAlignment());
  }

  public void testIntParameterWithFullyQualifiedClassReference() throws Exception {
    setButtonProperty("horizontalAlignment", "javax.swing.JLabel.RIGHT");
    assertEquals(JLabel.RIGHT, button.getHorizontalAlignment());
  }

  public void testIntParameterWithInvalidClassReference() throws Exception {
    checkError("horizontalAlignment", "AnUnknownClass.RIGHT", "Unable to locate class for constant 'AnUnknownClass.RIGHT'");
  }

  public void testIntParameterWithInvalidFieldReference() throws Exception {
    checkError("horizontalAlignment", "JLabel.BLAH", "Field 'BLAH' not found in class: javax.swing.JLabel");
  }

  public void testFloatParameter() throws Exception {
    setButtonProperty("alignmentX", "0.45");
    assertEquals(0.45, button.getAlignmentX(), 0.01);
  }

  public void testIconParameterUsesIconLocator() throws Exception {
    setButtonProperty("pressedIcon", DummyIconLocator.ICON2_NAME);
    assertSame(DummyIconLocator.ICON2, button.getPressedIcon());
  }

  public void testDimensionParameter() throws Exception {
    setButtonProperty("size", "(800,600)");
    assertEquals(new Dimension(800, 600), button.getSize());
  }

  public void testColorParameterInstallsTheColorIfNeeded() throws Exception {
    colorService.set("fg", Color.RED);
    setButtonProperty("foreground", "fg");
    assertEquals(Color.RED, button.getForeground());
    colorService.set("fg", Color.BLUE);
    assertEquals(Color.BLUE, button.getForeground());
  }

  public void testColorParameterCanBeAFixedColor() throws Exception {
    setButtonProperty("foreground", "#FF0000");
    assertEquals(Color.RED, button.getForeground());
  }

  public void testFontParameter() throws Exception {
    setButtonProperty("font", "Arial,plain,8");
    Font font = button.getFont();
    assertEquals("Arial", font.getFamily());
    assertEquals(Font.PLAIN, font.getStyle());
    assertEquals(8, font.getSize());
  }

  public void testActionParameter() throws Exception {
    DummyAction action = new DummyAction();
    context.add("action1", action);
    setButtonProperty("action", "action1");
    assertEquals(action, button.getAction());
  }

  public void testEmptyValueMeansNullForNonStringProperties() throws Exception {
    button.setBackground(Color.BLACK);
    setButtonProperty("background", "");
    assertEquals(null, button.getBackground());

    button.setDisabledIcon(DummyIconLocator.ICON1);
    setButtonProperty("disabledIcon", "");
    assertEquals(null, button.getDisabledIcon());
  }

  public void testEmptyValuesAreForbiddenForPrimitiveTypes() throws Exception {
    checkEmptyValueError("enabled", "boolean");
    checkEmptyValueError("horizontalAlignment", "int");
    checkEmptyValueError("alignmentX", "float");
  }

  private void checkEmptyValueError(String property, String propertyType) {
    checkError(property, "", "Empty value not allowed for property '" + property + "' of type " +
                             propertyType + " on class JButton");
  }

  public void testUnknownParameterError() throws Exception {
    checkError("toto", "blah", "No property 'toto' found for class JButton");
  }

  public void testUnparseableValueError() throws Exception {
    checkError("transferHandler", "blah", "Cannot use string value for property 'transferHandler' of type 'TransferHandler' " +
                                          "in class JButton");
  }

  public void testExcludeList() throws Exception {
    DefaultSplitProperties properties = new DefaultSplitProperties();
    properties.put("tExT", "blah");
    PropertySetter.process(button, properties, context, "text");
    assertEquals("", button.getText());
  }

  private void setButtonProperty(String key, String value) {
    DefaultSplitProperties properties = new DefaultSplitProperties();
    properties.put(key, value);
    PropertySetter.process(button, properties, context);
  }

  private void checkError(String parameter, String value, String expectedMessage) {
    try {
      setButtonProperty(parameter, value);
      fail();
    }
    catch (SplitsException e) {
      assertEquals(expectedMessage, e.getMessage());
    }
  }
}
