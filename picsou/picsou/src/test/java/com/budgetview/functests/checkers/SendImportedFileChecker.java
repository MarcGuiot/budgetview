package com.budgetview.functests.checkers;

import junit.framework.Assert;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.uispec4j.MenuItem;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowInterceptor;

import java.io.ByteArrayInputStream;

public class SendImportedFileChecker extends GuiChecker {
  private Window dialog;

  public SendImportedFileChecker(Window dialog) {
    this.dialog = dialog;
  }

  public static SendImportedFileChecker open(MenuItem menu) {
    return new SendImportedFileChecker(WindowInterceptor.getModalDialog(menu.triggerClick()));
  }

  public SendImportedFileChecker checkChoices(String... expected) {
    assertThat(dialog.getComboBox().contentEquals(expected));
    return this;
  }

  public SendImportedFileChecker select(String name) {
    dialog.getComboBox().select(name);
    return this;
  }

  public SendImportedFileChecker checkMessageContains(String expected) {
    String trimmedExpected = Strings.trimLines(expected);
    String trimmedActual = Strings.trimLines(dialog.getInputTextBox("details").getText());
    if (!trimmedActual.contains(trimmedExpected)) {
      Assert.fail("Text not found:\n" + trimmedExpected + "\nActual:\n" + trimmedActual);
    }
    return this;
  }

  public SendImportedFileChecker saveContentToFile(String fileName) throws Exception {
    Files.copyStreamTofile(new ByteArrayInputStream(dialog.getInputTextBox("details").getText().getBytes("UTF-8")), fileName);
    return this;
  }

  public SendImportedFileChecker toggleObfuscate() {
    dialog.getCheckBox().click();
    return this;
  }

  public void close() {
    dialog.getButton("Close").click();
  }
}
