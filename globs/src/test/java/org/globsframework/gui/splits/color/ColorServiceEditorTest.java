package org.globsframework.gui.splits.color;

import org.globsframework.gui.splits.color.editor.ColorServiceEditor;
import org.uispec4j.*;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ColorServiceEditorTest extends UISpecTestCase {
  private ColorService colorService;
  private ListBox list;
  private TextBox text;
  private ComboBox colorSetCombo;
  protected Panel panel;
  protected ColorServiceEditor editor;

  protected void setUp() throws Exception {
    colorService = new ColorService(getClass(), "/splits/colors/set1.colors", "/splits/colors/set2.colors");
    initEditor();
  }

  private void initEditor() {
    editor = new ColorServiceEditor(colorService);
    JPanel panel = editor.getBuilder().load();
    this.panel = new Panel(panel);
    list = this.panel.getListBox();
    text = this.panel.getInputTextBox("color");
    colorSetCombo = this.panel.getComboBox();
  }

  public void testSelectingColors() throws Exception {
    assertTrue(list.contentEquals("color1", "color2", "color3"));

    list.select("color1");
    assertTrue(text.textEquals("0000FF"));

    text.setText("123456");
    assertEquals("123456", Colors.toString(colorService.get("color1")));
  }

  public void testPrint() throws Exception {
    list.select("color1");
    text.setText("123456");

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    editor.setOutputStream(new PrintStream(stream));
    panel.getButton("Print").click();
    assertEquals("color1=123456\n" +
                 "color2=00ff00\n" +
                 "color3=ff00ff\n" +
                 "\n",
                 stream.toString());
  }

  public void testChangingColorSets() throws Exception {
    assertTrue(colorSetCombo.contentEquals("set1.colors", "set2.colors"));
    assertTrue(colorSetCombo.selectionEquals("set1.colors"));
    assertTrue(list.contentEquals("color1", "color2", "color3"));
    list.select("color2");
    assertTrue(text.textEquals("00FF00"));

    colorSetCombo.select("set2.colors");
    assertTrue(list.contentEquals("color1", "color2", "color3", "color4"));
    assertTrue(list.selectionEquals("color2"));
    assertTrue(text.textEquals("00FF00"));

    list.select("color4");
    assertTrue(text.textEquals("FFFFFF"));

    colorSetCombo.select("set1.colors");

    assertTrue(list.contains("color4"));
    list.select("color4");
    assertTrue(text.textEquals("FF0000"));
  }

  public void testMissingColorsAreAutomaticallyAddedAndInitializedToRed() throws Exception {
    colorService.addListener(new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {
        colorLocator.get("new.key");
      }
    });
    assertTrue(list.contentEquals("color1", "color2", "color3", "new.key"));

    colorSetCombo.select("set2.colors");
    assertTrue(list.contentEquals("color1", "color2", "color3", "color4", "new.key"));
  }

  public void testColorSetComboIsInitializedWithTheCurrentColorSet() throws Exception {
    colorSetCombo.select("set2.colors");
    initEditor();
    assertTrue(colorSetCombo.selectionEquals("set2.colors"));
  }

  public void assertTrue(Assertion assertion) {
    UISpecAssert.assertTrue(assertion);
  }
}
