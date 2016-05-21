package com.budgetview.functests.checkers;

import junit.framework.Assert;
import com.budgetview.functests.checkers.components.TableChecker;
import com.budgetview.gui.description.Formatting;
import org.uispec4j.Table;

public class CategorizationTableChecker extends TableChecker {

  private Table table;

  CategorizationTableChecker(Table table) {
    this.table = table;
  }

  public CategorizationTableChecker add(String date, String series, String label, double amount) {
    add(new String[]{date, series, label.toUpperCase(), Formatting.toString(amount)});
    return this;
  }

  public void dumpCode() {
    Table table = getTable();
    String code = getCode(table);
    Assert.fail("Add this:\n" + code);
  }

  public static String getCode(Table table) {
    StringBuilder builder = new StringBuilder();
    for (int row = 0; row < table.getRowCount(); row++) {
      builder
        .append("  .add(\"")
        .append(table.getContentAt(row, 0).toString())
        .append("\", \"")
        .append(table.getContentAt(row, 1).toString())
        .append("\", \"")
        .append(table.getContentAt(row, 2).toString())
        .append("\", ")
        .append(table.getContentAt(row, 3))
        .append(")\n");
    }
    builder.append("  .check();");
    return builder.toString();
  }

  protected Table getTable() {
    return table;
  }
}
