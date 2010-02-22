package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.Panel;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class WizardPageChecker<T> extends GuiChecker {

  private Window window;

  protected WizardPageChecker(Window window) {
    this.window = window;
  }

  public WizardPageChecker(WizardPageChecker page) {
    this.window = page.window;
  }

  public T checkTitle(String title) {
    assertThat(window.getTextBox("title").textEquals(title));
    assertThat(getControlPanel().getComboBox("combo").selectionEquals(title));
    return (T)this;
  }

  public WizardPageChecker gotoPage(String title) {
    getControlPanel().getComboBox("combo").select(title);
    return this;
  }

  public WizardPageChecker nextPage() {
    getControlPanel().getButton("next").click();
    return this;
  }

  public WizardPageChecker previousPage() {
    getControlPanel().getButton("previous").click();
    return this;
  }

  private Panel getControlPanel() {
    return window.getPanel("wizardControls");
  }

  protected Panel getPage(String title) {
    gotoPage(title);
    return getContent();
  }

  public Panel getContent() {
    return window.getPanel("content");
  }

  public void validate() {
    window.getButton("OK").click();
  }

  public void close() {
    window.getButton("Close").click();
  }
}
