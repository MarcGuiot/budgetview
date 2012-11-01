package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.uispec4j.Panel;
import org.uispec4j.Table;
import org.uispec4j.TableCellValueConverter;
import org.uispec4j.assertion.UISpecAssert;

import java.awt.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;

public class ReconciliationChecker extends GuiChecker {
  private Panel panel;
  private Table table;

  public ReconciliationChecker(Panel panel) {
    this.panel = panel;
  }

  public CategorizationTableChecker initTable() {
    return new CategorizationTableChecker(getTable());
  }

  public ReconciliationChecker select(String label) {
    getTable().selectRowsWithText(2, label);
    return this;
  }

 public ReconciliationChecker select(int index){
   getTable().selectRow(index);
   return this;
 }

  public ReconciliationChecker checkReconcileDisabled() {
    assertFalse(panel.getButton("reconcile").isEnabled());
    return this;
  }

  public ReconciliationChecker reconcile() {
    panel.getButton("reconcile").click();
    return this;
  }

  public ReconciliationChecker keepManualTransaction() {
    panel.getButton("keepManual").click();
    return this;
  }

  private Table getTable() {
    if (table == null) {
      table = panel.getTable();
      table.setCellValueConverter(3, new TableCellValueConverter() {
        public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
          Glob transaction = (Glob)modelObject;
          return Formatting.toString(transaction.get(Transaction.AMOUNT));
        }
      });
    }

    return table;
  }
}
