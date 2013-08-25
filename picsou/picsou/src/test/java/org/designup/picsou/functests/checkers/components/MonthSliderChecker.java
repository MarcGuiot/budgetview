package org.designup.picsou.functests.checkers.components;

import org.designup.picsou.functests.checkers.MonthChooserChecker;
import org.uispec4j.Panel;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class MonthSliderChecker {
  private Panel panel;

  public static MonthSliderChecker init(Panel enclosingPanel, String monthSliderName) {
    return new MonthSliderChecker(enclosingPanel.getPanel(monthSliderName));
  }

  private MonthSliderChecker(Panel panel) {
    this.panel = panel;
  }

  public MonthSliderChecker checkText(String text) {
    assertThat(panel.getButton("month").textEquals(text));
    return this;
  }

  public Object getText() {
    return panel.getButton("month").getLabel();
  }

  public MonthSliderChecker setMonth(int monthId) {
    MonthChooserChecker.open(panel.getButton("month").triggerClick()).selectMonth(monthId);
    return this;
  }

  public MonthSliderChecker previous() {
    panel.getButton("previous").click();
    return this;
  }

  public MonthSliderChecker next() {
    panel.getButton("next").click();
    return this;
  }
}
