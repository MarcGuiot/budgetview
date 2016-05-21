package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.converters.DateCellConverter;
import com.budgetview.functests.checkers.converters.ReconciliationAnnotationCellConverter;
import com.budgetview.gui.transactions.reconciliation.annotations.ReconciliationAnnotationColumn;
import com.budgetview.model.Transaction;
import junit.framework.Assert;
import org.globsframework.model.Glob;
import org.uispec4j.Button;
import org.uispec4j.MenuItem;
import org.uispec4j.Panel;
import org.uispec4j.*;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.interception.PopupMenuInterceptor;

import javax.swing.*;
import java.awt.*;

import static org.uispec4j.assertion.UISpecAssert.*;
import static org.uispec4j.assertion.UISpecAssert.or;

public class ReconciliationAnnotationChecker extends ViewChecker {

  private Table table;

  private static final int LABEL_COLUMN_INDEX = 3;

  public ReconciliationAnnotationChecker(Window mainWindow) {
    super(mainWindow);
  }

  public void show() {
    getViewMenu().getSubMenu("Show reconciliation").click();
  }

  public void hide() {
    getViewMenu().getSubMenu("Hide reconciliation").click();
  }

  public void checkMenuEnabled() {
    assertThat(getViewMenu().getSubMenu("Show reconciliation").isEnabled());
  }

  public void checkMenuDisabled() {
    assertFalse(getViewMenu().getSubMenu("Show reconciliation").isEnabled());
  }

  public void checkColumnAndMenuHidden() {
    assertThat(getTable().getHeader().contentEquals("Date", "Series", "Label", "Amount"));
    getViewMenu().getSubMenu("Show reconciliation");
  }

  public void checkColumnAndMenuShown() {
    assertThat(getTable().getHeader().contentEquals("", "Date", "Series", "Label", "Amount"));
    getViewMenu().getSubMenu("Hide reconciliation");
  }

  private org.uispec4j.MenuItem getViewMenu() {
    return mainWindow.getMenuBar().getMenu("View");
  }

  public void checkSignpostDisplayed(String text) {
    checkSignpostVisible(getPanel(), getTable(), text);
  }

  public void checkSignpostHidden() {
  }

  public void toggle(String label) {
    toggle(getRowIndex(label));
  }

  public void toggle(int rowIndex) {
    getTable().editCell(rowIndex, 0).getButton().click();
  }

  public void checkToggleDisabled(int rowIndex) {
    final JButton button = getTable().editCell(rowIndex, 0).getButton().getAwtComponent();
    Assert.assertFalse(button.isEnabled());
    assertThat(new Assertion() {
      public void check() {
        Icon icon = button.getDisabledIcon();
        if ((icon != ReconciliationAnnotationColumn.RECONCILED_ICON_DISABLED) &&
            (icon != ReconciliationAnnotationColumn.UNRECONCILED_ICON_DISABLED)) {
          Assert.fail("Unexpected disabled icon shown");
        }
      }
    });
  }

  public void checkToggleTooltip(int rowIndex, String expectedTooltip) {
    Button button = getTable().editCell(rowIndex, 0).getButton();
    assertThat(button.tooltipEquals(expectedTooltip));
  }

  public void reconcileWithPopup(int row) {
    toggleWithPopup(row, "Reconcile");
  }

  public void unreconcileWithPopup(int row) {
    toggleWithPopup(row, "Unreconcile");
  }

  private void toggleWithPopup(int row, String menuAction) {
    Table table = getTable();
    PopupMenuInterceptor
      .run(table.triggerRightClick(row, LABEL_COLUMN_INDEX))
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
      Panel panel = getPanel();
      table = panel.getTable("transactionsToCategorize");
      table.setCellValueConverter(0, new ReconciliationAnnotationCellConverter());
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

  private Panel getPanel() {
    return mainWindow.getPanel("categorizationSelector");
  }
}
