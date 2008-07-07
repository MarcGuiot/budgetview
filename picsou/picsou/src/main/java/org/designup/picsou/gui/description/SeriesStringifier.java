package org.designup.picsou.gui.description;

import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.utils.Strings;

public class SeriesStringifier extends AbstractGlobStringifier {
  public String toString(Glob series, GlobRepository repository) {
    if (series == null) {
      return "";
    }
    final String label = series.get(Series.LABEL);
    if (Strings.isNotEmpty(label)) {
      return label;
    }
    return Lang.get(Series.TYPE.getName() + "." + series.get(Series.NAME));
  }
}
