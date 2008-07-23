package org.designup.picsou.gui.categories.columns;

import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.views.CellPainter;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class CategoryBackgroundPainter implements CellPainter, ColorChangeListener {
  private Color selectionBorder;
  private Color background;
  private Color selectionTop;
  private Color selectionBottom;

  public CategoryBackgroundPainter(Directory directory) {
    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    selectionBorder = colorLocator.get(PicsouColors.CATEGORIES_SELECTED_BORDER);
    selectionTop = colorLocator.get(PicsouColors.CATEGORIES_SELECTED_BG_TOP);
    selectionBottom = colorLocator.get(PicsouColors.CATEGORIES_SELECTED_BG_BOTTOM);
    background = colorLocator.get(PicsouColors.CATEGORIES_BG);
  }

  public void paint(Graphics g, Glob glob, int row, int column,
                    boolean isSelected, boolean hasFocus, int width, int height) {
    Graphics2D g2 = (Graphics2D)g;

    if (!isSelected) {
      g2.setColor(background);
      g2.fillRect(0, 0, width, height);
    }
    else {
      g2.setPaint(new GradientPaint(0, 0, selectionTop, 0, height, selectionBottom));
      g2.fillRect(0, 0, width, height);
      g2.setColor(selectionBorder);
      g2.drawLine(0, 0, width, 0);
      g2.drawLine(0, height - 1, width, height - 1);
    }
  }
}
