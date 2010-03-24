package org.designup.picsou.gui.startup;

import org.designup.picsou.model.*;
import org.designup.picsou.model.util.Amounts;
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

  public void run(Glob transaction, final GlobRepository repository) throws Exception {
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
    {
      ValidTransactionFunctor strictAutoCategorization =
        new ValidTransactionFunctor(transaction, repository, referenceRepository) {
          boolean isValid(Glob transaction, Glob findTransaction, Glob currentSeries) {
            if (super.isValid(transaction, findTransaction, currentSeries)) {
              boolean sameSign = isSameSign(findTransaction, transaction);
              return sameSign && transaction.get(Transaction.LABEL).equals(findTransaction.get(Transaction.LABEL));
            }
            return false;
          }
        };
      if (strictAutoCategorization.apply(index)) {
        autocategorized++;
        return;
      }
    }
    {
      ValidTransactionFunctor strictAutoCategorization =
        new ValidTransactionFunctor(transaction, repository, referenceRepository) {
          boolean isValid(Glob transaction, Glob findTransaction, Glob currentSeries) {
            if (super.isValid(transaction, findTransaction, currentSeries)) {
              return transaction.get(Transaction.LABEL).equals(findTransaction.get(Transaction.LABEL));
            }
            return false;
          }
        };
      if (strictAutoCategorization.apply(index)) {
        autocategorized++;
        return;
      }
    }
    {
      ValidTransactionFunctor autoCategorization =
        new ValidTransactionFunctor(transaction, repository, referenceRepository) {
          boolean isValid(Glob transaction, Glob findTransaction, Glob currentSeries) {
            boolean sameSign = isSameSign(findTransaction, transaction);
            return sameSign && super.isValid(transaction, findTransaction, currentSeries);
          }
        };
      if (autoCategorization.apply(index)) {
        autocategorized++;
        return;
      }
    }
    {
      ValidTransactionFunctor autoCategorization =
        new ValidTransactionFunctor(transaction, repository, referenceRepository);
      if (autoCategorization.apply(index)) {
        autocategorized++;
        return;
      }
    }
  }

  private boolean isSameSign(Glob findTransaction, Glob transaction) {
    return Amounts.sameSign(findTransaction.get(Transaction.AMOUNT), transaction.get(Transaction.AMOUNT));
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
      int currentMonthId = 0;
      while (iterator.hasPrevious()) {
        Glob findTransaction = iterator.previous();
        Glob currentSeries = referenceRepository.findLinkTarget(findTransaction, Transaction.SERIES);
        if (currentSeries.get(Series.ID).equals(Series.UNCATEGORIZED_SERIES_ID) ||
            !isValid(transaction, findTransaction, currentSeries)) {
          continue;
        }
        if (currentMonthId == 0) {
          currentMonthId = findTransaction.get(Transaction.MONTH);
        }
        //on check ici pour ne pas prendre en comptes les operations en mois n-2 si count >= 3
        // ca veux dire que le mois n-1 est bon.
        if (count >= 3 && Month.distance(findTransaction.get(Transaction.MONTH), currentMonthId) > 1) {
          break;
        }
        Integer currentSubSeries = findTransaction.get(Transaction.SUB_SERIES);
        if (!isSameSeries(currentSeries, currentSubSeries)) {
          return false;
        }
        count++;
        if (count >= 3 && Month.distance(findTransaction.get(Transaction.MONTH), currentMonthId) > 1) {
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

    boolean isValid(Glob transaction, Glob findTransaction, Glob currentSeries) {
      if (!transaction.get(Transaction.ACCOUNT).equals(findTransaction.get(Transaction.ACCOUNT))) {
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
