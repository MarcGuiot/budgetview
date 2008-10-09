package org.designup.picsou.gui.description;

import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.utils.Strings;

import java.util.Comparator;

public class SeriesStringifier implements GlobStringifier {
  public String toString(Glob series, GlobRepository repository) {
    if (series == null || series.get(Series.ID).equals(Series.UNCATEGORIZED_SERIES_ID)) {
      return "";
    }
    final String label = series.get(Series.LABEL);
    if (Series.OCCASIONAL_SERIES_ID.equals(series.get(Series.ID))) {
      return Lang.get(Series.TYPE.getName() + "." + series.get(Series.NAME));
    }
    if (Strings.isNotEmpty(label)) {
      return label;
    }
    return series.get(Series.NAME);
  }

  public Comparator<Glob> getComparator(GlobRepository repository) {
    return new GlobFieldsComparator(Series.LABEL, true, Series.ID, true);
  }
}
