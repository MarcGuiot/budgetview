package org.designup.picsou.functests.checkers;

import org.uispec4j.*;

import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowInterceptor;

public class SlaValidationDialogChecker extends GuiChecker {
  private Window dialog;

  public static SlaValidationDialogChecker init(Trigger trigger) {
    return new SlaValidationDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public SlaValidationDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public SlaValidationDialogChecker checkTitle(String title) {
    assertThat(dialog.getTextBox("titleLabel").textEquals(title));
    return this;
  }

  public SlaValidationDialogChecker checkNoErrorMessage() {
    TextBox errorMessage = dialog.getTextBox("errorMessage");
    assertFalse(errorMessage.isVisible());
    return this;
  }

  public SlaValidationDialogChecker checkErrorMessage(String message) {
    TextBox errorMessage = dialog.getTextBox("errorMessage");
    assertTrue(errorMessage.isVisible());
    assertThat(errorMessage.textEquals(message));
    return this;
  }

  public SlaValidationDialogChecker checkValidationFailed() {
    dialog.getButton("OK").click();
    assertTrue(dialog.isVisible());
    return this;
  }

  public SlaValidationDialogChecker acceptTerms() {
    try {
      dialog.getCheckBox().select();
    }
    catch (ItemNotFoundException e) {
      throw new ItemNotFoundException(
        "Unexpected dialog: " + dialog.getDescription() + "\n" +
        "=> An exception is probably thrown on application startup. Try to start the application manually to check that.", e);
    }
    return this;
  }

  public void validate() {
    dialog.getButton("OK").click();
    assertFalse(dialog.isVisible());
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
    assertFalse(dialog.isVisible());
  }

  public Trigger triggerOk() {
    return dialog.getButton("OK").triggerClick();
  }

  public static class TriggerSlaOk implements Trigger {
    private Window mainWindow;
    private final SlaValidationDialogChecker slaValidationDialogChecker;

    public TriggerSlaOk(SlaValidationDialogChecker slaValidationDialogChecker) {
      this.slaValidationDialogChecker = slaValidationDialogChecker;
    }

    public void run() {
      mainWindow = WindowInterceptor.run(slaValidationDialogChecker.triggerOk());
    }

    public Window getMainWindow() {
      return mainWindow;
    }
  }
}
