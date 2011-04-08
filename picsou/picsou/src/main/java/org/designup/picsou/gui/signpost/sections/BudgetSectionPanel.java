package org.designup.picsou.gui.signpost.sections;

import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import static org.globsframework.model.utils.GlobMatchers.*;

public class BudgetSectionPanel extends SignpostSectionPanel {
  public BudgetSectionPanel(GlobRepository repository, Directory directory) {
    super(SignpostSection.BUDGET, repository, directory);
  }

  protected boolean isCompleted(GlobRepository repository) {
    Integer monthId = CurrentMonth.findCurrentMonth(repository);
    return monthId != null && !repository.contains(SeriesBudget.TYPE,
                                                   and(fieldEquals(SeriesBudget.MONTH, monthId),
                                                       not(fieldEquals(SeriesBudget.SERIES, Series.UNCATEGORIZED_SERIES_ID)),
                                                       isNull(SeriesBudget.AMOUNT),
                                                       not(isNull(SeriesBudget.OBSERVED_AMOUNT))));
  }
}
