package org.designup.picsou.gui.time;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.time.selectable.ChainedSelectableElement;
import org.designup.picsou.gui.time.selectable.Selectable;
import org.designup.picsou.gui.time.selectable.TransformationAdapter;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

public class YearGraph extends DefaultCompositeComponent {
  private int year;
  private MonthViewColors colors;
  private int yearCellHeight;
  private int yearWidth;
  private int shortYearWidth;
  private String yearText;
  private String shortYearText;
  private MonthGraph[] monthsGraph;
  public static final int HEIGHT = 6;
  public static final int ENLARGE = 4;
  private boolean isFirstYear;
  private boolean isLastYear;

  public YearGraph(boolean isFirstYear, boolean isLastYear, int year, java.util.List<Glob> months,
                   MonthViewColors colors, ChainedSelectableElement monthElement,
                   ChainedSelectableElement yearElement, TimeService timeService) {
    super(monthElement, yearElement);
    this.isFirstYear = isFirstYear;
    this.isLastYear = isLastYear;
    this.year = year;
    this.colors = colors;
    this.monthsGraph = new MonthGraph[months.size()];
    int i = 0;
    for (Glob month : months) {
      this.monthsGraph[i] = new MonthGraph(month, colors, new DefaultChainedSelectableElement(i), timeService);
      i++;
    }
    add(this.monthsGraph);
  }

  public void init(MonthFontMetricInfo monthFontMetricInfo, final FontMetrics yearFontMetrics) {
    yearCellHeight = yearFontMetrics.getHeight() + ENLARGE;
    yearText = Integer.toString(year);
    yearWidth = yearFontMetrics.stringWidth(yearText);
    shortYearText = yearText.substring(2);
    shortYearWidth = yearFontMetrics.stringWidth(shortYearText);
    for (MonthGraph month : monthsGraph) {
      month.init(monthFontMetricInfo);
    }
  }

  public int draw(Graphics2D graphics2D, TransformationAdapter transformationAdapter, int height, int monthWidth,
                  int monthRank, Rectangle visibleRectangle) {
    int monthDim = monthsGraph.length * monthWidth;
    transformationAdapter.save();
    clickableArea = TimeGraph.getClickableArea(transformationAdapter.getTransform(), monthDim, height);
    Rectangle2D intersection = visibleRectangle.createIntersection(clickableArea);
    if (intersection.getWidth() != clickableArea.getWidth()) {
      isVisible = Visibility.PARTIALLY;
    }
    else {
      isVisible = Visibility.FULLY;
    }
    if (intersection.getWidth() < 0) {
      isVisible = Visibility.NOT_VISIBLE;
      for (AbstractComponent month : monthsGraph) {
        month.setNotVisible();
      }
      return monthDim;
    }
    int beginShift = 0;
    int endShift = 0;
    if (isFirstYear) {
      beginShift = 0;
    }
    if (isLastYear) {
      endShift = 0;
    }
    transformationAdapter.translate(0, yearCellHeight - HEIGHT);
    if (selected) {
      Paint paint = graphics2D.getPaint();
      graphics2D.setPaint(new GradientPaint(0, 0, colors.selectedTop, 0, HEIGHT, colors.selectedBottom));
      graphics2D.fillRect(beginShift, height - yearCellHeight, monthDim - endShift - beginShift, HEIGHT);
      graphics2D.setPaint(paint);
    }
    else {
      Paint paint = graphics2D.getPaint();
      if (year % 2 == 0) {
        graphics2D.setPaint(new GradientPaint(0, 0, colors.yearBackgroundEvenTop, 0, HEIGHT, colors.yearBackgroundEvenBottom));
      }
      else {
        graphics2D.setPaint(new GradientPaint(0, 0, colors.yearBackgroundOddTop, 0, HEIGHT, colors.yearBackgroundOddBottom));
      }
      graphics2D.fillRect(beginShift, height - yearCellHeight, monthDim - endShift - beginShift, HEIGHT);
      graphics2D.setPaint(paint);
    }
    try {
      for (MonthGraph month : monthsGraph) {
        month.draw(graphics2D, transformationAdapter, height - yearCellHeight, monthWidth, monthRank, visibleRectangle);
        transformationAdapter.translate(monthWidth, 0);
      }
    }
    finally {
      transformationAdapter.restore();
    }

    int startX;
    graphics2D.setFont(colors.getYearFont());
    if (clickableArea.getX() < 0) {
      startX = (int)(clickableArea.getWidth() - intersection.getWidth()) + 2;
    }
    else {
      startX = 2;
    }
    if (monthDim < yearWidth || intersection.getWidth() < yearWidth) {
      if (intersection.getWidth() > shortYearWidth + 1) {
        TimeGraph.drawStringIn(graphics2D, startX, yearCellHeight - HEIGHT - 3, shortYearText, colors);
      }
    }
    else {
      TimeGraph.drawStringIn(graphics2D, startX + (int)((intersection.getWidth() - yearWidth) / 2),
                             yearCellHeight - HEIGHT - 3, yearText, colors);
    }
    return monthDim;
  }

  public int getMonthCount() {
    return monthsGraph.length;
  }

  public void getSelectedGlobs(Collection<Glob> selected) {
    for (Selectable month : monthsGraph) {
      month.getSelectedGlobs(selected);
    }
  }

  public int getHeight() {
    int maxMonthHeight = 0;
    for (MonthGraph month : monthsGraph) {
      maxMonthHeight = Math.max(maxMonthHeight, month.getHeight());
    }
    return maxMonthHeight + yearCellHeight + HEIGHT;
  }

  public int getMaxWidth() {
    int max = 0;
    for (MonthGraph month : monthsGraph) {
      max = Math.max(max, month.getMaxWidth());
    }
    return max;
  }

  public int getMinWidth() {
    int max = shortYearWidth;
    for (MonthGraph month : monthsGraph) {
      max = Math.max(max, month.getMinWidth());
    }
    return max;
  }

  public void getSelected(java.util.List<Selectable> list) {
    for (AbstractComponent month : monthsGraph) {
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
    for (MonthGraph month : monthsGraph) {
      month.select(selectedMonth, selectable);
    }
  }

  public void getAllSelectableMonth(GlobList globList) {
    for (MonthGraph month : monthsGraph) {
      globList.add(month.getMonth());
    }
  }

  public int getMinMonthRank(int monthWidth) {
    int rank = 0;
    for (MonthGraph month : monthsGraph) {
      rank = Math.max(rank, month.getNearestRank(monthWidth));
    }
    return rank;
  }

  public int compareTo(YearGraph yearGraph) {
    return yearGraph.year - year;
  }

  public Selectable getFirstMonth() {
    return monthsGraph[0];
  }

  public Selectable getLastMonth() {
    return monthsGraph[monthsGraph.length - 1];
  }

  public int getMonthHeight() {
    int maxMonthHeight = 0;
    for (MonthGraph month : monthsGraph) {
      maxMonthHeight = Math.max(maxMonthHeight, month.getHeight());
    }
    return maxMonthHeight;
  }

}
