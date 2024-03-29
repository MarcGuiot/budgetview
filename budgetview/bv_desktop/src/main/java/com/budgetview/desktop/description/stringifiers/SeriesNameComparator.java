package com.budgetview.desktop.description.stringifiers;

import com.budgetview.model.Series;
import org.globsframework.model.Glob;
import org.globsframework.utils.Utils;

import java.util.Comparator;

public class SeriesNameComparator implements Comparator<Glob> {
  public static Comparator<Glob> INSTANCE = new SeriesNameComparator();

  public int compare(Glob o1, Glob o2) {
    return Utils.compareIgnoreCase(o1.get(Series.NAME), o2.get(Series.NAME));
  }
}
