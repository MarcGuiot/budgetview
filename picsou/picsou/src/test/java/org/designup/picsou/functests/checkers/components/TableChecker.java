package org.designup.picsou.functests.checkers.components;

import org.uispec4j.Table;

import java.util.List;
import java.util.ArrayList;

import junit.framework.Assert;

public abstract class TableChecker {

  protected List<Object[]> content = new ArrayList<Object[]>();

  protected abstract Table getTable();

  protected void add(Object... row) {
    content.add(row);
  }

  public void check() {
    Object[][] expectedContent = content.toArray(new Object[content.size()][]);
    org.uispec4j.assertion.UISpecAssert.assertTrue(getTable().contentEquals(expectedContent));
  }

}
