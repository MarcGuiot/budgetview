package org.globsframework.gui.views.impl;

import org.globsframework.gui.views.CellPainter;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.model.Glob;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class LabelTableCellRenderer extends DefaultTableCellRenderer {
  private LabelCustomizer customizer;
  private CellPainter backgroundPainter;
  private boolean selected;
  private boolean hasFocus;
  private int row;
  private int column;
  private Glob glob;

  public LabelTableCellRenderer(LabelCustomizer customizer,
                                CellPainter backgroundPainter) {
    this.customizer = customizer;
    this.backgroundPainter = backgroundPainter;
  }

  public Component getTableCellRendererComponent(JTable table, Object object,
                                                 boolean isSelected, boolean hasFocus,
                                                 int row, int column) {
    this.selected = isSelected;
    this.hasFocus = hasFocus;
    this.row = row;
    this.column = column;
    this.glob = object instanceof Glob ? (Glob)object : null;

    if (object == null){
      return null;
    }

    this.setOpaque(backgroundPainter == null || backgroundPainter == CellPainter.NULL);
    super.getTableCellRendererComponent(table, getText(), isSelected, hasFocus, row, column);
    customizer.process(this, glob, isSelected, hasFocus, row, column);
    setBorder(null);
    return this;
  }

  public void paintComponent(Graphics g) {
    backgroundPainter.paint(g, glob, row, column, selected, hasFocus, getWidth(), getHeight());
    super.paintComponent(g);
  }
}
