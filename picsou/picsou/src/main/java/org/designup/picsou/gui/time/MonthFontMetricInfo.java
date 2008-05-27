package org.designup.picsou.gui.time;

import org.crossbowlabs.globs.utils.exceptions.ResourceAccessFailed;
import org.designup.picsou.utils.Lang;

import java.awt.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class MonthFontMetricInfo {
  private Map<Integer, MonthSizes> monthInfos = new HashMap<Integer, MonthSizes>();

  public MonthFontMetricInfo(Graphics graphics) {
    for (int i = 1; i <= 12; i++) {
      monthInfos.put(i, new MonthSizes(new Size(".long", i, Lang.getKeys(), graphics),
                                       new Size(".medium", i, Lang.getKeys(), graphics),
                                       new Size(".short", i, Lang.getKeys(), graphics)));
    }
  }

  public MonthSizes getMonthInfo(int month) {
    return monthInfos.get(month);
  }

  public static class MonthSizes {
    private Size[] orderedSize;

    public MonthSizes(Size... size) {
      this.orderedSize = size;
    }

    public int getNearest(int width) {
      for (int i = 0; i < this.orderedSize.length; i++) {
        Size size = this.orderedSize[i];
        if (size.getWidth() < width) {
          return i;
        }
      }
      return orderedSize.length - 1;
    }

    public Size getSize(int rank) {
      if (rank > orderedSize.length) {
        rank = orderedSize.length - 1;
      }
      return orderedSize[rank];
    }

    public int getMaxWidth() {
      return orderedSize[0].getWidth();
    }

    public int getMinWidth() {
      return orderedSize[orderedSize.length - 1].getWidth();
    }

    public int getHeight() {
      int height = orderedSize[0].getHeigth();
      for (Size size : orderedSize) {
        height = Math.max(height, size.getHeigth());
      }
      return height;
    }
  }

  public static class Size {
    private String name;
    private int width;
    private int height;

    public Size(String type, int month, Enumeration<String> keys, Graphics graphics) {
      while (keys.hasMoreElements()) {
        String element = keys.nextElement();
        if (element.equals("month." + month + type)) {
          name = Lang.get(element);
        }
      }
      if (name == null) {
        throw new ResourceAccessFailed("for month " + month + " type : " + type);
      }
      Graphics2D d = ((Graphics2D) graphics);
      FontMetrics fontMetrics = d.getFontMetrics();
      width = fontMetrics.stringWidth(name);
      height = fontMetrics.getHeight();
    }

    public String getName() {
      return name;
    }

    public int getWidth() {
      return width;
    }

    public int getHeigth() {
      return height;
    }
  }
}
