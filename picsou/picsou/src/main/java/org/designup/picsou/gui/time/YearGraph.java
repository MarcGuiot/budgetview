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
  private TimeService timeService;
  protected int yearCellHeight;
  private int yearWidth;
  private int shortYearWidth;
  private String yearText;
  private String shortYearText;
  private MonthGraph[] monthsGraph;

  public YearGraph(int year, java.util.List<Glob> months,
                   MonthViewColors colors, ChainedSelectableElement monthElement,
                   ChainedSelectableElement yearElement, TimeService timeService) {
    super(monthElement, yearElement);
    this.year = year;
    this.colors = colors;
    this.timeService = timeService;
    this.monthsGraph = new MonthGraph[months.size()];
    int i = 0;
    for (Glob month : months) {
      this.monthsGraph[i] = new MonthGraph(month, colors, new DefaultChainedSelectableElement(i), timeService);
      i++;
    }
    add(this.monthsGraph);
  }

  public void init(Graphics2D graphics2D, MonthFontMetricInfo monthFontMetricInfo) {
    yearCellHeight = graphics2D.getFontMetrics().getHeight() + 5;
    yearText = Integer.toString(year);
    yearWidth = graphics2D.getFontMetrics().stringWidth(yearText);
    shortYearText = yearText.substring(2);
    shortYearWidth = graphics2D.getFontMetrics().stringWidth(shortYearText);
    for (MonthGraph month : monthsGraph) {
      month.init(graphics2D, monthFontMetricInfo);
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
    try {
      if (selected) {
        Paint paint = graphics2D.getPaint();
        graphics2D.setPaint(new GradientPaint(0, 0, colors.selectedTop, 0, height, colors.selectedBottom));
        graphics2D.fillRect(0, height - yearCellHeight, monthDim, yearCellHeight);
        graphics2D.setPaint(paint);
      }
      for (MonthGraph month : monthsGraph) {
        month.draw(graphics2D, transformationAdapter, height - yearCellHeight, monthWidth, monthRank, visibleRectangle);
        transformationAdapter.translate(monthWidth, 0);
      }
    }
    finally {
      transformationAdapter.restore();
    }

    graphics2D.setPaint(colors.grid);
    graphics2D.drawRect(0, height - yearCellHeight, monthDim, yearCellHeight);

    graphics2D.setPaint(colors.yearSeparator);
    graphics2D.drawLine(0, 0, 0, height);
    graphics2D.drawLine(monthDim, 0, monthDim, height);

    int startX;
    if (clickableArea.getX() < 0) {
      startX = (int)(clickableArea.getWidth() - intersection.getWidth()) + 2;
    }
    else {
      startX = 2;
    }
    if (monthDim < yearWidth || intersection.getWidth() < yearWidth) {
      if (intersection.getWidth() > shortYearWidth + 1) {
        TimeGraph.drawStringIn(graphics2D, startX, height - 5, shortYearText, colors);
      }
    }
    else {
      TimeGraph.drawStringIn(graphics2D, startX + (int)((intersection.getWidth() - yearWidth) / 2), height - 5, yearText, colors);
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

  public int getPreferredHeight(Graphics2D graphics2D) {
    int maxMonthHeight = 0;
    for (MonthGraph month : monthsGraph) {
      maxMonthHeight = Math.max(maxMonthHeight, month.getPreferredHeight(graphics2D));
    }
    return maxMonthHeight + yearCellHeight + 4;
  }

  public int getMaxWidth(Graphics2D graphics2D) {
    int max = 0;
    for (MonthGraph month : monthsGraph) {
      max = Math.max(max, month.getMaxWidth(graphics2D));
    }
    return max;
  }

  public int getMinWidth(Graphics2D graphics2D) {
    int max = shortYearWidth;
    for (MonthGraph month : monthsGraph) {
      max = Math.max(max, month.getMinWidth(graphics2D));
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

}
