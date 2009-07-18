package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.assertion.UISpecAssert;

public class NotesChecker extends GuiChecker {
  private Window window;

  public NotesChecker(Window mainWindow) {
    this.window = mainWindow;
  }

  public NotesChecker checkText(String text) {
    UISpecAssert.assertThat(getNotesArea().textEquals(text));
    return this;
  }

  public NotesChecker setText(String text) {
    getNotesArea().setText(text);
    return this;
  }

  private TextBox getNotesArea() {
    return window.getInputTextBox("notesEditor");
  }
}