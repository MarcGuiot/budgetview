package org.designup.picsou.functests.checkers;

import org.globsframework.utils.Files;
import org.globsframework.utils.Ref;
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

  public SendImportedFileChecker checkMessageContains(String content) {
    assertThat(dialog.getInputTextBox("details").textContains(content));
    return this;
  }

  public SendImportedFileChecker saveContentToFile(String fileName) throws Exception {
    Files.copyStreamTofile(new ByteArrayInputStream(dialog.getInputTextBox("details").getText().getBytes("UTF-8")), fileName);
    return this;
  }

  public void close() {
    dialog.getButton("Close").click();
  }
}
