package com.budgetview.desktop.components.charts.histo.daily;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class HistoDailyBlockPainter {

  public static final BasicStroke DEFAULT_LINE_STROKE = new BasicStroke(1.8f);
  public static final BasicStroke FUTURE_LINE_STROKE =
    new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{2, 3}, 0);

  private Graphics2D g2;
  private HistoDailyColors colors;
  private int y0;

  private GeneralPath fillPath;
  private GeneralPath linePath;

  private Params lastParams;
  private int firstX;
  private int firstY;
  private int lastX;
  private int lastY;

  public HistoDailyBlockPainter(Graphics2D g2, HistoDailyColors colors, int y0) {
    this.g2 = g2;
    this.colors = colors;
    this.y0 = y0;
  }

  public void draw(int previousX, Integer previousY, int x, int y,
                   boolean positive, boolean current, boolean future, boolean selected, boolean rollover) {

    Params newParams = new Params(positive, current, future, selected, rollover);
    if (lastParams != null && !newParams.equals(lastParams)) {
      flush();
    }
    lastParams = newParams;

    if (fillPath == null) {
      fillPath = new GeneralPath();
      fillPath.moveTo(previousX, previousY);
      firstX = previousX;
      firstY = previousY;
    }
    fillPath.lineTo(x, y);

    if (linePath == null) {
      linePath = new GeneralPath();
      linePath.moveTo(previousX, previousY);
    }
    linePath.lineTo(x, y);

    lastX = x;
    lastY = y;
  }

  private void flush() {
    if (fillPath == null) {
      return;
    }
    fillPath.lineTo(lastX, y0);
    fillPath.lineTo(firstX, y0);
    fillPath.lineTo(firstX, firstY);
    fillPath.closePath();
    colors.line.setFillStyle(g2, lastParams.positive, lastParams.current, lastParams.future, lastParams.selected, lastParams.rollover);
    g2.fill(fillPath);
    fillPath = new GeneralPath();
    fillPath.moveTo(lastX,lastY);
    firstX = lastX;

    g2.setStroke(lastParams.future ? FUTURE_LINE_STROKE : DEFAULT_LINE_STROKE);
    colors.line.setLineStyle(g2, lastParams.positive, lastParams.future);
    g2.draw(linePath);
    linePath = new GeneralPath();
    linePath.moveTo(lastX,lastY);
  }

  public void complete() {
    flush();
  }

  private class Params {
    final boolean positive;
    final boolean current;
    final boolean future;
    final boolean selected;
    final boolean rollover;

    private Params(boolean positive, boolean current, boolean future, boolean selected, boolean rollover) {
      this.positive = positive;
      this.current = current;
      this.future = future;
      this.selected = selected;
      this.rollover = rollover;
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Params params = (Params)o;

      if (current != params.current) {
        return false;
      }
      if (future != params.future) {
        return false;
      }
      if (positive != params.positive) {
        return false;
      }
      if (rollover != params.rollover) {
        return false;
      }
      if (selected != params.selected) {
        return false;
      }

      return true;
    }

    public int hashCode() {
      int result = (positive ? 1 : 0);
      result = 31 * result + (current ? 1 : 0);
      result = 31 * result + (future ? 1 : 0);
      result = 31 * result + (selected ? 1 : 0);
      result = 31 * result + (rollover ? 1 : 0);
      return result;
    }
  }
}
