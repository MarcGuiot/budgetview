package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.UISpecAssert;

public class TitleChecker {
  private TextBox textBox;

  public TitleChecker(Panel panel) {
    textBox = panel.getTextBox("title");
  }

  public void checkContent(String expected) {
    UISpecAssert.assertTrue(textBox.textEquals(expected));
  }
}
