package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

public class ActionViewChecker extends GuiChecker {
  private Window window;

  public ActionViewChecker(Window window) {
    this.window = window;
  }

  public ImportChecker openImport() {
    return ImportChecker.open(getPanel().getButton("Import").triggerClick());
  }

  public ActionViewChecker checkImportMessage(String message) {
        UISpecAssert.assertThat(getPanel().getPanel("import").getTextBox("editor").textEquals(message));
    return this;
  }

  public ActionViewChecker checkImportSignpostDisplayed(String message) {
    checkSignpostVisible(getPanel(), getPanel().getButton("Import"), message);
    return this;
  }

  public ActionViewChecker checkImportSignpostHidden() {
    checkSignpostHidden(getPanel(), getPanel().getButton("Import"));
    return this;
  }

  public HelpChecker openHelp() {
    return HelpChecker.open(getPanel().getButton("Help").triggerClick());
  }

  private Panel getPanel() {
    return window.getPanel("actionView");
  }
}