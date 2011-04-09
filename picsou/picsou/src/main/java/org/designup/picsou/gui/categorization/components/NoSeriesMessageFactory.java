package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.BudgetArea;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class NoSeriesMessageFactory {
  public static DynamicMessage create(BudgetArea budgetArea, GlobRepository repository, Directory directory) {
    if (BudgetArea.SAVINGS.equals(budgetArea)) {
      return new NoSavingsSeriesMessage(repository, directory);
    }
    return new NoSeriesMessage(budgetArea, repository, directory);
  }
}
