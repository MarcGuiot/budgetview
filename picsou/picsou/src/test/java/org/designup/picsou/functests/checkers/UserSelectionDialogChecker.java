package org.designup.picsou.functests.checkers;

import org.uispec4j.Clickable;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class UserSelectionDialogChecker extends GuiChecker {
  
  private Window dialog;
  
  public static UserSelectionDialogChecker open(Clickable clickable) {
    return new UserSelectionDialogChecker(WindowInterceptor.getModalDialog(clickable.triggerClick()));
  }

  private UserSelectionDialogChecker(Window dialog) {
    this.dialog = dialog;
  }
  
  public UserSelectionDialogChecker checkNames(String... names) {
    assertThat(dialog.getListBox().contentEquals(names));
    return this;
  }
  
  public UserSelectionDialogChecker select(String name) {
    dialog.getListBox().select(name);
    return this;
  }

  public UserSelectionDialogChecker checkSelected(String name) {
    assertThat(dialog.getListBox().selectionEquals(name));
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
}
