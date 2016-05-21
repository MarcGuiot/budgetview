package com.budgetview.functests.checkers.components;

import org.uispec4j.Table;

import java.util.List;
import java.util.ArrayList;

public abstract class TableChecker {

  protected List<Object[]> rows = new ArrayList<Object[]>();

  protected abstract Table getTable();

  protected void add(Object... row) {
    rows.add(row);
  }

  public void check() {
    Object[][] expectedContent = rows.toArray(new Object[rows.size()][]);
    org.uispec4j.assertion.UISpecAssert.assertTrue(getTable().contentEquals(expectedContent));
  }

  public abstract void dumpCode();
}
