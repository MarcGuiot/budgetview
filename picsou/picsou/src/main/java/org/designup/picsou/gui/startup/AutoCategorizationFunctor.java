package org.designup.picsou.gui.startup;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;

import java.util.ListIterator;

public class AutoCategorizationFunctor implements GlobFunctor {
  private GlobRepository referenceRepository;

  public AutoCategorizationFunctor(GlobRepository referenceRepository) {
    this.referenceRepository = referenceRepository;
  }

  public void run(Glob transaction, GlobRepository repository) throws Exception {
    GlobList index = referenceRepository.findByIndex(Transaction.LABEL_FOR_CATEGORISATION_INDEX,
                                                     transaction.get(Transaction.LABEL_FOR_CATEGORISATION))
      .sort(TransactionComparator.ASCENDING);
    if (index.size() == 0) {
      return;
    }
    ListIterator<Glob> iterator = index.listIterator(index.size());
    Integer transactionType = transaction.get(Transaction.TRANSACTION_TYPE);
    if (transactionType.equals(TransactionType.CHECK.getId()) ||
        transactionType.equals(TransactionType.WITHDRAWAL.getId()) ||
        transactionType.equals(TransactionType.DEPOSIT.getId()) ||
        !transaction.get(Transaction.SERIES).equals(Series.UNCATEGORIZED_SERIES_ID)) {
      return;
    }
    Integer lastSeries = null;
    Integer lastCategory = null;
    int count = 0;
    while (iterator.hasPrevious()) {
      Glob findTransaction = iterator.previous();
      if (!findTransaction.get(Transaction.ACCOUNT).equals(transaction.get(Transaction.ACCOUNT))){
        continue;
      }
      Integer currentSeries = findTransaction.get(Transaction.SERIES);
      Integer currentCategory = findTransaction.get(Transaction.CATEGORY);
      if (lastSeries != null && !lastSeries.equals(currentSeries)
          || lastCategory != null && !lastCategory.equals(currentCategory)) {
        return;
      }
      else {
        lastSeries = currentSeries;
        lastCategory = currentCategory;
        count++;
        if (count == 3) {
          break;
        }
      }
    }
    if (lastSeries != null) {
      repository.update(transaction.getKey(), Transaction.SERIES, lastSeries);
      repository.update(transaction.getKey(), Transaction.CATEGORY, lastCategory);
    }
  }
}
