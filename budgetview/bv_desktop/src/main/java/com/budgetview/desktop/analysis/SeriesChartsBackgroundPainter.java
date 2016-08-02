package com.budgetview.desktop.analysis;

import org.globsframework.gui.splits.painters.Paintable;
import org.globsframework.gui.splits.painters.Painter;
import org.globsframework.gui.views.CellPainter;
import org.globsframework.model.Glob;

import java.awt.*;

public class SeriesChartsBackgroundPainter implements CellPainter, Paintable {
  private SeriesChartsColors seriesChartsColors;
  private Painter painter;

  public SeriesChartsBackgroundPainter(SeriesChartsColors seriesChartsColors) {
    this.seriesChartsColors = seriesChartsColors;
  }

  public void setPainter(Painter painter) {
    this.painter = painter;
  }

  public void paint(Graphics g, Glob seriesWrapper,
                    int row, int column,
                    boolean isSelected, boolean hasFocus,
                    int width, int height) {
    seriesChartsColors.setColors(seriesWrapper, row, -1, -1, isSelected, null, this);
    painter.paint(g, width, height);
  }
}
