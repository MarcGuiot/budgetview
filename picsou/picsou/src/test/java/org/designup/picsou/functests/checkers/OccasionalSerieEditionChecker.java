package org.designup.picsou.functests.checkers;

import org.uispec4j.Table;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class OccasionalSerieEditionChecker extends DataChecker {
  private Window dialog;
  private Table table;

  public OccasionalSerieEditionChecker(Window window) {
    this.dialog = window;
  }


  public TextBox getAmount() {
    return dialog.getInputTextBox("amountEditor");
  }

  public OccasionalSerieEditionChecker setAmount(String value) {
    getAmount().setText(value);
    return this;
  }

  public OccasionalSerieEditionChecker checkTable(Object[][] content) {
    assertThat(getTable().contentEquals(content));
    return this;
  }

  private Table getTable() {
    if (table == null) {
      this.table = dialog.getTable();
    }
    return table;
  }

  public OccasionalSerieEditionChecker setManual() {
    dialog.getButton("manual").click();
    return this;
  }

  public OccasionalSerieEditionChecker setAutomatic() {
    WindowInterceptor.init(dialog.getButton("automatic").triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          return window.getButton("ok").triggerClick();
        }
      }).run();
    table = null;
    return this;
  }

  public void validate() {
    dialog.getButton("ok").click();
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void cancel() {
    dialog.getButton("cancel").click();
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public OccasionalSerieEditionChecker setAllMonths() {
    getTable().selectRowSpan(0, getTable().getRowCount() - 1);
    return this;
  }
}
