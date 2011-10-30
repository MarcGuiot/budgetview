package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.TextBox;

import static org.uispec4j.assertion.UISpecAssert.assertTrue;

public class TitleChecker {
  private Panel panel;
  private TextBox sectionText;
  private TextBox periodText;

  public TitleChecker(Panel panel) {
    this.panel = panel;
  }

  public void checkContent(String section) {
    assertTrue(getSectionText().textEquals(section));
    assertTrue(getPeriodText().textIsEmpty());
  }

  public void checkContent(String section, String period) {
    assertTrue(getSectionText().textEquals(section));
    assertTrue(getPeriodText().textEquals(period));
  }

  public TextBox getSectionText() {
    if (sectionText == null) {
      sectionText = panel.getTextBox("sectionTitle");
    }
    return sectionText;
  }

  public TextBox getPeriodText() {
    if (periodText == null) {
      periodText = panel.getTextBox("periodTitle");
    }
    return periodText;
  }
}
