package org.designup.picsou.functests.checkers.converters;

import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.uispec4j.TableCellValueConverter;

import java.awt.*;

public class ReconciliationCellConverter implements TableCellValueConverter {
  public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
    if (!renderedComponent.isEnabled()) {
      return "!";
    }
    Glob transaction = (Glob)modelObject;
    if (transaction.isTrue(Transaction.RECONCILED)) {
      return "x";
    }
    return "-";
  }
}
