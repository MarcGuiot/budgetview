package com.budgetview.functests.checkers;

import org.uispec4j.Window;

public class NotesViewChecker extends ViewChecker {

  private OperationChecker operations;

  public NotesViewChecker(OperationChecker operations, Window dialog) {
    super(dialog);
    this.operations = operations;
  }

  public NotesViewChecker checkText(String text) {
    operations.openNotes()
      .checkText(text)
      .close();
    return this;
  }

  public NotesViewChecker setText(String text) {
    operations.openNotes()
      .setText(text)
      .close();
    return this;
  }
}
