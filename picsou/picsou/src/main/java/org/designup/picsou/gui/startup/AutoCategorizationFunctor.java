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
    Integer lastSeriesId = null;
    Integer lastSubSeries = null;
    int count = 0;
    while (iterator.hasPrevious()) {
      Glob findTransaction = iterator.previous();
      if (!findTransaction.get(Transaction.ACCOUNT).equals(transaction.get(Transaction.ACCOUNT))) {
        continue;
      }
      Glob currentSeries = referenceRepository.findLinkTarget(findTransaction, Transaction.SERIES);
      if (currentSeries == null){
        continue;
      }
      if (!Series.checkIsValidMonth(transaction.get(Transaction.MONTH), currentSeries)) {
        continue;
      }
      Integer currentSubSeries = findTransaction.get(Transaction.SUB_SERIES);

      if (lastSeriesId != null && !lastSeriesId.equals(currentSeries.get(Series.ID))
          || lastSubSeries != null && !lastSubSeries.equals(currentSubSeries)) {
        return;
      }
      else {
        lastSeriesId = currentSeries.get(Series.ID);
        lastSubSeries = currentSubSeries;
        count++;
        if (count == 3) {
          break;
        }
      }
    }
    if (lastSeriesId != null) {
      repository.update(transaction.getKey(), Transaction.SERIES, lastSeriesId);
      repository.update(transaction.getKey(), Transaction.SUB_SERIES, lastSubSeries);
    }
  }
}
