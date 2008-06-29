package org.globsframework.gui.splits;

import org.globsframework.utils.exceptions.ItemNotFound;
import org.uispec4j.TextBox;

import javax.swing.*;

public class SplitsAutoHideTest extends SplitsTestCase {
  public void testAutoHide() throws Exception {

    JLabel label1 = new JLabel();
    JLabel label2 = new JLabel();

    builder.add("label1", label1);
    builder.add("label2", label2);
    parse("<column>" +
          "  <label ref='label1' autoHideSource='label2'/>" +
          "  <label ref='label2'/>" +
          "</column>");

    assertTrue(label1.isVisible());

    label2.setVisible(false);
    assertFalse(new TextBox(label1).isVisible());

    label2.setVisible(true);
    assertTrue(new TextBox(label1).isVisible());
  }

  public void testAutoHideWithSourceInitiallyHidden() throws Exception {
    JLabel label1 = new JLabel();
    JLabel label2 = new JLabel();

    builder.add("label1", label1);
    builder.add("label2", label2);
    label2.setVisible(false);

    parse("<column>" +
          "  <label ref='label1' autoHideSource='label2'/>" +
          "  <label ref='label2'/>" +
          "</column>");

    assertFalse(label2.isVisible());
    assertFalse(label1.isVisible());
  }

  public void testUnknownReference() throws Exception {

    builder.setSource(
      "<splits>" +
      "  <label autoHideSource='label2'/>" +
      "</splits>");

    try {
      builder.load();
    }
    catch (ItemNotFound e) {
      assertTrue(e.getMessage().contains("References autoHideSource component 'label2' does not exist"));

    }
  }
}
