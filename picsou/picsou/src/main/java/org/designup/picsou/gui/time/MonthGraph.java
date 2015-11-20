package org.designup.picsou.gui.time;

import org.designup.picsou.gui.time.selectable.AbstractSelectable;
import org.designup.picsou.gui.time.selectable.ChainedSelectableElement;
import org.designup.picsou.gui.time.selectable.Selectable;
import org.designup.picsou.gui.time.selectable.TransformationAdapter;
import org.designup.picsou.gui.time.utils.MonthFontMetricInfo;
import org.designup.picsou.gui.time.utils.TimeViewColors;
import org.designup.picsou.model.Month;
import org.globsframework.model.Glob;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

public class MonthGraph extends AbstractSelectable implements Comparable<MonthGraph> {
  private Glob month;
  private TimeViewColors colors;
  private TimeService timeService;
  private MonthFontMetricInfo.MonthSizes monthSize;
  private PositionProvider positionProvider;

  private static final int MIN_WIDTH = 30;
  private static final int MARGIN = 5;
  public static final int BALANCE_HEIGHT = 2;

  public MonthGraph(Glob month, TimeViewColors colors, ChainedSelectableElement element,
                    TimeService timeService, PositionProvider positionProvider) {
    super(element);
    this.month = month;
    this.colors = colors;
    this.timeService = timeService;
    this.positionProvider = positionProvider;
  }

  public void init(MonthFontMetricInfo monthFontMetricInfo) {
    monthSize = monthFontMetricInfo.getMonthInfo(Month.toMonth(month.get(Month.ID)));
  }

  public int getNearestRank(int width) {
    return monthSize.getNearest(width);
  }

  public void draw(Graphics2D graphics2D, TransformationAdapter transformationAdapter, int height, int width,
                   int monthRank, Rectangle visibleRectangle) {
    clickableAreaTop = TimeGraph.getClickableArea(transformationAdapter.getTransform(), width, height);
    Rectangle2D intersection = visibleRectangle.createIntersection(clickableAreaTop);
    if (intersection.getWidth() != clickableAreaTop.getWidth()) {
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
      graphics2D.setPaint(colors.selectedMonthBg);
      graphics2D.fillRoundRect(MARGIN, 0, width - 2 * MARGIN, height, 8, 8);
    }

    MonthFontMetricInfo.Size nearest = monthSize.getSize(monthRank);
    graphics2D.setFont(colors.getMonthFont());
    TimeGraph.drawStringIn(graphics2D,
                           (width - nearest.getWidth() + 2) / 2,
                           height - 6 - 2 * BALANCE_HEIGHT,
                           nearest.getName(),
                           colors.getMonthTextColor(selected));

    try {
      transformationAdapter.save();
      transformationAdapter.translate(0, height - BALANCE_HEIGHT - 4);
      Double minPosition = positionProvider.getMinPosition(this.month.get(Month.ID));
      if (minPosition != null) {
        Color color = colors.getAmountColor(minPosition);
        graphics2D.setPaint(color);
        int barWidth = (int)(0.3 * width);
        graphics2D.fillRect((width - barWidth) / 2, 0, barWidth, BALANCE_HEIGHT);
      }
    }
    finally {
      transformationAdapter.restore();
    }
  }

  public int getHeight() {
    return monthSize.getHeight() + 6 + 2 * BALANCE_HEIGHT;
  }

  public int getMinWidth() {
    return Math.max(monthSize.getMinWidth() + 4, MIN_WIDTH) + 2 * MARGIN;
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
