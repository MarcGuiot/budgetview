package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.converters.DateCellConverter;
import org.designup.picsou.functests.checkers.converters.ReconciliationCellConverter;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.uispec4j.MenuItem;
import org.uispec4j.Panel;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.interception.PopupMenuInterceptor;

import java.awt.*;

import static org.uispec4j.assertion.UISpecAssert.*;

public class ReconciliationChecker extends ViewChecker {

  private Table table;

  private static final int LABEL_COLUMN_INDEX = 3;

  public ReconciliationChecker(Window mainWindow) {
    super(mainWindow);
  }

  public void show() {
    getViewMenu().getSubMenu("Show reconciliation").click();
  }

  public void hide() {
    getViewMenu().getSubMenu("Hide reconciliation").click();
  }

  public void checkHidden() {
    assertThat(getTable().getHeader().contentEquals("Date", "Series", "Label", "Amount"));
    getViewMenu().getSubMenu("Show reconciliation");
  }

  public void checkShown() {
    assertThat(getTable().getHeader().contentEquals("", "Date", "Series", "Label", "Amount"));
    getViewMenu().getSubMenu("Hide reconciliation");
  }

  private org.uispec4j.MenuItem getViewMenu() {
    return mainWindow.getMenuBar().getMenu("View");
  }

  public void toggle(String label) {
    getTable().editCell(getRowIndex(label), 0).getButton().click();
  }

  public void reconcileWithPopup(String label) {
    toggleWithPopup(label, "Reconcile");
  }

  public void unreconcileWithPopup(String label) {
    toggleWithPopup(label, "Unreconcile");
  }

  private void toggleWithPopup(String label, String menuAction) {
    Table table = getTable();
    PopupMenuInterceptor
      .run(table.triggerRightClick(getRowIndex(label), LABEL_COLUMN_INDEX))
      .getSubMenu(menuAction)
      .click();
  }

  public void checkPopupEntryShown(int row) {
    MenuItem menu = PopupMenuInterceptor.run(getTable().triggerRightClick(row, LABEL_COLUMN_INDEX));
    assertThat(or(menu.contain("Reconcile"),
                  menu.contain("Unreconcile")));
  }

  public void checkPopupEntryHidden(int row) {
    MenuItem menu = PopupMenuInterceptor.run(getTable().triggerRightClick(row, LABEL_COLUMN_INDEX));
    assertFalse(or(menu.contain("Reconcile"),
                   menu.contain("Unreconcile")));
  }

  private int getRowIndex(String label) {
    int index = getTable().getRowIndex(LABEL_COLUMN_INDEX, label.toUpperCase());
    if (index < 0) {
      Assert.fail("Label '" + label.toUpperCase() + "' not found - actual table content: \n" +
                  table.toString());
    }
    return index;
  }

  public Table getTable() {
    views.selectCategorization();
    if (table == null) {
      Panel panel = mainWindow.getPanel("categorizationView");
      table = panel.getTable("transactionsToCategorize");
      table.setCellValueConverter(0, new ReconciliationCellConverter());
      table.setCellValueConverter(1, new DateCellConverter());
      table.setCellValueConverter(4, new TableCellValueConverter() {
        public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
          Glob transaction = (Glob)modelObject;
          return transaction.get(Transaction.AMOUNT);
        }
      });
    }
    return table;
  }
}
