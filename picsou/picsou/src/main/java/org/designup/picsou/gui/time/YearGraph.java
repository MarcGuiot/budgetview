package org.designup.picsou.gui.time;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.time.selectable.*;
import org.designup.picsou.gui.time.utils.MonthFontMetricInfo;
import org.designup.picsou.gui.time.utils.TimeViewColors;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

public class YearGraph extends DefaultCompositeSelectable {

  private int year;

  private TimeViewColors colors;
  private int yearCellHeight;
  private int yearWidth;
  private int shortYearWidth;
  private String yearText;
  private String shortYearText;
  private MonthGraph[] monthGraphs;
  private int monthHeight;

  public static final int HEIGHT = 3;
  public static final int VERTICAL_MARGIN = 4;
  private static final int BALANCE_HEIGHT = 4;

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
      this.monthGraphs[i] = new MonthGraph(month, colors, new DefaultChainedSelectableElement(i),
                                           timeService, positionProvider);
      i++;
    }
    add(this.monthGraphs);
  }

  public void init(MonthFontMetricInfo monthFontMetricInfo, final FontMetrics yearFontMetrics) {
    yearCellHeight = yearFontMetrics.getHeight() + VERTICAL_MARGIN;
    yearText = Integer.toString(year);
    yearWidth = yearFontMetrics.stringWidth(yearText);
    shortYearText = yearText.substring(2);
    shortYearWidth = yearFontMetrics.stringWidth(shortYearText);
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
//      if (selected) {
//        Paint paint = graphics2D.getPaint();
//        graphics2D.setPaint(new GradientPaint(0, 0, colors.selectedMonthTop, 0, HEIGHT, colors.selectedMonthBottom));
//        graphics2D.fillRect(0, monthHeight + BALANCE_HEIGHT, monthDim, HEIGHT);
//        graphics2D.setPaint(paint);
//      }
//      else {
//        Paint paint = graphics2D.getPaint();
//        graphics2D.setPaint(colors.yearBackground);
//        graphics2D.fillRect(0, monthHeight + BALANCE_HEIGHT, monthDim, HEIGHT);
//        graphics2D.setPaint(paint);
//      }
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
      startX = (int)(clickableAreaTop.getWidth() - intersection.getWidth()) + 2;
    }
    else {
      startX = 2;
    }
    if (monthDim < yearWidth || intersection.getWidth() < yearWidth) {
      if (intersection.getWidth() > shortYearWidth + 1) {
        TimeGraph.drawStringIn(graphics2D, startX, yearCellHeight - 5, shortYearText,
                               colors.yearText, colors.textShadow);
      }
    }
    else {
      TimeGraph.drawStringIn(graphics2D, startX + (int)((intersection.getWidth() - yearWidth) / 2),
                             yearCellHeight - 5, yearText,
                             colors.yearText, colors.textShadow);
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
    return monthHeight + yearCellHeight;
  }

  private void initMonthHeight() {
    monthHeight = 0;
    for (MonthGraph month : monthGraphs) {
      monthHeight = Math.max(monthHeight, month.getHeight());
    }
  }

  public int getMinWidth() {
    int max = shortYearWidth;
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

    YearGraph yearGraph = (YearGraph)o;

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

  public int getMonthHeight() {
    int maxMonthHeight = 0;
    for (MonthGraph month : monthGraphs) {
      maxMonthHeight = Math.max(maxMonthHeight, month.getHeight());
    }
    return maxMonthHeight;
  }

  public int getYear() {
    return year;
  }

  //pour les test
  MonthGraph[] getMonthsGraph() {
    return monthGraphs;
  }
}
