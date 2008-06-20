package org.designup.picsou.gui.transactions.details;

import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.GlobList;
import org.globsframework.model.Glob;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.gui.description.PicsouDescriptionService;

public class TransactionAmountListStringifier implements GlobListStringifier {
  public String toString(GlobList selected) {
    if (selected.isEmpty()) {
      return "";
    }
    double total = 0;
    for (Glob transaction : selected) {
      total += transaction.get(Transaction.AMOUNT);
    }
    return PicsouDescriptionService.DECIMAL_FORMAT.format(total);
  }
}
