package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;

import javax.swing.*;

public class SpecialCaseCategorizationChecker<T extends SpecialCaseCategorizationChecker> extends GuiChecker {
  protected Panel panel;

  public SpecialCaseCategorizationChecker(Panel panel) {
    this.panel = panel;
  }

  public T toggle() {
    panel.getButton("showHide").click();
    return (T)this;
  }

  public T checkToggleDisabled() {
    assertFalse(panel.getButton("showHide").isEnabled());
    return (T)this;
  }

  public T checkShown() {
    checkComponentVisible(panel, JPanel.class, "specialCasePanel", true);
    return (T)this;
  }

  public T checkHidden() {
    checkComponentVisible(panel, JPanel.class, "specialCasePanel", false);
    return (T)this;
  }

  protected Panel getSpecialCasePanel() {
   return panel.getPanel("specialCasePanel");
  }
}
