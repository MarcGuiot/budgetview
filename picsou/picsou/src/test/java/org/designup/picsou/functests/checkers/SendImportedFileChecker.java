package org.designup.picsou.functests.checkers;

import org.globsframework.utils.Ref;
import org.uispec4j.MenuItem;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowInterceptor;

public class SendImportedFileChecker extends GuiChecker {
  private Window dialog;

  public SendImportedFileChecker(Window dialog) {
    this.dialog = dialog;
  }

  public static SendImportedFileChecker open(MenuItem menu) {
    return new SendImportedFileChecker(WindowInterceptor.getModalDialog(menu.triggerClick()));
  }

  public SendImportedFileChecker checkChoice(String... expected) {
    assertThat(dialog.getComboBox().contentEquals(expected));
    return this;
  }

  public SendImportedFileChecker select(String name) {
    dialog.getComboBox().select(name);
    return this;
  }

  public SendImportedFileChecker checkContentContain(String content) {
    assertThat(dialog.getInputTextBox("details").textContains(content));
    return this;
  }

  public SendImportedFileChecker getContent(Ref<String> content) {
    content.set(dialog.getInputTextBox("details").getText());
    return this;
  }

  public void close() {
    dialog.getButton("close").click();
    assertFalse(dialog.isVisible());
  }
}
