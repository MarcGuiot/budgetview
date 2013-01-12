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

  public PreferencesChecker checkFutureMonthsCount(int month) {
    assertThat(window.getComboBox("futureMonth").selectionEquals(Integer.toString(month)));
    return this;
  }

  public PreferencesChecker selectColorTheme(ColorTheme theme) {
    window.getToggleButton(theme.name()).click();
    return this;
  }

  public PreferencesChecker checkColorThemeSelected(ColorTheme theme) {
    assertThat(window.getToggleButton(theme.name()).isSelected());
    return this;
  }

  public PreferencesChecker checkTextDateSelected(String text) {
    assertThat(window.getComboBox("textDate").selectionEquals(text));
    return this;
  }

  public PreferencesChecker selectTextDate(String text) {
    window.getComboBox("textDate").select(text);
    return this;
  }

  public PreferencesChecker checkNumericDateSelected(String numeric) {
    assertThat(window.getComboBox("numericDate").selectionEquals(numeric));
    return this;
  }

  public PreferencesChecker selectNumericDate(String numeric) {
    window.getComboBox("numericDate").select(numeric);
    return this;
  }

  public void cancel() {
    window.getButton("cancel").click();
    UISpecAssert.assertFalse(window.isVisible());
  }

  public void validate() {
    window.getButton("ok").click();
    UISpecAssert.assertFalse(window.isVisible());
  }

  public PreferencesChecker setLang(String lang) {
    window.getComboBox("lang").select(lang);
    return this;
  }
}
