package com.budgetview.desktop.categorization.components.messages;

import com.budgetview.model.BudgetArea;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class NoSeriesMessageFactory {
  public static DynamicMessage create(BudgetArea budgetArea, GlobRepository repository, Directory directory) {
    if (BudgetArea.TRANSFER.equals(budgetArea)) {
      return new NoSavingsSeriesMessage(repository, directory);
    }
    return new NoSeriesMessage(budgetArea, repository, directory);
  }
}
