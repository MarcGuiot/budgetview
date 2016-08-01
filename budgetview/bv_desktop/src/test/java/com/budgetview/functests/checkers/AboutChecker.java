package com.budgetview.functests.checkers;

import com.budgetview.gui.PicsouApplication;
import org.uispec4j.Panel;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

public class AboutChecker extends GuiChecker {
  private Window dialog;

  public static AboutChecker open(Trigger trigger) {
    Window dialog = WindowInterceptor.getModalDialog(trigger);
    return new AboutChecker(dialog);
  }

  private AboutChecker(Window dialog) {
    this.dialog = dialog;
  }

  public void checkVersion() {
    UISpecAssert.assertThat(dialog.getTextBox("versionLabel").textContains(PicsouApplication.APPLICATION_VERSION));
  }

  public void checkConfigurationContains(String text) {
    dialog.getTabGroup().selectTab("Configuration");
    Panel tab = dialog.getTabGroup().getSelectedTab();
    UISpecAssert.assertThat(tab.getTextBox("configurationArea").textContains(text));
  }

  public void checkLicensesContain(String text) {
    dialog.getTabGroup().selectTab("Licenses");
    Panel tab = dialog.getTabGroup().getSelectedTab();
    UISpecAssert.assertThat(tab.getTextBox("licensesArea").textContains(text));
  }

  public void close() {
    dialog.getButton("Close").click();
  }
}
