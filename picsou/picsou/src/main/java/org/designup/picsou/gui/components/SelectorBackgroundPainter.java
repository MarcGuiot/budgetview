package org.designup.picsou.gui.components;

import org.designup.picsou.gui.utils.ApplicationColors;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.painters.FillPainter;
import org.globsframework.gui.splits.painters.Painter;
import org.globsframework.gui.views.CellPainter;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class SelectorBackgroundPainter implements CellPainter {
  private Painter normalPainter;
  private Painter selectionPainter;

  public SelectorBackgroundPainter(Directory directory) {
    ColorService colorService = directory.get(ColorService.class);
    normalPainter = new FillPainter(ApplicationColors.CATEGORIES_BG, colorService);
    selectionPainter = ApplicationColors.createTableSelectionBackgroundPainter(colorService);
  }

  public void paint(Graphics g, Glob glob, int row, int column,
                    boolean isSelected, boolean hasFocus, int width, int height) {
    if (isSelected) {
      selectionPainter.paint(g, width, height);
    }
    else {
      normalPainter.paint(g, width, height);
    }
  }
}
