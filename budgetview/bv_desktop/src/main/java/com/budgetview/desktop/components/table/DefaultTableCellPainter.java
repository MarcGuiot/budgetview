package com.budgetview.desktop.components.table;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.views.CellPainter;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class DefaultTableCellPainter implements CellPainter, ColorChangeListener {
  private Color evenRowsBg;
  private Color oddRowsBg;
  private Color selectionBg;

  public DefaultTableCellPainter(Directory directory) {
    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    evenRowsBg = colorLocator.get("transactionTable.rows.even.bg");
    oddRowsBg = colorLocator.get("transactionTable.rows.odd.bg");
    selectionBg = colorLocator.get("transactionTable.selected.bg");
  }

  public void paint(Graphics g, Glob glob, int row, int column, boolean isSelected, boolean hasFocus, int width, int height) {
    if (isSelected) {
      g.setColor(selectionBg);
    }
    else if (row % 2 == 0) {
      g.setColor(evenRowsBg);
    }
    else {
      g.setColor(oddRowsBg);
    }
    g.fillRect(0, 0, width, height);
  }
}
