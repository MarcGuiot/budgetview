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
  private int autocategorized = 0;
  private int transactionCount = 0;

  public AutoCategorizationFunctor(GlobRepository referenceRepository) {
    this.referenceRepository = referenceRepository;
  }

  public void run(Glob transaction, GlobRepository repository) throws Exception {
    transactionCount++;
    GlobList index = referenceRepository.findByIndex(Transaction.LABEL_FOR_CATEGORISATION_INDEX,
                                                     transaction.get(Transaction.LABEL_FOR_CATEGORISATION))
      .sort(TransactionComparator.ASCENDING);
    if (index.size() == 0) {
      return;
    }
    Integer transactionType = transaction.get(Transaction.TRANSACTION_TYPE);
    if (transactionType.equals(TransactionType.CHECK.getId()) ||
        transactionType.equals(TransactionType.WITHDRAWAL.getId()) ||
        transactionType.equals(TransactionType.DEPOSIT.getId()) ||
        !transaction.get(Transaction.SERIES).equals(Series.UNCATEGORIZED_SERIES_ID)) {
      return;
    }
    ValidTransactionFunctor strictAutoCategorization =
      new ValidTransactionFunctor(transaction, repository, referenceRepository) {
        boolean isValid(Glob findTransaction, Glob transaction, Glob currentSeries) {
          if (super.isValid(findTransaction, transaction, currentSeries)) {
            return transaction.get(Transaction.LABEL).equals(findTransaction.get(Transaction.LABEL));
          }
          return false;
        }
      };
    if (strictAutoCategorization.apply(index)) {
      autocategorized++;
      return;
    }
    ValidTransactionFunctor autoCategorization =
      new ValidTransactionFunctor(transaction, repository, referenceRepository);
    if (autoCategorization.apply(index)){
      autocategorized++;
    }
  }

  public int getAutocategorizedTransaction() {
    return autocategorized;
  }

  public int getTransactionCount() {
    return transactionCount;
  }

  static class ValidTransactionFunctor {
    private Integer lastSeriesId = null;
    private Integer lastSubSeries = null;
    private Glob transaction;
    private GlobRepository repository;
    private GlobRepository referenceRepository;
    private int count = 0;

    ValidTransactionFunctor(Glob transaction, GlobRepository repository, GlobRepository referenceRepository) {
      this.transaction = transaction;
      this.repository = repository;
      this.referenceRepository = referenceRepository;
    }

    boolean apply(GlobList index) {
      ListIterator<Glob> iterator = index.listIterator(index.size());
      while (iterator.hasPrevious()) {
        Glob findTransaction = iterator.previous();
        Glob currentSeries = referenceRepository.findLinkTarget(findTransaction, Transaction.SERIES);
        if (!isValid(transaction, findTransaction, currentSeries)) {
          continue;
        }

        Integer currentSubSeries = findTransaction.get(Transaction.SUB_SERIES);
        if (!isSameSeries(currentSeries, currentSubSeries)) {
          return false;
        }
        count++;
        if (count == 3) {
          break;
        }
      }
      if (lastSeriesId != null) {
        repository.update(transaction.getKey(), Transaction.SERIES, lastSeriesId);
        repository.update(transaction.getKey(), Transaction.SUB_SERIES, lastSubSeries);
        return true;
      }
      return false;
    }

    boolean isValid(Glob findTransaction, Glob transaction, Glob currentSeries) {
      if (!findTransaction.get(Transaction.ACCOUNT).equals(transaction.get(Transaction.ACCOUNT))) {
        return false;
      }
      if (currentSeries == null) {
        return false;
      }
      if (!Series.isValidMonth(transaction.get(Transaction.BUDGET_MONTH), currentSeries)) {
        return false;
      }
      return true;
    }

    public boolean isSameSeries(Glob currentSeries, Integer currentSubSeries) {
      if (lastSeriesId != null && !lastSeriesId.equals(currentSeries.get(Series.ID))
          || lastSubSeries != null && !lastSubSeries.equals(currentSubSeries)) {
        return false;
      }
      lastSeriesId = currentSeries.get(Series.ID);
      lastSubSeries = currentSubSeries;
      return true;
    }
  }
}
