package org.designup.picsou.functests.checkers;

import org.apache.xpath.operations.Operation;
import org.uispec4j.MenuItem;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import javax.naming.OperationNotSupportedException;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

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
