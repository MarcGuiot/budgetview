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
    return toString(listStringifier.toString(list, repository), budgetArea);
  }

  public static String toString(double amount, final BudgetArea budgetArea) {
    return toString(Formatting.DECIMAL_FORMAT.format(amount), budgetArea);
  }

  public static String toString(String amount, final BudgetArea budgetArea) {
    if (!budgetArea.isIncome() && budgetArea != BudgetArea.SAVINGS) {
      if (amount.startsWith("-")) {
        return amount.replace("-", "+");
      }
    }
    return amount;
  }
}
