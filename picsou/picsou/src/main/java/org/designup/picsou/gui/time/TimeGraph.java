package org.designup.picsou.gui.time;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.time.selectable.ChainedSelectableElement;
import org.designup.picsou.gui.time.selectable.Selectable;
import org.designup.picsou.gui.time.selectable.TransformationAdapter;
import org.designup.picsou.model.Month;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;

public class TimeGraph {
  protected List<YearGraph> yearGraphs = Collections.emptyList();
  private GlobList months;
  private MonthFontMetricInfo monthFontMetricInfo;
  private int monthWidth;
  private int totalHeight;
  private int monthRank;
  private int monthHeight;

  public TimeGraph(GlobList months, MonthViewColors colors, TimeService timeService, FontMetrics yearFontMetrics,
                   FontMetrics monthFontMetrics) {
    this.months = months;
    this.yearGraphs = new ArrayList<YearGraph>();
    if (months.isEmpty()) {
      return;
    }
    GlobList monthForYear = new GlobList();
    int yearCount = 0;
    int currentYear = Month.toYear(months.get(0).get(Month.ID));
    final SortedSet<Integer> years = new TreeSet<Integer>();
    months.safeApply(new GlobFunctor() {
      public void run(Glob glob, GlobRepository repository) throws Exception {
        years.add(Month.toYear(glob.get(Month.ID)));
      }
    }, null);
    for (Glob month : months) {
      int year = Month.toYear(month.get(Month.ID));
      if (currentYear != year) {
        yearGraphs.add(new YearGraph(years.first().equals(currentYear),
                                     years.last().equals(currentYear),
                                     currentYear, monthForYear, colors,
                                     new MonthChainedSelectableElement(yearCount),
                                     new YearChainedSelectableElement(yearCount), timeService));
        monthForYear.clear();
        currentYear = year;
        yearCount++;
      }
      monthForYear.add(month);
    }
    yearGraphs.add(new YearGraph(years.first().equals(currentYear), years.last().equals(currentYear),
                                 currentYear, monthForYear, colors,
                                 new MonthChainedSelectableElement(yearCount),
                                 new YearChainedSelectableElement(yearCount), timeService));
    initFontMetrics(yearFontMetrics, monthFontMetrics);
  }

  public void initFontMetrics(final FontMetrics yearFontMetrics, FontMetrics monthFontMetrics) {
    if (monthFontMetricInfo != null || months.isEmpty()) {
      return;
    }
    monthFontMetricInfo = new MonthFontMetricInfo(monthFontMetrics);
    for (YearGraph year : yearGraphs) {
      year.init(monthFontMetricInfo, yearFontMetrics);
    }
    monthHeight = yearGraphs.get(0).getMonthHeight();
    totalHeight = yearGraphs.get(0).getHeight();
  }

  public Selectable getSelectableAt(int x, int y) {
    for (YearGraph year : yearGraphs) {
      Selectable selectable = year.getSelectable(x, y);
      if (selectable != null) {
        return selectable;
      }
    }
    return null;
  }

  public void draw(Graphics2D graphics2D, TransformationAdapter transformationAdapter,
                   int preferredWidth, int preferredHeight) {
    Rectangle visibleRectangle = new Rectangle(0, 0, preferredWidth, preferredHeight);
    transformationAdapter.save();
    try {
      if (yearGraphs.isEmpty()) {
        return;
      }
      init(preferredWidth);

      for (YearGraph yearGraph : yearGraphs) {
        int actualMonthPos =
          yearGraph.draw(graphics2D, transformationAdapter, totalHeight, monthWidth, monthRank, visibleRectangle);
        transformationAdapter.translate(actualMonthPos, 0);
      }
    }
    finally {
      transformationAdapter.restore();
    }
  }

  public void init(int preferredWidth) {

    int totalMonthCount = 0;
    for (YearGraph year : yearGraphs) {
      totalMonthCount += year.getMonthCount();
    }

    if (totalMonthCount == 0) {
      return;
    }
    monthWidth = preferredWidth / totalMonthCount;
    for (YearGraph year : yearGraphs) {
      monthWidth = Math.max(monthWidth, year.getMinWidth());
    }
    for (YearGraph year : yearGraphs) {
      monthRank = Math.max(monthRank, year.getMinMonthRank(monthWidth));
    }
  }

  public int getYearWeigth() {
    return monthWidth * 12;
  }

  public int getTotalHeight() {
    return totalHeight;
  }

  public int getMonthHeight() {
    return monthHeight;
  }

  public static void drawStringIn(Graphics2D graphics2D, int x, int y, String text, MonthViewColors colors) {
    graphics2D.setPaint(colors.textShadow);
    graphics2D.drawString(text, x - 1, y - 1);
    graphics2D.setPaint(colors.text);
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
    for (YearGraph year : yearGraphs) {
      year.getSelected(list);
    }
    return list;
  }

  public void selectLastMonth(Collection<Selectable> selectable) {
    if (months.isEmpty()) {
      return;
    }
    Glob lastMonth = months.get(months.size() - 1);
    for (YearGraph year : yearGraphs) {
      year.select(lastMonth, selectable);
    }
  }

  public void selectFirstMonth(Set<Selectable> selectable) {
    if (months.isEmpty()) {
      return;
    }
    Glob lastMonth = months.get(0);
    for (YearGraph year : yearGraphs) {
      year.select(lastMonth, selectable);
    }
  }

  public void selectMonth(int[] indexes, Collection<Selectable> selectable) {
    for (int index : indexes) {
      for (YearGraph year : yearGraphs) {
        year.select(months.get(index), selectable);
      }
    }
  }

  public void selectMonth(GlobList months, Collection<Selectable> selectable) {
    for (Glob month : months) {
      for (YearGraph year : yearGraphs) {
        year.select(month, selectable);
      }
    }
  }

  public void selectMonth(Set<Integer> monthIds, Set<Selectable> selectables) {
    Set<Integer> remainingIds = new HashSet<Integer>(monthIds);
    for (Glob month : months) {
      if (remainingIds.remove(month.get(Month.ID))) {
        for (YearGraph year : yearGraphs) {
          year.select(month, selectables);
        }
      }
    }
    if (!remainingIds.isEmpty()) {
      throw new InvalidParameter("Unknown months: " + remainingIds);
    }
  }

  public void selectAll(Collection<Selectable> selectables) {
    selectMonth(months, selectables);
  }

  public void getAllSelectableMonth(GlobList globs) {
    for (YearGraph year : yearGraphs) {
      year.getAllSelectableMonth(globs);
    }
  }

  public Selectable getFirstSelectable() {
    return yearGraphs.get(0).getFirstMonth();
  }

  public Selectable getLastSelectable() {
    return yearGraphs.get(yearGraphs.size() - 1).getLastMonth();
  }

  public int getWidth() {
    return months.size() * monthWidth;
  }

  public int getMonthWidth() {
    return monthWidth;
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
      YearGraph yearGraph = yearGraphs.get(yearCount - 1);
      return yearGraph.getLastMonth();
    }

    public Selectable getRight() {
      if (yearCount == yearGraphs.size() - 1) {
        return null;
      }
      YearGraph yearGraph = yearGraphs.get(yearCount + 1);
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
      return yearGraphs.get(yearCount - 1);
    }

    public Selectable getRight() {
      if (yearCount == yearGraphs.size() - 1) {
        return null;
      }
      return yearGraphs.get(yearCount + 1);
    }
  }
}
