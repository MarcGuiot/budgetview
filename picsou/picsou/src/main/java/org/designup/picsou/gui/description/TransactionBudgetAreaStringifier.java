package org.designup.picsou.gui.description;

import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.utils.AbstractGlobStringifier;

public class TransactionBudgetAreaStringifier extends AbstractGlobStringifier {
  public String toString(Glob transaction, GlobRepository repository) {
    if (transaction == null) {
      return "";
    }
    Glob series = repository.findLinkTarget(transaction, Transaction.SERIES);
    if (series == null) {
      return "";
    }
    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    if (BudgetArea.UNCATEGORIZED.equals(budgetArea)) {
      return "";
    }
    return budgetArea.getLabel();
  }
}
