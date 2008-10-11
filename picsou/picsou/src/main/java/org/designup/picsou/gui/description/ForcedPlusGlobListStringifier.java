package org.designup.picsou.gui.description;

import org.designup.picsou.model.BudgetArea;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;

public class ForcedPlusGlobListStringifier implements GlobListStringifier {
  private BudgetArea budgetArea;
  private final GlobListStringifier listStringifier;

  public ForcedPlusGlobListStringifier(BudgetArea budgetArea, GlobListStringifier listStringifier) {
    this.budgetArea = budgetArea;
    this.listStringifier = listStringifier;
  }

  public String toString(GlobList list, GlobRepository repository) {
    String amount = listStringifier.toString(list, repository);
    if (!budgetArea.isIncome()) {
      if (amount.startsWith("-")) {
        return amount.replace("-", "+");
      }
    }
    return amount;
  }
}
