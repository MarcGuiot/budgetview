package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.ColorTheme;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class PreferencesChecker extends GuiChecker {
  private Window window;

  public PreferencesChecker(Window window) {
    this.window = window;
  }

  public PreferencesChecker setFutureMonthsCount(int month) {
    window.getComboBox("futureMonth").select(Integer.toString(month));
    assertThat(window.getComboBox("futureMonth").selectionEquals(Integer.toString(month)));
    return this;
  }

  public PreferencesChecker checkFutureMonthsCount(int month){
    assertThat(window.getComboBox("futureMonth").selectionEquals(Integer.toString(month)));
    return this;
  }

  public PreferencesChecker setPeriodInMonth(int count){
    window.getComboBox("period").select(Integer.toString(count));
    assertThat(window.getComboBox("period").selectionEquals(Integer.toString(count)));
    return this;
  }

  public PreferencesChecker setMonthBack(int count){
    window.getComboBox("monthBack").select(Integer.toString(count));
    assertThat(window.getComboBox("monthBack").selectionEquals(Integer.toString(count)));
    return this;
  }
  
  public void selectColorTheme(ColorTheme theme) {
    window.getToggleButton(theme.name()).click();
  }

  public void checkColorThemeSelected(ColorTheme theme) {
    assertThat(window.getToggleButton(theme.name()).isSelected());
  }

  public void cancel() {
    window.getButton("cancel").click();
    UISpecAssert.assertFalse(window.isVisible());
  }

  public void validate() {
    window.getButton("ok").click();
    UISpecAssert.assertFalse(window.isVisible());
  }
}
