package org.globsframework.gui.views.utils;

import org.globsframework.gui.views.CellPainter;
import org.globsframework.model.Glob;

import javax.swing.*;
import java.awt.*;

public class CellPainterPanel extends JPanel {

  private CellPainter painter = CellPainter.NULL;
  private Glob glob;
  private int row;
  private int column;
  private boolean selected;
  private boolean hasFocus;

  public void setPainter(CellPainter painter) {
    this.painter = painter;
  }

  public void update(Glob glob,
                   int row, int column,
                   boolean isSelected, boolean hasFocus) {
    this.glob = glob;
    this.row = row;
    this.column = column;
    this.selected = isSelected;
    this.hasFocus = hasFocus;
  }

  public void paintComponent(Graphics g) {
    painter.paint(g, glob, row, column, selected, hasFocus, getWidth(), getHeight());
  }
}
