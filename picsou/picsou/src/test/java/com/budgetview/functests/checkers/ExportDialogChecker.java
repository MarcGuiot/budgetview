package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.utils.ConfirmationHandler;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;

public class ExportDialogChecker extends GuiChecker {
  private Window dialog;

  public static ExportDialogChecker init(Trigger trigger) {
    return new ExportDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public ExportDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public ExportDialogChecker selectOfx() {
    dialog.getRadioButton("OFX").click();
    return this;
  }

  public ExportDialogChecker selectTsv() {
    dialog.getRadioButton("TSV").click();
    return this;
  }

  public void validate(String fileName) {
    WindowInterceptor.init(dialog.getButton("OK").triggerClick())
      .process(FileChooserHandler.init().select(fileName))
      .run();
    assertFalse(dialog.isVisible());
  }

  public void validateAndConfirmReplace(String fileName) {
    WindowInterceptor.init(dialog.getButton("OK").triggerClick())
      .process(FileChooserHandler.init().select(fileName))
      .process(ConfirmationHandler.validate("Confirmation",
                                            "This file already exists. Do you want to replace it?"))
      .run();
    assertFalse(dialog.isVisible());
  }

  public void validateAndCancelReplace(String fileName) {
    WindowInterceptor.init(dialog.getButton("OK").triggerClick())
      .process(FileChooserHandler.init().select(fileName))
      .process(ConfirmationHandler.cancel("Confirmation",
                                          "This file already exists. Do you want to replace it?"))
      .run();
    assertFalse(dialog.isVisible());
  }
}
