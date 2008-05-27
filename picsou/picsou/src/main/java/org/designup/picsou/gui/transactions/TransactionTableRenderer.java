package org.designup.picsou.gui.transactions;

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
    component.setForeground(isSelected ? Color.WHITE : Color.BLACK);
    rendererColors.setTransactionBackground(component, isSelected, row);
    return component;
  }
}
