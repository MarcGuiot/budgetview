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
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class BudgetValuesUpdater {

  private static BudgetArea[] BUDGET_AREAS;
  private GlobRepository sourceRepository;
  private GlobRepository targetRepository;
  private Integer[] selectedMonths;
  private int currentMonthId;

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

      createVersion();
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

  private void createVersion() {
    targetRepository.create(BudgetViewVersion.key,
                            value(BudgetViewVersion.MAJOR_VERSION, MobileModel.MAJOR_VERSION),
                            value(BudgetViewVersion.MINOR_VERSION, MobileModel.MINOR_VERSION));
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
    Set<Integer> seriesIds = new HashSet<Integer>();
    for (Glob seriesStat : sourceRepository.getAll(SeriesStat.TYPE, fieldIn(SeriesStat.MONTH, selectedMonths))) {
      Glob series = sourceRepository.findLinkTarget(seriesStat, SeriesStat.SERIES);

      if (seriesStat.isTrue(SeriesStat.ACTIVE)) {
        Integer seriesId = series.get(Series.ID);
        seriesIds.add(seriesId);
        targetRepository.create(SeriesValues.TYPE,
                                value(SeriesValues.SERIES_ENTITY, seriesId),
                                value(SeriesValues.MONTH, seriesStat.get(SeriesStat.MONTH)),
                                value(SeriesValues.BUDGET_AREA, series.get(Series.BUDGET_AREA)),
                                value(SeriesValues.AMOUNT, seriesStat.get(SeriesStat.AMOUNT)),
                                value(SeriesValues.PLANNED_AMOUNT, seriesStat.get(SeriesStat.PLANNED_AMOUNT)),
                                value(SeriesValues.OVERRUN_AMOUNT, seriesStat.get(SeriesStat.OVERRUN_AMOUNT)),
                                value(SeriesValues.REMAINING_AMOUNT, seriesStat.get(SeriesStat.REMAINING_AMOUNT)));
      }
    }
    for (Integer seriesId : seriesIds) {
      Glob series = sourceRepository.get(Key.create(Series.TYPE, seriesId));
      targetRepository.create(SeriesEntity.TYPE,
                              value(SeriesEntity.ID, seriesId),
                              value(SeriesEntity.NAME, series.get(Series.NAME)),
                              value(SeriesEntity.BUDGET_AREA, series.get(Series.BUDGET_AREA)));
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
                              value(AccountEntity.POSITION, account.get(Account.POSITION_WITH_PENDING)),
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
      targetRepository.create(TransactionValues.TYPE,
                              value(TransactionValues.AMOUNT, transaction.get(Transaction.AMOUNT)),
                              value(TransactionValues.ACCOUNT, transaction.get(Transaction.ACCOUNT)),
                              value(TransactionValues.LABEL, transaction.get(Transaction.LABEL)),
                              value(TransactionValues.BANK_DAY, transaction.get(Transaction.BANK_DAY)),
                              value(TransactionValues.BANK_MONTH, transaction.get(Transaction.BANK_MONTH)),
                              value(TransactionValues.PLANNED, transaction.get(Transaction.PLANNED)),
                              value(TransactionValues.SERIES, transaction.get(Transaction.SERIES)),
                              value(TransactionValues.SEQUENCE_NUMBER, sequenceNumber++)
      );
    }
  }
}
