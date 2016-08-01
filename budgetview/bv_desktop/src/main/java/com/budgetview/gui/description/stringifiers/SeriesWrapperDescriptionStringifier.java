package com.budgetview.gui.description.stringifiers;

import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import com.budgetview.gui.series.view.SeriesWrapper;

public class SeriesWrapperDescriptionStringifier extends AbstractGlobStringifier {
  public String toString(Glob wrapper, GlobRepository repository) {
    String description = SeriesWrapper.getDescription(wrapper, repository);
    return Strings.toSplittedHtml(description, 50);
  }
}
