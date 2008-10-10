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
    else if (timeService.getCurrentMonthId() > month.get(Month.ID)) {
      if (selected) {
        graphics2D.setPaint(new GradientPaint(0, 0, colors.pastSelectedTop, 0, height, colors.pastSelectedBottom));
      }
      else {
        graphics2D.setPaint(new GradientPaint(0, 0, colors.pastBackgroundTop, 0, height, colors.pastBackgroundBottom));
      }
    }
    else if (timeService.getCurrentMonthId() < month.get(Month.ID)) {
      if (selected) {
        graphics2D.setPaint(new GradientPaint(0, 0, colors.futureSelectedTop, 0, height, colors.futureSelectedBottom));
      }
      else {
        graphics2D.setPaint(new GradientPaint(0, 0, colors.futureBackgroundTop, 0, height, colors.futureBackgroundBottom));
      }
    }
    else if (timeService.getCurrentMonthId() == month.get(Month.ID)) {
      if (selected) {
        graphics2D.setPaint(new GradientPaint(0, 0, colors.currentSelectedTop, 0, height, colors.currentSelectedBottom));
      }
      else {
        graphics2D.setPaint(new GradientPaint(0, 0, colors.currentBackgroundTop, 0, height, colors.currentBackgroundBottom));
      }
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
    TimeGraph.drawStringIn(graphics2D, (width - nearest.getWidth() + 2) / 2, getHeight() - 5,
                           nearest.getName(), colors);

    try {
      transformationAdapter.save();
      transformationAdapter.translate(0, topGraph);
      clickableAreaButton = TimeGraph.getClickableArea(transformationAdapter.getTransform(), width, heightGraph);
      double balance = balancesProvider.getBalance(this.month.get(Month.ID));
      double currentLevel = balancesProvider.getCurrentLevel(this.month.get(Month.ID));
      double diff = balance - currentLevel;
      Color color = getColor(diff);
      graphics2D.setPaint(color);
      graphics2D.fillRect(0, 0, width, heightGraph);
    }
    finally {
      transformationAdapter.restore();
    }
  }

  private Color getColor(double diff) {
    Color color = colors.balanceZero;
    if (diff > 400) {
      color = colors.balancePlus4;
    }
    else if (diff > 300) {
      color = colors.balancePlus3;
    }
    else if (diff > 200) {
      color = colors.balancePlus2;
    }
    else if (diff > 100) {
      color = colors.balancePlus1;
    }
    else if (diff > 50) {
      color = colors.balancePlus05;
    }
    else if (diff < 400) {
      color = colors.balanceMinus4;
    }
    else if (diff < 300) {
      color = colors.balanceMinus3;
    }
    else if (diff < 200) {
      color = colors.balanceMinus2;
    }
    else if (diff < 100) {
      color = colors.balanceMinus1;
    }
    else if (diff < 50) {
      color = colors.balanceMinus05;
    }
    return color;
  }

  public int getHeight() {
    return monthSize.getHeight() + 4;
  }

  public int getMaxWidth() {
    return monthSize.getMaxWidth() + 4;
  }

  public int getMinWidth() {
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
