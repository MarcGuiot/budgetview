package org.designup.picsou.functests.checkers;

import org.uispec4j.Table;

import java.util.List;
import java.util.ArrayList;

public abstract class TableChecker {

  private List<Object[]> content = new ArrayList<Object[]>();

  protected abstract Table getTable();

  protected void add(Object[] row) {
    content.add(row);
  }

  public void check() {
    Object[][] expectedContent = content.toArray(new Object[content.size()][]);
    org.uispec4j.assertion.UISpecAssert.assertTrue(getTable().contentEquals(expectedContent));
  }
}
