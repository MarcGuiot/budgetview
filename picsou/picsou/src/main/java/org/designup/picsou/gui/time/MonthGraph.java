package org.designup.picsou.gui.time;

import org.crossbowlabs.globs.model.Glob;
import org.designup.picsou.gui.time.selectable.ChainedSelectableElement;
import org.designup.picsou.gui.time.selectable.Selectable;
import org.designup.picsou.gui.time.selectable.TransformationAdapter;
import org.designup.picsou.model.Month;

import java.awt.*;
import java.util.Collection;

public class MonthGraph implements Selectable, Comparable<MonthGraph> {
  private boolean selected = false;
  private Glob month;
  private MonthViewColors colors;
  private ChainedSelectableElement element;
  private MonthFontMetricInfo.MonthSizes monthSize;
  protected Rectangle clickableArea = new Rectangle();

  public MonthGraph(Glob month, MonthViewColors colors, ChainedSelectableElement element) {
    this.month = month;
    this.colors = colors;
    this.element = element;
  }

  public void init(Graphics2D graphics2D, MonthFontMetricInfo monthFontMetricInfo) {
    monthSize = monthFontMetricInfo.getMonthInfo(Month.toMonth(month.get(Month.ID)));
  }

  public int getNearestRank(int widht) {
    return monthSize.getNearest(widht);
  }

  public void draw(Graphics2D graphics2D, TransformationAdapter transformationAdapter, int height, int width, int monthRank) {
    clickableArea = TimeGraph.getClickableArea(transformationAdapter.getTransform(), width, height);
    if (selected) {
      Paint previousPaint = graphics2D.getPaint();
      graphics2D.setPaint(new GradientPaint(0, 0, colors.selectedTop, 0, height, colors.selectedBottom));
      graphics2D.fillRect(1, 1, width - 1, height - 1);
      graphics2D.setPaint(previousPaint);
    }

    graphics2D.setPaint(colors.grid);
    graphics2D.drawRect(0, 0, width, height);

    MonthFontMetricInfo.Size nearest = monthSize.getSize(monthRank);

    graphics2D.setPaint(colors.text);
    TimeGraph.drawStringIn(graphics2D, (width - nearest.getWidth()) / 2, nearest.getHeigth() + 2, nearest.getName());
  }

  public int getPreferredHeight(Graphics2D graphics2D) {
    return monthSize.getHeight() + 4;
  }

  public int getMaxWidth(Graphics2D graphics2D) {
    return monthSize.getMaxWidth() + 4;
  }

  public int getMinWidth(Graphics2D graphics2D) {
    return monthSize.getMinWidth() + 4;
  }

  public Selectable getSelectable(int x, int y) {
    if (clickableArea.contains(x, y)) {
      return this;
    }
    return null;
  }

  public void getSelected(java.util.List<Selectable> list) {
    if (selected) {
      list.add(this);
    }
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MonthGraph that = (MonthGraph) o;

    return month.get(Month.ID) == that.month.get(Month.ID);
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
    return "month";
  }

  public void getObject(Collection<Glob> selected) {
    selected.add(month);
  }

  public int compareTo(MonthGraph o) {
    return getMonth().get(Month.ID).compareTo(o.getMonth().get(Month.ID));
  }

  public Selectable getLeft() {
    return element.getLeft();
  }

  public Selectable getRight() {
    return element.getRight();
  }
}
