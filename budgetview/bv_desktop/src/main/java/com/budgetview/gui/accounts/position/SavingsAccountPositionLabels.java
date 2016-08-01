package com.budgetview.gui.accounts.position;

import com.budgetview.gui.model.SavingsBudgetStat;
import com.budgetview.gui.utils.AmountColors;
import com.budgetview.model.Account;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;
import org.globsframework.metamodel.GlobType;

import java.awt.*;

public class SavingsAccountPositionLabels extends AccountPositionLabels {

  public SavingsAccountPositionLabels(Key accountKey, GlobRepository repository, Directory directory) {
    super(accountKey, repository, directory);
  }

  protected GlobType getType() {
    return SavingsBudgetStat.TYPE;
  }

  protected Double getEndOfMonthPosition(Key accountKey, GlobRepository repository, Integer monthId) {
    Glob stats = repository.find(Key.create(SavingsBudgetStat.ACCOUNT, accountKey.get(Account.ID),
                                            SavingsBudgetStat.MONTH, monthId));
    if (stats == null) {
      return null;
    }
    return stats.get(SavingsBudgetStat.END_OF_MONTH_POSITION);
  }

  protected Color getLabelColor(Double position, AmountColors amountColors) {
    return amountColors.getNormalColor();
  }
}
