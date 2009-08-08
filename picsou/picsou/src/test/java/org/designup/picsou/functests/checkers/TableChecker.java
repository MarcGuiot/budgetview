package org.designup.picsou.functests.checkers;

import org.uispec4j.Table;

import java.util.List;
import java.util.ArrayList;

public abstract class TableChecker {

  private List<Object[]> content = new ArrayList<Object[]>();

  protected abstract Table getTable();

  protected void add(Object... row) {
    content.add(row);
  }

  public void check() {
    Object[][] expectedContent = content.toArray(new Object[content.size()][]);
    org.uispec4j.assertion.UISpecAssert.assertTrue(getTable().contentEquals(expectedContent));
  }

  public void dump() {
    StringBuilder builder = new StringBuilder();

    Table table = getTable();
    for (int row = 0; row < table.getRowCount(); row++) {
      builder.append("  .add(");
      for (int column = 1; column < table.getColumnCount(); column++) {
        builder
          .append("\"")
          .append(table.getContentAt(row, column))
          .append("\"");
        if (column < table.getColumnCount() - 1) {
          builder.append(", ");
        }
      }
      builder.append(")\n");
    }
    builder.append("  .check();");

    System.out.print(builder.toString());
  }

}
