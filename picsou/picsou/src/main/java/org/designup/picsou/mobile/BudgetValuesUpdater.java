package org.designup.picsou.mobile;

import com.budgetview.shared.model.*;
import org.designup.picsou.gui.budget.summary.TotalBudgetAreaAmounts;
import org.designup.picsou.gui.description.stringifiers.AccountComparator;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class BudgetValuesUpdater {

  private static BudgetArea[] BUDGET_AREAS;
  private GlobRepository sourceRepository;
  private GlobRepository targetRepository;
  private Integer[] selectedMonths;
  private int currentMonthId;
  private Map<SeriesValueKey, Integer> seriesValuesMap;

  static {
    BUDGET_AREAS = new BudgetArea[BudgetArea.INCOME_AND_EXPENSES_AREAS.length + 1];
    for (int i = 0; i < BudgetArea.INCOME_AND_EXPENSES_AREAS.length; i++) {
      BUDGET_AREAS[i] = BudgetArea.INCOME_AND_EXPENSES_AREAS[i];
    }
    BUDGET_AREAS[BUDGET_AREAS.length - 1] = BudgetArea.UNCATEGORIZED;
  }

  public static void process(GlobRepository sourceRepository, GlobRepository targetRepository) {
    BudgetValuesUpdater updater = new BudgetValuesUpdater(sourceRepository, targetRepository);
    updater.run();
  }

  private BudgetValuesUpdater(GlobRepository sourceRepository, GlobRepository targetRepository) {
    this.sourceRepository = sourceRepository;
    this.targetRepository = targetRepository;
    this.currentMonthId = CurrentMonth.getCurrentMonth(sourceRepository);
    this.selectedMonths = new Integer[]{
      Month.previous(currentMonthId),
      currentMonthId,
      Month.next(currentMonthId)
    };
  }

  private void run() {
    targetRepository.startChangeSet();
    try {
      targetRepository.deleteAll();

      createMonths();
      createBudgetAreas();
      createBudgetStats();
      createSeriesValues();
      createAccounts();
      createTransactions();

    }
    finally {
      targetRepository.completeChangeSet();
    }
  }

  private void createMonths() {
    for (Integer monthId : selectedMonths) {
      targetRepository.create(MonthEntity.TYPE,
                              value(MonthEntity.ID, monthId));
    }
  }

  private void createBudgetAreas() {
    for (BudgetArea budgetArea : BUDGET_AREAS) {
      targetRepository.create(BudgetAreaEntity.TYPE,
                              value(BudgetAreaEntity.ID, budgetArea.getId()),
                              value(BudgetAreaEntity.LABEL, budgetArea.getLabel()));
    }
  }

  private void createBudgetStats() {
    TotalBudgetAreaAmounts totalAmounts = new TotalBudgetAreaAmounts() {
      protected Integer getCurrentMonths() {
        return currentMonthId;
      }
    };
    for (Glob budgetStat : sourceRepository.getAll(BudgetStat.TYPE, fieldIn(BudgetStat.MONTH, selectedMonths))) {
      for (BudgetArea budgetArea : BudgetArea.INCOME_AND_EXPENSES_AREAS) {
        totalAmounts.update(new GlobList(budgetStat), budgetArea);
        targetRepository.create(BudgetAreaValues.TYPE,
                                value(BudgetAreaValues.BUDGET_AREA, budgetArea.getId()),
                                value(BudgetAreaValues.MONTH, budgetStat.get(BudgetStat.MONTH)),
                                value(BudgetAreaValues.REMAINDER, totalAmounts.getFutureRemaining() + totalAmounts.getPastRemaining()),
                                value(BudgetAreaValues.OVERRUN, totalAmounts.getFutureOverrun() + totalAmounts.getPastOverrun()),
                                value(BudgetAreaValues.INITIALLY_PLANNED, totalAmounts.getInitiallyPlanned()),
                                value(BudgetAreaValues.ACTUAL, totalAmounts.getActual()));
      }
    }
  }

  private void createSeriesValues() {
    seriesValuesMap = new HashMap<SeriesValueKey, Integer>();
    for (Glob seriesStat : sourceRepository.getAll(SeriesStat.TYPE, fieldIn(SeriesStat.MONTH, selectedMonths))) {
      Glob series = sourceRepository.findLinkTarget(seriesStat, SeriesStat.SERIES);

      if (seriesStat.isTrue(SeriesStat.ACTIVE)) {
        Glob seriesValues = targetRepository.create(SeriesValues.TYPE,
                                                    value(SeriesValues.NAME, series.get(Series.NAME)),
                                                    value(SeriesValues.MONTH, seriesStat.get(SeriesStat.MONTH)),
                                                    value(SeriesValues.BUDGET_AREA, series.get(Series.BUDGET_AREA)),
                                                    value(SeriesValues.AMOUNT, seriesStat.get(SeriesStat.AMOUNT)),
                                                    value(SeriesValues.PLANNED_AMOUNT, seriesStat.get(SeriesStat.PLANNED_AMOUNT)),
                                                    value(SeriesValues.OVERRUN_AMOUNT, seriesStat.get(SeriesStat.OVERRUN_AMOUNT)),
                                                    value(SeriesValues.REMAINING_AMOUNT, seriesStat.get(SeriesStat.REMAINING_AMOUNT)));
        seriesValuesMap.put(new SeriesValueKey(series.get(Series.ID), seriesStat.get(SeriesStat.MONTH)),
                            seriesValues.get(SeriesValues.ID));
      }
    }
  }

  private void createAccounts() {

    GlobMatcher matcher =
      GlobMatchers.and(Matchers.userCreatedAccounts(),
                       new Matchers.AccountDateMatcher(Utils.set(selectedMonths)));

    GlobList accounts =
      sourceRepository.getAll(Account.TYPE, matcher)
        .sort(new AccountComparator());
    int index = 0;
    for (Glob account : accounts) {
      Date positionDate = account.get(Account.POSITION_DATE);
      targetRepository.create(AccountEntity.TYPE,
                              value(AccountEntity.ID, account.get(Account.ID)),
                              value(AccountEntity.LABEL, account.get(Account.NAME)),
                              value(AccountEntity.POSITION, account.get(Account.POSITION)),
                              value(AccountEntity.POSITION_MONTH, Month.getMonthId(positionDate)),
                              value(AccountEntity.POSITION_DAY, Month.getDay(positionDate)),
                              value(AccountEntity.ACCOUNT_TYPE, convertAccountType(account.get(Account.ACCOUNT_TYPE))),
                              value(AccountEntity.SEQUENCE_NUMBER, index++));
    }
  }

  private Integer convertAccountType(Integer accountType) {
    switch (AccountType.get(accountType)) {
      case MAIN:
        return AccountEntity.ACCOUNT_TYPE_MAIN;
      case SAVINGS:
        return AccountEntity.ACCOUNT_TYPE_SAVINGS;
      default:
        throw new InvalidParameter("Unexpected AccountType");
    }
  }

  private void createTransactions() {
    GlobList transactions = sourceRepository
      .getAll(Transaction.TYPE, fieldIn(Transaction.BUDGET_MONTH, selectedMonths))
      .sort(TransactionComparator.DESCENDING_BANK_SPLIT_AFTER);
    int sequenceNumber = 0;
    for (Glob transaction : transactions) {
      Integer seriesValuesId = seriesValuesMap.get(new SeriesValueKey(transaction.get(Transaction.SERIES),
                                                                      transaction.get(Transaction.BUDGET_MONTH)));
      if (seriesValuesId == null) {
        throw new UnexpectedApplicationState("No series values found for " + transaction);
      }

      targetRepository.create(TransactionValues.TYPE,
                              value(TransactionValues.AMOUNT, transaction.get(Transaction.AMOUNT)),
                              value(TransactionValues.ACCOUNT, transaction.get(Transaction.ACCOUNT)),
                              value(TransactionValues.LABEL, transaction.get(Transaction.LABEL)),
                              value(TransactionValues.BANK_DAY, transaction.get(Transaction.BANK_DAY)),
                              value(TransactionValues.BANK_MONTH, transaction.get(Transaction.BANK_MONTH)),
                              value(TransactionValues.PLANNED, transaction.get(Transaction.PLANNED)),
                              value(TransactionValues.SERIES_VALUES, seriesValuesId),
                              value(TransactionValues.SEQUENCE_NUMBER, sequenceNumber++)
      );
    }
  }

  private static class SeriesValueKey {
    private Integer seriesId;
    private Integer monthId;

    private SeriesValueKey(Integer seriesId, Integer monthId) {
      this.seriesId = seriesId;
      this.monthId = monthId;
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      SeriesValueKey that = (SeriesValueKey)o;

      if (monthId != null ? !monthId.equals(that.monthId) : that.monthId != null) {
        return false;
      }
      if (seriesId != null ? !seriesId.equals(that.seriesId) : that.seriesId != null) {
        return false;
      }

      return true;
    }

    public int hashCode() {
      int result = seriesId != null ? seriesId.hashCode() : 0;
      result = 31 * result + (monthId != null ? monthId.hashCode() : 0);
      return result;
    }
  }

}
