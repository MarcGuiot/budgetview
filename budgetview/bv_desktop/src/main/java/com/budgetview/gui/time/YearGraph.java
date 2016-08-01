package com.budgetview.gui.time;

import com.budgetview.gui.time.selectable.*;
import com.budgetview.gui.time.utils.MonthFontMetricInfo;
import com.budgetview.gui.time.utils.TimeViewColors;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

public class YearGraph extends DefaultCompositeSelectable {

  private int year;

  private TimeViewColors colors;
  private MonthGraph[] monthGraphs;
  private int monthHeight;

  public YearGraph(boolean isFirstYear, boolean isLastYear, int year, java.util.List<Glob> months,
                   TimeViewColors colors, ChainedSelectableElement monthElement,
                   ChainedSelectableElement yearElement, TimeService timeService,
                   PositionProvider positionProvider) {
    super(monthElement, yearElement);
    this.year = year;
    this.colors = colors;
    this.monthGraphs = new MonthGraph[months.size()];
    int i = 0;
    for (Glob month : months) {
      this.monthGraphs[i] = new MonthGraph(month, colors,
                                           new DefaultChainedSelectableElement(i),
                                           positionProvider);
      i++;
    }
    add(this.monthGraphs);
  }

  public void init(MonthFontMetricInfo monthFontMetricInfo) {
    for (MonthGraph month : monthGraphs) {
      month.init(monthFontMetricInfo);
    }
    initMonthHeight();
  }

  public int draw(Graphics2D graphics2D, TransformationAdapter transformationAdapter, int height, int monthWidth,
                  int monthRank, Rectangle visibleRectangle) {
    int monthDim = monthGraphs.length * monthWidth;
    transformationAdapter.save();
    clickableAreaTop = TimeGraph.getClickableArea(transformationAdapter.getTransform(), monthDim, height);
    Rectangle2D intersection = visibleRectangle.createIntersection(clickableAreaTop);
    if (intersection.getWidth() != clickableAreaTop.getWidth()) {
      isVisible = Visibility.PARTIALLY;
    }
    else {
      isVisible = Visibility.FULLY;
    }
    if (intersection.getWidth() < 0) {
      isVisible = Visibility.NOT_VISIBLE;
      for (AbstractSelectable month : monthGraphs) {
        month.setNotVisible();
      }
      return monthDim;
    }
    try {
      transformationAdapter.translate(0, getHeight() - monthHeight);
      for (MonthGraph month : monthGraphs) {
        month.draw(graphics2D, transformationAdapter, monthHeight, monthWidth, monthRank, visibleRectangle);
        transformationAdapter.translate(monthWidth, 0);
      }
    }
    finally {
      transformationAdapter.restore();
    }

    int startX;
    graphics2D.setFont(colors.getYearFont());
    if (clickableAreaTop.getX() < 0) {
      startX = (int) (clickableAreaTop.getWidth() - intersection.getWidth()) + 2;
    }
    else {
      startX = 2;
    }
    return monthDim;
  }

  public int getMonthCount() {
    return monthGraphs.length;
  }

  public void getSelectedGlobs(Collection<Glob> selected) {
    for (Selectable month : monthGraphs) {
      month.getSelectedGlobs(selected);
    }
  }

  public int getHeight() {
    return monthHeight;
  }

  private void initMonthHeight() {
    monthHeight = 0;
    for (MonthGraph month : monthGraphs) {
      monthHeight = Math.max(monthHeight, month.getHeight());
    }
  }

  public int getMinWidth() {
    int max = 0;
    for (MonthGraph month : monthGraphs) {
      max = Math.max(max, month.getMinWidth());
    }
    return max;
  }

  public void getSelected(java.util.List<Selectable> list) {
    for (AbstractSelectable month : monthGraphs) {
      month.getSelected(list);
    }
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    YearGraph yearGraph = (YearGraph) o;

    return year == yearGraph.year;
  }

  public int hashCode() {
    return year;
  }

  public void select(Glob selectedMonth, Collection<Selectable> selectable) {
    for (MonthGraph month : monthGraphs) {
      month.select(selectedMonth, selectable);
    }
  }

  public void getAllSelectableMonth(GlobList globList) {
    for (MonthGraph month : monthGraphs) {
      globList.add(month.getMonth());
    }
  }

  public int getMinMonthRank(int monthWidth) {
    int rank = 0;
    for (MonthGraph month : monthGraphs) {
      rank = Math.max(rank, month.getNearestRank(monthWidth));
    }
    return rank;
  }

  public int compareTo(YearGraph yearGraph) {
    return yearGraph.year - year;
  }

  public Selectable getFirstMonth() {
    return monthGraphs[0];
  }

  public Selectable getLastMonth() {
    return monthGraphs[monthGraphs.length - 1];
  }

  public int getYear() {
    return year;
  }
}
