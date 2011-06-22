package org.designup.picsou.functests.checkers.converters;

import org.designup.picsou.gui.transactions.reconciliation.ReconciliationColumn;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.views.utils.CellPainterPanel;
import org.globsframework.model.Glob;
import org.uispec4j.*;

import javax.swing.*;
import java.awt.*;

public class ReconciliationCellConverter implements TableCellValueConverter {
  public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
    org.uispec4j.Panel panel = new org.uispec4j.Panel((CellPainterPanel)renderedComponent);
    Icon icon = panel.getButton().getAwtComponent().getIcon();
    if (icon == ReconciliationColumn.DISABLED_ICON) {
      return "!";
    }
    else if (icon == ReconciliationColumn.RECONCILED_ICON) {
      return "x";
    }
    else if (icon == ReconciliationColumn.UNRECONCILED_ICON) {
      return "-";
    }
    else {
      return "?";
    }
  }
}
