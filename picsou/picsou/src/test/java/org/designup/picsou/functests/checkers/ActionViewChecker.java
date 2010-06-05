package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class ActionViewChecker extends GuiChecker {
  private Window window;

  public ActionViewChecker(Window window) {
    this.window = window;
  }

  public ImportChecker openImport() {
    return ImportChecker.open(getPanel().getButton("Import").triggerClick());
  }

  public ActionViewChecker checkImportMessage(String message) {
    assertThat(getPanel().getPanel("import").getTextBox("content").textEquals(message));
    return this;
  }

  public ActionViewChecker checkImportSignpostDisplayed(String message) {
    checkSignpostVisible(getPanel(), getPanel().getButton("Import"), message);
    return this;
  }

  public HelpChecker openHelp() {
    return HelpChecker.open(getPanel().getButton("Help").triggerClick());
  }

  private Panel getPanel() {
    return window.getPanel("actionView");
  }
}