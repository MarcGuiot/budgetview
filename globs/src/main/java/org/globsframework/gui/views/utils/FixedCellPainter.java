package org.globsframework.gui.views.utils;

import org.globsframework.gui.views.CellPainter;
import org.globsframework.gui.splits.painters.Painter;
import org.globsframework.model.Glob;

import java.awt.*;

public class FixedCellPainter implements CellPainter {
  private Painter painter;

  public FixedCellPainter(Painter painter) {
    this.painter = painter;
  }

  public void paint(Graphics g, Glob glob,
                    int row, int column,
                    boolean isSelected, boolean hasFocus,
                    int width, int height) {
    painter.paint(g, width, height);
  }
}
