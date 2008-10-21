package org.designup.picsou.gui.time;

import org.designup.picsou.gui.TimeService;
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
  private TimeService timeService;
  private MonthFontMetricInfo.MonthSizes monthSize;
  private BalancesProvider balancesProvider;
  private static final int MIN_WIDTH = 50;

  public MonthGraph(Glob month, MonthViewColors colors, ChainedSelectableElement element,
                    TimeService timeService, BalancesProvider balancesProvider) {
    super(element);
    this.month = month;
    this.colors = colors;
    this.timeService = timeService;
    this.balancesProvider = balancesProvider;
  }

  public void init(MonthFontMetricInfo monthFontMetricInfo) {
    monthSize = monthFontMetricInfo.getMonthInfo(Month.toMonth(month.get(Month.ID)));
  }

  public int getNearestRank(int widht) {
    return monthSize.getNearest(widht);
  }

  public void draw(Graphics2D graphics2D, TransformationAdapter transformationAdapter, int height, int width,
                   int monthRank, Rectangle visibleRectangle, int topGraph, int heightGraph) {
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
      graphics2D.setPaint(new GradientPaint(0, 0, colors.selectedMonthTop, 0, height, colors.selectedMonthBottom));
    }
    else if (timeService.getCurrentMonthId() != month.get(Month.ID)) {
      graphics2D.setPaint(new GradientPaint(0, 0, colors.monthTop, 0, height, colors.monthBottom));
    }
    else if (timeService.getCurrentMonthId() == month.get(Month.ID)) {
      graphics2D.setPaint(new GradientPaint(0, 0, colors.currentBackgroundTop, 0, height, colors.currentBackgroundBottom));
    }
    graphics2D.fillRect(0, 0, width, height);

    int month = Month.toMonth(this.month.get(Month.ID));
    if (month == 1) {
      graphics2D.setPaint(colors.yearSeparator);
      graphics2D.drawLine(0, 0, 0, height - 1);
    }
    if (month == 12) {
      graphics2D.setPaint(colors.yearSeparator);
      graphics2D.drawLine(width - 1, 0, width - 1, height - 1);
    }

    MonthFontMetricInfo.Size nearest = monthSize.getSize(monthRank);
    graphics2D.setFont(colors.getMonthFont());
    TimeGraph.drawStringIn(graphics2D, (width - nearest.getWidth() + 2) / 2, getHeight() - 5, nearest.getName(),
                           colors.getMonthTextColor(this.month.get(Month.ID), timeService.getCurrentMonthId()), colors.textShadow);

    try {
      transformationAdapter.save();
      transformationAdapter.translate(0, topGraph);
      clickableAreaButton = TimeGraph.getClickableArea(transformationAdapter.getTransform(), width, heightGraph);

      graphics2D.setPaint(selected ? colors.selectedMonthBottom : colors.yearBackground);
      graphics2D.fillRect(0, 0, width, heightGraph);

      Double accountBalance = balancesProvider.getAccountBalance(this.month.get(Month.ID));
      double accountBalanceLimit = balancesProvider.getAccountBalanceLimit(this.month.get(Month.ID));

      if (accountBalance != null) {

        double diff = accountBalance - accountBalanceLimit;

        Color color = colors.getAmountColor(diff);
        graphics2D.setPaint(color);
        int barWidth = width / 4;
        graphics2D.fillRect((width - barWidth) / 2, 0, barWidth, heightGraph);
      }
    }
    finally {
      transformationAdapter.restore();
    }
  }

  public int getHeight() {
    return monthSize.getHeight() + 4;
  }

  public int getMaxWidth() {
    return monthSize.getMaxWidth() + 4;
  }

  public int getMinWidth() {
    return Math.max(monthSize.getMinWidth() + 4, MIN_WIDTH);
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
