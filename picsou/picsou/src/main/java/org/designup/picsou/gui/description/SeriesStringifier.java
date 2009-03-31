package org.designup.picsou.gui.description;

import org.designup.picsou.model.Series;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.utils.Strings;

import java.util.Comparator;

public class SeriesStringifier implements GlobStringifier {
  public static final GlobFieldsComparator COMPARATOR = new GlobFieldsComparator(Series.NAME, true, Series.ID, true);

  public String toString(Glob series, GlobRepository repository) {
    if (series == null || series.get(Series.ID).equals(Series.UNCATEGORIZED_SERIES_ID)) {
      return "";
    }
    return series.get(Series.NAME);
  }

  public Comparator<Glob> getComparator(GlobRepository repository) {
    return COMPARATOR;
  }
}
