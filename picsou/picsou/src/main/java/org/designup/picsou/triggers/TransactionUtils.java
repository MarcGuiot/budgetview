package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.globsframework.model.*;

public class TransactionUtils {
  public static Integer createMirrorTransaction(Key source, FieldValues transaction,
                                                final Integer accountId, GlobRepository repository) {
    Double amount = -transaction.get(Transaction.AMOUNT);
    Glob savingsTransaction =
      repository.create(Transaction.TYPE,
                        FieldValue.value(Transaction.AMOUNT, amount),
                        FieldValue.value(Transaction.ACCOUNT, accountId),
                        FieldValue.value(Transaction.BANK_DAY, transaction.get(Transaction.BANK_DAY)),
                        FieldValue.value(Transaction.BANK_MONTH, transaction.get(Transaction.BANK_MONTH)),
                        FieldValue.value(Transaction.DAY, transaction.get(Transaction.DAY)),
                        FieldValue.value(Transaction.MONTH, transaction.get(Transaction.MONTH)),
                        FieldValue.value(Transaction.TRANSACTION_TYPE,
                                         amount > 0 ? TransactionType.VIREMENT.getId() : TransactionType.PRELEVEMENT.getId()),
                        FieldValue.value(Transaction.CATEGORY,
                                         transaction.get(Transaction.CATEGORY)),
                        FieldValue.value(Transaction.LABEL, transaction.get(Transaction.LABEL)),
                        FieldValue.value(Transaction.SERIES, transaction.get(Transaction.SERIES)),
                        FieldValue.value(Transaction.MIRROR, true),
                        FieldValue.value(Transaction.PLANNED,
                                         transaction.get(Transaction.PLANNED)));
    repository.update(source, Transaction.NOT_IMPORTED_TRANSACTION,
                      savingsTransaction.get(Transaction.ID));
    return savingsTransaction.get(Transaction.ID);
  }

  public static Glob createTransactionForNotImportedAccount(FieldValues seriesBudget, Glob series,
                                                               Integer accountId, Integer currentMonthId,
                                                               Integer currentDay,
                                                               GlobRepository repository) {
    Double multiplier =
      Account.getMultiplierForInOrOutputOfTheAccount(repository.findLinkTarget(series, Series.FROM_ACCOUNT),
                                                     repository.findLinkTarget(series, Series.TO_ACCOUNT),
                                                     repository.get(Key.create(Account.TYPE, accountId)));
    Double amount = multiplier * Math.abs(seriesBudget.get(SeriesBudget.AMOUNT));
    boolean isPlanned = (seriesBudget.get(SeriesBudget.MONTH) >= currentMonthId) &&
                        ((seriesBudget.get(SeriesBudget.MONTH) > currentMonthId)
                         || (seriesBudget.get(SeriesBudget.DAY) > currentDay));
    if (Math.abs(amount) > 0.0001) {
      return repository.create(Transaction.TYPE,
                          FieldValue.value(Transaction.AMOUNT, amount),
                        FieldValue.value(Transaction.ACCOUNT, accountId),
                        FieldValue.value(Transaction.BANK_DAY, seriesBudget.get(SeriesBudget.DAY)),
                        FieldValue.value(Transaction.BANK_MONTH, seriesBudget.get(SeriesBudget.MONTH)),
                        FieldValue.value(Transaction.DAY, seriesBudget.get(SeriesBudget.DAY)),
                        FieldValue.value(Transaction.MONTH, seriesBudget.get(SeriesBudget.MONTH)),
                        FieldValue.value(Transaction.TRANSACTION_TYPE,
                                         amount > 0 ? TransactionType.VIREMENT.getId() : TransactionType.PRELEVEMENT.getId()),
                        FieldValue.value(Transaction.CATEGORY,
                                         series.get(Series.DEFAULT_CATEGORY)),
                        FieldValue.value(Transaction.LABEL, Transaction.getLabel(isPlanned, series)),
                        FieldValue.value(Transaction.SERIES, series.get(Series.ID)),
                        FieldValue.value(Transaction.CREATED_BY_SERIES, true),
                        FieldValue.value(Transaction.PLANNED, isPlanned));
    }
    return null;
  }
}
