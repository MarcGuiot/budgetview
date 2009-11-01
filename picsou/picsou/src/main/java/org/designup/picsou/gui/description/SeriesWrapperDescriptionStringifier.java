package org.designup.picsou.gui.description;

import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;

public class SeriesWrapperDescriptionStringifier extends AbstractGlobStringifier {
  public String toString(Glob wrapper, GlobRepository repository) {
    String description = SeriesWrapper.getDescription(wrapper, repository);
    return Strings.toSplittedHtml(description, 50);
  }
}
