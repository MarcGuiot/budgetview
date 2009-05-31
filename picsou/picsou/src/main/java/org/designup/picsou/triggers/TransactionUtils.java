package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

public class TransactionUtils {
  public static Integer createMirrorTransaction(Key source, FieldValues transaction,
                                                final Integer accountId, GlobRepository repository) {
    Double amount = -transaction.get(Transaction.AMOUNT);
    Glob savingsTransaction =
      repository.create(Transaction.TYPE,
                        value(Transaction.AMOUNT, amount),
                        value(Transaction.ACCOUNT, accountId),
                        value(Transaction.DAY_BEFORE_SHIFT, transaction.get(Transaction.DAY_BEFORE_SHIFT)),
                        value(Transaction.BANK_MONTH, transaction.get(Transaction.BANK_MONTH)),
                        value(Transaction.BANK_DAY, transaction.get(Transaction.BANK_DAY)),
                        value(Transaction.BANK_MONTH, transaction.get(Transaction.BANK_MONTH)),
                        value(Transaction.DAY, transaction.get(Transaction.DAY)),
                        value(Transaction.MONTH, transaction.get(Transaction.MONTH)),
                        value(Transaction.TRANSACTION_TYPE,
                              amount > 0 ? TransactionType.VIREMENT.getId() : TransactionType.PRELEVEMENT.getId()),
                        value(Transaction.CATEGORY,
                              transaction.get(Transaction.CATEGORY)),
                        value(Transaction.LABEL, transaction.get(Transaction.LABEL)),
                        value(Transaction.SERIES, transaction.get(Transaction.SERIES)),
                        value(Transaction.MIRROR, true),
                        value(Transaction.PLANNED,
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
                               value(Transaction.AMOUNT, amount),
                               value(Transaction.ACCOUNT, accountId),
                               value(Transaction.BANK_DAY, seriesBudget.get(SeriesBudget.DAY)),
                               value(Transaction.BANK_MONTH, seriesBudget.get(SeriesBudget.MONTH)),
                               value(Transaction.DAY, seriesBudget.get(SeriesBudget.DAY)),
                               value(Transaction.MONTH, seriesBudget.get(SeriesBudget.MONTH)),
                               value(Transaction.TRANSACTION_TYPE,
                                     amount > 0 ? TransactionType.VIREMENT.getId() : TransactionType.PRELEVEMENT.getId()),
                               value(Transaction.LABEL, Transaction.getLabel(isPlanned, series)),
                               value(Transaction.SERIES, series.get(Series.ID)),
                               value(Transaction.CREATED_BY_SERIES, true),
                               value(Transaction.PLANNED, isPlanned));
    }
    return null;
  }
}
