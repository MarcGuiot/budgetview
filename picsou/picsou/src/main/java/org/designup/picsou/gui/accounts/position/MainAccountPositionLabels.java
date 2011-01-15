package org.designup.picsou.gui.accounts.position;

import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.utils.AmountColors;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class MainAccountPositionLabels extends AccountPositionLabels {
  public MainAccountPositionLabels(Key accountKey, GlobRepository repository, Directory directory) {
    super(accountKey, repository, directory);
  }

  protected GlobType getType() {
    return BudgetStat.TYPE;
  }

  protected Double getEndOfMonthPosition(Key accountKey, GlobRepository repository, Integer monthId) {
    Glob stats = repository.find(Key.create(BudgetStat.TYPE, monthId));
    if (stats == null) {
      return null;
    }
    return stats.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION);
  }

  protected Color getLabelColor(Double position, AmountColors amountColors) {
    if (position == null) {
      return amountColors.getNormalColor();
    }

    return amountColors.getTextColor(position);
  }
}
