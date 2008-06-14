package org.globsframework.gui.views;

import org.globsframework.model.Glob;

import java.awt.*;

public interface CellPainter {

  void paint(Graphics g, Glob glob,
             int row, int column,
             boolean isSelected, boolean hasFocus,
             int width, int height);

  static CellPainter NULL = new CellPainter() {
    public void paint(Graphics g, Glob glob,
                      int row, int column,
                      boolean isSelected, boolean hasFocus,
                      int width, int height) {
    }
  };
}
