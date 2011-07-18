package org.designup.picsou.gui.transactions.columns;

import org.globsframework.model.Glob;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TransactionTableRenderer implements TableCellRenderer {
  private TableCellRenderer renderer;
  private TransactionRendererColors rendererColors;
  private int seriesColumnIndex;
  private Border border = BorderFactory.createEmptyBorder(0, 3, 0, 3);

  public TransactionTableRenderer(TableCellRenderer renderer, TransactionRendererColors rendererColors,
                                  int seriesColumnIndex) {
    this.renderer = renderer;
    this.rendererColors = rendererColors;
    this.seriesColumnIndex = seriesColumnIndex;
  }

  public Component getTableCellRendererComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row,
                                                 int column) {
    if (value == null){
      return null;
    }
    Component component = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    if (component == null){
      return null;
    }
    if (table.getColumnModel().getColumn(column).getModelIndex() == seriesColumnIndex) {
      return component;
    }
    if (component instanceof JLabel) {
      JLabel label = (JLabel)component;
      label.setBorder(border);
    }
    rendererColors.update(component, isSelected, (Glob)value, row);
    return component;
  }
}
