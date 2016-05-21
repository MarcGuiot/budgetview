package com.budgetview.functests.checkers;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.model.ColorTheme;
import com.budgetview.utils.Lang;
import org.uispec4j.ComboBox;
import org.uispec4j.TabGroup;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class PreferencesChecker extends GuiChecker {
  private Window window;

  public PreferencesChecker(Window window) {
    this.window = window;
  }

  public PreferencesChecker setFutureMonthsCount(int month) {
    ComboBox futureMonthCombo = window.getComboBox("futureMonth");
    UISpecAssert.assertThat(futureMonthCombo.contains(Integer.toString(month)));
    futureMonthCombo.select(Integer.toString(month));
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

  public PreferencesChecker setLang(String langKey) {
    window.getComboBox("lang").select(Lang.get("lang." + langKey));
    return this;
  }

  public PreferencesChecker checkDataPathModificationHidden() {
    TabGroup tabs = window.getTabGroup();
    tabs.selectTab(Lang.get("preferences.tab.storage"));
    checkComponentVisible(tabs.getSelectedTab(), JPanel.class, "storageDirChangePanel", false);
    return this;
  }

  public PreferencesChecker checkDataPathModificationShown() {
    TabGroup tabs = window.getTabGroup();
    tabs.selectTab(Lang.get("preferences.tab.storage"));
    checkComponentVisible(tabs.getSelectedTab(), JPanel.class, "storageDirChangePanel", true);
    return this;
  }

  public PreferencesChecker setDataPath(String path) {
    WindowInterceptor.init(window.getButton(Lang.get("browse")))
      .process(FileChooserHandler.init().select(new String[]{path}))
      .run();
    assertThat(window.getTextBox("storageDir").textEquals(path));
    return this;
  }

  public void validateRestart(LoggedInFunctionalTestCase functionalTestCase) throws Exception {
    MessageDialogChecker.open(window.getButton("ok").triggerClick())
      .checkInfoMessageContains(Lang.get("data.path.exit"))
      .close();
    functionalTestCase.restartApplication(false);
  }

  public PreferencesChecker revertToDefaultDataPath(String expectedPath) {
    window.getButton("revertToDefault").click();
    assertThat(window.getTextBox("storageDir").textEquals(expectedPath));
    return this;
  }

  public void validateUseTargetAndRestart(LoggedInFunctionalTestCase functionalTestCase) throws Exception {
    ConfirmOverwriteDialogChecker.open(window.getButton("ok").triggerClick())
      .checkOverwriteSelected()
      .selectUse()
      .validateAndConfirm();
    functionalTestCase.restartApplication(false);
  }

  public void validateOverwriteTargetAndRestart(LoggedInFunctionalTestCase testCase) throws Exception {
    ConfirmOverwriteDialogChecker.open(window.getButton("ok").triggerClick())
      .checkOverwriteSelected()
      .validateAndConfirm();
    testCase.restartApplication(false);
  }
}
