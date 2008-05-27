package org.designup.picsou.gui.time;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.designup.picsou.gui.time.selectable.ChainedSelectableElement;
import org.designup.picsou.gui.time.selectable.Selectable;
import org.designup.picsou.gui.time.selectable.TransformationAdapter;
import org.designup.picsou.model.Month;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;

public class TimeGraph {
  protected List<YearGraph> years = Collections.emptyList();
  private GlobList months;
  private MonthFontMetricInfo monthFontMetricInfo;

  public TimeGraph(GlobList months, MonthViewColors colors) {
    this.months = months;
    this.years = new ArrayList<YearGraph>();
    if (months.isEmpty()) {
      return;
    }
    GlobList monthForYear = new GlobList();
    int yearCount = 0;
    int currentYear = Month.toYear(months.get(0).get(Month.ID));
    for (Glob month : months) {
      int year = Month.toYear(month.get(Month.ID));
      if (currentYear != year) {
        years.add(new YearGraph(currentYear, monthForYear, colors,
                                new MonthChainedSelectableElement(yearCount),
                                new YearChainedSelectableElement(yearCount)));
        monthForYear.clear();
        currentYear = year;
        yearCount++;
      }
      monthForYear.add(month);
    }
    years.add(new YearGraph(currentYear, monthForYear, colors,
                            new MonthChainedSelectableElement(yearCount),
                            new YearChainedSelectableElement(yearCount)));
  }

  private void init(Graphics2D graphics2D) {
    if (monthFontMetricInfo != null || months.isEmpty()) {
      return;
    }
    monthFontMetricInfo = new MonthFontMetricInfo(graphics2D);
    for (YearGraph year : years) {
      year.init(graphics2D, monthFontMetricInfo);
    }
  }

  public Selectable getSelectableAt(int x, int y) {
    for (YearGraph year : years) {
      Selectable selectable = year.getSelectable(x, y);
      if (selectable != null) {
        return selectable;
      }
    }
    return null;
  }

  public void draw(Graphics2D graphics2D, TransformationAdapter transformationAdapter,
                   int preferredHeight, int preferredWidth) {
    transformationAdapter.save();
    try {
      init(graphics2D);

      int totalMonthCount = 0;
      for (YearGraph year : years) {
        totalMonthCount += year.getMonthCount();
      }
      if (totalMonthCount == 0) {
        return;
      }

      int height = years.get(0).getPreferredHeight(graphics2D);
      int monthWidth = preferredWidth / totalMonthCount;
      for (YearGraph year : years) {
        monthWidth = Math.max(monthWidth, year.getMinWidth(graphics2D));
      }
      int monthRank = 0;
      for (YearGraph year : years) {
        monthRank = Math.max(monthRank, year.getMinMonthRank(monthWidth));
      }
      int y = preferredHeight - height;
      transformationAdapter.translate(0, y < 0 ? 0 : y);
      for (YearGraph yearGraph : years) {
        int actualMonthPos = yearGraph.draw(graphics2D, transformationAdapter, height, monthWidth, monthRank);
        transformationAdapter.translate(actualMonthPos, 0);
      }
    }
    finally {
      transformationAdapter.restore();
    }
  }

  public static void drawStringIn(Graphics2D graphics2D, int x, int y, String text) {
    graphics2D.drawString(text, x, y);
  }

  public static Rectangle getClickableArea(AffineTransform affineTransform, int width, int height) {
    Point p1 = new Point();
    affineTransform.transform(new Point(0, 0), p1);
    Point p2 = new Point();
    affineTransform.transform(new Point(width, height), p2);
    return new Rectangle(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
  }

  public List<Selectable> getSelected() {
    ArrayList<Selectable> list = new ArrayList<Selectable>();
    for (YearGraph year : years) {
      year.getSelected(list);
    }
    return list;
  }

  public void selectLastMonth(Collection<Selectable> selectable) {
    if (months.isEmpty()) {
      return;
    }
    Glob lastMonth = months.get(months.size() - 1);
    for (YearGraph year : years) {
      year.select(lastMonth, selectable);
    }
  }

  public void selectFirstMonth(Set<Selectable> selectable) {
    if (months.isEmpty()) {
      return;
    }
    Glob lastMonth = months.get(0);
    for (YearGraph year : years) {
      year.select(lastMonth, selectable);
    }
  }

  public void selectMonth(int[] indexes, Collection<Selectable> selectable) {
    for (int index : indexes) {
      for (YearGraph year : years) {
        year.select(months.get(index), selectable);
      }
    }
  }

  public void selectMonth(GlobList months, Collection<Selectable> selectable) {
    for (Glob month : months) {
      for (YearGraph year : years) {
        year.select(month, selectable);
      }
    }
  }

  public void getAllSelectableMonth(GlobList globs) {
    for (YearGraph year : years) {
      year.getAllSelectableMonth(globs);
    }
  }

  private class MonthChainedSelectableElement implements ChainedSelectableElement {
    private int yearCount;

    public MonthChainedSelectableElement(int yearCount) {
      this.yearCount = yearCount;
    }

    public Selectable getLeft() {
      if (yearCount == 0) {
        return null;
      }
      YearGraph yearGraph = years.get(yearCount - 1);
      return yearGraph.getLastMonth();
    }

    public Selectable getRight() {
      if (yearCount == years.size() - 1) {
        return null;
      }
      YearGraph yearGraph = years.get(yearCount + 1);
      return yearGraph.getFirstMonth();
    }
  }

  private class YearChainedSelectableElement implements ChainedSelectableElement {
    private int yearCount;

    public YearChainedSelectableElement(int yearCount) {
      this.yearCount = yearCount;
    }

    public Selectable getLeft() {
      if (yearCount == 0) {
        return null;
      }
      return years.get(yearCount - 1);
    }

    public Selectable getRight() {
      if (yearCount == years.size() - 1) {
        return null;
      }
      return years.get(yearCount + 1);
    }
  }
}
