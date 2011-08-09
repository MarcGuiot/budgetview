package org.designup.picsou.functests.checkers.converters;

import org.designup.picsou.gui.transactions.reconciliation.ReconciliationColumn;
import org.globsframework.gui.views.utils.CellPainterPanel;
import org.uispec4j.TableCellValueConverter;

import javax.swing.*;
import java.awt.*;

public class ReconciliationCellConverter implements TableCellValueConverter {
  public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
    org.uispec4j.Panel panel = new org.uispec4j.Panel((CellPainterPanel)renderedComponent);
    JButton jButton = panel.getButton().getAwtComponent();
    if (jButton.isEnabled()) {
      Icon icon = jButton.getIcon();
      if (icon == ReconciliationColumn.RECONCILED_ICON) {
        return "x";
      }
      else if (icon == ReconciliationColumn.UNRECONCILED_ICON) {
        return "-";
      }
    }
    else {
      Icon icon = jButton.getDisabledIcon();
      if (icon == ReconciliationColumn.RECONCILED_ICON_DISABLED) {
        return "/x/";
      }
      else if (icon == ReconciliationColumn.UNRECONCILED_ICON_DISABLED) {
        return "/-/";
      }
    }
    return "?";
  }
}
