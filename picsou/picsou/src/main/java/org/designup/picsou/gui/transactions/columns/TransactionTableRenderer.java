package org.designup.picsou.gui.transactions.columns;

import org.globsframework.model.Glob;
import org.globsframework.utils.Utils;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.model.Transaction;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TransactionTableRenderer implements TableCellRenderer {
  private TableCellRenderer renderer;
  private TransactionRendererColors rendererColors;
  private int categoryColumnIndex;

  public TransactionTableRenderer(TableCellRenderer renderer, TransactionRendererColors rendererColors,
                                  int categoryColumnIndex) {
    this.renderer = renderer;
    this.rendererColors = rendererColors;
    this.categoryColumnIndex = categoryColumnIndex;
  }

  public Component getTableCellRendererComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row,
                                                 int column) {
    Component component = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    if (column == categoryColumnIndex) {
      return component;
    }
    component.setForeground(getForeground((Glob)value, isSelected));
    
    rendererColors.setTransactionBackground(component, isSelected, row);
    return component;
  }

  private Color getForeground(Glob transaction, boolean isSelected) {
    if (isSelected) {
      return Color.WHITE;
    }
    if (Utils.equal(TransactionType.PLANNED.getId(), transaction.get(Transaction.TRANSACTION_TYPE))) {
      return Color.LIGHT_GRAY;
    }
    return rendererColors.getTransactionTextColor();

  }
}
