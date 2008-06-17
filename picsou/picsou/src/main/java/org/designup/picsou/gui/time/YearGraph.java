package org.designup.picsou.gui.time;

import org.designup.picsou.gui.time.selectable.ChainedSelectableElement;
import org.designup.picsou.gui.time.selectable.Selectable;
import org.designup.picsou.gui.time.selectable.TransformationAdapter;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

public class YearGraph implements Selectable, Comparable<YearGraph> {
  private int year;
  private MonthViewColors colors;
  private ChainedSelectableElement monthElement;
  private ChainedSelectableElement yearElement;
  private MonthGraph[] months;
  protected int yearCellHeight;
  private int yearWidth;
  private int shortYearWidth;
  private String yearText;
  private String shortYearText;
  private Rectangle clickableArea = new Rectangle();
  private boolean selected = false;
  private boolean isVisible;

  public YearGraph(int year, java.util.List<Glob> months,
                   MonthViewColors colors, ChainedSelectableElement monthElement,
                   ChainedSelectableElement yearElement) {
    this.year = year;
    this.colors = colors;
    this.monthElement = monthElement;
    this.yearElement = yearElement;
    this.months = new MonthGraph[months.size()];
    int i = 0;
    for (Glob month : months) {
      this.months[i] = new MonthGraph(month, colors, new MonthChainedSelectableElement(i));
      i++;
    }
  }

  public void init(Graphics2D graphics2D, MonthFontMetricInfo monthFontMetricInfo) {
    yearCellHeight = graphics2D.getFontMetrics().getHeight() + 5;
    yearText = Integer.toString(year);
    yearWidth = graphics2D.getFontMetrics().stringWidth(yearText);
    shortYearText = yearText.substring(2);
    shortYearWidth = graphics2D.getFontMetrics().stringWidth(shortYearText);
    for (MonthGraph month : months) {
      month.init(graphics2D, monthFontMetricInfo);
    }
  }

  public int draw(Graphics2D graphics2D, TransformationAdapter transformationAdapter, int height, int monthWidth,
                  int monthRank, Rectangle visibleRectangle) {
    int monthDim = months.length * monthWidth;
    transformationAdapter.save();
    clickableArea = TimeGraph.getClickableArea(transformationAdapter.getTransform(), monthDim, height);
    Rectangle2D intersection = visibleRectangle.createIntersection(clickableArea);
    if (intersection.getWidth() < 0 && intersection.getHeight() < 0) {
      isVisible = false;
      return monthDim;
    }
    isVisible = true;
    try {
      if (selected) {
        Paint paint = graphics2D.getPaint();
        graphics2D.setPaint(new GradientPaint(0, 0, colors.selectedTop, 0, height, colors.selectedBottom));
        graphics2D.fillRect(0, height - yearCellHeight, monthDim, yearCellHeight);
        graphics2D.setPaint(paint);
      }
      for (MonthGraph month : months) {
        month.draw(graphics2D, transformationAdapter, height - yearCellHeight, monthWidth, monthRank);
        transformationAdapter.translate(monthWidth, 0);
      }
    }
    finally {
      transformationAdapter.restore();
    }

    graphics2D.setPaint(colors.grid);
    graphics2D.drawRect(0, height - yearCellHeight, monthDim, yearCellHeight);

    graphics2D.setPaint(colors.text);
    if (monthDim < yearWidth) {
      TimeGraph.drawStringIn(graphics2D, 0, height - 5, shortYearText);
    }
    else {
      TimeGraph.drawStringIn(graphics2D, (monthDim - yearWidth) / 2, height - 5, yearText);
    }
    return monthDim;
  }

  public int getMonthCount() {
    return months.length;
  }

  public int getPreferredHeight(Graphics2D graphics2D) {
    int maxMonthHeight = 0;
    for (MonthGraph month : months) {
      maxMonthHeight = Math.max(maxMonthHeight, month.getPreferredHeight(graphics2D));
    }
    return maxMonthHeight + yearCellHeight + 4;
  }

  public int getMaxWidth(Graphics2D graphics2D) {
    int max = 0;
    for (MonthGraph month : months) {
      max = Math.max(max, month.getMaxWidth(graphics2D));
    }
    return max;
  }

  public int getMinWidth(Graphics2D graphics2D) {
    int max = shortYearWidth;
    for (MonthGraph month : months) {
      max = Math.max(max, month.getMinWidth(graphics2D));
    }
    return max;
  }

  public Selectable getSelectable(int x, int y) {
    Selectable selectable;
    if (clickableArea.contains(x, y)) {
      for (MonthGraph month : months) {
        selectable = month.getSelectable(x, y);
        if (selectable != null) {
          return selectable;
        }
      }
      return this;
    }
    return null;
  }

  public void getSelected(java.util.List<Selectable> list) {
    for (MonthGraph month : months) {
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
    for (MonthGraph month : months) {
      month.select(selectedMonth, selectable);
    }
  }

  public void getAllSelectableMonth(GlobList globList) {
    for (MonthGraph month : months) {
      globList.add(month.getMonth());
    }
  }

  public int getMinMonthRank(int monthWidth) {
    int rank = 0;
    for (MonthGraph month : months) {
      rank = Math.max(rank, month.getNearestRank(monthWidth));
    }
    return rank;
  }

  public Selectable getLeft() {
    return yearElement.getLeft();
  }

  public Selectable getRight() {
    return yearElement.getRight();
  }

  public void select() {
    selected = true;
  }

  public void unSelect() {
    selected = false;
  }

  public void inverseSelect() {
    selected = !selected;
  }

  public String getCommonParent() {
    return "year";
  }

  public void getObject(Collection<Glob> selected) {
    for (MonthGraph month : months) {
      selected.add(month.getMonth());
    }
  }

  public int compareTo(YearGraph yearGraph) {
    return yearGraph.year - year;
  }

  public Selectable getFirstMonth() {
    return months[0];
  }

  public Selectable getLastMonth() {
    return months[months.length - 1];
  }

  private class MonthChainedSelectableElement implements ChainedSelectableElement {
    private int index;

    public MonthChainedSelectableElement(int index) {
      this.index = index;
    }

    public Selectable getLeft() {
      if (index == 0) {
        return monthElement.getLeft();
      }
      return months[index - 1];
    }

    public Selectable getRight() {
      if (index == months.length - 1) {
        return monthElement.getRight();
      }
      return months[index + 1];
    }
  }
}
