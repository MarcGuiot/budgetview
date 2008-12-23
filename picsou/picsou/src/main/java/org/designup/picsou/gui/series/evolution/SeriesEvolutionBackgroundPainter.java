package org.designup.picsou.gui.series.evolution;

import org.globsframework.gui.views.CellPainter;
import org.globsframework.gui.splits.painters.Painter;
import org.globsframework.gui.splits.painters.Paintable;
import org.globsframework.model.Glob;

import java.awt.*;

public class SeriesEvolutionBackgroundPainter implements CellPainter, Paintable {
  private SeriesEvolutionColors seriesEvolutionColors;
  private Painter painter;

  public SeriesEvolutionBackgroundPainter(SeriesEvolutionColors seriesEvolutionColors) {
    this.seriesEvolutionColors = seriesEvolutionColors;
  }

  public void setPainter(Painter painter) {
    this.painter = painter;
  }

  public void paint(Graphics g, Glob seriesWrapper,
                    int row, int column,
                    boolean isSelected, boolean hasFocus,
                    int width, int height) {
    seriesEvolutionColors.setColors(seriesWrapper, row, -1, -1, isSelected, null, this);
    painter.paint(g, width, height);
  }
}
