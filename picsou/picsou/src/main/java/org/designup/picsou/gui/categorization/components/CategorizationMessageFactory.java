package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.BudgetArea;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class CategorizationMessageFactory {
  public static DynamicMessage create(BudgetArea budgetArea, GlobRepository repository, Directory directory) {
    if (BudgetArea.SAVINGS.equals(budgetArea)) {
      return new SavingsCategorizationMessage(repository, directory);
    }
    return new BlankDynamicMessage(repository, directory);
  }
}
