package org.designup.picsou.gui.transactions;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.model.utils.GlobFieldComparator;
import org.designup.picsou.model.Transaction;

import java.util.Comparator;

public class TransactionTimeCostStringifier implements GlobStringifier {
  public String toString(Glob transaction, GlobRepository repository) {
    double amount = transaction.get(Transaction.AMOUNT);
    return TimeCost.get(amount, 15.0, 12);
  }

  public Comparator<Glob> getComparator(GlobRepository repository) {
    return new GlobFieldComparator(Transaction.AMOUNT);
  }
}
