package org.designup.picsou.gui.time;

import org.designup.picsou.gui.time.selectable.ChainedSelectableElement;
import org.designup.picsou.gui.time.selectable.Selectable;
import org.designup.picsou.gui.time.selectable.TransformationAdapter;
import org.designup.picsou.model.Month;
import org.globsframework.model.Glob;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

public class MonthGraph extends AbstractComponent implements Comparable<MonthGraph> {
  private Glob month;
  private MonthViewColors colors;
  private MonthFontMetricInfo.MonthSizes monthSize;

  public MonthGraph(Glob month, MonthViewColors colors, ChainedSelectableElement element) {
    super(element);
    this.month = month;
    this.colors = colors;
  }

  public void init(Graphics2D graphics2D, MonthFontMetricInfo monthFontMetricInfo) {
    monthSize = monthFontMetricInfo.getMonthInfo(Month.toMonth(month.get(Month.ID)));
  }

  public int getNearestRank(int widht) {
    return monthSize.getNearest(widht);
  }

  public void draw(Graphics2D graphics2D, TransformationAdapter transformationAdapter, int height, int width,
                   int monthRank, Rectangle visibleRectangle) {
    clickableArea = TimeGraph.getClickableArea(transformationAdapter.getTransform(), width, height);
    Rectangle2D intersection = visibleRectangle.createIntersection(clickableArea);
    if (intersection.getWidth() != clickableArea.getWidth()) {
      isVisible = Visibility.PARTIALLY;
    }
    else {
      isVisible = Visibility.FULLY;
    }
    if (intersection.getWidth() < 0) {
      isVisible = Visibility.NOT_VISIBLE;
      return;
    }
    if (selected) {
      Paint previousPaint = graphics2D.getPaint();
      graphics2D.setPaint(new GradientPaint(0, 0, colors.selectedTop, 0, height, colors.selectedBottom));
      graphics2D.fillRect(1, 1, width - 1, height - 1);
      graphics2D.setPaint(previousPaint);
    }

    graphics2D.setPaint(colors.grid);
    graphics2D.drawRect(0, 0, width, height);

    MonthFontMetricInfo.Size nearest = monthSize.getSize(monthRank);

    graphics2D.setPaint(colors.text);
    TimeGraph.drawStringIn(graphics2D, (width - nearest.getWidth()) / 2, nearest.getHeigth() + 2, nearest.getName());
  }

  public int getPreferredHeight(Graphics2D graphics2D) {
    return monthSize.getHeight() + 4;
  }

  public int getMaxWidth(Graphics2D graphics2D) {
    return monthSize.getMaxWidth() + 4;
  }

  public int getMinWidth(Graphics2D graphics2D) {
    return monthSize.getMinWidth() + 4;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MonthGraph that = (MonthGraph)o;

    return month.get(Month.ID).equals(that.month.get(Month.ID));
  }

  public int hashCode() {
    return month.get(Month.ID);
  }

  public String toString() {
    return Month.toString(month.get(Month.ID));
  }

  public Glob getMonth() {
    return month;
  }

  public void select(Glob selectedMonth, Collection<Selectable> selectable) {
    if (month.equals(selectedMonth)) {
      selected = true;
      selectable.add(this);
    }
  }

  public void getSelectedGlobs(Collection<Glob> selected) {
    selected.add(month);
  }

  public int compareTo(MonthGraph o) {
    return getMonth().get(Month.ID).compareTo(o.getMonth().get(Month.ID));
  }

}
