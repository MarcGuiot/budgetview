package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

public class SeriesWizardChecker extends DataChecker {
  private Window dialog;
  private Panel content;

  public static SeriesWizardChecker open(Trigger trigger) {
    Window dialog = WindowInterceptor.getModalDialog(trigger);
    return new SeriesWizardChecker(dialog);
  }

  private SeriesWizardChecker(Window dialog) {
    this.dialog = dialog;
    this.content = this.dialog.getPanel("content");
  }

  public SeriesWizardChecker select(String text) {
    content.getCheckBox(text).select();
    return this;
  }

  public SeriesWizardChecker checkSelected(String text) {
    UISpecAssert.assertThat(content.getCheckBox(text).isSelected());
    return this;
  }

  public void validate() {
    WindowInterceptor.init(dialog.getButton("OK").triggerClick())
      .processWithButtonClick("OK")
      .run();
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
    UISpecAssert.assertFalse(dialog.isVisible());
  }
}
