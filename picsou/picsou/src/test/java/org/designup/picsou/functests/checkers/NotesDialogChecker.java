package org.designup.picsou.functests.checkers;

import org.uispec4j.MenuItem;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

class NotesDialogChecker extends GuiChecker {

  private Window dialog;

  public static NotesDialogChecker open(MenuItem menuItem) {
      return new NotesDialogChecker(WindowInterceptor.run(menuItem.triggerClick()));
    }

  public NotesDialogChecker(Window dialog) {
    this.dialog = dialog;
  }


  public NotesDialogChecker checkText(String text) {

    assertThat(getNotesArea().textEquals(text));
    return this;
  }

  public NotesDialogChecker setText(String text) {
    getNotesArea().setText(text);
    return this;
  }

  private TextBox getNotesArea() {
    return dialog.getInputTextBox("notesEditor");
  }

  public void close() {
    dialog.getButton("close").click();
    assertFalse(dialog.isVisible());
  }
}
