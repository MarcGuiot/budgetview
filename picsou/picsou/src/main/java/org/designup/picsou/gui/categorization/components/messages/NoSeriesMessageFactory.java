package org.designup.picsou.gui.categorization.components.messages;

import org.designup.picsou.model.BudgetArea;
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
