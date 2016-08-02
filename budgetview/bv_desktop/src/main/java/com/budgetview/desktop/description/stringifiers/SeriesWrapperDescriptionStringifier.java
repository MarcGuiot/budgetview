package com.budgetview.desktop.description.stringifiers;

import com.budgetview.desktop.series.view.SeriesWrapper;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.utils.Strings;

public class SeriesWrapperDescriptionStringifier extends AbstractGlobStringifier {
  public String toString(Glob wrapper, GlobRepository repository) {
    String description = SeriesWrapper.getDescription(wrapper, repository);
    return Strings.toSplittedHtml(description, 50);
  }
}
