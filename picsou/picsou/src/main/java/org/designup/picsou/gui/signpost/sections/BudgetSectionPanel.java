package org.designup.picsou.gui.signpost.sections;

import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.model.GlobRepository;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

public class BudgetSectionPanel extends SignpostSectionPanel {
  public BudgetSectionPanel(GlobRepository repository, Directory directory) {
    super(SignpostSection.BUDGET, repository, directory);
  }

  protected boolean isCompleted(GlobRepository repository) {
    Integer monthId = CurrentMonth.getLastTransactionMonth(repository);
    if (monthId != null) {
      boolean isComplete = check(repository, monthId);
      if (isComplete) {
        monthId = Month.previous(monthId);
        return check(repository, monthId);
      }
      return isComplete;
    }
    return false;
  }

  private boolean check(GlobRepository repository, Integer monthId) {
    return !repository.contains(SeriesBudget.TYPE,
                                and(fieldEquals(SeriesBudget.MONTH, monthId),
                                    not(fieldEquals(SeriesBudget.SERIES, Series.UNCATEGORIZED_SERIES_ID)),
                                    isNull(SeriesBudget.AMOUNT),
                                    not(isNull(SeriesBudget.OBSERVED_AMOUNT))));
  }
}
